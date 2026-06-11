# K8S-ARCH-03 — Itération 2 : Overlays Kustomize + Sealed Secrets (V4-A)

**Domaine :** Orchestration / Kubernetes  
**Couche :** Déploiement local — Structuration & Sécurisation  
**Statut :** 🟦 À implémenter

> **Objectif :** introduire les overlays Kustomize (dev/prod) et sécuriser l'accès GHCR via Sealed Secrets afin de rendre le déploiement Kubernetes local structuré, reproductible et conforme aux bonnes pratiques DevSecOps.

> **Prérequis :** itération 1 validée — cluster kind opérationnel, ingress-nginx installé, déploiement fonctionnel sur `demo.local`.

---

## 1. Fiche d'implémentation

### 1.1. Structure k8s/ cible

```
k8s/
  config/
    kind-config.yaml
  base/
    namespace.yaml
    configmap.yaml
    deployment.yaml
    service.yaml
    ingress.yaml
    sealed-ghcr-secret.yaml
    kustomization.yaml
  overlays/
    dev/
      kustomization.yaml
      patch-deployment.yaml
    prod/
      kustomization.yaml
      patch-deployment.yaml
```

**Principes :**

- `base/` contient les ressources génériques communes à tous les environnements
- `overlays/` contient les variations par environnement (image, replicas, config)
- Les secrets sont gérés via Sealed Secrets et versionnés en toute sécurité dans Git
- `kind-config.yaml` reste indépendant — utilisé uniquement pour créer le cluster local

---

### 1.2. Manifests de base

#### kustomization.yaml (base)

```yaml
# k8s/base/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: demo-app

resources:
  - namespace.yaml
  - configmap.yaml
  - deployment.yaml
  - service.yaml
  - ingress.yaml
  - sealed-ghcr-secret.yaml

commonLabels:
  app: demo-app
```

> Les autres manifests de base (namespace, configmap, deployment, service, ingress) sont identiques à ceux définis en itération 1 — se référer à K8S-ARCH-03 itération 1, §1.2.

---

### 1.3. Overlays dev et prod

#### Overlay dev

Objectif : rapidité, simplicité, ressources minimales, image SHA du dernier build CI.

```yaml
# k8s/overlays/dev/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: demo-app

resources:
  - ../../base

patches:
  - path: patch-deployment.yaml
```

```yaml
# k8s/overlays/dev/patch-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-app
  namespace: demo-app
spec:
  replicas: 1
  template:
    spec:
      containers:
        - name: demo-app
          image: ghcr.io/<org>/<repo>:<sha-dev>
```

> **`:latest` est interdit** — toujours un tag SHA immuable produit par le pipeline CI. Remplacer `<sha-dev>` par le SHA du dernier build.

---

#### Overlay prod

Objectif : stabilité, haute disponibilité, image immuable validée.

```yaml
# k8s/overlays/prod/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: demo-app

resources:
  - ../../base

patches:
  - path: patch-deployment.yaml
```

```yaml
# k8s/overlays/prod/patch-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-app
  namespace: demo-app
spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: demo-app
          image: ghcr.io/<org>/<repo>:<sha-prod>
```

---

### 1.4. Installation de Sealed Secrets

#### Installer le controller Sealed Secrets dans le cluster

```bash
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.26.0/controller.yaml

# Attendre que le controller soit prêt
kubectl wait --namespace kube-system \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=sealed-secrets-controller \
  --timeout=90s
```

---

#### Installer kubeseal (client local)

```bash
# macOS
brew install kubeseal

# Linux
KUBESEAL_VERSION=0.26.0
curl -sSL "https://github.com/bitnami-labs/sealed-secrets/releases/download/v${KUBESEAL_VERSION}/kubeseal-${KUBESEAL_VERSION}-linux-amd64.tar.gz" \
  | tar -xz kubeseal
sudo mv kubeseal /usr/local/bin/

# Windows (via Scoop)
scoop install kubeseal
```

---

#### Créer et chiffrer le secret GHCR

```bash
# 1. Créer le secret Kubernetes en mémoire (sans l'appliquer)
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=<ton_user> \
  --docker-password=<ton_token_ghcr> \
  --namespace=demo-app \
  --dry-run=client \
  -o yaml > /tmp/ghcr-secret.yaml

# 2. Chiffrer avec kubeseal
kubeseal \
  --controller-name=sealed-secrets-controller \
  --controller-namespace=kube-system \
  --format yaml \
  < /tmp/ghcr-secret.yaml \
  > k8s/base/sealed-ghcr-secret.yaml

# 3. Supprimer le fichier temporaire non chiffré
rm /tmp/ghcr-secret.yaml
```

