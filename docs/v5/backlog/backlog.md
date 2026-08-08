# Backlog — CoreService (V1.2)
Backlog dérivé des UC fonctionnels définis dans `spec_fonctionnelle.md`.  
Traçabilité complète : EM → BR → UC métier → UC fonctionnel → Epic → US → CA.

---

# EPIC-01 — Gestion de compte
Capacité produit : permettre la création et l’existence d’un compte actif.

## US-01 — Créer un compte
En tant qu’agent, je veux créer un compte afin de permettre à un client de disposer d’un compte actif.

### Traçabilité
- EM : EM-02  
- BR : BR-07  
- UC métier : UC01  
- UC fonctionnel : UC01F  
- Epic : EPIC-01

### Critères d’acceptation
- CA-01.1 : La création échoue si les données du titulaire sont invalides.
- CA-01.2 : La création échoue si le titulaire existe déjà.
- CA-01.3 : Le compte est créé en statut Actif.
- CA-01.4 : Une entrée d’historique est créée.

---

# EPIC-02 — Opérations sur compte
Capacité produit : permettre les opérations simples sur un compte existant.

## US-02 — Dépôt
En tant que client, je veux déposer de l’argent afin d’augmenter mon solde.

### Traçabilité
- EM : EM-02  
- BR : BR-02, BR-03, BR-07  
- UC métier : UC02  
- UC fonctionnel : UC02F  
- Epic : EPIC-02

### Critères d’acceptation
- CA-02.1 : Le dépôt échoue si le montant ≤ 0.
- CA-02.2 : Le dépôt échoue si le compte est suspendu.
- CA-02.3 : Le solde est augmenté du montant.
- CA-02.4 : Une opération CREDIT est historisée.

---

## US-03 — Retrait
En tant que client, je veux retirer de l’argent afin de diminuer mon solde si celui-ci est suffisant.

### Traçabilité
- EM : EM-02, EM-01  
- BR : BR-02, BR-03, BR-01, BR-07  
- UC métier : UC03  
- UC fonctionnel : UC03F  
- Epic : EPIC-02

### Critères d’acceptation
- CA-03.1 : Le retrait échoue si le montant ≤ 0.
- CA-03.2 : Le retrait échoue si le compte est suspendu.
- CA-03.3 : Le retrait échoue si le solde est insuffisant.
- CA-03.4 : Le solde est diminué du montant.
- CA-03.5 : Une opération DEBIT est historisée.

---

# EPIC-03 — Virement interne
Capacité produit : permettre les virements internes simples et sensibles.

## US-04 — Initier un virement
En tant que client, je veux initier un virement interne afin de transférer de l’argent vers un autre compte.

### Traçabilité
- EM : EM-03, EM-05, EM-01  
- BR : BR-03, BR-05, BR-10, BR-11, BR-07, BR-08  
- UC métier : UC04  
- UC fonctionnel : UC04F  
- Epic : EPIC-03

### Critères d’acceptation
- CA-04.1 : Le virement échoue si le montant ≤ 0.
- CA-04.2 : Le virement échoue si compteSource = compteDestination.
- CA-04.3 : Le virement échoue si le compte source est suspendu.
- CA-04.4 : Le virement échoue si le solde est insuffisant.
- CA-04.5 : Le virement échoue si le compte destination est fermé.
- CA-04.6 : Si montant ≤ seuil, le virement est COMPLETED.
- CA-04.7 : Si montant > seuil, le virement est PENDING.
- CA-04.8 : Le virement est historisé.

---

## US-05 — Valider un virement
En tant qu’agent SUPERVISEUR, je veux valider un virement en attente afin de permettre ou refuser son exécution.

### Traçabilité
- EM : EM-04, EM-05  
- BR : BR-09, BR-07  
- UC métier : UC05  
- UC fonctionnel : UC05F  
- Epic : EPIC-03

### Critères d’acceptation
- CA-05.1 : La validation échoue si l’agent n’est pas SUPERVISEUR.
- CA-05.2 : La validation échoue si le virement n’est pas en état PENDING.
- CA-05.3 : Si APPROVE, le virement passe à COMPLETED.
- CA-05.4 : Si REJECT, le virement passe à FAILED.
- CA-05.5 : La décision est historisée.

---

# EPIC-04 — Consultation
Capacité produit : permettre la consultation de l’historique d’un compte.

## US-06 — Consulter l’historique
En tant que client, je veux consulter l’historique de mon compte afin de visualiser mes opérations et virements.

### Traçabilité
- EM : EM-05  
- BR : BR-07  
- UC métier : UC06  
- UC fonctionnel : UC06F  
- Epic : EPIC-04

### Critères d’acceptation
- CA-06.1 : L’historique contient les opérations et virements du compte.
- CA-06.2 : Si aucun historique, la liste est vide.
- CA-06.3 : Si le compte n’existe pas, une erreur est renvoyée.

---

# 10. Traçabilité complète EM → BR → UC → Epic → US

| EM | BR | UC métier | UC fonctionnel | Epic | US |
|----|----|-----------|----------------|------|----|
| EM-02 | BR-07 | UC01 | UC01F | EPIC-01 | US-01 |
| EM-02 | BR-02, BR-03, BR-07 | UC02 | UC02F | EPIC-02 | US-02 |
| EM-02, EM-01 | BR-02, BR-03, BR-01, BR-07 | UC03 | UC03F | EPIC-02 | US-03 |
| EM-03, EM-05, EM-01 | BR-03, BR-05, BR-10, BR-11, BR-07, BR-08 | UC04 | UC04F | EPIC-03 | US-04 |
| EM-04, EM-05 | BR-09, BR-07 | UC05 | UC05F | EPIC-03 | US-05 |
| EM-05 | BR-07 | UC06 | UC06F | EPIC-04 | US-06 |

---

# 11. Notes de périmètre V1
- Pas d’authentification (sera ajoutée en V2).  
- Pas d’autorisation RBAC (sera ajoutée en V2).  
- Pas de gestion avancée des transactions (verrouillage en V2).  
- Pas de CI/CD, Docker, Kubernetes (V3).  
- Pas d’architecture hexagonale (V2).  