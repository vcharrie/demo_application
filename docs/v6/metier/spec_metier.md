# Spécification Métier — CoreService (V6)

## 0. Contexte de la version V6

La version V6 étend la spécification métier V1 révisée en intégrant un modèle RBAC (Role-Based Access Control) strictement métier.  
L’objectif est d’ajouter des exigences de sécurité métier, des règles métier de contrôle d’accès, des cas d’usage métier de sécurité, et d’intégrer ces exigences dans les cas d’usage métier existants.

Cette évolution s’inspire des référentiels suivants :

- NIST RBAC Model (NIST 800‑162) : modèle de référence pour la définition des rôles et permissions au niveau métier.  
- ISO 27001 / 27002 — Access Control : exigences de contrôle d’accès définies au niveau organisationnel et métier.  
- IAM / IGA (Identity Governance & Administration) : gouvernance des rôles et permissions.  
- OWASP SAMM / BSIMM : intégration de la sécurité dans les cas d’usage métier.  
- BPMN / UML Use Case : modélisation des préconditions d’accès dans les UC.

---

## 1. Catégorisation des exigences de sécurité

### 1.1 Exigences de sécurité métier

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

Elles vivent dans :
- les Exigences métier (EM)  
- les Règles métier (BR)  
- les Cas d’usage métier (UC)  
- les UC fonctionnels  

Exemples :
- Seuls les superviseurs peuvent valider un virement > seuil.  
- Un client ne peut initier un virement que sur ses propres comptes.  
- Toute opération doit être historisée avec la permission utilisée.

### 1.2 Exigences de sécurité techniques

Définissent :
- les propriétés techniques que le système doit garantir  
- sans décrire la solution technique

Elles concernent :
- authentification des acteurs  
- identification des acteurs  
- chiffrement des communications  
- protection des secrets  
- journalisation technique  
- sécurité des composants techniques  
- contrôles d’accès API  
- sécurité CI/CD  
- sécurité cloud  

Elles vivent dans :
- la spécification technique  
- l’architecture sécurité  
- les documents de conception technique  
- les guides Secure-by-Design  

Exemples :
- Tout acteur réalisant une opération métier doit être identifié et authentifié.  
- Les communications doivent garantir la confidentialité et l’intégrité des données.  
- Les secrets doivent être stockés dans un coffre-fort applicatif.

### 1.3 Spécification technique

La spécification technique décrit ensuite **comment** les exigences techniques sont satisfaites (JWT, OAuth2, TLS 1.3, Spring Security, etc.).

### 1.4 Règle d’or

- Impacte un acteur / rôle / permission / responsabilité → exigence métier.  
- Imposée comme propriété technique à garantir → exigence technique.  
- Décrit une solution → spécification technique.

---

## 2. Exigences métier (EM) — V6

### EM-01 — Garantir l’intégrité des opérations financières
Le système doit empêcher toute opération conduisant à un solde incohérent ou négatif, afin de préserver l’intégrité comptable.

### EM-02 — Permettre la gestion des opérations simples sur un compte
Le système doit permettre au titulaire d’un compte d’effectuer des opérations simples (dépôt, retrait) de manière autonome et sécurisée.

### EM-03 — Permettre l’exécution de virements internes
Le système doit permettre au titulaire d’un compte d’initier un virement interne vers un autre compte, dans le respect des règles métier.

### EM-04 — Garantir la validation des opérations sensibles
Les opérations sensibles (virements élevés) doivent être validées par un agent habilité afin de prévenir les fraudes et erreurs.

### EM-05 — Assurer la traçabilité complète des opérations
Toute opération ou virement doit être historisé afin de permettre un audit complet.

### EM-06 — Contrôler l’accès aux cas d’usage métier via des permissions métier (NOUVEAU)
Toute exécution d’un cas d’usage métier doit être conditionnée à une permission métier explicite.

