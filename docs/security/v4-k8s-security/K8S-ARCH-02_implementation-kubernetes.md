# K8S-ARCH-02 — Implémentation de l'architecture Kubernetes de base (V4-A)

**Domaine :** Orchestration / Kubernetes  
**Couche :** Implémentation architecture de base (local)  
**Statut :** 🟦 À implémenter

> **Note :** cette fiche est opérationnelle, pas une fiche sécurité. Elle met en place la fondation sur laquelle s'appuieront SEC-K8S-01 → 04.

---

## 1. Objectif

Mettre en place concrètement l'architecture définie dans K8S-ARCH-01 :

- créer la structure `k8s/`
- écrire les manifests de base
- préparer les overlays `dev` / `prod`
- valider un premier déploiement local fonctionnel

---

## 2. Structure du répertoire k8s/

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
      patches-dev.yaml
    prod/
      kustomization.yaml
      patches-prod.yaml
```

> **Rappel :** aucun `secret.yaml` dans Git, même en placeholder. Les secrets sont introduits exclusivement via Sealed Secrets en SEC-K8S-02.

---

## 3. Manifests de base

### 3.1. Namespace

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

### 3.2. ConfigMap

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

> Les valeurs spécifiques à chaque environnement (dev/prod) sont surchargées via un ConfigMap dans chaque overlay — pas via `env:` direct dans le patch Deployment.

---

### 3.3. Deployment

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

> **Note probes :** Spring Boot nécessite plusieurs secondes pour démarrer. Sans `initialDelaySeconds`, Kubernetes tue le pod avant qu'il soit prêt. Les valeurs ci-dessus sont adaptées à une application Spring Boot standard — à ajuster si le démarrage est plus long.

> **Image :** remplacer `<sha>` par le tag SHA immuable produit par le pipeline CI/CD (V3).

---

### 3.4. Service

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

### 3.5. Ingress

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

## 4. Kustomize overlays

### 4.1. Overlay dev

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

---

### 4.2. Overlay prod

```yaml
# k8s/overlays/prod/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: demo-app

resources:
  - ../../base
  - configmap-prod.yaml

patches:
  - path: patches-prod.yaml
```

```yaml
# k8s/overlays/prod/configmap-prod.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: demo-app-config
  namespace: demo-app
data:
  APP_ENV: "prod"
```

```yaml
# k8s/overlays/prod/patches-prod.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-app
  namespace: demo-app
spec:
  replicas: 2
  template:
    spec:
      containers:
        - name: demo-app
          image: ghcr.io/<org>/<repo>:<sha-prod>
```

---

## 5. Tests locaux

### 5.1. Création du cluster kind

```bash
kind create cluster --name demo
kubectl config use-context kind-demo
```

---

### 5.2. Installation de l'ingress-nginx controller

> **Prérequis obligatoire :** l'ingress controller n'est pas installé par défaut dans kind. Sans cette étape, l'Ingress ne fonctionnera pas.

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/kind/deploy.yaml

# Attendre que le controller soit prêt
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```

---

### 5.3. Application des manifests

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

### 5.4. Accès local

Ajouter dans `/etc/hosts` :

```
127.0.0.1   demo.local
```

Si l'ingress controller n'expose pas automatiquement le port 80, utiliser un port-forward :

```bash
kubectl port-forward -n ingress-nginx svc/ingress-nginx-controller 8080:80
```

Tester l'accès :

```bash
curl http://demo.local/actuator/health
# ou avec port-forward :
curl http://localhost:8080/actuator/health
```

---

## 6. Pipeline CD — périmètre V4-A

> **Important :** en V4-A, le cluster Kubernetes est local. Un job CD GitHub Actions ne peut pas atteindre un cluster local directement. **Le déploiement en V4-A se fait manuellement depuis le poste** avec les commandes §5.3.

Le job CD GitHub Actions sera opérationnel à partir de **V4-B**, quand le cluster sera accessible depuis le runner (EKS + OIDC GitHub → AWS).

Structure du job CD (à titre de référence pour V4-B) :

```yaml
# .github/workflows/cd.yml (V4-B)
- name: Deploy to dev
  run: kubectl apply -k k8s/overlays/dev
```

> La vérification de digest et le durcissement CD (gate SEC-CD-01) arrivent en V4-B.

---

## 7. Checklist de validation avant SEC-K8S-01

Avant de passer aux fiches sécurité, valider chaque point :

| Critère | Commande de vérification | Attendu |
|---|---|---|
| Namespace créé | `kubectl get ns demo-app` | `Active` |
| Pod en cours d'exécution | `kubectl get pods -n demo-app` | `Running` |
| Readiness probe OK | `kubectl describe pod -n demo-app` | `Readiness probe succeeded` |
| Service accessible | `kubectl get svc -n demo-app` | `ClusterIP` défini |
| Ingress configuré | `kubectl get ingress -n demo-app` | Host `demo.local` visible |
| Health endpoint OK | `curl http://demo.local/actuator/health` | `{"status":"UP"}` |
| ConfigMap appliqué | `kubectl get configmap -n demo-app` | `demo-app-config` présent |

✅ Tous ces points validés → enchaîner sur **SEC-K8S-01 (SecurityContext)**.

---

## Références croisées

- **K8S-ARCH-01** — Architecture Kubernetes sécurisée (V4-A)
- **SEC-K8S-01** — SecurityContext
- **SEC-K8S-02** — Secrets Kubernetes (Sealed Secrets)
- **SEC-K8S-03** — Network Policies
- **SEC-K8S-04** — RBAC
- **SEC-CI-01** — Pipeline CI/CD (build + push image GHCR)
