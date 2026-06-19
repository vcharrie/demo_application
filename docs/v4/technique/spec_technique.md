# 📘 SPÉCIFICATION TECHNIQUE — VERSION V4-A
## Déploiement Kubernetes local (kind)

> **Périmètre :** Cette version introduit le premier déploiement Kubernetes de CoreService dans un cluster local (kind). Elle n'ajoute aucune logique applicative : uniquement l'infrastructure Kubernetes, la structure Kustomize, la gestion sécurisée des secrets et le processus de déploiement manuel.
>
> **Relation avec V4-B :** chaque choix technique de cette version locale est fait en anticipation d'un déploiement cloud (EKS). Les points de continuité sont signalés explicitement tout au long du document.

---

## 1. 🎯 Objet & périmètre

### 1.1 Position dans la chaîne de livraison

```
CI/CD GitHub Actions (V3) → Kubernetes local kind (V4-A) → Kubernetes cloud EKS (V4-B)
```

La V4-A constitue la fondation locale. Elle valide les patterns d'architecture avant leur transposition cloud. Le pipeline CI/CD construit et publie l'image sur GHCR (V3) ; la V4-A ajoute le déploiement de cette image dans un cluster local sécurisé.

### 1.2 Périmètre V4-A

- Création et configuration d'un cluster Kubernetes local via `kind`
- Structure Kustomize complète (`base/` + `overlays/local`, `dev`, `prod`)
- Déploiement de l'application containerisée avec image SHA immuable
- Configuration namespace, Deployment, Service, Ingress NGINX, ConfigMap
- Gestion sécurisée des secrets GHCR via Sealed Secrets (Bitnami)
- Probes readiness/liveness adaptées au démarrage Spring Boot
- Durcissement du container (SecurityContext complet, ressources, non-root)
- Déploiement manuel depuis le poste (GitHub Actions ne peut pas atteindre un cluster local)

### 1.3 Hors périmètre V4-A

| Élément | Raison d'exclusion | Cible |
|---|---|---|
| Persistance (PVC) | Pas de base de données externe | V4-B ou ultérieur |
| Monitoring Prometheus/Grafana | Hors scope local | V4-B |
| HPA (autoscaling) | Hors scope local | V4-B |
| RBAC interne applicatif | Traité en SEC-K8S-04 | V4-A itération 4 |
| NetworkPolicies | Traité en SEC-K8S-03 | V4-A itération 4 |
| TLS / Cert-Manager | Local uniquement | V4-B (Cert-Manager EKS) |
| CD GitHub Actions opérationnel | Cluster local inaccessible depuis runner | V4-B (OIDC → AWS) |
| Gate digest au déploiement | Complexité hors scope local | V4-B |

---

## 2. 🏗️ Architecture Kubernetes

### 2.1 Vue d'ensemble

```
+-------------------------------------------------------+
|                    kind cluster                       |
|                                                       |
|  Namespace: coreservice                               |
|                                                       |
|  +-----------------+   +---------------------------+  |
|  | Deployment      |   | ConfigMap                 |  |
|  |  replicas: 1    |   |  APP_ENV, SERVER_PORT      |  |
|  |  image: SHA     |   +---------------------------+  |
|  |  securityCtx    |                                  |
|  |  probes         |   +---------------------------+  |
|  |  resources      |   | SealedSecret → Secret     |  |
|  +-----------------+   |  ghcr-secret              |  |
|         |              +---------------------------+  |
|  +-----------------+                                  |
|  | Service         |                                  |
|  |  ClusterIP:80   |                                  |
|  |  → 8080         |                                  |
|  +-----------------+                                  |
|         |                                             |
|  +-----------------+                                  |
|  | Ingress NGINX   |                                  |
|  |  coreservice.   |                                  |
|  |  localdev.me    |                                  |
|  +-----------------+                                  |
+-------------------------------------------------------+
         |
   [localhost:80]
```

### 2.2 Décisions d'architecture

