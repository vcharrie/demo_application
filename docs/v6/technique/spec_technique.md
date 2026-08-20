# Spécification Technique V6  
## Fonction technique : RBAC (métier + technique Kubernetes + technique Cloud)

---

# 0. Objectif du document

Décrire la solution technique de contrôle d’accès (RBAC) du système, en distinguant :

- **RBAC métier** : autorisation applicative basée sur rôles et permissions métier.  
- **RBAC technique Kubernetes** : autorisation sur les ressources du cluster.  
- **RBAC technique Cloud (IAM)** : autorisation sur les ressources cloud et l’infrastructure.

Ce document constitue la partie RBAC de la Spécification Technique V6.

---

# 1. Exigences techniques

Les exigences techniques expriment **des besoins** (formulation : « Le système doit… »).  
Elles sont ensuite déclinées en **fonctions techniques**, **solutions**, et **composants**.

---

## 1.1 Exigences techniques locales (RBAC métier — liées aux UC)

- **ET‑RBAC‑MET‑01** : Le système doit contrôler l’accès aux UC métier en fonction du rôle métier de l’utilisateur.  
- **ET‑RBAC‑MET‑02** : Le système doit appliquer des permissions métier fines pour chaque UC métier.  
- **ET‑RBAC‑MET‑03** : Le système doit empêcher l’exécution d’un UC métier en absence de permission métier.  
- **ET‑RBAC‑MET‑04** : Le système doit tracer les décisions d’autorisation métier (audit).

---

## 1.2 Fonctions techniques locales (RBAC métier)

- **FT‑RBAC‑MET‑01** : Le système assure l’autorisation métier via un modèle RBAC basé sur rôles et permissions.  
- **FT‑RBAC‑MET‑02** : Le système assure la résolution des permissions métier pour chaque UC.  
- **FT‑RBAC‑MET‑03** : Le système assure l’application des contrôles d’accès avant l’exécution de la logique métier.  
- **FT‑RBAC‑MET‑04** : Le système assure l’audit des décisions d’autorisation métier.

---

## 1.3 Solutions techniques locales (RBAC métier)

- **ST‑RBAC‑MET‑01** : Nous utiliserons Spring Security pour porter le modèle RBAC métier.  
- **ST‑RBAC‑MET‑02** : Nous utiliserons des permissions atomiques (`GrantedAuthority`) et des rôles (`ROLE_*`).  
- **ST‑RBAC‑MET‑03** : Nous utiliserons `@PreAuthorize` et des contrôles dans les services applicatifs.  
- **ST‑RBAC‑MET‑04** : Nous utiliserons un middleware d’audit pour tracer les décisions ALLOW/DENY.

---

## 1.4 Composants techniques locaux (RBAC métier)

- Spring Security  
- Filtres de sécurité  
- Services applicatifs  
- Middleware d’audit  
- Enum `Permissions` / Enum `Roles`

---

# 2. Exigences techniques transverses (RBAC technique Kubernetes + Cloud)

---

## 2.1 Exigences techniques transverses — Kubernetes

- **ET‑RBAC‑TECH‑K8S‑01** : Le système doit contrôler l’accès aux ressources Kubernetes.  
- **ET‑RBAC‑TECH‑K8S‑02** : Le système doit séparer les responsabilités techniques (DEV, OPS, SEC).  
- **ET‑RBAC‑TECH‑K8S‑03** : Le système doit limiter les permissions par namespace.  
- **ET‑RBAC‑TECH‑K8S‑04** : Le système doit tracer les actions d’administration Kubernetes.

---

## 2.2 Fonctions techniques transverses — Kubernetes

- **FT‑RBAC‑TECH‑K8S‑01** : Le système assure l’autorisation technique sur les ressources du cluster.  
- **FT‑RBAC‑TECH‑K8S‑02** : Le système assure la séparation des rôles techniques.  
- **FT‑RBAC‑TECH‑K8S‑03** : Le système assure l’isolation des environnements via les namespaces.  
- **FT‑RBAC‑TECH‑K8S‑04** : Le système assure l’audit des actions administratives Kubernetes.

---

## 2.3 Solutions techniques transverses — Kubernetes

- **ST‑RBAC‑TECH‑K8S‑01** : Nous utiliserons le RBAC natif Kubernetes (Role, ClusterRole, RoleBinding, ClusterRoleBinding).  
- **ST‑RBAC‑TECH‑K8S‑02** : Nous utiliserons des rôles techniques dédiés (`ROLE_K8S_DEV`, `ROLE_K8S_OPS`, `ROLE_K8S_SEC`).  
- **ST‑RBAC‑TECH‑K8S‑03** : Nous utiliserons des namespaces (`dev`, `preprod`, `prod`) pour isoler les environnements.  
- **ST‑RBAC‑TECH‑K8S‑04** : Nous utiliserons les logs du control plane pour l’audit.

---

## 2.4 Composants techniques transverses — Kubernetes

- Kubernetes RBAC  
- API Server  
- Namespaces  
- Manifests YAML versionnés dans Git  
- Logs du control plane

---

## 2.5 Exigences techniques transverses — Cloud IAM

- **ET‑RBAC‑TECH‑CLOUD‑01** : Le système doit contrôler l’accès aux ressources cloud.  
- **ET‑RBAC‑TECH‑CLOUD‑02** : Le système doit séparer les responsabilités techniques (INFRA, PLATFORM, SECURITY).  
- **ET‑RBAC‑TECH‑CLOUD‑03** : Le système doit tracer les actions IAM.  
- **ET‑RBAC‑TECH‑CLOUD‑04** : Le système doit appliquer le principe de moindre privilège.

