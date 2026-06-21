# Release Notes – CoreService V2

## 1. Résumé de la version
La version V2 de CoreService introduit une première évolution majeure de l’application :

- passage d’un simple healthcheck à une architecture applicative en couches
- introduction d’un modèle métier Resource
- ajout d’un service CRUD minimaliste
- containerisation complète en local et dans le pipeline CI/CD
- aucune mesure de sécurité nouvelle (prévue en V3)

---

## 2. Nouveautés

### 2.1. Architecture applicative
- Introduction d’une architecture en couches :
  - API (Controllers + ExceptionHandler)
  - Application (ServiceImpl)
  - Domaine (entité métier + exceptions + interface de service)
  - Infrastructure (repository en mémoire + mapper + entité de stockage)

### 2.2. Modèle métier
- Ajout de l’entité métier `Resource`
- Ajout des exceptions métier :
  - `ResourceNotFoundException`
  - `ResourceConflictException`

### 2.3. Services exposés
- Nouveau contrôleur `ResourceController`
- Exposition d’un CRUD minimaliste :
  - `GET /api/resources`
  - `GET /api/resources/{id}`
  - `POST /api/resources`
  - `PUT /api/resources/{id}`
  - `DELETE /api/resources/{id}`

### 2.4. Gestion centralisée des erreurs
- Ajout du `GlobalExceptionHandler`
- Format JSON homogène pour les erreurs applicatives

---

## 3. Changements

### 3.1. Health applicatif
- Mise à jour de la version renvoyée :
- `{"version": "v2", "status": "UP"}`

### 3.2. Packaging
- Le JAR Spring Boot reste identique, mais il est désormais intégré dans une image Docker runtime.

---

## 4. Containerisation

### 4.1. Docker multi-stage
- Ajout d’un Dockerfile multi-stage :
  - Stage 1 : build Maven
  - Stage 2 : image runtime JRE 21

### 4.2. Exécution locale
- L’application s’exécute désormais dans un container Docker via Docker Desktop.

### 4.3. CI/CD GitHub Actions
- Le pipeline CI/CD :
  - build Maven
  - build Docker multi-stage
  - push de l’image dans GHCR
  - exécution d’un smoke test dans un container

### 4.4. Registry GHCR
- Publication automatique de l’image `coreservice:latest` dans GHCR.

---

## 5. Corrections
Aucune correction fonctionnelle majeure.  
La V2 est principalement une extension de la V1.

---

## 6. Limitations
- Le repository est en mémoire (pas de persistance).
- Le CRUD est minimaliste (pas de validation avancée).
- Pas de sécurité applicative (prévue en V3).
- Pas de durcissement Docker (prévu en V3).
- Pas de SAST / supply chain security (prévu en V3).

---

## 7. Sécurité
Aucune nouvelle mesure de sécurité en V2.  
Les fiches sécurité applicables restent :

- SEC-CORE-01-V1
- SEC-APP-02

Les mesures de sécurité CI/CD, SAST, durcissement image, headers HTTP, validation d’entrée, sanitizing logs, etc. seront introduites en V3.

---

## 8. Compatibilité
- Compatible Windows 10/11 + Docker Desktop
- Compatible GitHub Actions (runner Ubuntu)
- Compatible GHCR

---

## 9. Points d’attention pour V3
- Sécurisation applicative (validation, sanitizing, headers)
- Sécurisation pipeline (SAST, provenance, durcissement image)
- Introduction de Sealed Secrets + overlays Kubernetes
- Déploiement sur cluster local (kind / Docker 