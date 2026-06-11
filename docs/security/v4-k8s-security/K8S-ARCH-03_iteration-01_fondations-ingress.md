# K8S-ARCH-03 — Itération 1 : Fondations + Ingress complet (V4-A)

**Domaine :** Orchestration / Kubernetes  
**Couche :** Implémentation locale — fondations  
**Statut :** 🟦 À implémenter

> **Objectif :** obtenir un cluster local fonctionnel, avec routing HTTP opérationnel, image SHA immuable, namespace dédié, et structure Kustomize propre.

---

## 1. Fiche d'implémentation

### 1.1. Créer la structure k8s/

```
k8s/
  base/
    namespace.yaml
    deployment.yaml
    service.yaml
    ingress.yaml
    configmap.yaml
  overlays/
    dev/
      kustomization.yaml
      configmap-dev.yaml
      patches-dev.yaml
```

> **Rappel :** aucun `secret.yaml` dans Git, même en placeholder. Les secrets sont introduits exclusivement via Sealed Secrets en itération 2.

---

### 1.2. Manifests de base

#### Namespace

```yaml
# k8s/base/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: demo-app
  labels:
    app: demo-app
```

---

#### ConfigMap (base)

```yaml
# k8s/base/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: demo-app-config
  namespace: demo-app
data:
  APP_ENV: "dev"
```

> Les valeurs spécifiques à chaque environnement sont surchargées via un ConfigMap dans chaque overlay.

---

#### Deployment (base)

```yaml
# k8s/base/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-app
  namespace: demo-app
  labels:
    app: demo-app
    tier: backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: demo-app
  template:
    metadata:
      labels:
        app: demo-app
        tier: backend
    spec:
      containers:
        - name: demo-app
          image: ghcr.io/<org>/<repo>:<sha>
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: demo-app-config
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 10
            failureThreshold: 3
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 15
            failureThreshold: 3
          resources:
            requests:
              cpu: "100m"
              memory: "256Mi"
            limits:
              cpu: "500m"
              memory: "512Mi"
```

> **Note probes :** `failureThreshold: 3` est explicite ici — sans ce paramètre, Kubernetes utilise la valeur par défaut qui peut être trop agressive pour le démarrage de Spring Boot. Les valeurs `initialDelaySeconds` sont adaptées à une application Spring Boot standard — à ajuster si le démarrage est plus long.

> **Image :** remplacer `<sha>` par le tag SHA immuable produit par le pipeline CI/CD (V3). Ne jamais utiliser `:latest`.

---

#### Service (base)

```yaml
# k8s/base/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: demo-app
  namespace: demo-app
spec:
  selector:
    app: demo-app
  ports:
    - name: http
      port: 80
      targetPort: 8080
  type: ClusterIP
```

---

#### Ingress (base)

```yaml
# k8s/base/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: demo-app
  namespace: demo-app
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  ingressClassName: nginx
  rules:
    - host: demo.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: demo-app
                port:
                  number: 80
```

---

### 1.3. Overlay dev

#### kustomization.yaml

```yaml
# k8s/overlays/dev/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: demo-app

resources:
  - ../../base
  - configmap-dev.yaml

patches:
  - path: patches-dev.yaml
```

---

#### configmap-dev.yaml

```yaml
# k8s/overlays/dev/configmap-dev.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: demo-app-config
  namespace: demo-app
data:
  APP_ENV: "dev"
```

---

#### patches-dev.yaml

```yaml
# k8s/overlays/dev/patches-dev.yaml
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

> **Convention :** le patch porte à la fois `replicas` et `image` — cela établit la convention réutilisée en overlay prod (replicas: 2, sha-prod). Remplacer `<sha-dev>` par le tag SHA immuable produit par le pipeline CI.

---

### 1.4. Déploiement local

#### Créer le cluster kind

```bash
kind create cluster --name demo --config k8s/config/kind-config.yaml
kubectl config use-context kind-demo
```

Fichier de configuration kind (`k8s/config/kind-config.yaml`) — requis pour exposer les ports 80/443 :

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
  - role: control-plane
    extraPortMappings:
      - containerPort: 80
        hostPort: 80
        protocol: TCP
      - containerPort: 443
        hostPort: 443
        protocol: TCP
  - role: worker
```

