# Spécification Fonctionnelle — CoreService (V6)

Ce document décrit les UC fonctionnels, dérivés 1:1 des UC métier définis dans `spec_metier_v6.md`.  
Les exigences fonctionnelles (EF) sont fusionnées dans chaque UC fonctionnel.  
Aucune notion technique n’est introduite ici.  
Les permissions métier (RBAC) sont intégrées comme préconditions fonctionnelles.

---

# UC01F — Créer un compte

## 1. Objectif
Permettre à un agent de créer un compte actif pour un client, avec solde initial.

## 2. Règles fonctionnelles
- RF-01F-01 : Le système doit vérifier que l’acteur possède la permission PERM_ACCOUNT_CREATE (BR-12).
- RF-01F-02 : Le système doit vérifier que les données du titulaire sont valides.
- RF-01F-03 : Le système doit créer le compte en statut Actif.
- RF-01F-04 : Le système doit historiser la création du compte (BR-07).
- RF-01F-05 : Le système doit tracer la permission utilisée (BR-16).

## 3. Données fonctionnelles manipulées
- titulaireId : UUID  
- soldeInitial : decimal(18,2)  
- statut : Actif  
- dateCreation : ISO 8601  
- permissionUtilisée : PERM_ACCOUNT_CREATE

## 4. Préconditions
- L’acteur doit posséder PERM_ACCOUNT_CREATE.
- Le titulaire doit exister ou être créé dans le système.
- Les données du titulaire doivent être valides.

## 5. Postconditions
- Un compte actif est créé.
- Une entrée d’historique est ajoutée.
- La permission utilisée est tracée.

## 6. Scénarios fonctionnels

### Nominal
- L’agent possède PERM_ACCOUNT_CREATE.
- Le système valide les données.
- Le compte est créé en statut Actif.
- Historisation + traçabilité de la permission.

### Alternatif
- Titulaire déjà existant → erreur “Titulaire déjà enregistré”.

### Erreur
- Données invalides → erreur “Données invalides”.
- Permission manquante → erreur “Permission insuffisante”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM | Permission |
|----------------|-----------|----|----|------------|
| UC01F | UC01 | BR-07, BR-12, BR-16 | EM-02, EM-06 | PERM_ACCOUNT_CREATE |

---

# UC02F — Effectuer un dépôt

## 1. Objectif
Permettre au client d’ajouter un montant positif au solde d’un compte actif.

## 2. Règles fonctionnelles
- RF-02F-01 : Vérifier que l’acteur possède PERM_ACCOUNT_DEPOSIT (BR-12).
- RF-02F-02 : Vérifier que le compte est en statut Actif (BR-02).
- RF-02F-03 : Vérifier que le montant est strictement positif (BR-03).
- RF-02F-04 : Historiser l’opération (BR-07).
- RF-02F-05 : Tracer la permission utilisée (BR-16).

## 3. Données fonctionnelles manipulées
- compteId : UUID  
- montant : decimal(18,2)  
- type : CREDIT  
- état : INITIATED → VALIDATED  
- date : ISO 8601  
- permissionUtilisée : PERM_ACCOUNT_DEPOSIT

## 4. Préconditions
- L’acteur doit posséder PERM_ACCOUNT_DEPOSIT.
- Le compte doit exister.
- Le compte doit être en statut Actif.
- Le montant doit être strictement positif.

## 5. Postconditions
- Le solde du compte est augmenté.
- Une opération CREDIT est historisée.
- La permission utilisée est tracée.

## 6. Scénarios fonctionnels

### Nominal
- Vérification RF-02F-01 à RF-02F-03.
- Mise à jour du solde.
- Historisation + traçabilité.

### Alternatif
- Compte suspendu → erreur “Compte suspendu”.

### Erreur
- Montant non numérique → erreur “Montant invalide”.
- Permission manquante → “Permission insuffisante”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM | Permission |
|----------------|-----------|----|----|------------|
| UC02F | UC02 | BR-03, BR-02, BR-07, BR-12, BR-16 | EM-02, EM-06 | PERM_ACCOUNT_DEPOSIT |

---

# UC03F — Effectuer un retrait

## 1. Objectif
Permettre au client de retirer un montant si le solde est suffisant.

