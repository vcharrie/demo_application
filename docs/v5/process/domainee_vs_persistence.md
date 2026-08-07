# DOMAIN_VS_PERSISTENCE — Release v8
Guide complet Domain ↔ Persistence pour architectures hexagonales

---

## 1. Objectif du document

Ce document clarifie :

- pourquoi le modèle métier (Domain) et le modèle de persistence (JPA) ne doivent pas être identiques,
- dans quels cas il est souhaitable qu’ils soient différents,
- quels problèmes concrets ces différences résolvent,
- comment persister proprement un modèle métier profond (A→B→C→D→E),
- quels sont les patterns Domain ↔ Persistence,
- ce que sont réellement les agrégats, avec exemples,
- quelles questions se poser avant de concevoir la persistence,
- quelles stratégies de chargement et de transaction adopter.

---

## 2. Domain vs Persistence : deux modèles, deux responsabilités

### 2.1 Modèle métier (Domain)

Rôle :

- représenter le business,
- exprimer les invariants métier,
- porter les règles métier,
- être indépendant de la persistence.

Caractéristiques :

- pas d’annotations JPA,
- pas de contraintes SQL,
- entités + value objects + services métier,
- invariants exprimés dans le code.

### 2.2 Modèle de persistence (JPA)

Rôle :

- représenter la structure de la base,
- optimiser les requêtes,
- gérer les relations, les index, les contraintes.

Caractéristiques :

- annotations JPA,
- types adaptés à la DB,
- relations optimisées,
- contraintes techniques.

---

## 3. Cas où le modèle JPA doit être différent du modèle métier

### 3.1 Relations N‑N complexes (A ↔ B)

Exemple métier :

User ↔ Group

Problème si JPA = Domain :

- cycles,
- lazy loading infini,
- cascade dangereuse,
- suppression impossible.

Solution JPA :

- introduction d’une entité de jointure `UserGroupLink` (userId, groupId),
- relations unidirectionnelles.

---

### 3.2 Modèle métier profond (A→B→C→D→E)

Exemple métier :

Project → Module → Component → Feature → Task

Problème si JPA = Domain :

- lazy loading en cascade,
- N+1 queries,
- graphes monstrueux,
- cycles potentiels.

Solution JPA : relations par IDs

- `ProjectEntity { List<Long> moduleIds }`
- `ModuleEntity { List<Long> componentIds }`
- `ComponentEntity { List<Long> featureIds }`
- `FeatureEntity { List<Long> taskIds }`

---

### 3.3 Optimisation des lectures (read‑model)

Exemple :

Domain :

- `Order { List<OrderLine>, Money total }`

Read‑model :

- `OrderSummaryEntity { orderId, total, lineCount }`

---

### 3.4 Dénormalisation

Exemple :

Domain :

- `Customer { Address address }`

DB optimisée :

- `CustomerEntity { street, city, zip, countryCode }`

---

### 3.5 Persistence événementielle

Exemple :

Domain :

- `AccountCreated`, `MoneyDeposited`, `MoneyWithdrawn`

Persistence :

- `EventEntity { type, payloadJson, timestamp }`

---

## 4. Les 5 patterns Domain ↔ Persistence

### Pattern 1 — Entity Flattening  
Aplatissement des Value Objects

- Domain : `Address { Street street; City city; ZipCode zip }`
- JPA : `CustomerEntity { String street; String city; String zip }`
- Mapping : Domain → JPA (aplatissement), JPA → Domain (reconstruction des VO).

---

### Pattern 2 — Reference by ID  
Relations par IDs pour maîtriser les graphes profonds

- Domain : `Project → Module → Component → Feature → Task`
- JPA : `ProjectEntity { List<Long> moduleIds }`, etc.
- Service : chargement explicite des entités via leurs IDs.

---

### Pattern 3 — Join Entity (N‑N)  
Entité de jointure pour les N‑N