| Sujet | Choix retenu | Justification |
|---|---|---|
| Gestion manifests | Kustomize (base + overlays) | Lisible, auditable, GitOps-ready ; pas de surcharge Helm |
| Secrets | Sealed Secrets (Bitnami) | Chiffrement dans Git ; pas de secret en clair ; prépare External Secrets (V4-B) |
| Image tagging | SHA immuable (jamais `:latest`) | Traçabilité ; cohérence avec le pipeline CI V3 |
| DNS local | `coreservice.localdev.me` | Résolution automatique vers 127.0.0.1 — pas de `/etc/hosts` à modifier |
| Déploiement | Manuel depuis le poste | GitHub Actions ne peut pas atteindre un cluster local |
| Sécurité container | SecurityContext complet | Préparation CIS Benchmark, cohérence SEC-K8S-01 |
| TLS | Non activé en V4-A | Local uniquement ; Cert-Manager prévu en V4-B |

---

## 3. 📦 Structure Kustomize

### 3.1 Arborescence cible

```
k8s/
  config/
    kind-config.yaml            ← configuration cluster kind (ports 80/443)
  base/
    namespace.yaml
    configmap.yaml
    deployment.yaml
    service.yaml
    ingress.yaml
    sealed-ghcr-secret.yaml     ← SealedSecret GHCR (versionné dans Git)
    kustomization.yaml
  overlays/
    local/                      ← cluster kind local (V4-A)
      kustomization.yaml
      patch-deployment.yaml
      patch-ingress.yaml
    dev/                        ← EKS dev (V4-B)
      kustomization.yaml
      patch-deployment.yaml
      patch-ingress.yaml
      sealedsecret-ghcr.yaml
    prod/                       ← EKS prod (V4-B)
      kustomization.yaml
      patch-deployment.yaml
      patch-ingress.yaml
      sealedsecret-ghcr.yaml
```

### 3.2 Principes

- `base/` est **100 % générique** : pas de namespace, pas d'image, pas de replicas, pas de host
- `overlays/` contient toutes les variations par environnement (image, replicas, host, secrets)
- Aucun `secret.yaml` en clair dans Git, même en placeholder
- Les SealedSecrets sont les seuls objets secrets versionnés dans Git

### 3.3 Base — kustomization.yaml

```yaml
# k8s/base/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - namespace.yaml
  - configmap.yaml
  - deployment.yaml
  - service.yaml
  - ingress.yaml
  - sealed-ghcr-secret.yaml

commonLabels:
  app: coreservice
```

> **Note `commonLabels` :** applique les labels à toutes les ressources. Vérifier l'absence de conflit avec les `selector.matchLabels` du Deployment.

### 3.4 Overlay local — kustomization.yaml

```yaml
# k8s/overlays/local/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: coreservice

resources:
  - ../../base

patches:
  - patch-deployment.yaml
  - patch-ingress.yaml
```

---

## 4. 🧩 Manifests de base

### 4.1 Namespace

```yaml
# k8s/base/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: coreservice
  labels:
    app: coreservice
```

> Prérequis du RBAC minimal (SEC-K8S-04) et d'IRSA sur EKS (V4-B).

---

### 4.2 ConfigMap

```yaml
# k8s/base/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: coreservice-config
data:
  APP_ENV: "local"
  SERVER_PORT: "8080"
```

> Principe 12-factor : la configuration est externalisée, jamais codée en dur dans l'image. Les valeurs spécifiques à chaque environnement sont surchargées dans les overlays.

---

### 4.3 Deployment

```yaml
# k8s/base/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: coreservice
  labels:
    app: coreservice
    tier: backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: coreservice
  template:
    metadata:
      labels:
        app: coreservice
        tier: backend
    spec:
      serviceAccountName: coreservice-sa     # SEC-K8S-04 — ServiceAccount dédié
      imagePullSecrets:
        - name: ghcr-secret                  # déchiffré automatiquement par Sealed Secrets
      containers:
        - name: coreservice
          image: ghcr.io/vcharrie/demo_application:PLACEHOLDER
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: coreservice-config
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
          securityContext:                   # SEC-K8S-01 — durcissement container
            runAsNonRoot: true
            runAsUser: 1000
            readOnlyRootFilesystem: true
            allowPrivilegeEscalation: false
            capabilities:
              drop: ["ALL"]
            seccompProfile:
              type: RuntimeDefault
```

