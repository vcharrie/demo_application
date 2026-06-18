# SPEC TECHNIQUE — Version V4
## Déploiement Kubernetes local (kind)

## 1. Contexte & périmètre

La version V4 introduit le premier déploiement Kubernetes du CoreService dans un cluster local basé sur kind.  
Cette version ne modifie pas la logique applicative : elle ajoute uniquement l’infrastructure de déploiement, la configuration Kustomize et l’intégration CI/CD associée.

Périmètre V4 :
- Création d’un cluster Kubernetes local via kind
- Déploiement de l’application containerisée
- Structure Kustomize (base + overlays/local)
- Configuration du namespace, Deployment, Service, Ingress
- Ajout des probes (readiness/liveness)
- Patch dynamique de l’image via SHA GitHub Actions
- Sécurisation du déploiement (user non-root, ressources, probes)
- Intégration CI/CD pour appliquer le déploiement

---

## 2. Architecture Kubernetes

### 2.1 Vue d’ensemble

```
+------------------------+
|      kind cluster      |
|                        |
|  Namespace: coreservice|
|                        |
|  +------------------+  |
|  | Deployment       |  |
|  |  - 1 replica     |  |
|  |  - image:sha     |  |
|  |  - probes        |  |
|  +------------------+  |
|  | Service          |  |
|  |  ClusterIP:8080  |  |
|  +------------------+  |
|  | Ingress NGINX    |  |
|  |  /api/*          |  |
|  +------------------+  |
+------------------------+
```

---

## 3. Structure Kustomize

### 3.1 Arborescence

```
k8s/
 ├── base/
 │    ├── namespace.yaml
 │    ├── deployment.yaml
 │    ├── service.yaml
 │    ├── ingress.yaml
 │    └── kustomization.yaml
 └── overlays/
      └── local/
           ├── patch-deployment.yaml
           ├── patch-resources.yaml
           ├── patch-probes.yaml
           └── kustomization.yaml
```

### 3.2 Base

Contient les manifests génériques :
- Namespace `coreservice`
- Deployment (image placeholder)
- Service ClusterIP
- Ingress NGINX
- Kustomization listant les ressources

### 3.3 Overlay local

Contient les patchs spécifiques à l’environnement local :
- `patch-deployment.yaml` : injection du SHA de l’image
- `patch-resources.yaml` : limites CPU/RAM
- `patch-probes.yaml` : readiness/liveness
- `kustomization.yaml` : référence à la base + patchs

---

## 4. Deployment

### 4.1 Caractéristiques techniques

- 1 replica
- Image : `ghcr.io/<repo>/coreservice:<SHA>`
- User non-root (hérité du Dockerfile)
- Probes activées :
  - readiness : `/actuator/health/readiness`
  - liveness : `/actuator/health/liveness`
- Ressources :
  - requests : 100m CPU / 128Mi RAM
  - limits : 250m CPU / 256Mi RAM

### 4.2 Patch image (SHA)

`patch-deployment.yaml` :

```yaml
spec:
  template:
    spec:
      containers:
        - name: coreservice
          image: ghcr.io/<repo>/coreservice:__IMAGE_SHA__
```

Le SHA est injecté automatiquement par GitHub Actions.

---

## 5. Service

### 5.1 Type
- ClusterIP

### 5.2 Port
- Port interne : 8080
- TargetPort : 8080

---

## 6. Ingress

### 6.1 Contrôleur
- NGINX (installé dans kind)

### 6.2 Routage
- `/api/*` → service coreservice:8080

### 6.3 Objectif
Permettre l’accès HTTP local à l’API via :

```
http://localhost/api/...
```

---

## 7. Probes

### 7.1 Readiness

```
/actuator/health/readiness
```

### 7.2 Liveness

```
/actuator/health/liveness
```

### 7.3 Objectif
- readiness : attendre que l’application soit prête
- liveness : redémarrer le pod si blocage

---

## 8. CI/CD — Intégration du déploiement

### 8.1 Étapes liées au déploiement

- Build Maven
- Build Docker
- Scan SBOM
- Scan SCA
- Scan image
- Push image SHA
- Patch du SHA dans Kustomize
- `kubectl apply -k overlays/local`
- Vérification des pods
- Vérification du rollout

### 8.2 Commandes exécutées

```
kubectl apply -k k8s/overlays/local
kubectl rollout status deployment/coreservice -n coreservice
kubectl get pods -n coreservice
```

---

## 9. Sécurité du déploiement

### 9.1 Dockerfile sécurisé
- user non-root
- image minimaliste
- pas de shell interactif
- pas de dépendances inutiles

### 9.2 Kubernetes
- pas de privilèges élevés
- pas de hostPath
- pas de capabilities
- ressources limitées
- probes activées

### 9.3 CI/CD
- blocage si vulnérabilité HIGH/CRITICAL
- SBOM obligatoire
- scan image obligatoire

---

## 10. Limitations V4

- Pas de persistance (pas de PVC)
- Pas de base de données externe
- Pas de secrets Kubernetes (non nécessaires en V4)
- Pas de monitoring Prometheus/Grafana
- Pas de autoscaling (HPA)
- Pas de RBAC interne

---

## 11. Annexes

- kind-config.yaml
- SBOM CycloneDX
- Rapport Trivy SCA
- Rapport Trivy Image