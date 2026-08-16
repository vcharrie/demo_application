# Process Agile & Traçabilité — Modèle de référence et simplification CoreService

## Objectif de ce document

Ce document a deux parties :

1. Le process idéal — celui qu'on trouverait dans une organisation avec des rôles séparés (Business Analyst, Product Owner, développeurs, QA), sur un projet soumis à des exigences de conformité (ISO 27001, NIS2, audit réglementaire). C'est le modèle de référence, complet, avec tous les niveaux de traçabilité.

2. Le choix de simplification pour CoreService — ce qu'on retient, ce qu'on allège, et pourquoi, compte tenu du contexte réel du projet (démonstrateur solo, objectif de démonstration de compétence plutôt que de conformité opposable).

L'idée n'est pas d'opposer les deux, mais de montrer que la version simplifiée est un choix conscient et documenté, pas une lacune.

---

## Partie 1 — Le process idéal (multi-rôles, orienté conformité)

### 1.1 Les rôles impliqués

| Rôle | Responsabilité | Niveaux portés |
|---|---|---|
| Sponsor / Métier | Exprime le besoin stratégique | Exigences métier (EM) |
| Business Analyst (BA) | Formalise les règles et cas d'usage métier | Règles métier (BR), UC métier |
| Product Owner (PO) | Découpe en valeur livrable, priorise | Epic |
| PO / UX | Précise l'interaction utilisateur | UC fonctionnel |
| Équipe de développement | Réalise et découpe techniquement | User Story, tâches techniques |
| QA / Testeur | Valide fonctionnellement | Critères d'acceptation, tests de validation |
| Développeur | Valide techniquement | Tests unitaires, tests d'intégration |
| Auditeur / RSSI | Vérifie la chaîne de bout en bout | Matrice de traçabilité |

---

### 1.2 Deux axes orthogonaux : traçabilité et planning

Axe TRAÇABILITÉ (preuve) :
EM ↔ BR ↔ UC métier → UC fonctionnel → US → tests

Axe PLANNING (livraison) :
UC métier → Epic

Un Epic n'est jamais un maillon de preuve.  
Il regroupe des UC métier qui forment une chaîne de valeur complète.

---

### 1.3 Définition de l'Epic

Définition : un Epic regroupe les UC métier qui, ensemble, forment une chaîne de valeur complète.

Deux variantes :

Variante 1 — dépendance dure  
Ex : UC04 initier virement + UC05 valider virement → Epic "Virement interne".

Variante 2 — cohérence de capacité produit  
Ex : UC02 dépôt + UC03 retrait → Epic "Opérations compte".

Résumé :  
Epic = chaîne de valeur.  
Fonctionnalité = lecture catalogue produit de cette chaîne.

---

### 1.4 La chaîne complète

Exigence métier (EM)
 ↕ relations N-N
Règles métier (BR)
 ↕ relations N-N
UC métier
 ↓
Epic
 ↓
UC fonctionnel
 ↓
User Story
 ↓
Critères d'acceptation → tests de validation
Tests d'intégration
Tâches techniques → tests unitaires

---

### 1.5 Pourquoi chaque niveau existe

(Identique à la version initiale, non modifié)

---

### 1.6 Ce que produit ce modèle complet

Une matrice de traçabilité complète :

| EM | BR | UC métier | Epic | EF | US | Test |
|---|---|---|---|---|---|---|
| EM-01 | BR-05, BR-06 | UC04, UC05 | Virement interne | EF-012, EF-013 | US-042, US-043 | TU-089, TI-034, TV-021 |

---

## 1.x — Catégorisation des exigences de sécurité

L’intégration de la sécurité dans un modèle Agile/Secure SDLC nécessite de distinguer deux catégories :

- Exigences de sécurité métier
- Exigences de sécurité techniques

---

### 1.x.1 Exigences de sécurité métier

Définissent :
- qui a le droit de faire quoi
- dans quelles conditions
- selon quelles règles métier
- avec quelles responsabilités

Elles concernent :
- acteurs métier
- rôles métier
- permissions métier
- habilitations
- validations métier
- gouvernance
- obligations réglementaires impactant le métier

Elles doivent vivre dans :
- EM
- BR
- UC métier
- UC fonctionnels

Exemples :
- Seuls les superviseurs valident les virements > seuil.
- Un client ne peut initier un virement que sur ses propres comptes.
- Toute opération doit être historisée avec la permission utilisée.

---

### 1.x.2 Exigences de sécurité techniques

Définissent :
- comment la sécurité est implémentée

Elles concernent :
- authentification (OAuth2, JWT)
- autorisation technique (Spring Security)
- chiffrement (TLS, AES)
- sécurité CI/CD
- sécurité cloud
- sécurité réseau
- logs techniques

Elles doivent vivre dans :
- spec technique
- architecture sécurité
- contrôles d’accès API
- guides Secure-by-Design

Exemples :
- Les tokens JWT doivent être signés RSA 2048 bits.
- Les communications doivent être chiffrées TLS 1.3.

---

### 1.x.3 Règle d’or

Si l’exigence impacte un acteur, un rôle, une permission, une responsabilité → métier.  
Si elle impacte un protocole, un composant, un mécanisme technique → technique.

---

### 1.x.4 Application à CoreService

RBAC = métier  
JWT / filtres / Spring Security = technique

---

## Partie 2 — Le choix de simplification pour CoreService

### 2.1 Contexte

CoreService est un démonstrateur solo.  
Objectif : prouver une maîtrise méthodologique, pas produire une preuve opposable.

### 2.2 Principe de simplification

On garde la logique complète, on réduit le nombre d’artefacts.

| Niveau idéal | Décision | Où ça vit |
|---|---|---|
| EM | Conservé | spec_metier.md |
| BR | Conservé | spec_metier.md |
| UC métier | Conservé | spec_metier.md |
| Epic | Conservé | backlog.md |
| EF | Fusionné dans UC fonctionnel | spec_fonctionnelle.md |
| UC fonctionnel | Conservé | spec_fonctionnelle.md |
| US | Conservé | backlog.md |
| Critères d'acceptation | Conservés | backlog.md |
| Tests | Conservés | code |

### 2.3 Ce qui ne change pas

- Traçabilité EM/BR/UC → US reste explicite.
- Distinction Epic ≠ UC métier maintenue.
- UC fonctionnel reste un document à part entière.

### 2.4 Schéma simplifié

spec_metier.md  
 → EM ↔ BR ↔ UC métier  
 → backlog.md (Epics)  
 → spec_fonctionnelle.md (UC fonctionnels)  
 → backlog.md (User Stories)  
 → spec_technique_v3/v4.md (tâches techniques, tests)

### 2.5 Ce qu'il faudrait rétablir si le projet changeait de nature

- EF numérotées  
- matrice de traçabilité complète  
- séparation stricte BA / PO / QA  
- artefacts opposables audit

