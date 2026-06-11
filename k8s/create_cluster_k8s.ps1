Write-Host "=== Suppression des anciens clusters kind ==="
$clusters = kind get clusters
foreach ($c in $clusters) {
    Write-Host " - Suppression du cluster: $c"
    kind delete cluster --name $c
}

Write-Host "=== Création du cluster kind ==="
kind create cluster --name coreservice --config k8s/config/kind-config.yaml

Write-Host "=== Installation du controller Sealed Secrets ==="
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.26.0/controller.yaml

Write-Host "=== Attente du controller Sealed Secrets ==="
kubectl wait --namespace kube-system `
  --for=condition=available deployment/sealed-secrets-controller `
  --timeout=120s

Write-Host "=== Création du namespace coreservice ==="
kubectl create namespace coreservice --dry-run=client -o yaml | kubectl apply -f -

Write-Host "=== Génération du secret GHCR en clair ==="
kubectl create secret docker-registry ghcr-secret `
  --docker-server=ghcr.io `
  --docker-username=vcharrie `
  --docker-password="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" `
  --namespace=coreservice `
  --dry-run=client -o yaml > secret.yaml

Write-Host "=== Scellement du secret ==="
Get-Content secret.yaml | kubeseal --format yaml | Set-Content sealedsecret.yaml

Write-Host "=== Application du SealedSecret ==="
kubectl apply -f sealedsecret.yaml

Write-Host "=== Installation de ingress-nginx ==="
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

Write-Host "=== Attente readiness ingress-nginx ==="
kubectl wait --namespace ingress-nginx `
  --for=condition=ready pod `
  --selector=app.kubernetes.io/component=controller `
  --timeout=120s

Write-Host "=== Déploiement de l'application via Kustomize ==="
kubectl apply -k k8s/overlays/local

Write-Host "=== Attente des Pods ==="
kubectl wait --for=condition=ready pod -n coreservice --all --timeout=120s

Write-Host "=== Redémarrage du Deployment ==="
kubectl rollout restart deployment coreservice -n coreservice

Write-Host "=== Vérification des Pods ==="
kubectl get pods -n coreservice

Write-Host "=== Attente que le déploiement soit prêt ==="
kubectl rollout status deployment/coreservice -n coreservice --timeout=60s

Write-Host "=== Test HTTP via curl ==="
curl http://coreservice.localdev.me:8080/actuator/health
