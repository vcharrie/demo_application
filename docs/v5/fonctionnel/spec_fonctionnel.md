# Spécification Fonctionnelle — CoreService (V1)

Ce document décrit les UC fonctionnels, dérivés 1:1 des UC métier définis dans `spec_metier.md`.  
Les exigences fonctionnelles (EF) sont fusionnées dans chaque UC fonctionnel.  
Aucune notion technique n’est introduite ici.

---

# UC01F — Créer un compte

## 1. Objectif
Permettre à un agent de créer un compte actif pour un client, avec solde initial.

## 2. Règles fonctionnelles
- RF-01F-01 : Le système doit vérifier que les données du titulaire sont valides.
- RF-01F-02 : Le système doit créer le compte en statut Actif.
- RF-01F-03 : Le système doit historiser la création du compte (BR-07).

## 3. Données fonctionnelles manipulées
- titulaireId : UUID  
- soldeInitial : decimal(18,2)  
- statut : Actif  
- dateCreation : ISO 8601

## 4. Préconditions
- Le titulaire doit exister ou être créé dans le système.
- Les données du titulaire doivent être valides.

## 5. Postconditions
- Un compte actif est créé.
- Une entrée d’historique est ajoutée.

## 6. Scénarios fonctionnels

### Nominal
- L’agent saisit les informations du titulaire.
- Le système valide les données.
- Le compte est créé en statut Actif.
- Historisation.

### Alternatif
- Titulaire déjà existant → erreur “Titulaire déjà enregistré”.

### Erreur
- Données invalides → erreur “Données invalides”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM |
|----------------|-----------|----|----|
| UC01F | UC01 | BR-07 | EM-02 |

---

# UC02F — Effectuer un dépôt

## 1. Objectif
Permettre au client d’ajouter un montant positif au solde d’un compte actif.

## 2. Règles fonctionnelles
- RF-02F-01 : Le système doit vérifier que le compte est en statut Actif (BR-02).
- RF-02F-02 : Le système doit vérifier que le montant est strictement positif (BR-03).
- RF-02F-03 : Le système doit historiser l’opération (BR-07).

## 3. Données fonctionnelles manipulées
- compteId : UUID  
- montant : decimal(18,2)  
- type : CREDIT  
- état : INITIATED → VALIDATED  
- date : ISO 8601

## 4. Préconditions
- Le compte doit exister.
- Le compte doit être en statut Actif.
- Le montant doit être strictement positif.

## 5. Postconditions
- Le solde du compte est augmenté.
- Une opération CREDIT est historisée.

## 6. Scénarios fonctionnels

### Nominal
- Input : compteId, montant positif.
- Vérification RF-02F-01 et RF-02F-02.
- Mise à jour du solde.
- Historisation.

### Alternatif
- Compte suspendu → erreur “Compte suspendu”.

### Erreur
- Montant non numérique → erreur “Montant invalide”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM |
|----------------|-----------|----|----|
| UC02F | UC02 | BR-03, BR-02, BR-07 | EM-02 |

---

# UC03F — Effectuer un retrait

## 1. Objectif
Permettre au client de retirer un montant si le solde est suffisant.

## 2. Règles fonctionnelles
- RF-03F-01 : Le système doit vérifier que le compte est en statut Actif (BR-02).
- RF-03F-02 : Le système doit vérifier que le montant est strictement positif (BR-03).
- RF-03F-03 : Le système doit vérifier que le solde est suffisant (BR-01).
- RF-03F-04 : Le système doit historiser l’opération (BR-07).

## 3. Données fonctionnelles manipulées
- compteId : UUID  
- montant : decimal(18,2)  
- type : DEBIT  
- état : INITIATED → VALIDATED  
- date : ISO 8601

## 4. Préconditions
- Le compte doit exister.
- Le compte doit être en statut Actif.
- Le montant doit être strictement positif.

## 5. Postconditions
- Le solde du compte est diminué.
- Une opération DEBIT est historisée.

## 6. Scénarios fonctionnels

### Nominal
- Input : compteId, montant positif.
- Vérification RF-03F-01, RF-03F-02, RF-03F-03.
- Mise à jour du solde.
- Historisation.

### Alternatif
- Solde insuffisant → erreur “Solde insuffisant”.

### Erreur
- Montant négatif → erreur “Montant invalide”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM |
|----------------|-----------|----|----|
| UC03F | UC03 | BR-03, BR-01, BR-02, BR-07 | EM-02, EM-01 |

