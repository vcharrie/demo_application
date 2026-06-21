# 📄 Release Note — Version V4

## 🗓 Date
14/06/2026

## 🎯 Objectif de la release
La version **V4** introduit le **premier déploiement Kubernetes** du CoreService dans un cluster local basé sur **kind**, accompagné d’un pipeline CI/CD complet et de contrôles de sécurité renforcés.

Cette release ne modifie pas la logique applicative : elle ajoute **l’infrastructure de déploiement**, la **structure Kustomize**, les **probes**, l’**ingress**, et l’intégration CI/CD permettant de déployer automatiquement l’application containerisée.

---

## 🚀 Nouveautés principales

### 1. Déploiement Kubernetes (kind)
- Création d’un cluster Kubernetes local via **kind**
- Déploiement complet de l’application dans le namespace `coreservice`
- Installation et utilisation de **Ingress NGINX**
- Routage HTTP local via `/api/*`
- Ajout des **readinessProbe** et **livenessProbe**
- Ajout des **requests/limits CPU & RAM**
- Exécution de l’application dans un **container non-root**

### 2. Structure Kustomize
- Ajout d’une base générique (`k8s/base`)
- Ajout d’un overlay local (`k8s/overlays/local`)
- Patch dynamique de l’image via SHA GitHub Actions
- Patch des ressources et des probes
- Manifests générés automatiquement via `kubectl apply -k`

### 3. Pipeline CI/CD (GitHub Actions)
- Build Maven
- Build Docker
- Scan **SBOM CycloneDX** via Trivy
- Scan **SCA** (fail on HIGH/CRITICAL)
- Scan **image Docker** (fail on HIGH/CRITICAL)
- Push de l’image taggée avec le **SHA**
- Déploiement automatique sur le cluster kind local
- Vérification du rollout et de l’état des Pods

### 4. Sécurité renforcée
- Dockerfile sécurisé (user non-root, image minimaliste)
- Politique CI/CD stricte :
  - blocage si vulnérabilité HIGH/CRITICAL
  - SBOM obligatoire
  - scan image obligatoire
- Probes Kubernetes pour garantir la stabilité du Pod
- Pas de privilèges élevés dans les manifests

---

## 🛠️ Changements techniques

- Ajout du dossier `k8s/` contenant :
  - `namespace.yaml`
  - `deployment.yaml`
  - `service.yaml`
  - `ingress.yaml`
  - `kustomization.yaml`
- Ajout des patchs :
  - `patch-deployment.yaml` (image SHA)
  - `patch-resources.yaml` (CPU/RAM)
  - `patch-probes.yaml` (readiness/liveness)
- Ajout du workflow CI/CD dédié au déploiement Kubernetes
- Ajout du script de création du cluster kind (optionnel)

---

## 🐞 Corrections

- Correction des erreurs liées au SecurityConfig en mode CI
- Correction du comportement des endpoints `/api/*` via Ingress
- Correction des erreurs de readiness lors du démarrage du Pod

---

## ⚠️ Limitations connues

- Pas de persistance (pas de PVC)
- Pas de base de données externe (H2 uniquement)
- Pas de monitoring (Prometheus/Grafana non installés)
- Pas de autoscaling (HPA)
- Pas de RBAC interne
- Pas de gestion avancée des secrets (SealedSecrets optionnel)

---

## 📦 Artifacts générés

- `sbom.json` (CycloneDX)
- `trivy-sbom-report.json`
- `trivy-image-report.json`
- Image Docker taggée avec SHA
- Manifests Kubernetes générés via Kustomize

---

## 🧭 Résumé

La V4 marque la transition du CoreService vers un **service déployable dans un cluster Kubernetes**, avec une chaîne CI/CD complète et sécurisée.  
Elle constitue la base technique pour les futures releases (V5+) qui introduiront persistance, authentification, monitoring et 