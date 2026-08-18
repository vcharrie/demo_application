# Backlog — CoreService (V6)
Backlog dérivé de `spec_metier_v6.md` et `spec_fonctionnelle_v6.md`.  
Traçabilité : UC fonctionnel → Epic → User Story → Critères d’acceptation → BR → EM → Permission.

---

# EPIC-01 — Gestion des comptes (capacité produit)
Regroupe UC01F, UC02F, UC03F, UC06F.  
Chaîne de valeur : création, opérations simples, consultation.

---

## US-01 — Créer un compte
En tant qu’**agent**, je veux **créer un compte actif** pour un client afin de lui permettre d’utiliser le service.

### Critères d’acceptation
- **CA-01.1** : La création échoue si les données du titulaire sont invalides.
- **CA-01.2** : La création échoue si le titulaire existe déjà.
- **CA-01.3** : Le compte est créé en statut Actif.
- **CA-01.4** : Une entrée d’historique est créée.
- **CA-01.5** : La permission PERM_ACCOUNT_CREATE est vérifiée et tracée.

### Traçabilité
| US | UC fonctionnel | BR | EM | Permission |
|----|----------------|----|----|------------|
| US-01 | UC01F | BR-07, BR-12, BR-16 | EM-02, EM-06 | PERM_ACCOUNT_CREATE |

---

## US-02 — Effectuer un dépôt
En tant que **client**, je veux **déposer de l’argent** sur mon compte afin d’augmenter mon solde.

### Critères d’acceptation
- **CA-02.1** : Le dépôt échoue si le montant ≤ 0.
- **CA-02.2** : Le dépôt échoue si le compte est suspendu.
- **CA-02.3** : Le solde est augmenté du montant.
- **CA-02.4** : Une opération CREDIT est historisée.
- **CA-02.5** : La permission PERM_ACCOUNT_DEPOSIT est vérifiée et tracée.

### Traçabilité
| US | UC fonctionnel | BR | EM | Permission |
|----|----------------|----|----|------------|
| US-02 | UC02F | BR-03, BR-02, BR-07, BR-12, BR-16 | EM-02, EM-06 | PERM_ACCOUNT_DEPOSIT |

---

## US-03 — Effectuer un retrait
En tant que **client**, je veux **retirer de l’argent** afin de diminuer mon solde si celui-ci est suffisant.

### Critères d’acceptation
- **CA-03.1** : Le retrait échoue si le montant ≤ 0.
- **CA-03.2** : Le retrait échoue si le compte est suspendu.
- **CA-03.3** : Le retrait échoue si le solde est insuffisant.
- **CA-03.4** : Le solde est diminué du montant.
- **CA-03.5** : Une opération DEBIT est historisée.
- **CA-03.6** : La permission PERM_ACCOUNT_WITHDRAW est vérifiée et tracée.

### Traçabilité
| US | UC fonctionnel | BR | EM | Permission |
|----|----------------|----|----|------------|
| US-03 | UC03F | BR-03, BR-01, BR-02, BR-07, BR-12, BR-16 | EM-02, EM-01, EM-06 | PERM_ACCOUNT_WITHDRAW |

---

## US-04 — Consulter l’historique
En tant que **client**, je veux **consulter l’historique** de mon compte afin de visualiser mes opérations et virements.

### Critères d’acceptation
- **CA-04.1** : L’historique contient les opérations et virements du compte.
- **CA-04.2** : Si aucun historique, la liste est vide.
- **CA-04.3** : Si le compte n’existe pas, une erreur est renvoyée.
- **CA-04.4** : La permission PERM_ACCOUNT_HISTORY est vérifiée et tracée.

### Traçabilité
| US | UC fonctionnel | BR | EM | Permission |
|----|----------------|----|----|------------|
| US-04 | UC06F | BR-07, BR-12, BR-16 | EM-05, EM-06 | PERM_ACCOUNT_HISTORY |

---

# EPIC-02 — Gestion des virements internes (chaîne de valeur)
Regroupe UC04F et UC05F.  
Chaîne de valeur : initier → valider → exécuter.

---

## US-05 — Initier un virement
En tant que **client**, je veux **initier un virement interne** afin de transférer de l’argent vers un autre compte.

### Critères d’acceptation
- **CA-05.1** : Le virement échoue si le montant ≤ 0.
- **CA-05.2** : Le virement échoue si compteSource = compteDestination.
- **CA-05.3** : Le virement échoue si le compte source est suspendu.
- **CA-05.4** : Le virement échoue si le solde est insuffisant.
- **CA-05.5** : Le virement échoue si le compte destination est fermé.
- **CA-05.6** : Si montant ≤ seuil, le virement est COMPLETED.
- **CA-05.7** : Si montant > seuil, le virement est PENDING.
- **CA-05.8** : Le virement est historisé.
- **CA-05.9** : La permission PERM_TRANSFER_INITIATE est vérifiée et tracée.

### Traçabilité
| US | UC fonctionnel | BR | EM | Permission |
|----|----------------|----|----|------------|
| US-05 | UC04F | BR-03, BR-05, BR-10, BR-11, BR-07, BR-08, BR-12, BR-16 | EM-03, EM-05, EM-01, EM-06 | PERM_TRANSFER_INITIATE |

---

## US-06 — Valider un virement
En tant qu’**agent SUPERVISEUR**, je veux **valider ou rejeter un virement en attente** afin de permettre ou refuser son exécution.

### Critères d’acceptation
- **CA-06.1** : La validation échoue si l’agent n’est pas SUPERVISEUR.
- **CA-06.2** : La validation échoue si le virement n’est pas en état PENDING.
- **CA-06.3** : Si APPROVE, le virement passe à COMPLETED.
- **CA-06.4** : Si REJECT, le virement passe à FAILED.
- **CA-06.5** : La décision est historisée.
- **CA-06.6** : La permission PERM_TRANSFER_VALIDATE est vérifiée et tracée.

### Traçabilité
| US | UC fonctionnel | BR | EM | Permission |
|----|----------------|----|----|------------|
| US-06 | UC05F | BR-09, BR-07, BR-12, BR-16 | EM-04, EM-05, EM-06 | PERM_TRANSFER_VALIDATE |