## 2. Règles fonctionnelles
- RF-03F-01 : Vérifier que l’acteur possède PERM_ACCOUNT_WITHDRAW (BR-12).
- RF-03F-02 : Vérifier que le compte est en statut Actif (BR-02).
- RF-03F-03 : Vérifier que le montant est strictement positif (BR-03).
- RF-03F-04 : Vérifier que le solde est suffisant (BR-01).
- RF-03F-05 : Historiser l’opération (BR-07).
- RF-03F-06 : Tracer la permission utilisée (BR-16).

## 3. Données fonctionnelles manipulées
- compteId : UUID  
- montant : decimal(18,2)  
- type : DEBIT  
- état : INITIATED → VALIDATED  
- date : ISO 8601  
- permissionUtilisée : PERM_ACCOUNT_WITHDRAW

## 4. Préconditions
- L’acteur doit posséder PERM_ACCOUNT_WITHDRAW.
- Le compte doit exister.
- Le compte doit être en statut Actif.
- Le montant doit être strictement positif.

## 5. Postconditions
- Le solde du compte est diminué.
- Une opération DEBIT est historisée.
- La permission utilisée est tracée.

## 6. Scénarios fonctionnels

### Nominal
- Vérification RF-03F-01 à RF-03F-04.
- Mise à jour du solde.
- Historisation + traçabilité.

### Alternatif
- Solde insuffisant → erreur “Solde insuffisant”.

### Erreur
- Montant négatif → erreur “Montant invalide”.
- Permission manquante → “Permission insuffisante”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM | Permission |
|----------------|-----------|----|----|------------|
| UC03F | UC03 | BR-03, BR-01, BR-02, BR-07, BR-12, BR-16 | EM-02, EM-01, EM-06 | PERM_ACCOUNT_WITHDRAW |

---

# UC04F — Initier un virement

## 1. Objectif
Permettre au client d’initier un virement interne entre deux comptes distincts.

## 2. Règles fonctionnelles
- RF-04F-01 : Vérifier que l’acteur possède PERM_TRANSFER_INITIATE (BR-12).
- RF-04F-02 : Vérifier que le montant est strictement positif (BR-03).
- RF-04F-03 : Vérifier que le solde est suffisant (BR-05).
- RF-04F-04 : Vérifier que compteSource ≠ compteDestination (BR-10).
- RF-04F-05 : Vérifier que le compte destination n’est pas Fermé (BR-11).
- RF-04F-06 : Historiser le virement (BR-07).
- RF-04F-07 : Tracer la permission utilisée (BR-16).
- RF-04F-08 : Si montant ≤ seuil → COMPLETED (BR-08).

## 3. Données fonctionnelles manipulées
- compteSourceId : UUID  
- compteDestinationId : UUID  
- montant : decimal(18,2)  
- état : INITIATED → COMPLETED ou INITIATED → PENDING  
- date : ISO 8601  
- permissionUtilisée : PERM_TRANSFER_INITIATE

## 4. Préconditions
- L’acteur doit posséder PERM_TRANSFER_INITIATE.
- Les deux comptes doivent exister.
- Le montant doit être strictement positif.
- Les statuts des comptes doivent permettre l’opération.

## 5. Postconditions
- Un virement est créé dans l’état approprié.
- Historisation + traçabilité.

## 6. Scénarios fonctionnels

### Nominal — montant ≤ seuil
- Vérification RF-04F-01 à RF-04F-05.
- Virement COMPLETED.
- Historisation + traçabilité.

### Alternatif — montant > seuil
- Vérification RF-04F-01 à RF-04F-05.
- Virement PENDING.

### Alternatif — solde insuffisant
- Erreur “Solde insuffisant”.

### Alternatif — compte destination fermé
- Erreur “Compte destination fermé”.

### Erreur — auto-virement
- Erreur “Auto-virement interdit”.

### Erreur — compte suspendu
- Erreur “Compte suspendu”.

### Erreur — permission manquante
- Erreur “Permission insuffisante”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM | Permission |
|----------------|-----------|----|----|------------|
| UC04F | UC04 | BR-03, BR-05, BR-10, BR-11, BR-07, BR-08, BR-12, BR-16 | EM-03, EM-05, EM-01, EM-06 | PERM_TRANSFER_INITIATE |

---

# UC05F — Valider un virement

## 1. Objectif
Permettre à un agent habilité de valider ou rejeter un virement en attente.