### EM-07 — Définir un référentiel métier des rôles et permissions (NOUVEAU)
Le métier doit définir un référentiel des rôles et permissions.

### EM-08 — Assurer la gouvernance métier des rôles et permissions (NOUVEAU)
Toute modification d’un rôle ou d’une permission doit être auditée.

---

## 3. Règles métier (BR) — V6

### BR-01 — Solde non négatif
Un compte ne peut jamais avoir un solde négatif.

### BR-02 — Compte suspendu
Un compte suspendu ne peut émettre ni recevoir d’opération.

### BR-03 — Montant positif
Toute opération ou virement doit avoir un montant strictement positif.

### BR-05 — Vérification du solde avant virement
Un virement interne doit vérifier le solde du compte source avant exécution.

### BR-06 — Validation des virements élevés
Les virements supérieurs à un seuil (ex : 1000€) nécessitent une validation par un agent habilité.

### BR-07 — Historisation obligatoire
Toute opération ou virement doit être historisé.

### BR-08 — Transition directe INITIATED → COMPLETED
Un virement peut passer directement de INITIATED à COMPLETED si le montant ≤ seuil.

### BR-09 — Droits de validation des agents
Seuls les agents ayant le rôle SUPERVISEUR peuvent valider les virements > seuil.

### BR-10 — Auto-virement interdit
Le compte source et le compte destination doivent être différents.

### BR-11 — Statut du compte destination
Un compte destination en statut Fermé ne peut recevoir de virement.

### BR-04 — Opération immuable
Une opération validée ne peut plus être modifiée.  
Nature : règle transversale du domaine.  
Portée : s’applique à toutes les opérations validées, indépendamment des UC métier.  
Justification : EM-01.  

#### Règles RBAC ajoutées

### BR-12 — Permission requise pour exécuter un cas d’usage
Un cas d’usage métier ne peut être exécuté que si l’acteur possède la permission métier associée.

### BR-13 — Rôle métier = regroupement de permissions métier
Un rôle ne donne aucun droit direct : seules les permissions comptent.

### BR-14 — Permission explicite obligatoire
Une permission doit être explicitement présente dans le référentiel métier.

### BR-15 — Permission non implicite
Aucune permission ne peut être déduite d’un contexte ou d’un statut.

### BR-16 — Traçabilité des permissions utilisées
Toute exécution d’un cas d’usage doit historiser la permission utilisée.

### BR-17 — Gouvernance des rôles
Toute création, modification ou suppression d’un rôle doit être auditée.

### BR-18 — Gouvernance des permissions
Toute création, modification ou suppression d’une permission doit être auditée.

---

## 4. Cas d’usage métier (UC) — V6

### UC01 — Créer un compte
Acteur : Agent  
Permission requise : PERM_ACCOUNT_CREATE  
Objectif : Créer un compte actif avec solde initial.  
Règles mobilisées : BR-07, BR-12, BR-16  
EM associées : EM-02, EM-06

### UC02 — Dépôt
Acteur : Client  
Permission requise : PERM_ACCOUNT_DEPOSIT  
Objectif : Ajouter un montant positif au solde d’un compte actif.  
Règles mobilisées : BR-03, BR-02, BR-07, BR-12, BR-16  
EM associées : EM-02, EM-06

### UC03 — Retrait
Acteur : Client  
Permission requise : PERM_ACCOUNT_WITHDRAW  
Objectif : Retirer un montant si le solde est suffisant.  
Règles mobilisées : BR-03, BR-01, BR-02, BR-07, BR-12, BR-16  
EM associées : EM-02, EM-01, EM-06

### UC04 — Initier un virement
Acteur : Client  
Permission requise : PERM_TRANSFER_INITIATE  
Objectif : Créer une demande de virement interne.  
Règles mobilisées : BR-03, BR-05, BR-10, BR-11, BR-07, BR-08, BR-12, BR-16  
EM associées : EM-03, EM-05, EM-01, EM-06

