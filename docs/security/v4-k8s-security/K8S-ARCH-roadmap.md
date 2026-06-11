# K8S-ARCH-03 — Roadmap Kubernetes V4-A (révisée)

**Domaine :** Orchestration / Kubernetes  
**Couche :** Roadmap itérative locale → préparation cloud  
**Statut :** 🟦 En cours (V4-A)

---

## 🎯 Objectif global

En 1 semaine, obtenir :

- une architecture Kubernetes complète, propre, reproductible
- une implémentation fonctionnelle (cluster local + ingress complet + config + SHA immuable)
- les contrôles de sécurité essentiels (SecurityContext, Secrets, NetworkPolicies, RBAC)
- une vision claire de la continuité cloud (V4-B)
- une maîtrise des concepts fondamentaux d'un architecte Kubernetes

> **Note CD :** en V4-A, le cluster est local. GitHub Actions ne peut pas l'atteindre. Tout le déploiement se fait manuellement depuis le poste avec `kubectl`. Le job CD GitHub Actions est documenté pour référence V4-B uniquement.

---

## 🟦 Vue d'ensemble des itérations

| Itération | Objectif | Livrables | Concepts clés | Durée estimée |
|---|---|---|---|---|
| 1. Fondations + Ingress complet | Déploiement fonctionnel + routing HTTP opérationnel | base/, overlay dev, ingress NGINX complet, demo.local, test curl | Pods, Deployments, Services, Ingress, Kustomize, DNS local | 3h–4h |
| 2. Configuration & Secrets | Config propre + secrets chiffrés | ConfigMap par env, Sealed Secrets installé + secret chiffré de test | ConfigMap, Secret, kubeseal, 12-factor | 2h–3h |
| 3. Immutabilité & SHA | SHA immuable bout en bout | Tag SHA injecté dans le patch Kustomize, vérification digest | Image tagging, Kustomize patching, immutabilité | 1h–1h30 |
| 4. Sécurité Kubernetes (SEC-K8S-01 → 04) | Durcissement pod + réseau + RBAC | SecurityContext, NetworkPolicy deny-all + exceptions, ServiceAccount dédié, RBAC minimal | PSS, NetworkPolicy, SA, RBAC, least privilege | 3h–4h |
| 5. Troubleshooting & Validation | Maîtriser le diagnostic Kubernetes | Lecture des events, CrashLoopBackOff, probe failures, logs | kubectl describe/logs/events, debug pod | 1h30–2h |
| 6. Observabilité infrastructure | Déploiement observable | Probes avancées, rollout strategy, logs structurés côté K8s | Rollout strategy, kubectl rollout, logging K8s | 1h30–2h |
| 7. Finalisation & Préparation V4-B | Architecture complète prête cloud | Structure GitOps-ready, manifests propres, documentation, préparation EKS | GitOps, Kustomize layering, immutabilité, continuité cloud | 1h–1h30 |

**Total : ~13h–18h** → faisable en 1 semaine à 2h–3h/jour.

---

## 🟩 Itération 1 — Fondations + Ingress complet

### 🎯 Objectif

Avoir un déploiement fonctionnel **et** un routing HTTP opérationnel dès le départ — pas de retour en arrière sur l'ingress plus tard.

### 🔧 Livrables

- `k8s/base/` complet (namespace, deployment, service, ingress, configmap)
- `k8s/overlays/dev/` opérationnel
- Cluster kind créé
- Ingress NGINX controller installé et prêt
- `demo.local` résolu dans `/etc/hosts`
- Test curl `http://demo.local/actuator/health` → `{"status":"UP"}`

### 📚 Concepts

- Pod / Deployment / ReplicaSet
- Service ClusterIP
- Ingress + Ingress Controller NGINX
- Probes (readiness vs liveness — distinction sémantique)
- Requests/Limits
- Kustomize base + overlay
- DNS local (`/etc/hosts`, `nip.io`)

### ⚠️ Points de vigilance