## 2. Règles fonctionnelles
- RF-05F-01 : Vérifier que l’acteur possède PERM_TRANSFER_VALIDATE (BR-12).
- RF-05F-02 : Vérifier que l’acteur a le rôle SUPERVISEUR (BR-09).
- RF-05F-03 : Appliquer la validation ou le rejet.
- RF-05F-04 : Historiser la décision (BR-07).
- RF-05F-05 : Tracer la permission utilisée (BR-16).

## 3. Données fonctionnelles manipulées
- virementId : UUID  
- validatedBy : UUID  
- état : PENDING → COMPLETED ou PENDING → FAILED  
- dateValidation : ISO 8601  
- permissionUtilisée : PERM_TRANSFER_VALIDATE

## 4. Préconditions
- L’acteur doit posséder PERM_TRANSFER_VALIDATE.
- L’acteur doit être SUPERVISEUR.
- Le virement doit exister.
- Le virement doit être en état PENDING.

## 5. Postconditions
- Le virement est validé ou rejeté.
- Historisation + traçabilité.

## 6. Scénarios fonctionnels

### Nominal
- Agent SUPERVISEUR valide.
- Débit/crédit exécuté.
- Historisation + traçabilité.

### Alternatif
- Agent rejette → FAILED.

### Erreur
- Agent non habilité → “Agent non autorisé”.
- Permission manquante → “Permission insuffisante”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM | Permission |
|----------------|-----------|----|----|------------|
| UC05F | UC05 | BR-09, BR-07, BR-12, BR-16 | EM-04, EM-05, EM-06 | PERM_TRANSFER_VALIDATE |

---

# UC06F — Consulter l’historique

## 1. Objectif
Permettre au client de consulter l’historique des opérations et virements d’un compte.

## 2. Règles fonctionnelles
- RF-06F-01 : Vérifier que l’acteur possède PERM_ACCOUNT_HISTORY (BR-12).
- RF-06F-02 : Retourner l’ensemble des opérations historisées (BR-07).
- RF-06F-03 : Tracer la permission utilisée (BR-16).

## 3. Données fonctionnelles manipulées
- compteId : UUID  
- listeOpérations : array  
- listeVirements : array  
- permissionUtilisée : PERM_ACCOUNT_HISTORY

## 4. Préconditions
- L’acteur doit posséder PERM_ACCOUNT_HISTORY.
- Le compte doit exister.

## 5. Postconditions
- L’historique est affiché.
- La permission utilisée est tracée.

## 6. Scénarios fonctionnels

### Nominal
- Le client consulte l’historique.
- Le système retourne les opérations et virements.
- Traçabilité de la permission.

### Alternatif
- Aucun historique → liste vide.

### Erreur
- Compte inexistant → “Compte introuvable”.
- Permission manquante → “Permission insuffisante”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM | Permission |
|----------------|-----------|----|----|------------|
| UC06F | UC06 | BR-07, BR-12, BR-16 | EM-05, EM-06 | PERM_ACCOUNT_HISTORY |

---

# UC-SEC01F — Vérifier une permission métier

## 1. Objectif
Permettre au système de vérifier qu’un acteur possède une permission métier.

## 2. Règles fonctionnelles
- RF-SEC01F-01 : Vérifier que la permission demandée existe (BR-14).
- RF-SEC01F-02 : Vérifier que la permission n’est pas implicite (BR-15).
- RF-SEC01F-03 : Déterminer si l’acteur possède la permission (BR-12).

## 3. Données fonctionnelles manipulées
- acteurId : UUID  
- permissionDemandée : string  
- résultat : bool

## 4. Préconditions
- La permission doit exister dans le référentiel métier.

## 5. Postconditions
- Le système retourne vrai/faux.

## 6. Scénarios fonctionnels

### Nominal
- Permission existante → résultat vrai/faux.

### Erreur
- Permission inexistante → “Permission inconnue”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM |
|----------------|-----------|----|----|
| UC-SEC01F | UC-SEC01 | BR-12, BR-14, BR-15 | EM-06 |

---

# UC-SEC02F — Vérifier un rôle métier

## 1. Objectif
Permettre au système de vérifier qu’un acteur possède un rôle métier.