---

## 2.6 Fonctions techniques transverses — Cloud IAM

- **FT‑RBAC‑TECH‑CLOUD‑01** : Le système assure l’autorisation technique sur les ressources cloud via IAM.  
- **FT‑RBAC‑TECH‑CLOUD‑02** : Le système assure la séparation des rôles techniques cloud.  
- **FT‑RBAC‑TECH‑CLOUD‑03** : Le système assure l’audit des actions IAM.  
- **FT‑RBAC‑TECH‑CLOUD‑04** : Le système assure la gestion des permissions cloud minimales.

---

## 2.7 Solutions techniques transverses — Cloud IAM

- **ST‑RBAC‑TECH‑CLOUD‑01** : Nous utiliserons IAM (roles, policies) pour gérer les permissions cloud.  
- **ST‑RBAC‑TECH‑CLOUD‑02** : Nous utiliserons des rôles techniques IAM (`ROLE_INFRA_ADMIN`, `ROLE_PLATFORM_ADMIN`, `ROLE_SECURITY_ADMIN`).  
- **ST‑RBAC‑TECH‑CLOUD‑03** : Nous utiliserons Terraform pour gérer les rôles et policies IAM.  
- **ST‑RBAC‑TECH‑CLOUD‑04** : Nous utiliserons CloudTrail pour l’audit IAM.

---

## 2.8 Composants techniques transverses — Cloud IAM

- AWS IAM  
- AWS STS  
- Policies IAM  
- Terraform  
- CloudTrail

---

# 3. RBAC métier — Modèle conceptuel

### Permissions métier
- `PERM_ACCOUNT_CREATE`  
- `PERM_ACCOUNT_DEPOSIT`  
- `PERM_ACCOUNT_WITHDRAW`  
- `PERM_TRANSFER_INITIATE`  
- `PERM_TRANSFER_VALIDATE`  
- `PERM_HISTORY_VIEW`

### Rôles métier
**ROLE_OPERATEUR**  
- `PERM_ACCOUNT_CREATE`  
- `PERM_ACCOUNT_DEPOSIT`  
- `PERM_ACCOUNT_WITHDRAW`  
- `PERM_TRANSFER_INITIATE`

**ROLE_SUPERVISEUR**  
- `PERM_TRANSFER_VALIDATE`  
- `PERM_HISTORY_VIEW`

### Mapping UC → permissions

| UC métier | Permission requise |
|----------|---------------------|
| UC01F Créer un compte | `PERM_ACCOUNT_CREATE` |
| UC02F Dépôt | `PERM_ACCOUNT_DEPOSIT` |
| UC03F Retrait | `PERM_ACCOUNT_WITHDRAW` |
| UC04F Initier un virement | `PERM_TRANSFER_INITIATE` |
| UC05F Valider un virement | `PERM_TRANSFER_VALIDATE` |
| UC06F Historique | `PERM_HISTORY_VIEW` |

---

# 4. RBAC technique Kubernetes — Modèle conceptuel

### ROLE_K8S_DEV
- lecture pods, deployments, services, logs  
- aucune modification en production

### ROLE_K8S_OPS
- création/modification deployments, services, ingress  
- gestion rollouts/rollbacks

### ROLE_K8S_SEC
- lecture secrets  
- validation policies réseau  
- contrôle configurations de sécurité

---

# 5. RBAC technique Cloud — Modèle conceptuel

### ROLE_INFRA_ADMIN
- gestion réseau, compute, clusters

### ROLE_PLATFORM_ADMIN
- gestion services managés, déploiements applicatifs

### ROLE_SECURITY_ADMIN
- gestion IAM, secrets, policies

---

# PARTIE 2 — Choix d’implémentation  
*(Guidelines légères — sans entrer dans le détail du code)*

---

# 6. Structure du code

- `security/` : configuration Spring Security  
- `security/rbac/` : rôles, permissions, mapping UC → permissions  
- `application/services/` : application des contrôles d’accès métier  
- `infrastructure/security/` : filtres, extracteurs de claims, audit

---

# 7. Implémentation RBAC métier (guidelines)

- Définir les permissions métier dans une enum dédiée.  
- Définir les rôles métier dans une enum dédiée.  
- Charger les permissions depuis le token (claim `authorities`).  
- Appliquer les contrôles via `@PreAuthorize("hasAuthority('PERM_X')")`.  
- Appliquer les contrôles métier critiques dans les services applicatifs.  
- Logguer systématiquement les décisions d’autorisation.

---

# 8. Implémentation RBAC Kubernetes (guidelines)

- Définir les rôles techniques dans des manifests YAML.  
- Appliquer les `RoleBinding` par namespace.  
- Appliquer les `ClusterRoleBinding` uniquement pour OPS/SEC.  
- Versionner les manifests dans Git.  
- Auditer via les logs du control plane.

---

# 9. Implémentation RBAC Cloud IAM (guidelines)

- Définir les rôles IAM dans Terraform.  
- Définir les policies IAM dans Terraform.  
- Appliquer le principe de moindre privilège.  
- Versionner l’infra dans Git.  
- Auditer via IAM / CloudTrail.

---

# 10. Synthèse

Cette Spec Technique V6 (RBAC) fournit :

- une **Partie 1** conceptuelle (exigences → fonctions → solutions → composants),  
- une **Partie 2** opérationnelle (guidelines d’implémentation),  
- un modèle RBAC métier clair,  
- un modèle RBAC technique Kubernetes,  
- un modèle RBAC technique Cloud IAM,  
- une séparation stricte entre les trois couches de gouvernance.

Document stabilisé, prêt pour intégration dans la Spec Technique V6.