> **Important :** le fichier `/tmp/ghcr-secret.yaml` contient le secret en clair — le supprimer immédiatement après chiffrement. Seul `sealed-ghcr-secret.yaml` va dans Git.

---

#### Vérifier que le SealedSecret est bien déchiffré

```bash
# Le controller crée automatiquement un Secret Kubernetes à partir du SealedSecret
kubectl get secret ghcr-secret -n demo-app

# Vérifier le contenu (encodé en base64)
kubectl get secret ghcr-secret -n demo-app -o yaml
```

---

#### Référencer le secret dans le Deployment

Ajouter dans `k8s/base/deployment.yaml` (`spec.template.spec`) :

```yaml
imagePullSecrets:
  - name: ghcr-secret
```

---

### 1.5. Déploiement complet

#### Ordre de déploiement depuis un cluster kind propre

```bash
# 1. Créer le cluster avec mapping ports 80/443
kind create cluster --name demo --config k8s/config/kind-config.yaml

# 2. Installer l'Ingress Controller NGINX
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s

# 3. Installer le controller Sealed Secrets
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.26.0/controller.yaml
kubectl wait --namespace kube-system \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=sealed-secrets-controller \
  --timeout=90s

# 4. Appliquer l'overlay dev (inclut le SealedSecret)
kubectl apply -k k8s/overlays/dev

# 5. Vérifier
kubectl get all -n demo-app
kubectl get secret ghcr-secret -n demo-app
kubectl get ingress -n demo-app

# 6. Tester
curl http://demo.local/actuator/health
```

> **Résultat attendu :** `{"status":"UP"}`

> **Ordre critique :** le controller Sealed Secrets doit être prêt **avant** d'appliquer les manifests, sinon le SealedSecret ne sera pas déchiffré et le pod ne pourra pas tirer l'image GHCR.

---

## 2. Fiche d'exigences

### 2.1. Exigences d'architecture

- Séparation stricte entre `base/` et `overlays/`
- Aucun secret Kubernetes en clair dans Git — uniquement des SealedSecrets
- Le namespace `demo-app` est unique et dédié
- Le déploiement utilise exclusivement l'image GHCR produite par le pipeline CI
- `:latest` interdit — toujours un tag SHA immuable

### 2.2. Exigences de comportement

- Le SealedSecret doit être automatiquement déchiffré en `ghcr-secret` par le controller
- Le pod doit pouvoir tirer l'image GHCR sans erreur (`ImagePullBackOff` = échec)
- L'ingress doit exposer l'application via `demo.local`
- Le déploiement doit être idempotent (`kubectl apply -k` applicable plusieurs fois)

### 2.3. Exigences de reproductibilité

- L'ordre de déploiement (§1.5) doit permettre de reconstruire l'environnement complet depuis zéro
- Aucun secret ne doit être créé manuellement après la génération initiale du SealedSecret
- Tous les manifests nécessaires doivent être versionnés dans Git

> **Exception :** si le cluster kind est recréé, le SealedSecret existant ne sera plus déchiffrable — la clé de chiffrement est liée au cluster. Il faudra regénérer le SealedSecret (§1.4).

---

## 3. Concepts à maîtriser

### 3.1. Kustomize — base vs overlays

- **Base** : manifests communs à tous les environnements. Ne contient aucune valeur spécifique à dev ou prod.
- **Overlay** : couche qui hérite de la base et la surcharge via des patches (image, replicas).
- **`kustomization.yaml`** : fichier de déclaration qui liste les ressources et les patches à appliquer.
- **Idée clé** : `kubectl apply -k overlays/dev` = base + patches dev. Zéro duplication de YAML.

### 3.2. Sealed Secrets — chiffrement asymétrique

- **Problème** : les Secrets Kubernetes sont encodés en base64, pas chiffrés — les mettre dans Git expose les valeurs.
- **Solution** : Sealed Secrets chiffre le secret avec la clé publique du cluster. Seul le controller (qui possède la clé privée) peut le déchiffrer.
- **Flux** : `Secret en clair` → `kubeseal` (chiffrement avec clé publique) → `SealedSecret` (dans Git) → controller → `Secret Kubernetes` (dans le cluster).
- **Clé liée au cluster** : un SealedSecret chiffré pour un cluster ne peut pas être déchiffré par un autre cluster.

