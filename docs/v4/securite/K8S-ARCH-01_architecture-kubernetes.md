# K8S-ARCH-01 — Architecture Kubernetes Sécurisée (V4-A)

**Domaine :** Orchestration / Kubernetes Security  
**Couche :** Déploiement Kubernetes local → préparation cloud  
**Statut :** 🟦 En cours (V4-A)

> **Note de conception :** chaque choix technique de cette version locale est fait en anticipation d'un déploiement Kubernetes cloud (V4-B). Les points de continuité sont explicitement signalés tout au long de la fiche.

---

## 1. Objectif

Déployer l'application dans un cluster Kubernetes sécurisé, reproductible et traçable, avec une architecture claire avant d'appliquer les contrôles de sécurité (SEC-K8S-01 → 04).

Cette version V4-A constitue la **fondation locale** de la chaîne complète :

```
CI/CD GitHub Actions (V3) → Kubernetes local (V4-A) → Kubernetes cloud / IaC (V4-B)
```

---

## 2. Modèle de déploiement retenu

**Choix : Manifests Kubernetes + Kustomize (base + overlays)**

| Critère | Justification |
|---|---|
| Simplicité | Pas de surcharge Helm pour un projet solo |
| Lisibilité | Manifests YAML directs, auditables |
| Versionnement Git | Chaque changement tracé |
| Extensibilité | Compatible GitOps (ArgoCD, Flux) en V4-B |

> **Continuité V4-B :** Kustomize est nativement compatible avec ArgoCD et Flux. Les overlays `dev/prod` définis ici seront réutilisables sans réécriture dans un contexte GitOps cloud.

### Structure des manifests

```
k8s/
  base/
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

> **Note :** aucun `secret.yaml` dans Git, même en placeholder. Les secrets sont gérés exclusivement via Sealed Secrets (SEC-K8S-02). Mettre un fichier secret dans le repo, même vide, crée un risque d'injection accidentelle de valeurs réelles.

---

## 3. Architecture Kubernetes cible

### Deployment

- 1 container → image GHCR (tag SHA immuable)
- `readinessProbe` + `livenessProbe`
- `resources` : requests et limits définis
- Labels : `app`, `version`, `tier`

> **Continuité V4-B :** les `resources` requests/limits définis ici sont directement transposables sur EKS. Les labels seront réutilisés pour le routing ALB et le monitoring CloudWatch.

### Service

- Type `ClusterIP` — exposition interne uniquement
- Interface stable pour l'ingress

### Ingress

- Ingress NGINX (local)
- TLS non activé en V4-A (local uniquement)
- Cert-Manager prévu en V4-B pour TLS automatique

> **Accès local sans TLS :** utiliser `nip.io` ou un ingress avec hostname local (`app.local` via `/etc/hosts`). Ne pas activer TLS en local pour éviter la complexité des certificats self-signed.

> **Continuité V4-B :** l'Ingress NGINX local sera remplacé par un ALB Ingress Controller sur EKS, avec Cert-Manager pour les certificats TLS automatiques.

### ConfigMap

- Configuration non sensible uniquement
- Variables d'environnement, URLs, paramètres applicatifs

### Secrets

- **Aucun secret en clair dans Git**
- Gestion via Sealed Secrets (Bitnami) — voir SEC-K8S-02

> **Continuité V4-B :** Sealed Secrets sera remplaçable par AWS Secrets Manager + External Secrets Operator sur EKS, sans modifier les manifests applicatifs.

### Namespace

```yaml
namespace: demo-app
```

Namespace dédié — isolation de l'application dès V4-A.

> **Continuité V4-B :** le namespace dédié est le prérequis du RBAC minimal et d'IRSA (IAM Roles for Service Accounts) sur EKS.

---

## 4. Pipeline CD (vue globale)

### Chaîne complète V4-A

```
Build image (V3) → Push GHCR (V3) → SBOM + provenance (V3)
→ CD : kubectl apply -k overlays/dev
→ Déploiement cluster local
```

### Modèle CD retenu

```yaml
# GitHub Actions → kubectl apply
- name: Deploy
  run: kubectl apply -k k8s/overlays/dev
