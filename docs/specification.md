# Spec v1 – Mini-application bancaire – Version initiale (CI basique)

### 

### 1. Contexte

Cette application est une **mini‑application bancaire pédagogique**, destinée à :

- illustrer une **architecture Java / Spring Boot** inspirée des systèmes bancaires,
- servir de support à la mise en place d’un **pipeline CI/CD DevSecOps** avec GitHub Actions,
- être présentée en **entretien** comme un exemple concret de :
    - conception d’architecture,
    - mise en œuvre de CI/CD,
    - prise en compte progressive de la sécurité.

Cette Spec v1 décrit la **première itération** du projet, volontairement limitée, qui servira de base aux versions suivantes (v2, v3, …).

### 2. Objectifs de la version v1

- Disposer d’un **projet Java 17 / Spring Boot minimal** qui :
    - compile,
    - expose un endpoint REST simple,
    - possède des tests unitaires.
- Mettre en place un **pipeline CI basique** avec GitHub Actions :
    - build Maven,
    - exécution des tests,
    - publication d’artefacts (JAR, rapports de tests),
    - utilisation du cache Maven.
- Poser les **fondations de l’architecture** (packages, couches) qui seront enrichies ensuite.

### 3. Périmètre fonctionnel v1

Fonctionnalités très limitées, purement pédagogiques :

- Exposer un endpoint REST :
    - `GET /api/health`
    - Réponse : un JSON simple, par exemple :
        
        json
        
        `{ "status": "UP", "version": "v1" }`
        
- Aucun vrai métier bancaire pour l’instant (pas encore de comptes, ni d’opérations).
- L’objectif est de **valider la structure du projet et la chaîne CI**, pas encore le métier.

### 4. Architecture logique v1

- **Couches prévues (même si certaines sont encore vides ou très simples) :**
    - `api` : controllers REST
    - `service` : logique métier (placeholder pour v1)
    - `domain` : entités métier (placeholder pour v1)
    - `infrastructure` : persistance, intégrations externes (non utilisé en v1)
- Pour v1 :
    - un seul controller REST dans la couche `api`,
    - un service minimal (ou directement dans le controller, mais la structure de packages doit déjà refléter la future architecture).

### 5. Architecture technique v1

- **Langage :** Java 17
- **Framework :** Spring Boot (version LTS compatible Java 17)
- **Build :** Maven
- **Packaging :** JAR exécutable (`spring-boot-maven-plugin`)
- **Base de données :** aucune en v1 (H2 ou PostgreSQL arrivera en v2/v3)
- **Configuration :**
    - `application.yml` minimal
    - gestion de la version de l’application via propriétés (ex : `app.version=v1`)

### 6. Exigences non fonctionnelles v1

- **Qualité minimale :**
    - tests unitaires pour le controller (ou service) exposant `/api/health`,
    - couverture de test non mesurée strictement en v1, mais au moins 1 test.
- **Lisibilité :**
    - code structuré par packages (`api`, `service`, `domain`, `infrastructure`),
    - nommage explicite.
- **Observabilité minimale :**
    - logs de démarrage Spring Boot,
    - logs simples sur l’appel de `/api/health` (optionnel en v1).

### 7. Exigences CI/CD v1 (GitHub Actions)

Objectif : **pipeline CI basique mais propre**.

- **Déclencheurs :**
    - `on: push` sur la branche principale (ex : `main`),
    - `on: pull_request` vers `main`.
- **Runners :**
    - GitHub‑hosted runners (`ubuntu-latest`).
- **Jobs :**
    1. **Build & Test**
        - checkout du code,
        - setup de Java 17,
        - cache Maven,
        - `mvn -B clean verify`,
        - publication des artefacts :
            - JAR généré,
            - rapports de tests (Surefire).
- **Artefacts :**
    - JAR de l’application,
    - rapports de tests (XML/HTML).
- **Objectifs pédagogiques CI :**
    - comprendre la structure d’un workflow,
    - manipuler jobs, steps, actions,
    - utiliser le cache Maven,
    - publier des artefacts.

### 8. Livrables v1

Dans le repo GitHub, la version v1 doit contenir :

- **Code :**
    - projet Maven Java 17 / Spring Boot,
    - endpoint `/api/health`,
    - au moins un test unitaire.
- **CI :**
    - fichier `.github/workflows/ci-basic.yml` (ou nom équivalent),
    - pipeline fonctionnel (build + tests + artefacts + cache).
- **Documentation :**
    - cette Spec v1 (par exemple `docs/spec-v1.md`),
    - un `README.md` qui :
        - présente le projet,
        - explique que v1 est la base d’un parcours DevSecOps,
        - mentionne le lien avec GitHub Actions.