> **Pourquoi cette config ?** Sans le mapping de ports, l'Ingress Controller ne peut pas exposer le port 80 sur le poste — `curl http://demo.local` ne fonctionnera pas.

---

#### Installer l'ingress-nginx controller

> **Prérequis obligatoire :** le controller NGINX doit être installé et prêt **avant** d'appliquer les manifests applicatifs. Sans cette étape, l'Ingress ne fonctionnera pas.

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/kind/deploy.yaml

# Attendre que le controller soit prêt
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```

---

#### Créer le secret d'authentification GHCR

L'image GHCR est privée. Kubernetes doit disposer d'un secret Docker pour pouvoir la tirer.

```bash
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=<ton_user> \
  --docker-password=<ton_token_ghcr> \
  --namespace=demo-app
```

> **Important :** ce secret ne doit jamais être commité dans Git. Il est créé manuellement une fois par cluster. Le Deployment doit le référencer via `imagePullSecrets`.

Ajouter dans le Deployment (`spec.template.spec`) :

```yaml
imagePullSecrets:
  - name: ghcr-secret
```

---

#### Appliquer les manifests

```bash
# Vérifier le rendu Kustomize avant d'appliquer
kubectl kustomize k8s/overlays/dev

# Appliquer
kubectl apply -k k8s/overlays/dev

# Vérifier l'état des ressources
kubectl get all -n demo-app
kubectl get ingress -n demo-app
```

---

#### Configurer la résolution DNS locale

**Linux/macOS** — ajouter dans `/etc/hosts` :

```
127.0.0.1   demo.local
```

**Windows** — ajouter dans `C:\Windows\System32\drivers\etc\hosts` :

```
127.0.0.1   demo.local
```

---

#### Tester l'accès

```bash
curl http://demo.local/actuator/health
```

Attendu :

```json
{"status":"UP"}
```

> **Si le port 80 n'est pas exposé automatiquement**, utiliser un port-forward :

```bash
kubectl port-forward -n ingress-nginx svc/ingress-nginx-controller 8080:80
# Puis tester :
curl http://localhost:8080/actuator/health
```

---

## 2. Fiche d'exigences

### 2.1. Exigences d'architecture

- Manifests déclaratifs uniquement
- Kustomize base + overlay
- Namespace dédié
- Service ClusterIP
- Ingress NGINX opérationnel
- Image SHA immuable — `:latest` interdit
- Pas de secrets en clair dans Git
- Probes obligatoires avec `failureThreshold` explicite
- Requests/Limits obligatoires

### 2.2. Exigences de comportement

- Pod en état `Running`
- Readiness probe OK
- Liveness probe OK
- Service routant correctement vers le pod
- Ingress routant correctement vers le service
- Health endpoint répondant `{"status":"UP"}`

### 2.3. Exigences de reproductibilité

- `kubectl kustomize` doit fonctionner sans erreur
- `kubectl apply -k` doit être idempotent (applicable plusieurs fois sans effet de bord)
- Le cluster kind doit être recréable à l'identique via `kind-config.yaml`
- Aucun secret dans Git

---

## 3. Concepts à maîtriser

### 3.1. Concepts fondamentaux

- **Pod / Deployment / ReplicaSet** — relation et cycle de vie
- **Service ClusterIP** — exposition interne uniquement, stabilité de l'endpoint
- **Ingress vs Ingress Controller** — le manifest Ingress déclare les règles, le controller les applique
- **DNS interne (CoreDNS)** — résolution des noms de service dans le cluster
- **`/etc/hosts` et résolution locale** — contournement DNS pour le développement local
- **Probes (readiness vs liveness)** — distinction sémantique : readiness = prêt à recevoir du trafic, liveness = vivant
- **Requests & Limits** — requests = garanti, limits = plafond
- **Kustomize (base, overlay, patches)** — layering déclaratif sans duplication

---

## 4. Points de vigilance

- Le cluster kind doit être créé avec le fichier `kind-config.yaml` (mapping ports 80/443) — sinon l'Ingress ne fonctionnera pas
- Le controller ingress-nginx doit être en état `Ready` **avant** d'appliquer les manifests applicatifs
- Le secret GHCR doit être créé manuellement dans le namespace avant le premier déploiement
- Spring Boot nécessite un `initialDelaySeconds` suffisant — tuer le pod trop tôt est l'erreur la plus fréquente en itération 1
- Le port-forward peut être nécessaire selon la configuration kind — la commande est fournie en §1.4
- Le namespace doit être créé avant les autres ressources — Kustomize respecte l'ordre si `namespace.yaml` est listé en premier dans `resources:`

### Si ça ne fonctionne pas

| Symptôme | Cause probable | Diagnostic |
|---|---|---|
| Pod en `Pending` | Ressources insuffisantes ou image introuvable | `kubectl describe pod -n demo-app` |
| Pod en `ImagePullBackOff` | Secret GHCR manquant ou token expiré | `kubectl describe pod -n demo-app` → section Events |
| Pod en `CrashLoopBackOff` | App qui plante au démarrage | `kubectl logs -n demo-app <pod>` |
| Pod en `Running` mais probe KO | `initialDelaySeconds` trop court | `kubectl describe pod -n demo-app` → section Events |
| Ingress ne route pas | Controller pas prêt, mapping ports manquant, ou annotation manquante | `kubectl get ingress -n demo-app` + `kubectl describe ingress -n demo-app` |
| `curl` timeout | Port-forward nécessaire ou mapping ports absent du cluster | Utiliser `kubectl port-forward` (cf. §1.4) |

---

## 5. Checklist de validation

| Vérification | Commande | Attendu |
|---|---|---|
| Namespace créé | `kubectl get ns demo-app` | `Active` |
| Pod en cours d'exécution | `kubectl get pods -n demo-app` | `Running` |
| Probes OK | `kubectl describe pod -n demo-app` | `Readiness` et `Liveness` OK |
| Service accessible | `kubectl get svc -n demo-app` | `ClusterIP` défini |
| Endpoints peuplés | `kubectl get endpoints -n demo-app` | IP du pod visible |
| Ingress configuré | `kubectl get ingress -n demo-app` | Host `demo.local` visible |
| Routing HTTP | `curl http://demo.local/actuator/health` | `{"status":"UP"}` |
| ConfigMap appliqué | `kubectl get configmap -n demo-app` | `demo-app-config` présent |
| Kustomize valide | `kubectl kustomize k8s/overlays/dev` | YAML valide sans erreur |

