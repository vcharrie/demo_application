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
kind create cluster --name demo
kubectl config use-context kind-demo
```

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

Ajouter dans `/etc/hosts` :

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
- Le cluster kind doit être recréable à l'identique
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

- Le controller ingress-nginx doit être en état `Ready` **avant** d'appliquer les manifests applicatifs
- Spring Boot nécessite un `initialDelaySeconds` suffisant — tuer le pod trop tôt est l'erreur la plus fréquente en itération 1
- Le port-forward peut être nécessaire selon la configuration kind — la commande est fournie en §1.4
- L'image SHA doit exister et être accessible dans GHCR depuis le cluster
- Le namespace doit être créé avant les autres ressources — Kustomize respecte l'ordre si `namespace.yaml` est listé en premier dans `resources:`

### Si ça ne fonctionne pas

| Symptôme | Cause probable | Diagnostic |
|---|---|---|
| Pod en `Pending` | Ressources insuffisantes ou image introuvable | `kubectl describe pod -n demo-app` |
| Pod en `CrashLoopBackOff` | App qui plante au démarrage | `kubectl logs -n demo-app <pod>` |
| Pod en `Running` mais probe KO | `initialDelaySeconds` trop court | `kubectl describe pod -n demo-app` → section Events |
| Ingress ne route pas | Controller pas prêt ou annotation manquante | `kubectl get ingress -n demo-app` + `kubectl describe ingress -n demo-app` |
| `curl` timeout | Port-forward nécessaire | Utiliser `kubectl port-forward` (cf. §1.4) |

---

## 5. Checklist de validation

| Vérification | Commande | Attendu |
|---|---|---|
| Namespace créé | `kubectl get ns demo-app` | `Active` |
| Pod en cours d'exécution | `kubectl get pods -n demo-app` | `Running` |
| Probes OK | `kubectl describe pod -n demo-app` | `Readiness` et `Liveness` OK |
| Service accessible | `kubectl get svc -n demo-app` | `ClusterIP` défini |
| Ingress configuré | `kubectl get ingress -n demo-app` | Host `demo.local` visible |
| Routing HTTP | `curl http://demo.local/actuator/health` | `{"status":"UP"}` |
| ConfigMap appliqué | `kubectl get configmap -n demo-app` | `demo-app-config` présent |
| Kustomize valide | `kubectl kustomize k8s/overlays/dev` | YAML valide sans erreur |

✅ Tous ces points validés → enchaîner sur **Itération 2 (Configuration & Secrets)**.

---

## Références croisées

- **K8S-ARCH-01** — Architecture Kubernetes sécurisée (V4-A)
- **K8S-ARCH-02** — Implémentation de l'architecture Kubernetes de base (V4-A)
- **K8S-ARCH-03** — Roadmap itérative V4-A
- **SEC-K8S-02** — Secrets Kubernetes (Sealed Secrets) — itération 2


## 6. Concepts à développer

Pod / Deployment / ReplicaSet — relation et cycle de vie

    Pod : plus petite unité d’exécution dans Kubernetes, regroupe un ou plusieurs containers qui partagent réseau et volumes.

    ReplicaSet : objet qui garantit qu’un nombre donné de Pods identiques est toujours en cours d’exécution (ex : 1, 3, 5 replicas).

    Deployment : objet de plus haut niveau qui gère les ReplicaSets (création, mise à jour, rollback).

    Cycle de vie : tu modifies le Deployment → il crée un nouveau ReplicaSet → qui crée de nouveaux Pods → l’ancien ReplicaSet est progressivement supprimé.

Service ClusterIP — exposition interne uniquement, stabilité de l’endpoint

    Rôle : fournir une IP stable et un nom DNS stable pour accéder à un ensemble de Pods.

    ClusterIP : type par défaut, accessible uniquement depuis l’intérieur du cluster (pas d’accès direct depuis l’extérieur).

    Décorrélation : même si les Pods changent (recréés, déplacés), le Service reste identique → l’endpoint ne change pas.

Ingress vs Ingress Controller — le manifest Ingress déclare les règles, le controller les applique

    Ingress : ressource Kubernetes qui décrit des règles HTTP(S) (host, path, backend service). C’est déclaratif.

    Ingress Controller : composant qui lit les Ingress et configure un reverse proxy (NGINX, Traefik, ALB…) pour appliquer ces règles.

    Idée clé : sans Ingress Controller, un Ingress ne fait rien. Le manifest décrit, le controller exécute.

DNS interne (CoreDNS) — résolution des noms de service dans le cluster

    CoreDNS : serveur DNS interne du cluster Kubernetes.

    Rôle : résoudre les noms des Services (ex : demo-app.demo-app.svc.cluster.local) en IP ClusterIP.

    Conséquence : les Pods peuvent se parler entre eux via des noms logiques (http://demo-app) sans connaître les IP.

/etc/hosts et résolution locale — contournement DNS pour le développement local

    Problème : en local, demo.local n’existe pas dans le DNS public.

    Solution : ajouter une entrée dans /etc/hosts (127.0.0.1 demo.local) pour forcer la résolution.

    Usage : permet de tester un Ingress local comme si on avait un vrai nom de domaine.

Probes (readiness vs liveness) — distinction sémantique

    Readiness probe : indique si l’application est prête à recevoir du trafic. Si elle échoue, le Pod reste en Running mais ne reçoit plus de requêtes via le Service.

    Liveness probe : indique si l’application est vivante. Si elle échoue, Kubernetes redémarre le container.

    Idée clé : readiness = “je suis prêt”, liveness = “je suis en vie”.

Requests & Limits — requests = garanti, limits = plafond

    Requests : ressources minimales garanties pour le Pod (CPU, mémoire). Le scheduler les utilise pour placer le Pod sur un nœud.

    Limits : ressources maximales que le Pod a le droit de consommer.

    CPU : si le Pod dépasse le limit CPU → il est throttlé.

    Mémoire : si le Pod dépasse le limit mémoire → il peut être tué (OOMKilled).

Kustomize (base, overlay, patches) — layering déclaratif sans duplication

    Base : manifests communs à tous les environnements (namespace, deployment, service, ingress, configmap générique).

    Overlay : couche spécifique à un environnement (dev, prod) qui réutilise la base et la surcharge (image, replicas, config).

    Patches : fragments YAML qui modifient certains champs d’un manifest existant (ex : spec.replicas, containers[].image).

    Idée clé : tu évites de dupliquer des fichiers YAML entiers, tu composes par couches.