### 3.3. imagePullSecrets — authentification GHCR

- **Problème** : GHCR est un registry privé — Kubernetes doit s'authentifier pour tirer l'image.
- **Solution** : un Secret de type `docker-registry` contient les credentials. Il est référencé dans le Deployment via `imagePullSecrets`.
- **Avec Sealed Secrets** : le secret GHCR est chiffré en SealedSecret — il peut être versionné dans Git sans risque.

### 3.4. Idempotence Kubernetes

- **Définition** : appliquer les mêmes manifests plusieurs fois produit le même résultat — sans duplication ni erreur.
- **`kubectl apply`** : compare l'état désiré (manifests) avec l'état réel (cluster) et ne modifie que ce qui a changé.
- **Importance** : un déploiement non idempotent est non reproductible — c'est une exigence fondamentale DevSecOps.

### 3.5. Séparation des environnements — dev vs prod

- **dev** : 1 replica, image du dernier build CI, ressources minimales, déploiement rapide.
- **prod** : 3 replicas, image validée et immuable, ressources plus élevées, stabilité prioritaire.
- **Kustomize** permet cette séparation sans dupliquer les manifests communs.

---

## 4. Points de vigilance

- **La clé Sealed Secrets est liée au cluster** : si le cluster est recréé, tous les SealedSecrets existants doivent être régénérés.
- **Le controller doit être prêt avant `kubectl apply -k`** : sinon le SealedSecret reste en attente de déchiffrement.
- **Le fichier secret temporaire** (`/tmp/ghcr-secret.yaml`) doit être supprimé immédiatement après chiffrement.
- **`:latest` est interdit** en dev comme en prod — toujours un SHA immuable.
- **`commonLabels` dans `kustomization.yaml`** ajoute les labels à toutes les ressources — vérifier que cela n'entre pas en conflit avec les selectors existants.

### Si ça ne fonctionne pas

| Symptôme | Cause probable | Diagnostic |
|---|---|---|
| `ImagePullBackOff` | Secret GHCR manquant ou non déchiffré | `kubectl get secret -n demo-app` |
| SealedSecret non déchiffré | Controller Sealed Secrets non prêt | `kubectl get pods -n kube-system` |
| `kubectl apply -k` échoue | Chemins Kustomize incorrects | `kubectl kustomize k8s/overlays/dev` |
| Ingress inaccessible | ingress-nginx non prêt | `kubectl get pods -n ingress-nginx` |
| SealedSecret invalide après recréation cluster | Clé privée différente | Régénérer le SealedSecret (§1.4) |
| Ordre incorrect | cluster → ingress → sealed → overlay non respecté | Suivre §1.5 strictement |

---

## 5. Checklist de validation

| Vérification | Commande | Attendu |
|---|---|---|
| Structure k8s/ conforme | `ls k8s/base/ k8s/overlays/` | Tous les fichiers présents |
| SealedSecret présent | `kubectl get sealedsecret -n demo-app` | `ghcr-secret` visible |
| Controller Sealed Secrets prêt | `kubectl get pods -n kube-system` | `sealed-secrets-controller` Running |
| Secret GHCR déchiffré | `kubectl get secret ghcr-secret -n demo-app` | Secret présent |
| Pods en état Running | `kubectl get pods -n demo-app` | `Running` |
| Image GHCR tirée sans erreur | `kubectl describe pod -n demo-app` | Pas d'`ImagePullBackOff` |
| Ingress accessible | `curl http://demo.local/actuator/health` | `{"status":"UP"}` |
| Kustomize valide (dev) | `kubectl kustomize k8s/overlays/dev` | YAML valide sans erreur |
| Kustomize valide (prod) | `kubectl kustomize k8s/overlays/prod` | YAML valide sans erreur |
| Idempotence | `kubectl apply -k k8s/overlays/dev` (2e fois) | `unchanged` sur toutes les ressources |

✅ Tous ces points validés → enchaîner sur **Itération 3 (Immutabilité & SHA)**.

---

## Références croisées

- **K8S-ARCH-01** — Architecture Kubernetes sécurisée (V4-A)
- **K8S-ARCH-02** — Implémentation de l'architecture Kubernetes de base (V4-A)
- **K8S-ARCH-03 itération 1** — Fondations + Ingress complet
- **K8S-ARCH-03 roadmap** — Roadmap itérative V4-A
- **SEC-K8S-02** — Secrets Kubernetes (Sealed Secrets)