✅ Tous ces points validés → enchaîner sur **Itération 2 (Configuration & Secrets)**.

---

## 6. Concepts développés

### 6.1. Pod / Deployment / ReplicaSet — relation et cycle de vie

- **Pod** : plus petite unité d'exécution dans Kubernetes, regroupe un ou plusieurs containers qui partagent réseau et volumes.
- **ReplicaSet** : objet qui garantit qu'un nombre donné de Pods identiques est toujours en cours d'exécution (ex : 1, 3, 5 replicas).
- **Deployment** : objet de plus haut niveau qui gère les ReplicaSets (création, mise à jour, rollback).
- **Cycle de vie** : tu modifies le Deployment → il crée un nouveau ReplicaSet → qui crée de nouveaux Pods → l'ancien ReplicaSet est progressivement supprimé.

### 6.2. Service ClusterIP — exposition interne, stabilité de l'endpoint

- **Rôle** : fournir une IP stable et un nom DNS stable pour accéder à un ensemble de Pods.
- **ClusterIP** : type par défaut, accessible uniquement depuis l'intérieur du cluster.
- **Décorrélation** : même si les Pods changent (recréés, déplacés), le Service reste identique → l'endpoint ne change pas.

### 6.3. Ingress vs Ingress Controller

- **Ingress** : ressource Kubernetes qui décrit des règles HTTP(S) (host, path, backend service). C'est déclaratif.
- **Ingress Controller** : composant qui lit les Ingress et configure un reverse proxy (NGINX, Traefik, ALB…) pour appliquer ces règles.
- **Idée clé** : sans Ingress Controller, un Ingress ne fait rien. Le manifest décrit, le controller exécute.

```
Navigateur → Ingress Controller → Service → Pod
```

### 6.4. DNS interne (CoreDNS)

- **CoreDNS** : serveur DNS interne du cluster Kubernetes.
- **Rôle** : résoudre les noms des Services (ex : `demo-app.demo-app.svc.cluster.local`) en IP ClusterIP.
- **Conséquence** : les Pods peuvent se parler entre eux via des noms logiques sans connaître les IP.

### 6.5. /etc/hosts et résolution locale