> **Notes d'implémentation :**
> - `initialDelaySeconds` est indispensable pour Spring Boot : sans ce paramètre, Kubernetes tue le pod avant qu'il soit prêt (démarrage JVM + context Spring ≈ 10–20s).
> - `readOnlyRootFilesystem: true` peut casser des applications qui écrivent dans `/tmp` — prévoir un `emptyDir` si nécessaire.
> - `PLACEHOLDER` est remplacé par le patch Kustomize de l'overlay — ne jamais modifier le manifest base directement.
> - `serviceAccountName` renvoie à la ressource créée dans SEC-K8S-04 ; le compte `default` n'est jamais utilisé.

---

### 4.4 Service

```yaml
# k8s/base/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: coreservice
spec:
  selector:
    app: coreservice
  ports:
    - name: http
      port: 80
      targetPort: 8080
  type: ClusterIP
```

> Type `ClusterIP` : exposition interne uniquement. L'accès externe passe exclusivement par l'Ingress.

---

### 4.5 Ingress

```yaml
# k8s/base/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: coreservice
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  ingressClassName: nginx
  rules:
    - host: PLACEHOLDER_HOST
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: coreservice
                port:
                  number: 80
```

> Le `host` est surchargé par le patch d'overlay. En local : `coreservice.localdev.me`. En EKS dev : `coreservice.dev.mydomain.com`.

---

## 5. 🔀 Patches overlay local

### 5.1 patch-deployment.yaml

```yaml
# k8s/overlays/local/patch-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: coreservice
spec:
  replicas: 1
  template:
    spec:
      containers:
        - name: coreservice
          image: ghcr.io/vcharrie/demo_application:<sha-local>
```

> Remplacer `<sha-local>` par le tag SHA immuable produit par le pipeline CI V3. `:latest` est interdit.

### 5.2 patch-ingress.yaml

```yaml
# k8s/overlays/local/patch-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: coreservice
spec:
  rules:
    - host: coreservice.localdev.me
```

> `localdev.me` résout automatiquement vers `127.0.0.1` — aucune modification de `/etc/hosts` requise.

---

## 6. 🐳 Configuration du cluster kind

### 6.1 kind-config.yaml

```yaml
# k8s/config/kind-config.yaml
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

> Le mapping des ports 80/443 est **obligatoire** pour que l'Ingress NGINX soit accessible depuis le poste. Sans cette configuration, les requêtes HTTP ne parviennent pas au cluster.

### 6.2 Création du cluster

```bash
kind create cluster --name coreservice --config k8s/config/kind-config.yaml
kubectl config use-context kind-coreservice
```

---

## 7. 🔐 Gestion des secrets — Sealed Secrets

### 7.1 Principe

Les Secrets Kubernetes sont encodés en base64, pas chiffrés. Les versionner en clair dans Git expose les credentials. Sealed Secrets chiffre le secret avec la clé publique du cluster. Seul le controller (qui détient la clé privée) peut le déchiffrer.

```
Secret en clair → kubeseal (chiffrement clé publique) → SealedSecret (Git)
                                                              ↓
                                              controller → Secret Kubernetes
```

> **Contrainte critique :** la clé est liée au cluster. Si le cluster est recréé, **tous les SealedSecrets doivent être régénérés**.

### 7.2 Installation du controller

```bash
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.26.0/controller.yaml

kubectl wait --namespace kube-system \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=sealed-secrets-controller \
  --timeout=90s
```

### 7.3 Installation de kubeseal (client local)

```bash
# Linux / WSL2
KUBESEAL_VERSION=0.26.0
curl -sSL "https://github.com/bitnami-labs/sealed-secrets/releases/download/v${KUBESEAL_VERSION}/kubeseal-${KUBESEAL_VERSION}-linux-amd64.tar.gz" \
  | tar -xz kubeseal
sudo mv kubeseal /usr/local/bin/
kubeseal --version
```

### 7.4 Génération du SealedSecret GHCR

```bash
# 1. Créer le secret en mémoire (sans l'appliquer au cluster)
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=vcharrie \
  --docker-password=<TOKEN_GHCR> \
  --namespace=coreservice \
  --dry-run=client \
  -o yaml > /tmp/ghcr-secret.yaml

