# Spécification Technique — CoreService (V5)
Architecture technique conforme au SDLC sécurisé défini dans la V5.  
Les contrôles de sécurité ne sont pas décrits ici : ils sont référencés via les fiches SEC‑xxx.

---

# 0. Configuration métier

Le seuil de validation des virements (BR‑06, BR‑08) est configurable.

application.yml :
core:
  transfer:
    validation-threshold: 1000.00

Injection Spring :
@Value("${core.transfer.validation-threshold}")
BigDecimal validationThreshold;

Références sécurité :
- Validation des montants : SEC‑VAL‑01  
- Workflow des virements : SEC‑WORKFLOW‑01  

---

# 1. Modèle de données (entités JPA)

## 1.1 Compte
@Entity
class Account {
    UUID id;
    UUID titulaireId;
    BigDecimal solde;
    AccountStatus statut; // ACTIF, SUSPENDU, FERME
}

Références sécurité :
- Statut ACTIF obligatoire pour opérations : SEC‑AUTHZ‑01  
- Protection contre double dépense : SEC‑TXN‑01  

## 1.2 Operation
@Entity
class Operation {
    UUID id;
    UUID compteId;
    OperationType type; // DEBIT, CREDIT
    BigDecimal montant;
    Instant date;
    OperationState etat; // INITIATED, VALIDATED, REJECTED
}

Références sécurité :
- Montant valide : SEC‑VAL‑01  
- Immutabilité des opérations validées : SEC‑IMMUT‑01  
- Historisation obligatoire : SEC‑AUDIT‑01  

## 1.3 Transfer
@Entity
class Transfer {
    UUID id;
    UUID compteSourceId;
    UUID compteDestinationId;
    BigDecimal montant;
    Instant date;
    TransferState etat; // INITIATED, PENDING, COMPLETED, FAILED
    UUID validatedBy;
}

Références sécurité :
- Comptes distincts : SEC‑VAL‑02  
- Statut du compte destination : SEC‑VAL‑03  
- Validation agent SUPERVISEUR : SEC‑AUTHZ‑02  
- Historisation : SEC‑AUDIT‑01  
- Workflow strict : SEC‑WORKFLOW‑01  

---

# 2. DTO (entrées/sorties REST)

Les DTO ne contiennent aucun contrôle de sécurité.  
Les validations sont appliquées dans les services (références SEC‑xxx).

---

# 3. Endpoints REST

Chaque endpoint implémente un UC fonctionnel et applique les mesures SEC‑xxx correspondantes.

## UC01F — Créer un compte
POST /accounts  
- Validation des données : SEC‑VAL‑01  
- Historisation : SEC‑AUDIT‑01  

## UC02F — Dépôt
POST /accounts/{id}/deposit  
- Montant positif : SEC‑VAL‑01  
- Compte ACTIF : SEC‑AUTHZ‑01  
- Historisation : SEC‑AUDIT‑01  

## UC03F — Retrait
POST /accounts/{id}/withdraw  
- Montant positif : SEC‑VAL‑01  
- Solde suffisant : SEC‑LOGIC‑01  
- Compte ACTIF : SEC‑AUTHZ‑01  
- Historisation : SEC‑AUDIT‑01  

## UC04F — Initier un virement
POST /transfers  
- Montant positif : SEC‑VAL‑01  
- Comptes distincts : SEC‑VAL‑02  
- Solde suffisant : SEC‑LOGIC‑01  
- Statut destination valide : SEC‑VAL‑03  
- Workflow strict : SEC‑WORKFLOW‑01  
- Historisation : SEC‑AUDIT‑01  

## UC05F — Valider un virement
POST /transfers/{id}/validate  
- Rôle SUPERVISEUR : SEC‑AUTHZ‑02  
- Workflow strict : SEC‑WORKFLOW‑01  
- Historisation : SEC‑AUDIT‑01  

## UC06F — Consulter l’historique
GET /accounts/{id}/history  
- Historisation obligatoire : SEC‑AUDIT‑01  

---

# 4. Services applicatifs

Les services appliquent les règles métier et les mesures de sécurité SEC‑xxx.

## 4.1 AccountService

### createAccount()
- Validation des données : SEC‑VAL‑01  
- Historisation : SEC‑AUDIT‑01  

### deposit()
- Montant > 0 : SEC‑VAL‑01  
- Compte ACTIF : SEC‑AUTHZ‑01  
- Historisation : SEC‑AUDIT‑01  
- Transaction ACID : SEC‑TXN‑01  

### withdraw()
- Montant > 0 : SEC‑VAL‑01  
- Solde suffisant : SEC‑LOGIC‑01  
- Compte ACTIF : SEC‑AUTHZ‑01  
- Historisation : SEC‑AUDIT‑01  
- Transaction ACID : SEC‑TXN‑01  

### getHistory()
- Historisation obligatoire : SEC‑AUDIT‑01  

---

## 4.2 TransferService

### initiateTransfer()
- Montant > 0 : SEC‑VAL‑01  
- Comptes distincts : SEC‑VAL‑02  
- Solde suffisant : SEC‑LOGIC‑01  
- Statut destination valide : SEC‑VAL‑03  
- Workflow strict : SEC‑WORKFLOW‑01  
- Historisation : SEC‑AUDIT‑01  
- Transaction ACID : SEC‑TXN‑01  

### validateTransfer()
- Rôle SUPERVISEUR : SEC‑AUTHZ‑02  
- Workflow strict : SEC‑WORKFLOW‑01  
- Historisation : SEC‑AUDIT‑01  
- Transaction ACID : SEC‑TXN‑01  

---

# 5. Repositories

Aucun contrôle de sécurité ici.  
Les validations sont appliquées dans les services.

---

# 6. Gestion des erreurs

Les erreurs fonctionnelles sont mappées aux mesures SEC‑xxx.

| Erreur | Code | Mesure |
|--------|------|--------|
| Montant invalide | 400 | SEC‑VAL‑01 |
| Auto‑virement | 400 | SEC‑VAL‑02 |
| Compte suspendu | 403 | SEC‑AUTHZ‑01 |
| Agent non autorisé | 403 | SEC‑AUTHZ‑02 |
| Solde insuffisant | 409 | SEC‑LOGIC‑01 |
| Compte fermé | 409 | SEC‑VAL‑03 |
| Virement non PENDING | 409 | SEC‑WORKFLOW‑01 |
| Compte introuvable | 404 | SEC‑AUDIT‑01 |

---

# 7. Transactions (@Transactional)

Les transactions garantissent l’intégrité métier et technique.

## 7.1 Double dépense
Risque : RISK‑LOG‑02  
Mesure : SEC‑TXN‑01  
Statut : risque résiduel accepté en V5 (ARB‑005)

## 7.2 Validation non autorisée
Risque : RISK‑AUTHZ‑02  
Mesure : SEC‑AUTHZ‑02  
Statut : corrigé en V5

---

# 8. États techniques

Mapping métier → technique conforme au workflow sécurisé (SEC‑WORKFLOW‑01).

---

# 9. Points d’extension V6

- Verrou optimiste (SEC‑TXN‑01)  
- Authentification (Spring Security)  
- Autorisation RBAC complète  
- Architecture hexagonale  

---

# 10. Conformité sécurité

Cette spec technique est conforme au formalisme V5 :  
**RISK → ARB → SEC → Implémentation**

Toutes les mesures de sécurité sont référencées via SEC‑xxx et traçables dans la matrice V5.