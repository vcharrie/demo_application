# Spécification Technique – Version V2

## 1. Objet du document

La présente spécification technique décrit l’architecture logicielle, les composants techniques, les mécanismes internes, la chaîne de build et la chaîne de livraison de la version V2 de l’application CoreService.

Elle constitue la référence technique pour la release V2.

---

## 2. Architecture technique

### 2.1. Architecture générale

L’application suit une **architecture hexagonale simplifiée**, structurée en trois couches :

- **API (Entrée)**  
  Contrôleurs REST, DTO, mappers API.

- **Domaine (Métier)**  
  Entité Resource, règles métier, exceptions métier, interface de service.

- **Infrastructure (Sortie)**  
  Repository en mémoire, entité de stockage, mappers infrastructure.

Aucune persistance réelle n’est utilisée en V2.

### 2.2. Architecture d’exécution (Runtime)

L’application est exécutée dans un **container Docker léger**, construit via un **Dockerfile multi‑stage** :

- **Stage Build** : compilation Maven (image lourde, non utilisée au runtime).  
- **Stage Runtime** : image finale légère (JRE 21 + app.jar).

### 2.3. Architecture CI/CD

La chaîne CI/CD repose sur **GitHub Actions**, avec deux jobs :

- **build** : compilation Maven, build Docker multi‑stage, push GHCR.  
- **deploy** : pull GHCR, exécution du container, smoke test.

### 2.4. Registry

Les images Docker sont stockées dans **GitHub Container Registry (GHCR)**.

Flux :

```
Code → Build Maven → Build Docker multi-stage → Push GHCR → Pull GHCR → Run container
```

---

## 3. Composants techniques

### 3.1. API REST

Localisation :  
`src/main/java/com/coreservice/api/`

Composants :

- **ResourceController**  
  Expose les endpoints REST `/resources`.

- **HealthController**  
  Expose `/api/health`.

- **GlobalExceptionHandler**  
  Convertit les exceptions en réponses HTTP **en texte brut**.

### 3.2. DTO (Data Transfer Objects)

Localisation :  
`api/dto/`

- `ResourceRequest`  
- `ResourceResponse`

### 3.3. Mappers API

Localisation :  
`api/mapper/`

- `ResourceApiMapper`  
  Convertit DTO ↔ modèle domaine.

### 3.4. Domaine

Localisation :  
`domain/`

- `Resource` (entité métier)  
- `ResourceService` (interface)  
- Exceptions métier :  
  - `ResourceNotFoundException`  
  - `ResourceConflictException`

### 3.5. Application (implémentation du service)

Localisation :  
`application/`

- `ResourceServiceImpl`  
  Implémente les opérations métier en s’appuyant sur le repository.

### 3.6. Infrastructure

Localisation :  
`infrastructure/`

- **Entity** : `ResourceEntity`  
- **Mapper** : `ResourceMapper`  
- **Repository** : `ResourceRepository` (stockage en mémoire)

---

## 4. Dockerfile multi‑stage (V2)

Localisation :  
`Dockerfile`

### 4.1. Stage 1 — Build

- Image : `maven:3.9.6-eclipse-temurin-21`  
- Compile l’application  
- Produit `target/*.jar`  
- **Non utilisé au runtime**  
- **Non poussé dans GHCR**

### 4.2. Stage 2 — Runtime (image finale)

- Image : `eclipse-temurin:21-jre`  
- Copie uniquement le JAR depuis le stage Build  
- Expose le port 8080  
- Contient uniquement :  
  - JRE 21  
  - `app.jar`  
- **Image finale poussée dans GHCR**  
- **Image exécutée dans le job deploy**

---

## 5. CI/CD GitHub Actions

Localisation :  
`.github/workflows/ci.yml`

### 5.1. Job build

Étapes principales :

- Checkout du code  
- Setup JDK 21  
- Cache Maven  
- `mvn clean verify`  
- Build Docker multi‑stage  
- Login GHCR  
- Push image :  
  `ghcr.io/<user>/coreservice:latest`

### 5.2. Job deploy

Étapes principales :

- Login GHCR  
- Pull image :  
  `docker pull ghcr.io/<user>/coreservice:latest`
- Run container  
- Smoke test `/actuator/health`  
- Stop container

### 5.3. Image utilisée dans le pipeline

- **Build** → image finale = stage Runtime  
- **Push** → image finale  
- **Pull** → image finale  
- **Run** → image finale

Le stage Build n’est jamais exécuté ni poussé.

---

## 6. Endpoints REST (niveau technique)

### 6.1. GET /api/health
- Retourne un message texte.  
- Version renvoyée : **V2**.

### 6.2. GET /resources
- Retourne une liste JSON de ResourceResponse.  
- Peut retourner `[]`.

### 6.3. GET /resources/{id}
- Retourne une ressource si elle existe.  
- Sinon : 404 + texte brut.

### 6.4. DELETE /resources/{id}
- Supprime la ressource si elle existe.  
- Sinon : 404 + texte brut.

### 6.5. POST /resources
- **Non fonctionnel en V2**.  
- Retourne systématiquement une erreur interne (500).

---

## 7. Gestion des erreurs

### 7.1. GlobalExceptionHandler

- Retourne des réponses **en texte brut**.  
- Pas de format JSON.  
- Pas de structure d’erreur standardisée.

### 7.2. Erreurs techniques connues

- `ResourceNotFoundException` → 404  
- `ResourceConflictException` → 409  
- Erreurs internes → 500

---

## 8. Stockage et persistance

### 8.1. Repository en mémoire

- Implémenté via une structure interne (Map).  
- Les données sont perdues à chaque redémarrage.  
- Pas de persistance JPA.  
- Pas de base de données.

### 8.2. Identifiants

- Aucun mécanisme de génération automatique d’identifiants.  
- Le domaine exige un id non null → cause du bug POST.

---

## 9. Tests techniques

### 9.1. Tests unitaires

- `ResourceTest`  
- `ResourceServiceImplTest`  
- `ResourceRepositoryTest`

### 9.2. Tests d’intégration

- `ResourceControllerIT`  
- `ResourceRepositoryIT`  
- `HealthControllerTest`

### 9.3. Résultats observés

- GET /resources → OK  
- GET /resources/{id inconnu} → 404 texte brut  
- DELETE /resources/{id inconnu} → 404 texte brut  
- POST /resources → **500 systématique**

---

## 10. Limitations techniques de la V2

- Pas de persistance réelle.  
- Pas de génération d’identifiants.  
- Pas de format d’erreur JSON.  
- Pas de sécurité Spring Security.  
- Pas de sanitizing des logs.  
- Pas de validation avancée.  
- POST non fonctionnel.  
- Pas de pagination, tri ou filtrage.  
- Pas de documentation OpenAPI.  
- Pas de signature d’image Docker.  
- Pas de provenance SLSA.  
- Pas de scan d’image dans le pipeline.  
- Pas de contrôle d’intégrité GHCR.

---

## 11. Éléments prévus pour la V3 (non présents en V2)

- Ajout de Spring Security.  
- Ajout du sanitizing des logs.  
- Ajout du format d’erreur JSON standardisé.  
- Ajout de la génération automatique d’identifiants.  
- Ajout de la persistance réelle (JPA).  
- Correction du POST.  
- Documentation OpenAPI.  
- Durcissement global de l’architecture.  
- Signature des images Docker (Cosign).  
- Provenance SLSA.  
- Scan d’image automatisé.  
- Admission controller 