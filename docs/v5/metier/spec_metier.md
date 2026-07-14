# Spécification Métier — CoreService (V1 révisée)

## 1. Exigences métier (EM)

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

---

## 2. Règles métier (BR)

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

---

## 2.x Règles transversales (hors UC)

### BR-04 — Opération immuable
Une opération validée ne peut plus être modifiée.

**Nature :** règle transversale du domaine.  
**Portée :** s’applique à toutes les opérations validées, indépendamment des UC métier.  
**Justification :** EM-01 (intégrité des opérations financières).  
**Statut :** hors scope V1 pour la traçabilité UC → US → tests.  
Elle sera rattachée à un UC métier dédié si une fonctionnalité de modification/annulation d’opération est ajoutée dans une version ultérieure.

---

## 3. Cas d’usage métier (UC)

### UC01 — Créer un compte
Acteur : Agent  
Objectif : Créer un compte actif avec solde initial.  
Règles mobilisées : BR-07  
EM associée : EM-02

### UC02 — Dépôt
Acteur : Client  
Objectif : Ajouter un montant positif au solde d’un compte actif.  
Règles mobilisées : BR-03, BR-02, BR-07  
EM associée : EM-02

### UC03 — Retrait
Acteur : Client  
Objectif : Retirer un montant si le solde est suffisant.  
Règles mobilisées : BR-03, BR-01, BR-02, BR-07  
EM associée : EM-02, EM-01

### UC04 — Initier un virement
Acteur : Client  
Objectif : Créer une demande de virement interne.  
Règles mobilisées : BR-03, BR-05, BR-10, BR-11, BR-07, BR-08  
EM associées : EM-03, EM-05, EM-01

### UC05 — Valider un virement
Acteur : Agent  
Objectif : Valider ou rejeter un virement en attente.  
Règles mobilisées : BR-06, BR-09, BR-07  
EM associée : EM-04, EM-05

### UC06 — Consulter l’historique
Acteur : Client  
Objectif : Afficher les opérations et virements d’un compte.  
Règles mobilisées : BR-07  
EM associée : EM-05

---

## 4. Scénarios métier

### UC01 — Créer un compte
#### Nominal
- L’agent saisit les informations du titulaire.
- Le système crée un compte actif.
- Historisation.

#### Alternatif
- Titulaire déjà existant → création refusée.

#### Erreur
- Données invalides → compte non créé.

---

### UC02 — Dépôt
#### Nominal
- Le client initie un dépôt.
- BR-03 validée.
- Solde augmenté.
- Historisation.

#### Alternatif
- Compte suspendu → dépôt refusé (BR-02).

#### Erreur
- Montant non numérique → rejet.

---

### UC03 — Retrait
#### Nominal
- Le client initie un retrait.
- BR-03 validée.
- BR-01 validée (solde suffisant).
- Solde diminué.
- Historisation.

#### Alternatif
- Solde insuffisant → retrait refusé.

#### Erreur
- Montant négatif → rejet.

---

### UC04 — Initier un virement
Acteur : Client  
Objectif : Créer une demande de virement interne.  
Règles mobilisées : BR-03, BR-05, BR-10, BR-11, BR-07, BR-08  
EM associées : EM-03, EM-05, EM-01

#### Scénario nominal — montant ≤ seuil
- Le client initie un virement.
- BR-03 (montant positif) validée.
- BR-05 (solde suffisant) validée.
- BR-10 (comptes distincts) validée.
- BR-11 (compte destination non fermé) validée.
- Le virement est exécuté immédiatement (COMPLETED, BR-08).
- Historisation (BR-07).

#### Scénario alternatif — montant > seuil
- Le client initie un virement.
- Toutes les règles BR-03, BR-05, BR-10, BR-11 sont validées.
- Le virement est placé en état PENDING (BR-06).
- Historisation (BR-07).

#### Scénario alternatif — solde insuffisant
- Le client initie un virement.
- BR-05 échoue (solde insuffisant).
- Le virement est refusé.
- Message métier : “Solde insuffisant”.

#### Scénario alternatif — compte destination fermé
- Le client initie un virement.
- BR-11 échoue (compte destination en statut Fermé).
- Le virement est refusé.
- Message métier : “Compte destination fermé”.

#### Scénario d’erreur — auto-virement
- compteSource = compteDestination.
- BR-10 échoue.
- Le virement est refusé.
- Message métier : “Auto-virement interdit”.

#### Scénario d’erreur — compte suspendu
- Le compte source est en statut Suspendu (BR-02).
- Le virement est refusé.
- Message métier : “Compte suspendu”.

---

### UC05 — Valider un virement
#### Nominal
- L’agent SUPERVISEUR valide un virement PENDING.
- Débit/crédit exécuté.
- Historisation.

#### Alternatif
- L’agent rejette → FAILED.

#### Erreur
- Agent non habilité → validation refusée.

---

### UC06 — Consulter l’historique
#### Nominal
- Le client consulte l’historique.
- Le système retourne les opérations et virements.

#### Alternatif
- Aucun historique → liste vide.

#### Erreur
- Compte inexistant → erreur métier.

---

## 5. Entités métier

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

## 6. États métier

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

## 7. Processus métier (BPMN simplifié)

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

## 8. Matrice BR → Risques

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