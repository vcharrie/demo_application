# Architecture Applicative — Release v5

## 1. Vision globale

L’architecture repose sur une approche hexagonale / clean architecture adaptée au contexte Spring Boot.  
Elle garantit :

- séparation stricte des responsabilités,
- évolutivité du modèle métier,
- compatibilité ascendante du schéma de base de données,
- testabilité complète (unitaires, intégration, métier),
- industrialisation CI/CD,
- cloud readiness (Docker, Kubernetes, EKS).

L’application est structurée en quatre couches :

API Layer → Application Layer → Domain Layer → Infrastructure Layer

Chaque couche a des responsabilités clairement définies et non chevauchantes.

---

## 2. Architecture logique

### 2.1 API Layer (REST + Validation + DTO)

Responsabilités :

- Exposer les endpoints REST.
- Valider les entrées (Bean Validation).
- Convertir les types primitifs (UUID, BigDecimal, enums).
- Ne contient aucun mapping DTO ↔ Domain.
- Ne contient aucune logique métier.
- Ne contient aucune logique d’orchestration.

Principes :

- Le contrôleur ne fait que : recevoir → valider → appeler le service → renvoyer la réponse DTO.
- Le contrôleur ne connaît pas le domaine.
- Le contrôleur ne connaît pas les entités JPA.
- Le contrôleur ne connaît pas les invariants métier.

---

### 2.2 Application Layer (Services applicatifs + Mappers API ↔ Domain)

Responsabilités :

- Orchestration métier.
- Gestion des transactions.
- Appels aux repositories.
- Gestion des erreurs fonctionnelles.
- Application des règles métier non invariantes.
- Mapping DTO ↔ Domain.
- Conversion Domain ↔ DTO pour la couche API.

Principes :

- Le service applicatif est le point d’entrée du domaine.
- Il isole la couche API du domaine.
- Il isole le domaine de l’infrastructure.
- Il convertit les erreurs techniques en erreurs fonctionnelles.
- Le mapping API ↔ Domain est centralisé dans cette couche.

---

### 2.3 Domain Layer (Modèle métier + invariants)

Responsabilités :

- Définition des entités métier.
- Définition des invariants métier (règles immuables).
- Logique métier pure.
- Pas de dépendance à Spring ou à l’infrastructure.
- Pas de DTO.
- Pas de JPA.
- Pas de JSON.

Principes :

- Le domaine est auto‑cohérent.
- Le domaine est testé indépendamment.
- Le domaine est indépendant de la base de données.

Exemples d’invariants :

- Un retrait ne peut pas créer un solde négatif.
- Une opération doit avoir un montant strictement positif.
- Un compte suspendu ne peut pas émettre d’opération.

---

### 2.4 Infrastructure Layer (Persistence + ORM + Repositories)

Responsabilités :

- ORM (JPA/Hibernate).
- Mapping Domain ↔ Entity.
- Repositories.
- Configuration technique (datasource, migrations).
- Implémentations concrètes des ports.

Principes :

- L’infrastructure ne contient aucune logique métier.
- Elle persiste, charge, mappe.
- Elle est interchangeable.

---

## 3. Gestion des erreurs

### 3.1 Erreurs fonctionnelles (métier)

Les erreurs métier sont levées via `FunctionalException` et converties en JSON structuré.

### 3.2 Erreurs techniques

Gérées par le `ControllerAdvice` :

- `MethodArgumentTypeMismatchException` → UUID invalide → 400
- `ConstraintViolationException` → validation → 400
- `DataIntegrityViolationException` → violation DB → 409
- `IllegalStateException` → erreur interne → 500

### 3.3 Erreurs lors d’un update DB

Certaines opérations nécessitent un `try/catch` global dans le service pour convertir une erreur technique en erreur fonctionnelle et éviter les 500 non maîtrisés.

---

## 4. Gestion incrémentale du schéma de base de données

### 4.1 Migrations incrémentales

Chaque évolution du modèle métier doit être accompagnée d’une migration Flyway ou Liquibase.

### 4.2 Compatibilité ascendante

Règles :

- ne jamais supprimer une colonne sans migration de données,
- ne jamais renommer une colonne sans script de migration,
- ne jamais changer un type sans conversion,
- toujours prévoir une période de compatibilité ascendante.

### 4.3 Migration des données

Exemples :

- ajout d’un champ `status` → valeur par défaut `ACTIVE`,
- ajout d’un champ `created_at` → backfill avec `NOW()`.

### 4.4 Alignement modèle métier ↔ schéma

Chaque modification du domaine doit être documentée, migrée, testée et validée en intégration.

---

## 5. Tests

### 5.1 Tests unitaires

- Domain (invariants)
- Services applicatifs (logique métier)
- Mappers

### 5.2 Tests d’intégration

- `@SpringBootTest`
- `MockMvc`
- Base H2 réelle
- Repositories réels
- Tests de sécurité
- Tests de validation
- Tests de mapping d’erreurs

### 5.3 Tests de non-régression

- ajout systématique lors de chaque évolution métier,
- couverture des cas limites,
- couverture des erreurs fonctionnelles.

### 5.4 Tests de compatibilité ascendante

- tests sur migrations Flyway,
- tests sur données existantes,
- tests sur schéma évolutif.

---

## 6. Industrialisation & Cloud

### 6.1 CI/CD

- build Maven
- tests unitaires + intégration
- build Docker
- push registry
- déploiement automatisé

### 6.2 Qualité

- SonarQube
- Checkstyle
- couverture de tests
- analyse des dépendances

### 6.3 Sécurité

- Basic Auth → JWT → RBAC
- scanning des dépendances
- secrets management

### 6.4 Cloud readiness

- Docker
- Kubernetes
- EKS
- readiness/liveness probes
- autoscaling
- logs structurés

---

## 7. Synthèse

Cette architecture garantit robustesse, évolutivité, testabilité, industrialisation, compatibilité ascendante, sécurité et cloud readiness.  
Elle est adaptée à un produit métier complexe, vivant, évolutif, destiné à un environnement cloud moderne.