# 2. Chiffrer avec kubeseal
kubeseal \
  --controller-name=sealed-secrets-controller \
  --controller-namespace=kube-system \
  --format yaml \
  < /tmp/ghcr-secret.yaml \
  > k8s/base/sealed-ghcr-secret.yaml

# 3. Supprimer immédiatement le fichier en clair
rm /tmp/ghcr-secret.yaml
```

> Seul `sealed-ghcr-secret.yaml` va dans Git. Le fichier `/tmp/ghcr-secret.yaml` contient les credentials en clair — suppression immédiate obligatoire.

### 7.5 Vérification

```bash
kubectl get sealedsecret -n coreservice
kubectl get secret ghcr-secret -n coreservice
```

---

## 8. 🔒 Sécurité du déploiement

### 8.1 Container (SEC-IMG-01)

- Image de base minimaliste (distroless ou alpine)
- User non-root déclaré dans le Dockerfile (`USER 1000`)
- Pas de shell interactif
- Pas de dépendances inutiles
- Image construite depuis le pipeline CI avec scan Trivy (blocage HIGH/CRITICAL)

### 8.2 SecurityContext (SEC-K8S-01)

Déclaré dans le Deployment base (§4.3) :

| Paramètre | Valeur | Effet |
|---|---|---|
| `runAsNonRoot` | `true` | Interdit l'exécution en root |
| `runAsUser` | `1000` | UID explicite non-root |
| `readOnlyRootFilesystem` | `true` | Filesystem racine en lecture seule |
| `allowPrivilegeEscalation` | `false` | Interdit l'élévation de privilèges |
| `capabilities.drop` | `["ALL"]` | Supprime toutes les Linux capabilities |
| `seccompProfile.type` | `RuntimeDefault` | Profil seccomp par défaut du runtime |

### 8.3 Secrets (SEC-K8S-02)

- Aucun secret Kubernetes en clair dans Git
- Credentials GHCR gérés exclusivement via Sealed Secrets
- Référencés dans le Deployment via `imagePullSecrets`

### 8.4 Network Policies (SEC-K8S-03 — itération 4)

- `deny-all` ingress et egress par défaut
- Autoriser : ingress depuis `ingress-nginx`, egress DNS (port 53)

### 8.5 RBAC (SEC-K8S-04 — itération 4)

- ServiceAccount dédié `coreservice-sa` (pas le `default`)
- Role et RoleBinding strictement limités au namespace `coreservice`
- Aucune permission cluster-wide

### 8.6 CI/CD (SEC-CI-01, SEC-CI-03)

- Scan SBOM (CycloneDX) obligatoire
- Scan image Trivy (blocage HIGH/CRITICAL)
- SHA immuable — jamais `:latest`
- Provenance de l'image traçable via GHCR

---

## 9. 🚀 Ordre de déploiement

En cas de création ou de recréation du cluster, respecter strictement cet ordre :

```bash
# 1. Supprimer l'ancien cluster si existant
kind delete cluster --name coreservice

# 2. Créer le cluster avec mapping des ports
kind create cluster --name coreservice --config k8s/config/kind-config.yaml
kubectl config use-context kind-coreservice

# 3. Installer l'Ingress Controller NGINX
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s

# 4. Installer le controller Sealed Secrets
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.26.0/controller.yaml
kubectl wait --namespace kube-system \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=sealed-secrets-controller \
  --timeout=90s

# 5. (Re)générer le SealedSecret GHCR si cluster recréé (voir §7.4)
#    → la clé privée change à chaque recréation

# 6. Valider le rendu Kustomize avant d'appliquer
kubectl kustomize k8s/overlays/local

# 7. Appliquer les manifests
kubectl apply -k k8s/overlays/local

# 8. Vérifier l'état des ressources
kubectl get all -n coreservice
kubectl get ingress -n coreservice
kubectl get secret ghcr-secret -n coreservice

# 9. Vérifier le rollout
kubectl rollout status deployment/coreservice -n coreservice

