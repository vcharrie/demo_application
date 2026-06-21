# CoreService – Version V3  
Service REST Spring Boot – Packaging Docker durci – Chaîne CI/CD sécurisée

## 1. Présentation

La version **V3** de CoreService est une évolution **technique** du projet, centrée sur :

- le durcissement du packaging Docker,
- la séparation stricte Build / Runtime,
- l’intégration complète des pratiques DevSecOps,
- la mise en place d’une chaîne CI/CD sécurisée,
- la gestion structurée des vulnérabilités (CVE + VEX),
- la montée de versions Spring Boot / Tomcat / Spring Security.

Aucune évolution fonctionnelle n’est introduite dans cette version.

---

## 2. Architecture

### 2.1. Architecture logicielle

- Application Spring Boot 3.4.x
- JAR exécutable : `CoreServiceApplication.jar`
- Endpoints :
  - `/actuator/health` (healthcheck)
  - `/api/health` (contrôleur applicatif)
- Tests unitaires Spring Web MVC (scope test)

### 2.2. Architecture technique

- Base image : `eclipse-temurin:21-jre` (digest épinglé)
- User non-root (UID 10001)
- Entrypoint exec : `["java", "-jar", "/app/CoreServiceApplication.jar"]`
- Healthcheck Docker : `/actuator/health`
- Labels OCI
- CI/CD GitHub Actions :
  - Build Maven
  - SAST (Checkstyle, PMD, SpotBugs)
  - SBOM (CycloneDX)
  - SCA (Trivy SBOM)
  - Build image Docker
  - Scan image Docker (Trivy)
  - Run container + smoke test
  - Push GHCR
  - Analyse SonarCloud

---

## 3. Build & Run

### 3.1. Build Maven

Le build Maven doit être exécuté **avant** le build Docker.

```
mvn clean package -DskipTests
```

Le JAR généré se trouve dans :

```
target/CoreServiceApplication.jar
```

### 3.2. Build Docker

```
docker build -t coreservice:v3 .
```

### 3.3. Run Docker

```
docker run -p 8080:8080 coreservice:v3
```

### 3.4. Vérification

```
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/health
```

---

## 4. Sécurité

### 4.1. SAST

Exécuté via Maven :

- Checkstyle
- PMD
- SpotBugs

### 4.2. SBOM

Généré via CycloneDX :

```
target/bom.json
```

### 4.3. SCA

Analyse des dépendances via Trivy :

```
trivy sbom bom.json
```

### 4.4. Scan image Docker

```
trivy image coreservice:v3
```

### 4.5. Gestion des vulnérabilités

- Fichier `.trivyignore` (VEX)
- Revue régulière des CVE
- Montées de versions Spring Boot / Tomcat / Spring Security

---

## 5. CI/CD

Pipeline GitHub Actions :

1. Checkout
2. Build Maven
3. SAST
4. SBOM
5. SCA
6. Build image Docker
7. Scan image Docker
8. Run container + smoke test
9. Cleanup
10. Push GHCR
11. Analyse SonarCloud

---

## 6. Limitations

- Le Dockerfile ne permet plus de compiler l’application.
- Le build Maven est obligatoire avant `docker build`.
- Le pipeline CI/CD bloque sur :
  - SAST,
  - SCA HIGH/CRITICAL,
  - scan image HIGH/CRITICAL,
  - Quality Gate SonarCloud.

---

## 7. Documentation associée

- Release Notes V3
- Spécification Technique V3
- Schémas d’architecture V3

---

## 8. État de la version

Version stable, sécurisée, conforme aux bonnes pratiques DevSecOps.  
Base solide pour la V4 (déploiement, readiness/liveness, secrets, ingress, 