# Se placer à la racine du repo demo_application
cd C:\perso\demo_application

# Créer l'arborescence
New-Item -ItemType Directory -Force -Path k8s/base
New-Item -ItemType Directory -Force -Path k8s/overlays/dev

# Créer les fichiers de base
New-Item -ItemType File -Force -Path k8s/base/namespace.yaml
New-Item -ItemType File -Force -Path k8s/base/deployment.yaml
New-Item -ItemType File -Force -Path k8s/base/service.yaml
New-Item -ItemType File -Force -Path k8s/base/ingress.yaml
New-Item -ItemType File -Force -Path k8s/base/configmap.yaml

# Créer les fichiers overlay dev
New-Item -ItemType File -Force -Path k8s/overlays/dev/kustomization.yaml
New-Item -ItemType File -Force -Path k8s/overlays/dev/configmap-dev.yaml
New-Item -ItemType File -Force -Path k8s/overlays/dev/patches-dev.yaml