```

> **Risque résiduel accepté en V4-A :** le déploiement ne vérifie pas le digest de l'image avant apply. La vérification de digest (gate CD) est prévue en V4-B avec OIDC GitHub → cloud.

> **Continuité V4-B :** le job CD GitHub Actions utilisera OIDC pour s'authentifier sur AWS sans token statique, conformément à SEC-CI-02.

---

## 5. Fiches sécurité Kubernetes (SEC-K8S-01 → 04)

Les fiches détaillées sont produites séquentiellement après cette architecture.

### SEC-K8S-01 — SecurityContext

Durcissement au niveau du container :

- `runAsNonRoot: true`
- `readOnlyRootFilesystem: true`
- `allowPrivilegeEscalation: false`
- `drop: ["ALL"]` (capabilities)
- `seccompProfile: RuntimeDefault`
- `fsGroup`

### SEC-K8S-02 — Secrets

Gestion sécurisée des secrets Kubernetes :

- Sealed Secrets (Bitnami) — chiffrement dans Git
- Jamais de secret en clair
- Rotation via Kubernetes

### SEC-K8S-03 — Network Policies

Contrôle du trafic réseau :

- `deny-all` par défaut
- Autoriser uniquement : ingress depuis ingress-controller, egress DNS, egress HTTP(S) si nécessaire

### SEC-K8S-04 — RBAC

Contrôle d'accès minimal :

- Namespace dédié
- ServiceAccount dédié (pas de `default`)
- RBAC strictement limité au namespace
- Aucune permission cluster-wide

> **Continuité V4-B :** le ServiceAccount dédié défini en SEC-K8S-04 est le prérequis direct d'IRSA sur EKS — même principe, implémentation cloud en V4-B.

---

## 6. Environnement local (V4-A)

| Outil | Usage |
|---|---|
| `kind` | Cluster local léger, recommandé |
| `minikube` | Alternative avec plus de features |
| `k3d` | Alternative ultra-légère |

**Recommandation : `kind`** — simple, reproductible, proche d'un vrai cluster.

---

## 7. Roadmap V4-A

### Étape 1 — Structure et manifests

- Créer `k8s/base/` avec les 4 manifests (sans secret.yaml)
- Créer overlays `dev/` et `prod/`
- Tester `kustomize build` en local

### Étape 2 — Pipeline CD minimal

- Job CD GitHub Actions
- `kubectl apply -k overlays/dev`
- Déploiement fonctionnel sur cluster local

### Étape 3 — Sécurité Kubernetes

- SEC-K8S-01 : SecurityContext
- SEC-K8S-02 : Sealed Secrets
- SEC-K8S-03 : Network Policies
- SEC-K8S-04 : RBAC

### Étape 4 — Continuité V4-B

La V4-B couvre le déploiement cloud et IaC. Les éléments préparés en V4-A qui s'y connectent directement :

| Élément V4-A | Continuité V4-B |
|---|---|
| Kustomize overlays | GitOps (ArgoCD / Flux) |
| OIDC GitHub Actions | Authentification AWS sans token statique |
| ServiceAccount dédié | IRSA (IAM Roles for Service Accounts) |
| Sealed Secrets | AWS Secrets Manager + External Secrets |
| Ingress NGINX | ALB Ingress Controller |
| Namespace dédié | Isolation EKS multi-tenant |
| Digest gate (risque résiduel) | Gate CD digest sur EKS |

---

## 8. Risques résiduels V4-A

| Risque | Justification d'acceptation | Couverture V4-B |
|---|---|---|
| Pas de vérification digest au déploiement | Complexité hors scope local | Gate CD digest (V4-B) |
| TLS non activé | Local uniquement | Cert-Manager sur EKS (V4-B) |
| Runner GitHub SaaS | Contrainte opérationnelle | OIDC → AWS (V4-B) |
| Cluster local non durci | Environnement de dev uniquement | EKS hardened (V4-B) |

---

## Références croisées

- **SEC-CI-01** — Pipeline CI/CD (build + push image GHCR)
- **SEC-CI-02** — Secrets GitHub (OIDC, permissions minimales)
- **SEC-CI-03** — Provenance & intégrité supply chain
- **SEC-IMG-01** — Image Docker durcie
- **SEC-K8S-01** — SecurityContext
- **SEC-K8S-02** — Secrets Kubernetes
- **SEC-K8S-03** — Network Policies
- **SEC-K8S-04** — RBAC