## 2. Règles fonctionnelles
- RF-SEC02F-01 : Vérifier que le rôle existe.
- RF-SEC02F-02 : Déterminer si l’acteur possède le rôle (BR-13).

## 3. Données fonctionnelles manipulées
- acteurId : UUID  
- rôleDemandé : string  
- résultat : bool

## 4. Préconditions
- Le rôle doit exister dans le référentiel métier.

## 5. Postconditions
- Le système retourne vrai/faux.

## 6. Scénarios fonctionnels

### Nominal
- Rôle existant → résultat vrai/faux.

### Erreur
- Rôle inexistant → “Rôle inconnu”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM |
|----------------|-----------|----|----|
| UC-SEC02F | UC-SEC02 | BR-13 | EM-07 |

---

# UC-SEC03F — Tracer une permission utilisée

## 1. Objectif
Historiser la permission utilisée lors d’un cas d’usage métier.

## 2. Règles fonctionnelles
- RF-SEC03F-01 : Historiser la permission utilisée (BR-16).

## 3. Données fonctionnelles manipulées
- acteurId : UUID  
- permission : string  
- date : ISO 8601

## 4. Préconditions
- La permission doit être valide.

## 5. Postconditions
- Une entrée d’historique est ajoutée.

## 6. Scénarios fonctionnels

### Nominal
- Historisation effectuée.

### Erreur
- Permission inconnue → “Permission inconnue”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM |
|----------------|-----------|----|----|
| UC-SEC03F | UC-SEC03 | BR-16 | EM-05, EM-06 |

---

# UC-SEC04F — Administrer un rôle métier

## 1. Objectif
Permettre à un superviseur de créer, modifier ou supprimer un rôle métier.

## 2. Règles fonctionnelles
- RF-SEC04F-01 : Vérifier que l’acteur possède les permissions d’administration des rôles (BR-17).
- RF-SEC04F-02 : Vérifier que le rôle est valide.
- RF-SEC04F-03 : Appliquer la création, modification ou suppression.
- RF-SEC04F-04 : Historiser l’action.

## 3. Données fonctionnelles manipulées
- rôleId : UUID  
- permissions : array  
- action : CREATE / UPDATE / DELETE  
- date : ISO 8601

## 4. Préconditions
- L’acteur doit être SUPERVISEUR.

## 5. Postconditions
- Le rôle est créé, modifié ou supprimé.
- Historisation effectuée.

## 6. Scénarios fonctionnels

### Nominal
- Action effectuée.

### Erreur
- Rôle invalide → “Rôle invalide”.

## 7.

# UC-SEC04F — Administrer un rôle métier

## 1. Objectif
Permettre à un superviseur de créer, modifier ou supprimer un rôle métier.

## 2. Règles fonctionnelles
- RF-SEC04F-01 : Vérifier que l’acteur possède les permissions d’administration des rôles (BR-17).
- RF-SEC04F-02 : Vérifier que le rôle est valide.
- RF-SEC04F-03 : Appliquer la création, modification ou suppression du rôle.
- RF-SEC04F-04 : Historiser l’action (BR-07).
- RF-SEC04F-05 : Tracer la permission utilisée (BR-16).

## 3. Données fonctionnelles manipulées
- rôleId : UUID  
- nomRôle : string  
- permissions : array  
- action : CREATE / UPDATE / DELETE  
- date : ISO 8601  
- permissionUtilisée : PERM_RBAC_ROLE_CREATE / PERM_RBAC_ROLE_UPDATE / PERM_RBAC_ROLE_DELETE

## 4. Préconditions
- L’acteur doit être SUPERVISEUR.
- L’acteur doit posséder la permission correspondant à l’action :
  - CREATE → PERM_RBAC_ROLE_CREATE  
  - UPDATE → PERM_RBAC_ROLE_UPDATE  
  - DELETE → PERM_RBAC_ROLE_DELETE

## 5. Postconditions
- Le rôle est créé, modifié ou supprimé.
- Une entrée d’historique est ajoutée.
- La permission utilisée est tracée.

## 6. Scénarios fonctionnels

### Nominal
- Le superviseur effectue l’action demandée.
- Le rôle est créé / mis à jour / supprimé.
- Historisation + traçabilité.

### Alternatif
- Rôle déjà existant (CREATE) → “Rôle déjà existant”.
- Rôle inexistant (UPDATE/DELETE) → “Rôle introuvable”.

