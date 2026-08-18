# Spécification Technique V6  
## Fonction technique : RBAC (métier + technique Kubernetes + technique Cloud)

---

# PARTIE 1 — Choix d’architecture  
*(DAT allégé — solutions conceptuelles)*

---

# 0. Objectif du document

Décrire la solution technique de contrôle d’accès (RBAC) du système, en distinguant :

- **RBAC métier** : autorisation applicative basée sur rôles et permissions métier.  
- **RBAC technique Kubernetes** : autorisation sur les ressources du cluster.  
- **RBAC technique Cloud (IAM)** : autorisation sur les ressources cloud et l’infrastructure.

Ce document constitue la partie RBAC de la Spécification Technique V6.

---

# 1. Exigences techniques

Les exigences techniques expriment **des besoins**, jamais des solutions.  
Les solutions (Spring Security, Kubernetes RBAC, IAM, Terraform, etc.)  
sont décrites dans la section **Choix d’architecture**.

---

## 1.1 Exigences techniques locales (liées aux UC métier)

- **ET‑RBAC‑MET‑01** : Le système doit contrôler l’accès aux UC métier en fonction du rôle métier de l’utilisateur.  
- **ET‑RBAC‑MET‑02** : Le système doit appliquer des permissions métier fines pour chaque UC métier.  
- **ET‑RBAC‑MET‑03** : Le système doit empêcher l’exécution d’un UC métier en absence de permission métier.  
- **ET‑RBAC‑MET‑04** : Le système doit tracer les décisions d’autorisation métier (audit).

---

## 1.2 Exigences techniques transverses (RBAC technique)

### Kubernetes
- **ET‑RBAC‑TECH‑K8S‑01** : Le système doit contrôler l’accès aux ressources Kubernetes.  
- **ET‑RBAC‑TECH‑K8S‑02** : Le système doit séparer les responsabilités techniques (DEV, OPS, SEC).

### Cloud / IAM
- **ET‑RBAC‑TECH‑CLOUD‑01** : Le système doit contrôler l’accès aux ressources cloud.  
- **ET‑RBAC‑TECH‑CLOUD‑02** : Le système doit séparer les responsabilités techniques (INFRA, PLATFORM, SECURITY).

### Audit
- **ET‑RBAC‑TECH‑AUD‑01** : Le système doit tracer les actions d’administration Kubernetes et IAM.

---

# 2. Choix d’architecture

Les choix d’architecture sont les **solutions techniques** retenues pour répondre aux exigences ci‑dessus.

---

## 2.1 Choix d’architecture — RBAC métier (applicatif)

### Framework de sécurité
**Spring Security** portera le modèle RBAC métier.  
Justification : support natif des rôles, permissions, filtres, annotations.

### Modèle d’autorisation
- Permissions métier → `GrantedAuthority`.  
- Rôles métier → `ROLE_*`.  
- Mapping UC → permission dans les services applicatifs.

### Point d’application des contrôles
- Endpoints REST via `@PreAuthorize`.  
- Services applicatifs via vérification programmatique.

### Audit
- Log des décisions d’autorisation (ALLOW/DENY).  
- Corrélation ID.

---

## 2.2 Choix d’architecture — RBAC technique Kubernetes

### Mécanisme d’autorisation
**RBAC natif Kubernetes** :  
`Role`, `ClusterRole`, `RoleBinding`, `ClusterRoleBinding`.

### Rôles techniques
- `ROLE_K8S_DEV`  
- `ROLE_K8S_OPS`  
- `ROLE_K8S_SEC`

### Isolation
Namespaces : `dev`, `preprod`, `prod`.

### Audit
Logs du control plane.

---

## 2.3 Choix d’architecture — RBAC technique Cloud (IAM / Terraform)

### Gestion des identités techniques
**IAM** pour :  
- identités techniques,  
- rôles IAM,  
- policies IAM.

### Rôles techniques IAM
- `ROLE_INFRA_ADMIN`  
- `ROLE_PLATFORM_ADMIN`  
- `ROLE_SECURITY_ADMIN`

### Gestion déclarative
**Terraform** pour gérer les rôles et policies IAM.

### Audit
Logs IAM / CloudTrail.

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

- une **Partie 1** conceptuelle (exigences + architecture),  
- une **Partie 2** opérationnelle (guidelines d’implémentation),  
- un modèle RBAC métier clair,  
- un modèle RBAC technique Kubernetes,  
- un modèle RBAC technique Cloud IAM,  
- une séparation stricte entre les trois couches de gouvernance.

Document stabilisé, prêt pour intégration dans la Spec Technique V6.

