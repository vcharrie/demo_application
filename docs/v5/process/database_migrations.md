# Database Migrations — Release v5

## 1. Objectifs

La gestion des migrations de base de données vise à garantir :

- une évolution incrémentale du schéma,
- une compatibilité ascendante,
- une synchronisation stricte entre modèle métier et modèle persistant,
- une reproductibilité des environnements,
- une intégration fluide dans le CI/CD,
- une traçabilité complète des changements.

Les migrations sont gérées via Flyway.

---

## 2. Principes fondamentaux

### 2.1 Migration incrémentale

Chaque évolution du modèle métier doit être accompagnée d’un script de migration versionné :

- V1__init.sql
- V2__add_operation_table.sql
- V3__add_status_enum.sql
- V4__rename_column_amount.sql
- etc.

Les scripts sont immuables : une fois commités, ils ne doivent jamais être modifiés.

---

### 2.2 Compatibilité ascendante

Règles :

- ne jamais supprimer une colonne sans migration de données,
- ne jamais renommer une colonne sans script de migration,
- ne jamais changer un type sans conversion explicite,
- toujours prévoir une période de compatibilité ascendante,
- toujours synchroniser les évolutions du domaine avec les migrations.

---

### 2.3 Migration des données

Les migrations peuvent inclure :

- des valeurs par défaut,
- des backfills,
- des conversions de type,
- des normalisations.

Exemples :

    ALTER TABLE account ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
    UPDATE account SET status = 'ACTIVE' WHERE status IS NULL;

---

## 3. Structure des migrations

Les migrations sont stockées dans :

    src/main/resources/db/migration/

Chaque fichier suit la convention Flyway :

    V<version>__<description>.sql

Exemples :

    V1__init.sql
    V2__create_account_table.sql
    V3__create_operation_table.sql
    V4__add_account_status.sql

---

## 4. Synchronisation Domain ↔ Database

Chaque évolution du domaine doit être synchronisée avec :

- une migration Flyway,
- une mise à jour des entités JPA,
- une mise à jour des mappers,
- une mise à jour des tests d’intégration.

Le domaine est la source de vérité métier.  
La base de données est la représentation persistée.

---

## 5. Tests des migrations

Les migrations doivent être testées via :

### 5.1 Tests d’intégration

- démarrage SpringBootTest avec Flyway activé,
- vérification du schéma final,
- insertion de données,
- vérification de la compatibilité ascendante.

### 5.2 Tests de non-régression

- tests sur données existantes,
- tests sur schéma évolutif,
- tests sur migrations successives.

---

## 6. Migrations dans le CI/CD

Le pipeline CI doit :

- exécuter Flyway en mode "migrate" sur une base jetable,
- exécuter les tests d’intégration,
- valider la cohérence du schéma,
- refuser tout merge si une migration échoue.

Le pipeline CD doit :

- exécuter Flyway sur l’environnement cible,
- garantir l’ordre strict des migrations,
- garantir l’absence de modification des scripts historiques.

---

## 7. Migrations dans les environnements locaux

Au démarrage de l’application :

- Flyway applique automatiquement les migrations,
- Hibernate valide le schéma (ddl-auto=validate),
- aucune création automatique de schéma n’est autorisée.

Les développeurs doivent :

- pull les migrations,
- redémarrer l’application,
- vérifier la cohérence du schéma local.

---

## 8. Synthèse

La gestion des migrations repose sur :

- Flyway pour la versioning,
- Hibernate pour la validation du schéma,
- une synchronisation stricte Domain ↔ Database,
- une compatibilité ascendante,
- une intégration complète dans le CI/CD.

Elle garantit une base de données stable, évolutive et cohérente avec le modèle métier.
