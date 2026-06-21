# Release Notes – CoreService V3

## 1. Résumé de la version

La version **V3** de CoreService est une évolution technique majeure centrée sur :

- le durcissement du packaging (image Docker sécurisée),
- la mise en place d’une chaîne CI/CD complète et sécurisée,
- l’intégration des mécanismes DevSecOps (SAST, SBOM, SCA, scan image),
- la gestion structurée des vulnérabilités (CVE + VEX),
- la montée de versions Spring Boot / Tomcat / Spring Security.

Aucune évolution fonctionnelle n’est introduite dans cette version.

---

## 2. Nouveautés principales

### 2.1. Packaging Docker durci

- Suppression du multi‑stage build.
- Build Maven **externe** au Dockerfile.
- Base image : `eclipse-temurin:21-jre` (digest épinglé).
- User non‑root (UID 10001).
- Healthcheck `/actuator/health`.
- Entrypoint en mode exec.
- Labels OCI ajoutés.
- Image finale plus légère, plus sécurisée et conforme aux fiches sécurité.

### 2.2. Chaîne CI/CD V3

Pipeline GitHub Actions entièrement refondu :

- Build Maven (`mvn clean verify`).
- SAST : Checkstyle, PMD, SpotBugs.
- SBOM : CycloneDX (`bom.json`).
- SCA : Trivy SBOM (HIGH/CRITICAL).
- Build image Docker.
- Scan image Docker (Trivy).
- Exécution du container en CI (healthcheck + smoke test).
- Cleanup (stop & remove).
- Push GHCR (digest immuable).
- Analyse SonarCloud (Quality Gate).

### 2.3. Gestion des vulnérabilités (CVE)

- Processus CVE structuré (détection → qualification → décision → suivi).
- Mise en place du fichier `.trivyignore` (VEX).
- Revue régulière des vulnérabilités ignorées.
- Montées de versions Spring Boot / Tomcat / Spring Security.

---

## 3. Changements techniques

### 3.1. Build local

Le build local nécessite désormais deux étapes :

- `mvn clean package -DskipTests`
- `docker build -t coreservice:local .`

Un simple `docker build` ne fonctionne plus (suppression du multi‑stage).

### 3.2. Versions techniques

- Java : 21 (Temurin JRE)
- Spring Boot : 3.4.x
- Tomcat : version patchée compatible Spring Boot 3.4.x
- Spring Security : version patchée 6.5.x
- CycloneDX : plugin Maven
- Trivy : dernière version stable CI/CD

---

## 4. Compatibilité

- Compatible avec Docker Desktop (Windows 10/11).
- Compatible avec GitHub Actions (runner Ubuntu).
- Compatible GHCR (digest immuable).
- Aucun changement d’API REST.

---

## 5. Points d’attention

- Le Dockerfile ne contient plus Maven : build externe obligatoire.
- Le pipeline CI/CD est désormais bloquant sur :
  - SAST,
  - SCA (HIGH/CRITICAL),
  - scan image (HIGH/CRITICAL),
  - Quality Gate SonarCloud.
- Toute vulnérabilité ignorée doit être justifiée dans `.trivyignore`.

---

## 6. Correctifs inclus

- Correction de vulnérabilités connues via montée de versions Spring Boot / Tomcat / Spring Security.
- Correction de vulnérabilités OS via base image Temurin 21 mise à jour.
- Nettoyage du Dockerfile (réduction surface d’attaque).

---

## 7. Éléments non inclus

- Pas d’évolution fonctionnelle.
- Pas de modification API.
- Pas de changement de modèle de données.

---

## 8. Prochaines étapes (V4)

- Intégration d’un test d’intégration réseau automatisé.
- Ajout d’un rapport consolidé SAST/SCA dans les artefacts CI.
- Mise en place d’un monitoring runtime (à définir).