# 10. Tester l'accès
curl http://coreservice.localdev.me/actuator/health
```

> **Résultat attendu :** `{"status":"UP"}`

---

## 10. ⚙️ CI/CD — périmètre V4-A

### 10.1 Limitation structurelle

En V4-A, le cluster est local. GitHub Actions (runner SaaS) **ne peut pas atteindre un cluster local**. Le déploiement s'effectue **manuellement depuis le poste** avec `kubectl`.

### 10.2 Pipeline CI — étapes (V3, inchangées)

1. Build Maven
2. Build Docker
3. Scan SBOM (CycloneDX)
4. Scan SCA (Trivy dépendances)
5. Scan image (Trivy — blocage HIGH/CRITICAL)
6. Push image GHCR avec tag SHA immuable

### 10.3 Déploiement manuel V4-A

```bash
# Mettre à jour le patch avec le SHA du dernier build CI
# (k8s/overlays/local/patch-deployment.yaml)
# image: ghcr.io/vcharrie/demo_application:<sha>

kubectl apply -k k8s/overlays/local
kubectl rollout status deployment/coreservice -n coreservice
kubectl get pods -n coreservice
```

### 10.4 Structure CD (référence V4-B uniquement)

```yaml
# .github/workflows/cd.yml (V4-B uniquement)
- name: Configure AWS credentials via OIDC
  uses: aws-actions/configure-aws-credentials@v4

- name: Deploy to dev
  run: kubectl apply -k k8s/overlays/dev

- name: Verify rollout
  run: kubectl rollout status deployment/coreservice -n coreservice
```

> En V4-B, l'authentification se fait via OIDC GitHub → AWS sans token statique (SEC-CI-02). La vérification de digest (gate CD) est également prévue.

---

## 11. 🧪 Checklist de validation

| Critère | Commande | Résultat attendu |
|---|---|---|
| Cluster kind opérationnel | `kubectl get nodes` | `Ready` |
| Ingress Controller prêt | `kubectl get pods -n ingress-nginx` | `Running` |
| Controller Sealed Secrets prêt | `kubectl get pods -n kube-system \| grep sealed` | `Running` |
| Namespace créé | `kubectl get ns coreservice` | `Active` |
| SealedSecret présent | `kubectl get sealedsecret -n coreservice` | `ghcr-secret` visible |
| Secret GHCR déchiffré | `kubectl get secret ghcr-secret -n coreservice` | Secret présent |
| Pod en état Running | `kubectl get pods -n coreservice` | `Running` |
| Readiness probe OK | `kubectl describe pod -n coreservice` | `Readiness probe succeeded` |
| Service accessible | `kubectl get svc -n coreservice` | `ClusterIP` défini |
| Ingress configuré | `kubectl get ingress -n coreservice` | Host `coreservice.localdev.me` visible |
| ConfigMap appliqué | `kubectl get configmap -n coreservice` | `coreservice-config` présent |
| Health endpoint OK | `curl http://coreservice.localdev.me/actuator/health` | `{"status":"UP"}` |
| Kustomize valide | `kubectl kustomize k8s/overlays/local` | YAML valide sans erreur |
| Idempotence | `kubectl apply -k k8s/overlays/local` (2e fois) | `unchanged` sur toutes les ressources |

✅ Tous ces points validés → enchaîner sur **SEC-K8S-01 (SecurityContext)**.

---

## 12. 🛠️ Diagnostic — problèmes fréquents

| Symptôme | Cause probable | Diagnostic |
|---|---|---|
| `ImagePullBackOff` | Secret GHCR manquant ou non déchiffré | `kubectl get secret -n coreservice` |
| SealedSecret non déchiffré | Controller Sealed Secrets non prêt | `kubectl get pods -n kube-system` |
| Pod tué au démarrage | `initialDelaySeconds` trop court pour Spring Boot | `kubectl describe pod -n coreservice` → events |
| `readOnlyRootFilesystem` cassé | Application écrit dans `/tmp` | Ajouter un `emptyDir` monté sur `/tmp` |
| Ingress inaccessible | ingress-nginx non prêt ou port non mappé | `kubectl get pods -n ingress-nginx` ; vérifier `kind-config.yaml` |
| SealedSecret invalide après recréation | Clé privée différente du nouveau cluster | Régénérer le SealedSecret (§7.4) |
| Ordre d'application incorrect | Sealed Secrets déployé avant le controller | Respecter l'ordre §9 strictement |