## 6. Étape 1 — Implémenter les overlays dev et prod

On va créer :

    k8s/overlays/dev/kustomization.yaml

    k8s/overlays/dev/patch-deployment.yaml

    k8s/overlays/prod/kustomization.yaml

    k8s/overlays/prod/patch-deployment.yaml

Et on va s’assurer que :

    la structure est correcte

    les chemins sont bons

    les patches sont valides

    les images utilisent bien des SHA immuables

    rien n’est dupliqué inutilement

🟦 1. Création de la structure

Voici ce que tu dois créer :
Code

k8s/
  overlays/
    dev/
      kustomization.yaml
      patch-deployment.yaml
    prod/
      kustomization.yaml
      patch-deployment.yaml

Tu confirmes que cette structure existe déjà dans ton repo ?
Sinon je te génère les fichiers exacts.

🟩 2. Contenu de l’overlay dev
k8s/overlays/dev/kustomization.yaml
yaml

apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: demo-app

resources:
  - ../../base

patches:
  - path: patch-deployment.yaml

k8s/overlays/dev/patch-deployment.yaml
yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-app
spec:
  replicas: 1
  template:
    spec:
      containers:
        - name: demo-app
          image: ghcr.io/<org>/<repo>:<sha-dev>

👉 Tu remplaceras <sha-dev> par le SHA du dernier build CI.
🟧 3. Contenu de l’overlay prod
k8s/overlays/prod/kustomization.yaml
yaml

apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: demo-app

resources:
  - ../../base

patches:
  - path: patch-deployment.yaml

k8s/overlays/prod/patch-deployment.yaml
yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-app
spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: demo-app
          image: ghcr.io/<org>/<repo>:<sha-prod>

👉 Tu remplaceras <sha-prod> par le SHA validé pour la prod.
🟪 4. Vérification Kustomize (important)

Avant d’aller plus loin, on valide que les overlays sont corrects :
Code

kubectl kustomize k8s/overlays/dev
kubectl kustomize k8s/overlays/prod

Tu dois obtenir un YAML complet, sans erreur.


## 7 Commandes utilisées pour la génération du SealedSecret

🟦 A. Installation de l’environnement Linux (WSL2)

    Installation d’Ubuntu via .appx

    Lancement de la distribution

    Mise à jour du système

    Installation des outils nécessaires (curl, wget, etc.)

🟦 B. Installation de kubectl dans Ubuntu WSL2

    Ajout du dépôt Kubernetes moderne pkgs.k8s.io

    Installation de kubectl

    Vérification de la version

🟦 C. Connexion d’Ubuntu WSL2 au cluster kind

    Copie du kubeconfig Windows → Ubuntu :
    Code

    mkdir -p ~/.kube
    cp /mnt/c/Users/vincent/.kube/config ~/.kube/config
    chmod 600 ~/.kube/config

    Vérification :
    Code

    kubectl get nodes

🟦 D. Installation de kubeseal

    Téléchargement du binaire

    Extraction

    Installation dans /usr/local/bin

    Vérification :
    Code

    kubeseal --version

🟦 E. Génération du secret clair GHCR
Code

kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=vcharrie \
  --docker-password=<TOKEN> \
  --namespace=coreservice \
  --dry-run=client -o yaml > secret.yaml

🟦 F. Scellement du secret avec la clé publique du cluster
Code

kubeseal --format yaml < secret.yaml > sealedsecret.yaml

🟦 G. Application du SealedSecret dans le cluster kind
Code

kubectl apply -f sealedsecret.yaml

🟦 H. Vérification que le Secret Kubernetes a été généré
Code

kubectl get secrets -n coreservice

🟦 I. Mise à jour du Deployment pour utiliser imagePullSecrets

Dans le manifest :
yaml

imagePullSecrets:
  - name: ghcr-secret

Ou patch :
Code

kubectl patch deployment coreservice-deployment \
  -n coreservice \
  --type merge \
  -p '{"spec":{"template":{"spec":{"imagePullSecrets":[{"name":"ghcr-secret"}]}}}}'

🟦 J. Redeploy du Deployment
Code

kubectl rollout restart deployment coreservice-deployment -n coreservice

🟦 K. Vérification du pull GHCR
Code

kubectl get pods -n coreservice
kubectl describe pod <pod> -n coreservice
