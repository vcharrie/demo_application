# 📘 SPÉCIFICATION TECHNIQUE — VERSION 1 (V1)

**Application :** CoreService
**Version :** V1
**Date :** 2026-06-11
**Auteur :** Vincent

---

## 🧭 1. Objectif technique de la V1

La version V1 constitue une base technique minimale permettant :

- de valider la stack Spring Boot,
- de valider la capacité à exposer des endpoints REST,
- de valider la compilation, les tests et le packaging via Maven,
- de valider le pipeline CI minimaliste GitHub Actions.

Cette version ne contient aucune architecture métier, aucune persistance, aucune sécurité, et ne constitue pas encore une architecture modulaire.

---

## 🏗️ 2. Architecture technique globale

### 2.1. Stack technique

- **Langage :** Java 21
- **Framework :** Spring Boot
- **Modules Spring utilisés :**
  - Spring Boot Web
  - Spring Boot Actuator
  - Spring Boot Test
- **Build :** Maven
- **CI/CD :** GitHub Actions (pipeline minimaliste)
- **Packaging :** JAR exécutable (`java -jar`)

Aucun module métier, aucune base de données, aucun service externe.

### 2.2. Structure du projet

```
src/
 └── main/
      ├── java/com/coreservice/
      │     ├── CoreServiceApplication.java
      │     └── api/HealthController.java
      └── resources/application.properties
```

Architecture monolithique minimale, sans couches métier.

---

## ⚙️ 3. Composants techniques

### 3.1. CoreServiceApplication

Classe principale Spring Boot.

**Rôle :**

- point d'entrée de l'application
- bootstrap du contexte Spring
- activation des auto-configurations Spring Boot

**Localisation :**
`src/main/java/com/coreservice/CoreServiceApplication.java`

---

### 3.2. HealthController

Contrôleur REST minimaliste exposant un endpoint applicatif.

**Rôle :**

- démontrer la capacité à exposer un endpoint REST
- retourner la version de l'application
- retourner un statut applicatif `UP`

**Endpoint exposé :**
`GET /api/health`

**Réponse :**

```json
{"version":"v1","status":"UP"}
```

**Localisation :**
`src/main/java/com/coreservice/api/HealthController.java`

---

### 3.3. Spring Boot Actuator

Actuator est activé pour exposer un endpoint technique.

**Endpoint exposé :**
`GET /actuator/health`

**Rôle :**

- fournir un indicateur de santé technique
- exposer les groupes `liveness` et `readiness`

Aucune configuration avancée.

---

## 🧪 4. Tests

### 4.1. Tests unitaires

Présents dans :

- `src/test/java/com/coreservice/api/HealthControllerTest.java`

**Rôle :**

- valider le comportement du contrôleur de manière isolée
- vérifier le format de la réponse
- simuler les appels HTTP via `MockMvc` ou équivalent

---

### 4.2. Tests d'intégration internes (Spring Boot Test)

Présents dans :

- `src/test/java/com/coreservice/CoreServiceApplicationTests.java`

**Rôle :**

- démarrer le contexte Spring Boot complet
- instancier les composants applicatifs
- vérifier que l'application démarre correctement
- valider l'intégration interne des composants Spring

Ces tests :

- utilisent `@SpringBootTest`,
- ne lancent pas le JAR packagé,
- n'effectuent pas d'appels HTTP réels.

---

### 4.3. Tests d'intégration externes (sur JAR packagé exécuté en local)

En complément, des tests d'intégration externes sont réalisés manuellement en :

1. packaging de l'application :

```
mvn clean package
```

2. exécution du JAR :

```
java -jar target/CoreServiceApplication-0.0.1-SNAPSHOT.jar
```

3. appels HTTP réels via `curl` :

```
curl http://localhost:8080/api/health
curl http://localhost:8080/actuator/health
```

**Rôle :**

- valider le comportement runtime réel
- vérifier que les endpoints exposés répondent correctement
- tester l'application comme un client externe

Ces tests ne sont pas automatisés dans la CI V1.

---

## 🔧 5. Build & Packaging

### 5.1. Build Maven

Commandes utilisées :

```
mvn clean test
mvn package
```

### 5.2. Artefact généré

- **JAR exécutable :** `CoreServiceApplication-0.0.1-SNAPSHOT.jar`
- **Localisation :** `target/`

### 5.3. Exécution locale

```
java -jar target/CoreServiceApplication-0.0.1-SNAPSHOT.jar
```

---

## 🚀 6. CI/CD — Pipeline minimaliste

### 6.1. Localisation

`.github/workflows/ci-basic.yml`

### 6.2. Étapes exécutées

1. checkout du code
2. installation du JDK
3. cache Maven
4. `mvn clean test`
5. `mvn package`

### 6.3. Limitations (volontaires)

- pas d'exécution du JAR
- pas de tests d'intégration externes
- pas de build Docker
- pas de déploiement
- pas de scan de sécurité
- pas de SBOM

Pipeline minimaliste, conforme à une V1.

---

## 📄 7. Configuration

### 7.1. application.properties

Minimaliste, aucune configuration avancée.

### 7.2. Ports

- Port par défaut Spring Boot : `8080`

### 7.3. Logs

- Logging Spring Boot par défaut
- Aucun logger custom

---

## 🔒 8. Sécurité

La V1 ne contient aucune sécurité applicative :

- pas d'authentification
- pas d'autorisation
- pas de gestion des rôles
- pas de gestion des tokens
- pas de configuration HTTPS

Les endpoints sont ouverts.

---

## 🧱 9. Limitations techniques de la V1

- pas de modèle métier
- pas de services métier
- pas de persistance
- pas de gestion d'erreurs avancée
- pas de configuration multi-environnements
- pas de monitoring avancé
- pas de Dockerfile
- pas de DevSecOps
- pas de tests d'intégration externes automatisés
- pas de modularisation

Ces éléments seront introduits en V2.

---

## 🟩 10. Conclusion

La V1 constitue une base technique minimale permettant :

- de valider la stack Spring Boot
- de valider la capacité à exposer des endpoints REST
- de valider le pipeline CI minimaliste
- de préparer l'introduction du métier en V2

Elle sert de fondation pour les futures versions, qui introduiront :

- le modèle métier
- les services métier
- la persistance
- la sécurité
- la CI/CD avancée
- la traçabilité complète
- le DevSecOps