---

# UC04F — Initier un virement

## 1. Objectif
Permettre au client d’initier un virement interne entre deux comptes distincts.

## 2. Règles fonctionnelles
- RF-04F-01 : Le système doit vérifier que le montant est strictement positif (BR-03).
- RF-04F-02 : Le système doit vérifier que le solde est suffisant (BR-05).
- RF-04F-03 : Le système doit vérifier que compteSource ≠ compteDestination (BR-10).
- RF-04F-04 : Le système doit vérifier que le compte destination n’est pas Fermé (BR-11).
- RF-04F-05 : Le système doit historiser le virement (BR-07).
- RF-04F-06 : Si montant ≤ seuil, le virement passe directement à COMPLETED (BR-08).

## 3. Données fonctionnelles manipulées
- compteSourceId : UUID  
- compteDestinationId : UUID  
- montant : decimal(18,2)  
- état : INITIATED → COMPLETED ou INITIATED → PENDING  
- date : ISO 8601

## 4. Préconditions
- Les deux comptes doivent exister.
- Le montant doit être strictement positif.
- Les statuts des comptes doivent permettre l’opération.

## 5. Postconditions
- Un virement est créé dans l’état approprié.
- Historisation effectuée.

## 6. Scénarios fonctionnels

### Nominal — montant ≤ seuil
- Vérification RF-04F-01 à RF-04F-04.
- Virement en COMPLETED (BR-08).
- Historisation.

### Alternatif — montant > seuil
- Vérification RF-04F-01 à RF-04F-04.
- Virement en PENDING.

### Alternatif — solde insuffisant
- Le solde du compte source est insuffisant.
- Le système renvoie l’erreur “Solde insuffisant”.

### Alternatif — compte destination fermé
- Le compte destination est en statut Fermé.
- Le système renvoie l’erreur “Compte destination fermé”.

### Erreur — auto-virement
- compteSource = compteDestination.
- Le système renvoie l’erreur “Auto-virement interdit”.

### Erreur — compte suspendu
- Le compte source est en statut Suspendu.
- Le système renvoie l’erreur “Compte suspendu”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM |
|----------------|-----------|----|----|
| UC04F | UC04 | BR-03, BR-05, BR-10, BR-11, BR-07, BR-08 | EM-03, EM-05, EM-01 |

---

# UC05F — Valider un virement

## 1. Objectif
Permettre à un agent habilité de valider ou rejeter un virement en attente.

## 2. Règles fonctionnelles
- RF-05F-01 : Le système doit vérifier que l’agent a le rôle SUPERVISEUR (BR-09).
- RF-05F-02 : Le système doit appliquer la validation ou le rejet.
- RF-05F-03 : Le système doit historiser la décision (BR-07).

## 3. Données fonctionnelles manipulées
- virementId : UUID  
- validatedBy : UUID  
- état : PENDING → COMPLETED ou PENDING → FAILED  
- dateValidation : ISO 8601

## 4. Préconditions
- Le virement doit exister.
- Le virement doit être en état PENDING.
- L’agent doit être SUPERVISEUR.

## 5. Postconditions
- Le virement est validé ou rejeté.
- Historisation effectuée.

## 6. Scénarios fonctionnels

### Nominal
- Agent SUPERVISEUR valide.
- Débit/crédit exécuté.
- Historisation.

### Alternatif
- Agent rejette → FAILED.

### Erreur
- Agent non habilité → erreur “Agent non autorisé”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM |
|----------------|-----------|----|----|
| UC05F | UC05 | BR-09, BR-07 | EM-04, EM-05 |

---

# UC06F — Consulter l’historique

## 1. Objectif
Permettre au client de consulter l’historique des opérations et virements d’un compte.

## 2. Règles fonctionnelles
- RF-06F-01 : Le système doit retourner l’ensemble des opérations historisées (BR-07).

## 3. Données fonctionnelles manipulées
- compteId : UUID  
- listeOpérations : array  
- listeVirements : array

## 4. Préconditions
- Le compte doit exister.

## 5. Postconditions
- L’historique est affiché.

## 6. Scénarios fonctionnels

### Nominal
- Le client consulte l’historique.
- Le système retourne les opérations et virements.

### Alternatif
- Aucun historique → liste vide.

### Erreur
- Compte inexistant → erreur “Compte introuvable”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM |
|----------------|-----------|----|----|
| UC06F | UC06 | BR-07 | EM-05 |