- Le controller NGINX doit être installé **avant** d'appliquer les manifests
- Sans `initialDelaySeconds` adapté, Spring Boot sera tué avant d'être prêt
- Vérifier `kubectl get all -n demo-app` et `kubectl get ingress -n demo-app` avant de tester curl

### ⏱ Durée

3h–4h

---

## 🟩 Itération 2 — Configuration & Secrets

### 🎯 Objectif

Séparer proprement la configuration et introduire la gestion sécurisée des secrets avec Sealed Secrets.

### 🔧 Livrables

- ConfigMap par environnement (dev/prod) via overlays
- Controller `kubeseal` installé dans le cluster
- Un secret de test chiffré avec `kubeseal` et appliqué dans le cluster
- Vérification que le secret est bien injecté dans le pod

### 📚 Concepts

- ConfigMap
- Secret Kubernetes (et pourquoi il ne suffit pas)
- Sealed Secrets — installation controller, récupération clé publique, chiffrement
- Mount vs Env (variables d'environnement vs fichiers montés)
- 12-factor configuration

### ⚠️ Points de vigilance

- L'installation de `kubeseal` et du controller est non triviale — prévoir du temps
- La clé publique change si le cluster est recréé : les secrets chiffrés ne sont pas portables entre clusters
- Aucun `secret.yaml` en clair dans Git, même en placeholder

### ⏱ Durée

2h–3h (prévoir le haut de la fourchette pour kubeseal)

---

## 🟩 Itération 3 — Immutabilité & SHA

### 🎯 Objectif

Comprendre et maîtriser le mécanisme concret qui rend chaque déploiement traçable et non ambigu.

### 🔧 Livrables

- Tag SHA produit par le pipeline CI (ou simulé localement)
- SHA injecté dans le patch Kustomize `overlays/dev/patches-dev.yaml`
- Vérification que l'image déployée correspond exactement au SHA attendu
- Documentation du mécanisme pour référence V4-B

### 📚 Concepts

- Image tagging (`:latest` vs tag SHA vs digest)
- Kustomize directive `images:` comme alternative aux patches manuels
- Immutabilité du déploiement
- Lien avec le pipeline CI (V3) — comment le SHA voyage de GHCR jusqu'au manifest

### ⚠️ Points de vigilance

- `:latest` est interdit en production — toujours un tag immuable
- La directive `images:` de Kustomize est plus propre qu'un patch manuel sur le champ `image:`

### ⏱ Durée

1h–1h30

---

## 🟩 Itération 4 — Sécurité Kubernetes (SEC-K8S-01 → 04)

### 🎯 Objectif

Durcir l'application, le namespace et le réseau.

### 🔧 Livrables

- SecurityContext complet (`runAsNonRoot`, `readOnlyRootFilesystem`, `drop ALL`, `seccompProfile`)
- NetworkPolicy `deny-all` par défaut
- NetworkPolicy autorisant ingress depuis ingress-controller
- NetworkPolicy autorisant egress DNS
- ServiceAccount dédié (pas `default`)
- RBAC minimal limité au namespace

### 📚 Concepts

- SecurityContext
- Pod Security Standards (PSS)
- NetworkPolicy
- ServiceAccount
- RBAC (Role, RoleBinding — pas ClusterRole)
- Least privilege

### ⚠️ Points de vigilance

- `readOnlyRootFilesystem` casse souvent des apps qui écrivent dans `/tmp` — prévoir un `emptyDir` si nécessaire
- La NetworkPolicy `deny-all` coupe aussi le DNS si l'egress n'est pas explicitement autorisé
- Tester chaque règle NetworkPolicy isolément avant de les combiner

### ⏱ Durée

3h–4h

---

## 🟩 Itération 5 — Troubleshooting & Validation

### 🎯 Objectif

Maîtriser le diagnostic Kubernetes — savoir lire ce qui se passe quand ça ne fonctionne pas.

### 🔧 Livrables

- Checklist de validation complète (cf. K8S-ARCH-02 §7) exécutée et documentée
- Au moins un scénario de panne simulé et résolu (probe qui échoue, image introuvable, NetworkPolicy trop restrictive)
- Référence personnelle des commandes de diagnostic

### 📚 Concepts

- `kubectl describe pod` — lecture des events
- `kubectl logs` — logs applicatifs
- États pod : `CrashLoopBackOff`, `ImagePullBackOff`, `Pending`, `OOMKilled`
- Probe failure — comment les lire et les corriger
- `kubectl exec` pour debug dans un pod

### ⚠️ Points de vigilance

- Cette itération est souvent sous-estimée — c'est pourtant là que se construit la vraie maîtrise opérationnelle
- Ne pas la sauter même si tout fonctionne : simuler des pannes volontairement

### ⏱ Durée

1h30–2h

---

## 🟩 Itération 6 — Observabilité infrastructure

### 🎯 Objectif

Rendre le déploiement robuste et observable au niveau Kubernetes.

### 🔧 Livrables

- Stratégie de rollout configurée (`RollingUpdate` avec `maxSurge`/`maxUnavailable`)
- `kubectl rollout status` et `kubectl rollout undo` maîtrisés
- Logs structurés vérifiés côté Kubernetes (`kubectl logs`)
- Probes affinées si nécessaire après les tests des itérations précédentes

### 📚 Concepts

- Rollout strategy (`RollingUpdate` vs `Recreate`)
- `kubectl rollout` (status, history, undo)
- Logging Kubernetes — `kubectl logs`, multi-container, logs précédents
- Lien avec l'observabilité applicative (gap identifié — à traiter côté app si nécessaire)

### ⚠️ Points de vigilance

- HPA retiré de cette itération — hors scope V4-A, à introduire en V4-B si pertinent
- Si les logs applicatifs sont en texte brut, noter le gap pour la phase app — ne pas bloquer ici

### ⏱ Durée

1h30–2h

---

## 🟩 Itération 7 — Finalisation & Préparation V4-B

### 🎯 Objectif

Avoir une architecture propre, stable, cloud-ready, et une vision claire de la suite.

### 🔧 Livrables

- Structure GitOps-ready vérifiée (`kustomize build` propre sur tous les overlays)
- Manifests nettoyés et commentés
- Checklist de continuité V4-B complétée
- Documentation personnelle des choix d'architecture

### 📚 Concepts

- GitOps (ArgoCD/Flux) — principes, pas implémentation
- Kustomize layering avancé
- Immutabilité stricte (SHA/digest)
- Continuité cloud : Ingress ALB, IRSA, External Secrets, EKS hardened, CD digest gate

### ⏱ Durée

1h–1h30

---

## 🟦 Tableau de continuité V4-A → V4-B

| Élément V4-A | Continuité V4-B |
|---|---|
| Kustomize overlays | GitOps (ArgoCD / Flux) |
| OIDC GitHub Actions | Authentification AWS sans token statique |
| ServiceAccount dédié | IRSA (IAM Roles for Service Accounts) |
| Sealed Secrets | AWS Secrets Manager + External Secrets |
| Ingress NGINX | ALB Ingress Controller |
| Namespace dédié | Isolation EKS multi-tenant |
| SHA immuable | Digest gate CD sur EKS |
| Troubleshooting local | CloudWatch Logs + Container Insights |
| Rollout strategy | EKS managed node groups + PodDisruptionBudget |

---

## Références croisées

- **K8S-ARCH-01** — Architecture Kubernetes sécurisée (V4-A)
- **K8S-ARCH-02** — Implémentation de l'architecture Kubernetes de base (V4-A)
- **SEC-K8S-01** — SecurityContext
- **SEC-K8S-02** — Secrets Kubernetes (Sealed Secrets)
- **SEC-K8S-03** — Network Policies
- **SEC-K8S-04** — RBAC
- **SEC-CI-01** — Pipeline CI/CD (build + push image GHCR)
- **SEC-CI-02** — Secrets GitHub (OIDC, permissions minimales)