### Erreur
- Permission manquante → “Permission insuffisante”.
- Rôle invalide → “Rôle invalide”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR | EM | Permission |
|----------------|-----------|----|----|------------|
| UC-SEC04F | UC-SEC04 | BR-17, BR-07, BR-16 | EM-07, EM-08 | PERM_RBAC_ROLE_CREATE / UPDATE / DELETE |

---

# UC-SEC05F — Administrer une permission métier

## 1. Objectif
Permettre à un superviseur de créer, modifier ou supprimer une permission métier.

## 2. Règles fonctionnelles
- RF-SEC05F-01 : Vérifier que l’acteur possède les permissions d’administration des permissions (BR-18).
- RF-SEC05F-02 : Vérifier que la permission est valide.
- RF-SEC05F-03 : Appliquer la création, modification ou suppression de la permission.
- RF-SEC05F-04 : Historiser l’action (BR-07).
- RF-SEC05F-05 : Tracer la permission utilisée (BR-16).

## 3. Données fonctionnelles manipulées
- permissionId : UUID  
- nomPermission : string  
- domaine : string  
- action : CREATE / UPDATE / DELETE  
- date : ISO 8601  
- permissionUtilisée : PERM_RBAC_PERMISSION_CREATE / PERM_RBAC_PERMISSION_UPDATE / PERM_RBAC_PERMISSION_DELETE

## 4. Préconditions
- L’acteur doit être SUPERVISEUR.
- L’acteur doit posséder la permission correspondant à l’action :
  - CREATE → PERM_RBAC_PERMISSION_CREATE  
  - UPDATE → PERM_RBAC_PERMISSION_UPDATE  
  - DELETE → PERM_RBAC_PERMISSION_DELETE

## 5. Postconditions
- La permission est créée, modifiée ou supprimée.
- Une entrée d’historique est ajoutée.
- La permission utilisée est tracée.

## 6. Scénarios fonctionnels

### Nominal
- Le superviseur effectue l’action demandée.
- La permission est créée / mise à jour / supprimée.
- Historisation + traçabilité.

### Alternatif
- Permission déjà existante (CREATE) → “Permission déjà existante”.
- Permission inexistante (UPDATE/DELETE) → “Permission introuvable”.

### Erreur
- Permission manquante → “Permission insuffisante”.
- Permission invalide → “Permission invalide”.

## 7. Traçabilité
| UC fonctionnel | UC métier | BR mobilisées                      | EM couvertes              | Permissions |
|----------------|-----------|------------------------------------|---------------------------|-------------|
| UC01F          | UC01      | BR-07, BR-12, BR-16               | EM-02, EM-06              | PERM_ACCOUNT_CREATE |
| UC02F          | UC02      | BR-03, BR-02, BR-07, BR-12, BR-16 | EM-02, EM-06              | PERM_ACCOUNT_DEPOSIT |
| UC03F          | UC03      | BR-03, BR-01, BR-02, BR-07, BR-12, BR-16 | EM-02, EM-01, EM-06 | PERM_ACCOUNT_WITHDRAW |
| UC04F          | UC04      | BR-03, BR-05, BR-10, BR-11, BR-07, BR-08, BR-12, BR-16 | EM-03, EM-05, EM-01, EM-06 | PERM_TRANSFER_INITIATE |
| UC05F          | UC05      | BR-09, BR-07, BR-12, BR-16        | EM-04, EM-05, EM-06       | PERM_TRANSFER_VALIDATE |
| UC06F          | UC06      | BR-07, BR-12, BR-16               | EM-05, EM-06              | PERM_ACCOUNT_HISTORY |
| UC-SEC01F      | UC-SEC01  | BR-12, BR-14, BR-15               | EM-06                     | — |
| UC-SEC02F      | UC-SEC02  | BR-13                             | EM-07                     | — |
| UC-SEC03F      | UC-SEC03  | BR-16                             | EM-05, EM-06              | — |
| UC-SEC04F      | UC-SEC04  | BR-17, BR-07, BR-16               | EM-07, EM-08              | PERM_RBAC_ROLE_* |
| UC-SEC05F      | UC-SEC05  | BR-18, BR-07, BR-16               | EM-07, EM-08              | PERM_RBAC_PERMISSION_* |