- **Problème** : en local, `demo.local` n'existe pas dans le DNS public.
- **Solution** : ajouter une entrée dans `/etc/hosts` (`127.0.0.1 demo.local`) pour forcer la résolution.
- **Usage** : permet de tester un Ingress local comme si on avait un vrai nom de domaine.

### 6.6. Probes — readiness vs liveness

- **Readiness probe** : indique si l'application est prête à recevoir du trafic. Si elle échoue, le Pod reste en `Running` mais ne reçoit plus de requêtes via le Service.
- **Liveness probe** : indique si l'application est vivante. Si elle échoue, Kubernetes redémarre le container.
- **Idée clé** : readiness = "je suis prêt", liveness = "je suis en vie".

### 6.7. Requests & Limits

- **Requests** : ressources minimales garanties pour le Pod (CPU, mémoire). Le scheduler les utilise pour placer le Pod sur un nœud.
- **Limits** : ressources maximales que le Pod a le droit de consommer.
- **CPU** : si le Pod dépasse le limit CPU → il est throttlé.
- **Mémoire** : si le Pod dépasse le limit mémoire → il peut être tué (`OOMKilled`).

### 6.8. Kustomize — base, overlay, patches

- **Base** : manifests communs à tous les environnements (namespace, deployment, service, ingress, configmap générique).
- **Overlay** : couche spécifique à un environnement (dev, prod) qui réutilise la base et la surcharge (image, replicas, config).
- **Patches** : fragments YAML qui modifient certains champs d'un manifest existant (ex : `spec.replicas`, `containers[].image`).
- **Idée clé** : tu évites de dupliquer des fichiers YAML entiers, tu composes par couches.

---

## 7. Namespace — détail d'implémentation

### 7.1. Rôle, utilité et bonnes pratiques

Un namespace est une unité logique d'isolation au sein d'un cluster Kubernetes. Il permet de regrouper et d'organiser les ressources (Pods, Services, Deployments, ConfigMaps, Secrets…) dans un périmètre cohérent.

**Objectifs principaux :**

- **Organisation** : éviter que toutes les ressources soient mélangées dans le namespace `default`.
- **Isolation logique** : séparer les applications, les environnements (dev, staging, prod) ou les équipes.
- **Éviter les collisions de noms** : deux Deployments peuvent s'appeler `api` s'ils sont dans des namespaces différents.
- **Application ciblée des règles** : RBAC, quotas CPU/mémoire, NetworkPolicies, PodSecurity, etc.
- **Gestion simplifiée** : un namespace peut être supprimé en une seule commande, ce qui supprime toutes les ressources qu'il contient.

**Pourquoi un namespace dédié dès l'itération 1 ?**

- Isoler l'application du reste du cluster
- Préparer les futurs overlays Kustomize (dev, prod)
- Préparer les futures règles de sécurité (RBAC, quotas, NetworkPolicies)
- Éviter les mauvaises pratiques liées à l'utilisation du namespace `default`

### 7.2. Commandes essentielles

```bash
# Appliquer le manifest
kubectl apply -f k8s/base/namespace.yaml

# Vérifier que le namespace existe et est actif
kubectl get ns demo-app

# Inspecter les détails
kubectl describe ns demo-app
```

**Résultat attendu :**
- Le namespace `demo-app` apparaît en `STATUS = Active`
- Aucun événement d'erreur dans `kubectl describe`

---

## 8. Deployment — détail d'implémentation

### 8.1. Rôle, utilité et bonnes pratiques

Un Deployment est l'objet Kubernetes qui gère le déploiement de ton application, la mise à jour (rolling update), la reprise automatique en cas de crash (self-healing), le scaling (nombre de replicas), et la cohérence entre l'état désiré et l'état réel.

**Bonnes pratiques essentielles :**

- **Labels cohérents** : les labels définis sur le Deployment seront réutilisés par le Service, les NetworkPolicies, et les outils de monitoring.
- **Selector stable** : le `selector.matchLabels` doit toujours correspondre aux labels du Pod template.
- **`containerPort` défini** : permet au Service de cibler le bon port.
- **Rester simple en itération 1** : les `securityContext`, stratégies de rolling update avancées, et autres durcissements arrivent en itération 4.

### 8.2. Commandes essentielles

