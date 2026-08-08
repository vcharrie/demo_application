# Domain Design — Release v5

## 1. Objectifs

Le domaine représente le cœur métier de l’application.  
Il doit être :

- pur (sans dépendance à Spring, JPA, JSON, HTTP),
- auto‑cohérent,
- testable indépendamment,
- stable dans le temps,
- compatible ascendant,
- structuré autour des invariants métier.

Le domaine est la seule couche autorisée à définir les règles métier immuables.

---

## 2. Principes fondamentaux

### 2.1 Pure Domain Model

Le domaine ne dépend d’aucune technologie.  
Il ne contient :

- ni annotations Spring,
- ni annotations JPA,
- ni DTO,
- ni exceptions HTTP,
- ni logique d’infrastructure.

Il est composé uniquement de :

- entités métier,
- value objects,
- règles métier,
- invariants,
- exceptions métier (BusinessException).

---

### 2.2 Invariants métier

Les invariants sont des règles immuables qui définissent la validité d’un état métier.

Exemples :

- un retrait ne peut pas rendre le solde négatif,
- une opération doit avoir un montant strictement positif,
- un compte suspendu ne peut pas émettre d’opération,
- une opération doit être cohérente avec le type (débit ou crédit).

Les invariants sont contrôlés **dans les méthodes du domaine**, jamais dans les services applicatifs.

---

### 2.3 BusinessException

Les violations d’invariants lèvent une BusinessException.

Caractéristiques :

- levée dans le domaine,
- ne connaît pas HTTP,
- ne connaît pas Spring,
- ne connaît pas la base de données,
- ne contient que des informations métier.

Propagation :

- remontent au service applicatif,
- converties en FunctionalException.

Raison :

- le domaine ne doit jamais exposer directement une erreur API.

---

## 3. Structure du domaine

Le domaine est structuré autour de trois types d’objets :

### 3.1 Entities

Les entités représentent des objets métier avec identité.

Exemples :

- Account
- Operation

Caractéristiques :

- possèdent un identifiant métier,
- contiennent les invariants,
- contiennent les comportements métier,
- ne contiennent aucune logique d’infrastructure.

---

### 3.2 Value Objects

Les value objects représentent des concepts métier sans identité propre.

Exemples :

- Amount
- OperationType
- AccountStatus

Caractéristiques :

- immuables,
- validés à la construction,
- utilisés pour encapsuler des primitives (BigDecimal, String, etc.).

---

### 3.3 Domain Services (si nécessaire)

Utilisés uniquement si une règle métier ne peut pas être placée dans une entité.

Exemples :

- règle métier impliquant plusieurs entités,
- logique métier transversale.

Dans la version actuelle, les entités suffisent.

---

## 4. Règles métier dans le domaine

Les règles métier sont implémentées dans les méthodes des entités.

Exemple de logique métier dans Account :

    debit(amount):
        vérifier que amount > 0
        vérifier que solde - amount >= 0
        mettre à jour le solde
        créer une opération de type DEBIT

Exemple de logique métier dans Operation :

    validate():
        vérifier que le montant est strictement positif
        vérifier que le type est cohérent avec le signe du montant

Ces règles sont testées dans les tests unitaires du domaine.

---

## 5. Interaction avec la couche Application

Le domaine ne connaît pas la couche Application.

La couche Application :

- appelle les méthodes du domaine,
- convertit les BusinessException en FunctionalException,
- orchestre les opérations métier,
- gère les transactions,
- gère les erreurs techniques maîtrisées.

Le domaine reste totalement isolé.

---

## 6. Interaction avec la couche Infrastructure

Le domaine ne connaît pas l’infrastructure.

La couche Infrastructure :

- mappe Domain ↔ Entity,
- persiste les entités,
- charge les entités,
- ne contient aucune logique métier.

Le domaine ne doit jamais dépendre :

- d’une entité JPA,
- d’un repository,
- d’une annotation,
- d’un type technique.

---

## 7. Tests du domaine

Les tests du domaine sont des tests unitaires purs.

Ils couvrent :

- les invariants métier,
- les comportements métier,
- les value objects,
- les BusinessException,
- les transitions d’état.

Ils ne nécessitent :

- ni Spring,
- ni base de données,
- ni MockMvc,
- ni contexte d’application.

Ils garantissent la stabilité du modèle métier.

---

## 8. Compatibilité ascendante du domaine

Le domaine évolue avec le métier.  
Pour garantir la compatibilité ascendante :

- les invariants ne doivent jamais être supprimés sans migration,
- les entités doivent évoluer sans casser les comportements existants,
- les value objects doivent rester immuables,
- les BusinessException doivent être versionnées.

Chaque évolution du domaine doit être synchronisée avec :

- une migration de base de données,
- une mise à jour des mappers,
- une mise à jour des tests.

---

## 9. Synthèse

Le domaine est :

- pur,
- stable,
- isolé,
- auto‑cohérent,
- centré sur les invariants métier,
- indépendant de l’infrastructure,
- testé de manière exhaustive.

Il constitue la base solide sur laquelle repose toute l’architecture applicative.