- Domain : `User ↔ Role`
- JPA : `UserRoleLink { Long userId; Long roleId }`
- Service : `roleIds = linkRepo.findRoleIdsByUserId(userId)` puis `roles = roleRepo.findAllById(roleIds)`.

---

### Pattern 4 — Aggregate Flattening  
Aplatissement d’un agrégat métier

- Domain : `Order { List<OrderLine> lines; Money total }`
- JPA : `OrderEntity { total }`, `OrderLineEntity { orderId, price }`.

---

### Pattern 5 — Event Store  
Persistence des événements métier

- Domain : `OrderPlaced`, `OrderCancelled`
- JPA : `EventEntity { type, jsonPayload }`.

---

## 5. Anti‑patterns Domain ↔ Persistence

- N‑N bidirectionnel JPA,
- relations profondes EAGER,
- domaine annoté JPA,
- entités JPA utilisées comme modèle métier,
- cascade ALL sur des graphes complexes.

---

## 6. Coûts assumés du double modèle

Coûts :

- double mapping,
- duplication apparente,
- coût CPU de reconstruction.

Bénéfices :

- domaine pur,
- persistence flexible,
- évolutivité forte.

---

## 7. Ce qui se passerait avec un seul modèle JPA

- domaine dépendant de la DB,
- invariants mélangés aux contraintes techniques,
- modèle dicté par la base,
- évolutivité faible.

---

## 8. Références bibliographiques

- Eric Evans — Domain-Driven Design  
- Vaughn Vernon — Implementing Domain-Driven Design  
- Alistair Cockburn — Hexagonal Architecture  
- Robert C. Martin — Clean Architecture  
- Vlad Mihalcea — High-Performance Java Persistence  
- Martin Fowler — Event Sourcing  
- Greg Young — CQRS  

---

## 9. Questions à se poser avant de concevoir la persistence

### Questions sur le domaine

- Quels sont les agrégats ?
- Quels sont les invariants métier ?
- Quelles entités doivent être chargées ensemble ?
- Quelles entités doivent être chargées séparément ?

### Questions sur les relations

- Relations profondes ?
- Relations N‑N ?
- Relations bidirectionnelles ?
- Relations par IDs ?

### Questions sur les performances

- Use cases de lecture ?
- Use cases d’écriture ?
- Projections nécessaires ?
- Read‑models nécessaires ?

### Questions sur les transactions

- Granularité ?
- Un agrégat par transaction ?
- Transactions distribuées ?
- Batch / streaming ?

### Questions sur la suppression

- Soft delete ou hard delete ?
- Cascade ou suppression contrôlée ?

### Questions sur l’évolution

- Schéma DB stable ou mouvant ?
- Domaine stable ?
- Event Sourcing prévu ?

---

## 10. Stratégies de chargement et de transaction

### Stratégies de chargement

- Chargement par profondeur contrôlée (pattern Reference by ID),
- Projections,
- Read‑models.

### Stratégies de transaction

- Transaction par agrégat,
- Transaction par use case,
- Transactions longues (batch),
- Transactions distribuées (à éviter).

---

## 11. Agrégats — Explication claire + exemples

### Définition

Un agrégat est :

- un groupe d’objets métier,
- soumis à un invariant métier commun,
- contrôlé par une racine (Aggregate Root),
- chargé et modifié uniquement via cette racine.

### Exemple : Order

Domaine :

- `Order { List<OrderLine> lines; Money total; void addLine(OrderLine line) { ... } }`

Invariant métier :

- `total = somme des lignes`

Conséquences :

- on ne modifie jamais une `OrderLine` seule,
- on ne charge jamais une `OrderLine` seule,
- toutes les modifications passent par `Order`.

---

## 12. Synthèse finale

Avec :

- les 5 patterns Domain ↔ Persistence,
- les anti‑patterns,
- les stratégies de read,
- les stratégies de transaction,
- les questions d’architecture,
- les agrégats expliqués,
- les exemples,

tu couvres plus de 80% des questions qui se posent pour mettre en œuvre une persistence propre dans une architecture hexagonale.