### UC05 — Valider un virement
Acteur : Agent  
Permission requise : PERM_TRANSFER_VALIDATE  
Objectif : Valider ou rejeter un virement en attente.  
Règles mobilisées : BR-06, BR-09, BR-07, BR-12, BR-16  
EM associées : EM-04, EM-05, EM-06

### UC06 — Consulter l’historique
Acteur : Client  
Permission requise : PERM_ACCOUNT_HISTORY  
Objectif : Afficher les opérations et virements d’un compte.  
Règles mobilisées : BR-07, BR-12, BR-16  
EM associées : EM-05, EM-06

---

## 5. Scénarios métier — V5 réintégrés

### UC01 — Créer un compte
Nominal  
- L’agent saisit les informations du titulaire.  
- Le système crée un compte actif.  
- Historisation.

Alternatif  
- Titulaire déjà existant → création refusée.

Erreur  
- Données invalides → compte non créé.

---

### UC02 — Dépôt
Nominal  
- Le client initie un dépôt.  
- BR-03 validée.  
- Solde augmenté.  
- Historisation.

Alternatif  
- Compte suspendu → dépôt refusé (BR-02).

Erreur  
- Montant non numérique → rejet.

---

### UC03 — Retrait
Nominal  
- Le client initie un retrait.  
- BR-03 validée.  
- BR-01 validée (solde suffisant).  
- Solde diminué.  
- Historisation.

Alternatif  
- Solde insuffisant → retrait refusé.

Erreur  
- Montant négatif → rejet.

---

### UC04 — Initier un virement
Acteur : Client  
Objectif : Créer une demande de virement interne.  

Scénario nominal — montant ≤ seuil  
- Le client initie un virement.  
- BR-03 (montant positif) validée.  
- BR-05 (solde suffisant) validée.  
- BR-10 (comptes distincts) validée.  
- BR-11 (compte destination non fermé) validée.  
- Le virement est exécuté immédiatement (COMPLETED, BR-08).  
- Historisation (BR-07).

Scénario alternatif — montant > seuil  
- Le client initie un virement.  
- Toutes les règles BR-03, BR-05, BR-10, BR-11 sont validées.  
- Le virement est placé en état PENDING (BR-06).  
- Historisation (BR-07).

Scénario alternatif — solde insuffisant  
- Le client initie un virement.  
- BR-05 échoue (solde insuffisant).  
- Le virement est refusé.  
- Message métier : “Solde insuffisant”.

Scénario alternatif — compte destination fermé  
- Le client initie un virement.  
- BR-11 échoue (compte destination en statut Fermé).  
- Le virement est refusé.  
- Message métier : “Compte destination fermé”.

Scénario d’erreur — auto-virement  
- compteSource = compteDestination.  
- BR-10 échoue.  
- Le virement est refusé.  
- Message métier : “Auto-virement interdit”.

Scénario d’erreur — compte suspendu  
- Le compte source est en statut Suspendu (BR-02).  
- Le virement est refusé.  
- Message métier : “Compte suspendu”.

---

### UC05 — Valider un virement
Nominal  
- L’agent SUPERVISEUR valide un virement PENDING.  
- Débit/crédit exécuté.  
- Historisation.

Alternatif  
- L’agent rejette → FAILED.

Erreur  
- Agent non habilité → validation refusée.

---

### UC06 — Consulter l’historique
Nominal  
- Le client consulte l’historique.  
- Le système retourne les opérations et virements.

Alternatif  
- Aucun historique → liste vide.

Erreur  
- Compte inexistant → erreur métier.

---

## 6. Entités métier — V5 réintégrées

### Compte
- id : UUID  
- titulaireId : UUID  
- solde : decimal(18,2)  
- statut : enum {Actif, Suspendu, Fermé}

### Opération
- id : UUID  
- compteId : UUID  
- type : DEBIT / CREDIT  
- montant : decimal(18,2)  
- date : ISO 8601  
- état : INITIATED / VALIDATED / REJECTED

