K8S-ARCH-04 — Itération 3 : Overlays Kustomize + GitOps local + Préparation EKS (V5-A)

Domaine : Orchestration / Kubernetes
Couche : Industrialisation — Structuration GitOps & Préparation Cloud
Statut : 🟦 À implémenter

    Objectif : structurer proprement les overlays Kustomize (local/dev/prod), préparer le dépôt GitOps, installer ArgoCD dans le cluster local et rendre le déploiement Kubernetes GitOps‑ready, reproductible et compatible EKS.

    Prérequis : itération 2 validée — cluster kind opérationnel, déploiement local fonctionnel, SealedSecret GHCR OK, script PowerShell fonctionnel.

1. Fiche d’implémentation
1.1. Structure k8s/ cible
Code

k8s/
  config/
    kind-config.yaml

  base/
    deployment.yaml
    service.yaml
    ingress.yaml
    kustomization.yaml

  overlays/
    local/
      kustomization.yaml
      patch-deployment.yaml
      patch-ingress.yaml

    dev/
      kustomization.yaml
      patch-deployment.yaml
      patch-ingress.yaml
      sealedsecret-ghcr.yaml

    prod/
      kustomization.yaml
      patch-deployment.yaml
      patch-ingress.yaml
      sealedsecret-ghcr.yaml

Principes :

    base/ contient uniquement les ressources génériques (sans image, sans namespace, sans replicas, sans host).

    overlays/ contient les variations par environnement (image, host, replicas, ingress class, secrets).

    Les secrets GHCR sont gérés via Sealed Secrets et versionnés en toute sécurité.

    kind-config.yaml reste indépendant (bootstrap cluster local uniquement).

    Structure GitOps‑ready compatible ArgoCD (local puis EKS).

1.2. Manifests de base
kustomization.yaml (base)
yaml

apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - deployment.yaml
  - service.yaml
  - ingress.yaml

Règles :

    Pas de namespace

    Pas d’image

    Pas de replicas

    Pas de host

    Pas de secrets

👉 Le base/ doit être 100% générique.
1.3. Overlay local (kind)
kustomization.yaml
yaml

apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: coreservice

resources:
  - ../../base

patches:
  - patch-deployment.yaml
  - patch-ingress.yaml

patch-deployment.yaml
yaml

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

patch-ingress.yaml
yaml

apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: coreservice
spec:
  rules:
    - host: coreservice.localdev.me

1.4. Overlay dev (EKS)
kustomization.yaml
yaml

apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: coreservice

resources:
  - ../../base
  - sealedsecret-ghcr.yaml

patches:
  - patch-deployment.yaml
  - patch-ingress.yaml

patch-deployment.yaml
yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: coreservice
spec:
  replicas: 2
  template:
    spec:
      containers:
        - name: coreservice
          image: ghcr.io/vcharrie/demo_application:<sha-dev>

patch-ingress.yaml (ALB)
yaml

apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: coreservice
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
spec:
  rules:
    - host: coreservice.dev.mydomain.com

1.5. Overlay prod (EKS)
patch-deployment.yaml
yaml

spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: coreservice
          image: ghcr.io/vcharrie/demo_application:<sha-prod>

patch-ingress.yaml
yaml

spec:
  rules:
    - host: coreservice.mydomain.com

1.6. Installation ArgoCD (local)
Objectif : valider GitOps avant EKS.

Étapes :

    Installer ArgoCD dans kind

    Exposer l’UI via ingress local

    Créer une App ArgoCD pointant vers overlays/local

    Activer syncPolicy: automated

    Vérifier que le déploiement est 100% GitOps

👉 Cette étape valide la structure avant de passer au cloud.
1.7. Mise à jour du script PowerShell
Objectif : rendre le script :

    idempotent

    robuste

    GitOps‑compatible

    compatible EKS

Ajouts :

    kubectl rollout status

    vérification du namespace

    vérification du SealedSecret

    séparation bootstrap / déploiement

    suppression des commandes non GitOps

2. Tests & Validation
✔ Test 1 — Validation Kustomize
Code

kubectl kustomize k8s/overlays/local
kubectl kustomize k8s/overlays/dev
kubectl kustomize k8s/overlays/prod

✔ Test 2 — Déploiement GitOps local

    ArgoCD synchronise automatiquement

    Pod READY

    Service OK

    Ingress OK

    Healthcheck OK

✔ Test 3 — Idempotence du script

    relancer le script plusieurs fois

    aucun échec

    aucun doublon