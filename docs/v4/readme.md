# CoreService — README V4

## 📌 Description
La version **V4** introduit le premier **déploiement Kubernetes** du CoreService dans un cluster local basé sur **kind**, avec un pipeline CI/CD complet et des contrôles de sécurité intégrés (SBOM, SCA, scan image).

L’objectif de cette version est de fournir un environnement d’exécution local réaliste, reproductible et sécurisé.

---

## 🚀 Fonctionnalités principales de la V4

- Déploiement complet sur un cluster Kubernetes **kind**
- Structure **Kustomize** (base + overlays/local)
- Ingress NGINX exposant l’API via `/api/*`
- Probes Kubernetes (readiness + liveness)
- Ressources CPU/RAM définies
- Image Docker sécurisée (user non-root)
- Pipeline CI/CD :
  - Build Maven
  - Build Docker
  - Scan SBOM (CycloneDX)
  - Scan SCA (fail on HIGH/CRITICAL)
  - Scan image (fail on HIGH/CRITICAL)
  - Push image taggée avec SHA
  - Déploiement automatique via `kubectl apply -k`

---

## 🏗️ Architecture V4 (vue simplifiée)

```
Client (Windows)
   |
   |-- Kustomize (assemble les manifests)
   |-- kubectl apply -k
   |
kind cluster (Docker Desktop)
   |
   |-- kube-apiserver
   |-- etcd
   |-- kube-scheduler
   |-- kube-controller-manager
   |-- kubelet
   |-- containerd
   |
   |-- Pods natifs : kube-proxy, coredns
   |-- Pods infra : ingress-nginx, sealed-secrets
   |-- Pod applicatif : coreservice
```

---

## 📦 Structure Kustomize

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

---

## 🐳 Image Docker

- Image minimaliste
- User non-root
- Port exposé : 8080
- Entrypoint Spring Boot
- Tag : SHA GitHub Actions

---

## 🌐 Accès à l’application

Après déploiement :

```
http://localhost/api/resources
```

Ingress NGINX route automatiquement vers le Service Kubernetes.

---

## 🧪 Probes Kubernetes

- **Readiness** : `/actuator/health/readiness`
- **Liveness** : `/actuator/health/liveness`

---

## 🔧 Déploiement local

### 1. Créer le cluster kind

```
kind create cluster --config kind-config.yaml
```

### 2. Déployer l’application

```
kubectl apply -k k8s/overlays/local
```

### 3. Vérifier

```
kubectl get pods -n coreservice
kubectl get ingress -n coreservice
kubectl logs -n coreservice deploy/coreservice
```

---

## 🔄 CI/CD (GitHub Actions)

Pipeline V4 :

1. Build Maven  
2. Build Docker  
3. Scan SBOM  
4. Scan SCA  
5. Scan image  
6. Push image SHA  
7. Patch SHA dans Kustomize  
8. Déploiement sur kind  
9. Vérification du rollout  

---

## 🔐 Sécurité

- Dockerfile sécurisé (user non-root)
- Pas de privilèges élevés dans les manifests
- Probes activées
- Ressources CPU/RAM définies
- CI/CD bloque toute vulnérabilité HIGH/CRITICAL
- SBOM obligatoire

---

## ⚠️ Limitations V4

- Pas de persistance (pas de PVC)
- Base H2 uniquement
- Pas de monitoring (Prometheus/Grafana)
- Pas de autoscaling (HPA)
- Pas de RBAC interne
- Pas de secrets sensibles (SealedSecrets optionnel)

---

## 📚 Documentation associée

- Spec technique V4
- Architecture technique V4
- Release Note V4
- SBOM CycloneDX
- Rapports Trivy (SCA + image)