### Virement
- id : UUID  
- compteSourceId : UUID  
- compteDestinationId : UUID  
- montant : decimal(18,2)  
- date : ISO 8601  
- état : INITIATED / PENDING / COMPLETED / FAILED  
- validatedBy : UUID (Agent)

### Client
- id : UUID  
- nom : string  
- email : string

### Agent
- id : UUID  
- nom : string  
- rôle : enum {AGENT, SUPERVISEUR}

---

## 7. États métier — V5 réintégrés

### Compte
- Actif → opérations autorisées  
- Suspendu → aucune opération autorisée  
- Fermé → lecture seule

### Opération
- INITIATED → VALIDATED  
- INITIATED → REJECTED

### Virement
- INITIATED → COMPLETED (montant ≤ seuil)  
- INITIATED → PENDING (montant > seuil)  
- PENDING → COMPLETED (validation agent)  
- PENDING → FAILED (rejet agent)  
- INITIATED → FAILED (solde insuffisant ou compte bloqué)

---

## 8. Processus métier (BPMN simplifié) — V5 réintégrés

### Processus : Exécution d’un virement interne
1. Le client initie un virement.  
2. Vérification BR-03, BR-05, BR-10, BR-11.  
3. Si montant ≤ seuil → COMPLETED (BR-08).  
4. Si montant > seuil → PENDING (BR-06).  
5. Agent SUPERVISEUR valide ou rejette (BR-09).  
6. Débit/crédit.  
7. Historisation (BR-07).  
8. Notification.

### Processus : Dépôt / Retrait
1. Le client initie une opération.  
2. Vérification BR-03.  
3. Vérification BR-01 pour les retraits.  
4. Exécution.  
5. Historisation.

---

## 9. Matrice BR → Risques — V5 réintégrée

| BR | Description | Risque associé |
|----|-------------|----------------|
| BR-01 | Solde non négatif | RISK-INT-01 : incohérence de solde |
| BR-02 | Compte suspendu | RISK-LOG-01 : contournement des statuts |
| BR-03 | Montant positif | RISK-VAL-01 : injection de montants invalides |
| BR-04 | Opération immuable | RISK-INT-02 : modification d’opération validée |
| BR-05 | Vérification solde | RISK-LOG-02 : double dépense |
| BR-06 | Validation virements élevés | RISK-AUTHZ-01 : validation non autorisée |
| BR-07 | Historisation obligatoire | RISK-AUDIT-01 : absence de traçabilité |
| BR-08 | Transition directe | RISK-LOG-03 : contournement du workflow |
| BR-09 | Droits agent | RISK-AUTHZ-02 : escalade de privilèges |
| BR-10 | Auto-virement interdit | RISK-LOG-04 : fraude interne |
| BR-11 | Statut compte destination | RISK-LOG-05 : virement vers compte invalide |

---

## 10. Extension risques RBAC (option V6)

## 10. Extension Matrice BR → Risques (RBAC V6)

| BR | Description | Risque associé |
|----|-------------|----------------|
| BR-12 | Permission requise pour exécuter un cas d’usage | RISK-AUTHZ-03 : exécution non autorisée d’un cas d’usage |
| BR-13 | Rôle = regroupement de permissions métier | RISK-AUTHZ-04 : rôle mal défini ou incohérent |
| BR-14 | Permission explicite obligatoire | RISK-AUTHZ-05 : permission implicite ou non déclarée |
| BR-15 | Permission non implicite | RISK-AUTHZ-06 : contournement des permissions par contexte |
| BR-16 | Traçabilité des permissions utilisées | RISK-AUDIT-02 : absence de traçabilité des permissions |
| BR-17 | Gouvernance des rôles | RISK-GOV-01 : gouvernance insuffisante des rôles |
| BR-18 | Gouvernance des permissions | RISK-GOV-02 : gouvernance insuffisante des permissions |