---

## 13. ⚠️ Limitations et risques résiduels V4-A

| Risque | Justification d'acceptation | Couverture V4-B |
|---|---|---|
| Pas de vérification digest au déploiement | Complexité hors scope local | Gate CD digest (V4-B) |
| TLS non activé | Local uniquement | Cert-Manager sur EKS (V4-B) |
| CD GitHub Actions inopérant | Contrainte structurelle cluster local | OIDC → AWS (V4-B) |
| NetworkPolicies et RBAC incomplets | Environnement de dev uniquement | Itération 4 + EKS hardened (V4-B) |
| Clé Sealed Secrets liée au cluster | Régénération nécessaire si recréation | Gestion centralisée secrets AWS (V4-B) |

---

## 14. 🗺️ Roadmap itérative V4-A

| Itération | Objectif | Durée estimée |
|---|---|---|
| 1 — Fondations + Ingress | Cluster local fonctionnel, routing HTTP opérationnel | 3h–4h |
| 2 — Overlays + Sealed Secrets | Structure Kustomize complète, secrets GHCR sécurisés | 2h–3h |
| 3 — SHA immuable | Tag SHA injecté bout en bout via Kustomize | 1h–1h30 |
| 4 — Sécurité K8S | SEC-K8S-01 → 04 : SecurityContext, NetworkPolicies, RBAC | 3h–4h |
| 5 — Troubleshooting | Diagnostic Kubernetes, simulation de pannes | 1h30–2h |
| 6 — Observabilité | RollingUpdate, rollout strategy, logs structurés | 1h30–2h |
| 7 — Finalisation | Structure GitOps-ready, préparation V4-B | 1h–1h30 |

**Total estimé : 13h–18h**

---

## 15. 🔭 Continuité vers V4-B (EKS)

| Élément V4-A | Continuité V4-B |
|---|---|
| Kustomize overlays `local/` | GitOps ArgoCD/Flux — overlays `dev/` et `prod/` |
| Ingress NGINX local | ALB Ingress Controller EKS |
| TLS désactivé | Cert-Manager + certificats automatiques |
| Sealed Secrets | AWS Secrets Manager + External Secrets Operator |
| Déploiement manuel | OIDC GitHub → AWS (sans token statique, SEC-CI-02) |
| ServiceAccount dédié | IRSA (IAM Roles for Service Accounts) |
| Namespace dédié | Isolation EKS multi-tenant |
| SHA immuable | Gate CD digest sur EKS |
| `kubectl logs` local | CloudWatch Logs + Container Insights |
| Rollout strategy locale | EKS managed node groups + PodDisruptionBudget |

---

## Annexes

- `k8s/config/kind-config.yaml` — configuration cluster kind
- SBOM CycloneDX (produit par pipeline CI V3)
- Rapport Trivy SCA (dépendances)
- Rapport Trivy Image
- Fiches sécurité : SEC-K8S-01, SEC-K8S-02, SEC-K8S-03, SEC-K8S-04
- Fiches architecture : K8S-ARCH-01, K8S-ARCH-02, K8S-ARCH-03 (roadmap), K8S-ARCH-04

---

## Références croisées

- **K8S-ARCH-01** — Architecture Kubernetes sécurisée (V4-A)
- **K8S-ARCH-02** — Implémentation de l'architecture Kubernetes de base
- **K8S-ARCH-03** — Roadmap itérative V4-A + fiches d'implémentation par itération
- **K8S-ARCH-04** — Overlays GitOps + préparation EKS (V4-B)
- **SEC-K8S-01** — SecurityContext
- **SEC-K8S-02** — Secrets Kubernetes (Sealed Secrets)
- **SEC-K8S-03** — Network Policies
- **SEC-K8S-04** — RBAC
- **SEC-CI-01** — Pipeline CI/CD (build + push image GHCR)
- **SEC-CI-02** — Secrets GitHub (OIDC, permissions minimales)
- **SEC-CI-03** — Provenance & intégrité supply chain
- **SEC-IMG-01** — Image Docker durcie