```bash
# Appliquer le Deployment
kubectl apply -f k8s/base/deployment.yaml

# Vérifier les Pods
kubectl get pods -n demo-app

# Suivre le rollout
kubectl rollout status deployment/demo-app -n demo-app

# Consulter les logs applicatifs
kubectl logs -n demo-app -l app=demo-app

# Forcer un redéploiement si nécessaire
kubectl rollout restart deployment demo-app -n demo-app
```

---

## 9. Service — détail d'implémentation

### 9.1. Rôle, utilité et bonnes pratiques

Un Service Kubernetes fournit une adresse réseau stable pour accéder à un ou plusieurs Pods. Il résout trois problèmes structurels : les Pods sont éphémères, leurs IP changent à chaque recréation, et les autres composants ont besoin d'un point d'accès fixe.

**Bonnes pratiques essentielles :**

- **Aligner les labels avec le Deployment** : le `selector` du Service doit cibler exactement les mêmes labels que le Pod. Si les labels ne correspondent pas → le Service ne voit aucun Pod.
- **Toujours définir `port` et `targetPort`** : `port` = port du Service, `targetPort` = port du container.
- **Type `ClusterIP` en itération 1** : exposition interne uniquement, parfait pour un cluster local avec Ingress.

### 9.2. Commandes essentielles

```bash
# Vérifier que le Service existe
kubectl get service -n demo-app

# Vérifier que le Service cible bien les Pods (endpoints peuplés)
kubectl get endpoints -n demo-app demo-app

# Vérifier les labels du Pod (diagnostic si endpoints vides)
kubectl get pods -n demo-app --show-labels

# Tester l'accès depuis l'intérieur du cluster
kubectl run tmp --rm -it --image=busybox -- sh
# Puis dans le shell :
# wget -qO- http://demo-app.demo-app.svc.cluster.local
```

> **Si les endpoints sont vides** → problème de labels entre le Service et le Pod.

---

## 10. Ingress — détail d'implémentation

### 10.1. Rôle, utilité et bonnes pratiques

Un Ingress est la ressource Kubernetes qui permet d'exposer l'application depuis l'extérieur du cluster, via un nom de domaine, des règles HTTP/HTTPS, et un reverse proxy (NGINX en V4-A, ALB en V4-B).

**Schéma mental :**

```
Navigateur → Ingress Controller → Service → Pod
```

**Bonnes pratiques essentielles :**

- **Toujours cibler le Service, jamais le Pod** directement.
- **Toujours définir un `host`** même en local — cela permet un routage propre et prépare la future gestion TLS.
- **Vérifier que l'Ingress Controller est installé** — sans lui, l'Ingress ne fait rien.
- **Rester minimaliste en itération 1** : pas de TLS, pas de règles multiples, pas d'annotations avancées.

### 10.2. Commandes essentielles

```bash
# Vérifier que l'Ingress est créé
kubectl get ingress -n demo-app

# Vérifier que la règle pointe vers le bon Service
kubectl describe ingress -n demo-app demo-app

# Vérifier que l'Ingress Controller est actif
kubectl get pods -n ingress-nginx

# Tester depuis le poste
curl http://demo.local/actuator/health
```

---

## 11. Ordre de déploiement depuis un cluster kind propre

En cas de recréation du cluster, voici l'ordre exact à respecter :

```bash
# 1. Supprimer l'ancien cluster
kind delete cluster --name demo

# 2. Recréer avec le mapping ports 80/443
kind create cluster --name demo --config k8s/config/kind-config.yaml

# 3. Installer l'Ingress Controller NGINX
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s

# 4. Créer le secret GHCR
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=<ton_user> \
  --docker-password=<ton_token> \
  -n demo-app

# 5. Appliquer tous les manifests via Kustomize
kubectl apply -k k8s/overlays/dev

# 6. Vérifier
kubectl get all -n demo-app
kubectl get ingress -n demo-app

# 7. Tester
curl http://demo.local/actuator/health
```

> **Résultat attendu :** `{"status":"UP"}`

---

## Références croisées

- **K8S-ARCH-01** — Architecture Kubernetes sécurisée (V4-A)
- **K8S-ARCH-02** — Implémentation de l'architecture Kubernetes de base (V4-A)
- **K8S-ARCH-03** — Roadmap itérative V4-A
- **SEC-K8S-02** — Secrets Kubernetes (Sealed Secrets) — itération 2
