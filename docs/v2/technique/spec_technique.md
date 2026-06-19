\# 📘 SPÉCIFICATION TECHNIQUE — VERSION 2 (V2) — HOMOGÉNÉISÉE

\*\*Application :\*\* CoreService  
\*\*Version :\*\* V2  
\*\*Date :\*\* 2026‑06‑11  
\*\*Auteur :\*\* Vincent  

---

\# 1. 🎯 Objet du document

La présente spécification technique décrit l’architecture logicielle, les composants techniques, les mécanismes internes, le packaging Docker, la chaîne CI/CD et les endpoints exposés de la version \*\*V2\*\* de l’application CoreService.

Elle constitue la \*\*référence technique\*\* de la release V2.

---

\# 2. 🏗️ Architecture technique

\## 2.1. Architecture générale

L’application suit une \*\*architecture hexagonale simplifiée\*\*, organisée en trois couches :

\- \*\*API (Entrée)\*\*  
  Contrôleurs REST, DTO, mappers API.

\- \*\*Domaine (Métier)\*\*  
  Entité métier \`Resource\`, règles métier, exceptions métier, interface de service.

\- \*\*Infrastructure (Sortie)\*\*  
  Repository en mémoire, entité de stockage, mappers infrastructure.

Aucune persistance réelle n’est utilisée en V2.

\## 2.2. Architecture d’exécution (Runtime)

L’application est exécutée dans un \*\*container Docker léger\*\*, construit via un \*\*Dockerfile multi‑stage\*\* :

\- \*\*Stage Build\*\*  
  \- Image : \`maven:3.9.6-eclipse-temurin-21\`  
  \- Compilation Maven  
  \- Production du JAR  
  \- Non utilisé au runtime  
  \- Non poussé dans GHCR  

\- \*\*Stage Runtime (image finale)\*\*  
  \- Image : \`eclipse-temurin:21-jre\`  
  \- Contient uniquement : JRE 21 + \`app.jar\`  
  \- Expose le port 8080  
  \- Image poussée dans GHCR  
  \- Image exécutée dans le job deploy

\## 2.3. Architecture CI/CD

La chaîne CI/CD repose sur \*\*GitHub Actions\*\*, avec deux jobs :

\- \*\*build\*\*  
  Compilation Maven, build Docker multi‑stage, push GHCR.

\- \*\*deploy\*\*  
  Pull GHCR, exécution du container, smoke test \`/actuator/health\`.

\## 2.4. Registry

Les images Docker sont stockées dans \*\*GitHub Container Registry (GHCR)\*\*.

Flux global :

\`\`\`
Code → Build Maven → Build Docker multi-stage → Push GHCR → Pull GHCR → Run container
\`\`\`

---

\# 3. ⚙️ Composants techniques

\## 3.1. API REST

Localisation : \`src/main/java/com/coreservice/api/\`

\- \*\*ResourceController\*\*  
  Expose les endpoints \`/resources\`.

\- \*\*HealthController\*\*  
  Expose \`/api/health\`.

\- \*\*GlobalExceptionHandler\*\*  
  Convertit les exceptions en réponses HTTP \*\*texte brut\*\*.

\## 3.2. DTO

Localisation : \`api/dto/\`

\- \`ResourceRequest\`  
\- \`ResourceResponse\`

\## 3.3. Mappers API

Localisation : \`api/mapper/\`

\- \`ResourceApiMapper\`  
  Convertit DTO ↔ domaine.

\## 3.4. Domaine

Localisation : \`domain/\`

\- \`Resource\`  
\- \`ResourceService\`  
\- Exceptions :  
  \- \`ResourceNotFoundException\`  
  \- \`ResourceConflictException\`

\## 3.5. Application (Service)

Localisation : \`application/\`

\- \`ResourceServiceImpl\`  
  Implémente les règles métier.

\## 3.6. Infrastructure

Localisation : \`infrastructure/\`

\- \`ResourceEntity\`  
\- \`ResourceMapper\`  
\- \`ResourceRepository\` (stockage en mémoire)

---

\# 4. 🐳 Dockerfile multi‑stage

Localisation : \`Dockerfile\`

\## 4.1. Stage Build

\- Image : \`maven:3.9.6-eclipse-temurin-21\`  
\- Compile l’application  
\- Produit \`target/*.jar\`  
\- Non utilisé au runtime  
\- Non poussé dans GHCR

\## 4.2. Stage Runtime

\- Image : \`eclipse-temurin:21-jre\`  
\- Copie du JAR depuis le stage Build  
\- Expose port 8080  
\- Contient uniquement JRE + app.jar  
\- Image finale poussée dans GHCR  
\- Image exécutée dans le job deploy

---

\# 5. 🚀 CI/CD GitHub Actions

Localisation : \`.github/workflows/ci.yml\`

\## 5.1. Job build

\- Checkout  
\- Setup JDK 21  
\- Cache Maven  
\- \`mvn clean verify\`  
\- Build Docker multi‑stage  
\- Login GHCR  
\- Push image \`ghcr.io/<user>/coreservice:latest\`

\## 5.2. Job deploy

\- Login GHCR  
\- Pull image  
\- Run container  
\- Smoke test \`/actuator/health\`  
\- Stop container

\## 5.3. Image utilisée

\- Build → image finale  
\- Push → image finale  
\- Pull → image finale  
\- Run → image finale  

Le stage Build n’est jamais exécuté ni poussé.

---

\# 6. 🌐 Endpoints REST (rappel fonctionnel minimal)

\## 6.1. GET /api/health
\- Retourne un message texte.  
\- Version renvoyée : \*\*V2\*\*.

\## 6.2. GET /resources
\- Retourne une liste JSON.  
\- Peut retourner \`[]\`.

\## 6.3. GET /resources/{id}
\- Retourne la ressource.  
\- Sinon : 404 texte brut.

\## 6.4. DELETE /resources/{id}
\- Supprime la ressource.  
\- Sinon : 404 texte brut.

\## 6.5. POST /resources
\- \*\*Non fonctionnel en V2\*\*  
\- Retourne systématiquement 500.

---

\# 7. ❗ Gestion des erreurs

\## 7.1. GlobalExceptionHandler

\- Réponses \*\*texte brut\*\*  
\- Pas de JSON  
\- Pas de format standardisé

\## 7.2. Erreurs connues

\- \`ResourceNotFoundException\` → 404  
\- \`ResourceConflictException\` → 409  
\- Erreurs internes → 500

---

\# 8. 🗄️ Stockage et persistance

\## 8.1. Repository en mémoire

\- Stockage via Map interne  
\- Données perdues à chaque redémarrage  
\- Pas de JPA  
\- Pas de base de données

\## 8.2. Identifiants

\- Aucun mécanisme de génération automatique  
\- Le domaine exige un id non null → cause du bug POST

---

\# 9. 🧪 Tests techniques

\## 9.1. Tests unitaires

\- \`ResourceTest\`  
\- \`ResourceServiceImplTest\`  
\- \`ResourceRepositoryTest\`

\## 9.2. Tests d’intégration

\- \`ResourceControllerIT\`  
\- \`ResourceRepositoryIT\`  
\- \`HealthControllerTest\`

\## 9.3. Résultats observés

\- GET /resources → OK  
\- GET /resources/{id inconnu} → 404  
\- DELETE /resources/{id inconnu} → 404  
\- POST /resources → 500 systématique

---

\# 10. 🧱 Limitations techniques de la V2

\- Pas de persistance réelle  
\- Pas de génération d’identifiants  
\- Pas de format d’erreur JSON  
\- Pas de sécurité Spring Security  
\- Pas de sanitizing des logs  
\- Pas de validation avancée  
\- POST non fonctionnel  
\- Pas de pagination / tri / filtrage  
\- Pas de documentation OpenAPI  
\- Pas de signature d’image Docker  
\- Pas de provenance SLSA  
\- Pas de scan d’image  
\- Pas de contrôle d’intégrité GHCR

---

\# 11. 🔭 Éléments prévus pour la V3

\- Spring Security  
\- Sanitizing des logs  
\- Format d’erreur JSON standardisé  
\- Génération automatique d’identifiants  
\- Persistance JPA  
\- Correction du POST  
\- Documentation OpenAPI  
\- Durcissement global  
\- Signature d’image (Cosign)  
\- Provenance SLSA  
\- Scan d’image automatisé  
\- Admission controller