# 📘 SPÉCIFICATION TECHNIQUE — VERSION 1 (V1)

**Application :** CoreService  
**Version :** V1  
**Date :** 2026-06-11  
**Auteur :** Vincent  

---

## 1. 🎯 Objet du document

La présente spécification technique décrit l'architecture logicielle, les composants techniques, la chaîne de build et le pipeline CI minimaliste de la version **V1** de l'application CoreService.

Elle constitue la **référence technique** de la release V1, dont l'objectif est de valider :

- la stack Spring Boot,
- la capacité à exposer un endpoint REST,
- la compilation, les tests et le packaging Maven,
- un pipeline CI minimaliste GitHub Actions.

> La V1 **ne contient aucun métier**, aucune persistance, aucune sécurité, et ne constitue pas encore une architecture modulaire. Elle sert de fondation technique aux versions ultérieures.

---

## 2. 🏗️ Architecture technique

### 2.1 Vue d'ensemble

```
+--------------------------------------------+
|             CoreService V1                 |
|                                            |
|   +-----------+      +------------------+  |
|   |    API    |      |     Actuator     |  |
|   |           |      |                  |  |
|   | GET       |      | GET              |  |
|   | /api/     |      | /actuator/health |  |
|   | health    |      |                  |  |
|   +-----------+      +------------------+  |
|                                            |
|   Domaine    : (vide)                      |
|   Infra      : (vide)                      |
|   Persistance: (aucune)                    |
+--------------------------------------------+
         |
   [JAR Spring Boot — port 8080]
```

### 2.2 Architecture générale

La V1 repose sur une architecture **monolithique minimale**, composée de :

- **API (Entrée)** : un unique contrôleur REST `HealthController`
- **Domaine** : aucun modèle métier
- **Infrastructure** : aucune persistance, aucun repository

L'application est packagée en **JAR Spring Boot exécutable**.

### 2.3 Stack technique

| Composant | Choix | Remarque |
|---|---|---|
| Langage | Java 21 | LTS — base de toutes les versions suivantes |
| Framework | Spring Boot 4.x | Auto-configuration, Actuator inclus |
| Build | Maven | `pom.xml` minimaliste |
| CI/CD | GitHub Actions | Pipeline minimaliste — pas de Docker |
| Packaging | JAR exécutable | `java -jar` |
| Runtime | JRE 21 | Exécution locale uniquement en V1 |
| Modules Spring | Web, Actuator, Test | Aucun module métier ou sécurité |

Aucun service externe, aucune base de données.

### 2.4 Structure du projet

```
CoreService/
 ├── .github/
 │    └── workflows/
 │         └── ci-basic.yml
 ├── src/
 │    ├── main/
 │    │    ├── java/com/coreservice/
 │    │    │    ├── CoreServiceApplication.java
 │    │    │    └── api/
 │    │    │         └── HealthController.java
 │    │    └── resources/
 │    │         └── application.properties
 │    └── test/
 │         └── java/com/coreservice/
 │              ├── CoreServiceApplicationTests.java
 │              └── api/
 │                   └── HealthControllerTest.java
 └── pom.xml
```

---

## 3. ⚙️ Composants techniques

### 3.1 CoreServiceApplication

**Rôle :**
- point d'entrée Spring Boot
- bootstrap du contexte
- activation des auto-configurations

**Localisation :** `src/main/java/com/coreservice/CoreServiceApplication.java`

```java
@SpringBootApplication
public class CoreServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoreServiceApplication.class, args);
    }
}
```

---

### 3.2 HealthController

**Rôle :**
- exposer un endpoint REST minimaliste
- retourner la version applicative
- démontrer la capacité à exposer une API

**Localisation :** `src/main/java/com/coreservice/api/HealthController.java`

**Endpoint exposé :** `GET /api/health`

**Réponse :**

```json
{"version":"v1","status":"UP"}
```

**Exemple d'appel :**

```bash
curl http://localhost:8080/api/health
# → {"version":"v1","status":"UP"}
```

---

### 3.3 Spring Boot Actuator

**Endpoints exposés :**

| Endpoint | Rôle |
|---|---|
| `GET /actuator/health` | Indicateur de santé technique |
| `GET /actuator/health/liveness` | Probe liveness (préparation K8S) |
| `GET /actuator/health/readiness` | Probe readiness (préparation K8S) |

**Réponse typique `/actuator/health` :**

```json
{"status":"UP"}
```

> Les groupes `liveness` et `readiness` sont exposés dès la V1 — ils seront utilisés par les probes Kubernetes en V4-A.

Aucune configuration avancée.

---

### 3.4 application.properties

```properties
# V1 — configuration minimale
spring.application.name=coreservice
server.port=8080
management.endpoints.web.exposure.include=health
management.endpoint.health.probes.enabled=true
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true
```

---

### 3.5 pom.xml — dépendances essentielles

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 4. 🧪 Tests techniques

### 4.1 Tests unitaires

**Localisation :** `src/test/java/com/coreservice/api/HealthControllerTest.java`

**Rôle :**
- valider le comportement du contrôleur
- vérifier le format de la réponse
- simuler les appels HTTP via `MockMvc`

```java
@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void health_shouldReturnV1Status() throws Exception {
        mockMvc.perform(get("/api/health"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.version").value("v1"))
               .andExpect(jsonPath("$.status").value("UP"));
    }
}
```

---

### 4.2 Tests d'intégration internes

**Localisation :** `src/test/java/com/coreservice/CoreServiceApplicationTests.java`

**Rôle :**
- démarrer le contexte Spring Boot complet
- vérifier que l'application démarre correctement

```java
@SpringBootTest
class CoreServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

---

### 4.3 Tests d'intégration externes (manuels)

Procédure :

```bash
# 1. Compiler et packager
mvn clean package

# 2. Lancer l'application
java -jar target/CoreServiceApplication-0.0.1-SNAPSHOT.jar

# 3. Appels HTTP réels
curl http://localhost:8080/api/health
# → {"version":"v1","status":"UP"}

curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

> Non automatisés dans la CI V1 — automatisation introduite en V2 (smoke test Docker).

---

## 5. 🔧 Build & Packaging

### 5.1 Commandes Maven

```bash
# Compiler et exécuter les tests
mvn clean test

# Compiler et packager (JAR)
mvn clean package

# Exécuter l'application localement
java -jar target/CoreServiceApplication-0.0.1-SNAPSHOT.jar
```

### 5.2 Artefact généré

| Artefact | Localisation | Usage |
|---|---|---|
| `CoreServiceApplication-0.0.1-SNAPSHOT.jar` | `target/` | Exécution locale |

---

## 6. 🚀 CI/CD — Pipeline minimaliste

### 6.1 Localisation

`.github/workflows/ci-basic.yml`

### 6.2 Extrait du workflow

```yaml
name: CI V1

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}

      - name: Build & Test
        run: mvn clean verify
```

### 6.3 Étapes exécutées

1. Checkout du code
2. Installation du JDK 21
3. Cache Maven (optimisation des builds suivants)
4. `mvn clean verify` (compilation + tests)

### 6.4 Limitations volontaires

| Élément absent | Raison | Introduit en |
|---|---|---|
| Build Docker | Pas de Dockerfile en V1 | V2 |
| Push GHCR | Pas d'image à pousser | V2 |
| Smoke test automatisé | Pas de container | V2 |
| Scan de sécurité (SAST/SCA) | Hors scope V1 | V3 |
| SBOM | Hors scope V1 | V3 |
| Déploiement | Hors scope V1 | V4-A |

---

## 7. 🔒 Sécurité

La V1 ne contient **aucune** sécurité applicative. C'est intentionnel à ce stade.

| Élément | État V1 | Introduit en |
|---|---|---|
| Authentification | Absent | V3 (Spring Security) |
| Autorisation | Absent | V3 |
| HTTPS | Absent | V4-B (Cert-Manager) |
| Scan SAST | Absent | V3 |
| Scan SCA | Absent | V3 |
| Scan image Docker | Absent | V3 |
| Signature image | Absent | V3+ |
| Secrets management | Absent | V4-A (Sealed Secrets) |

---

## 8. 🧱 Limitations techniques de la V1

| Limitation | Impact | Résolution prévue |
|---|---|---|
| Pas de modèle métier | Application sans domaine | V2 (architecture hexagonale) |
| Pas de persistance | Données perdues au redémarrage | V2 (in-memory) → V3 (JPA) |
| Pas de Dockerfile | Pas de containerisation | V2 |
| Pas de DevSecOps | Aucun contrôle qualité/sécurité CI | V3 |
| Pas de gestion d'erreurs | Réponses Spring par défaut | V2 (GlobalExceptionHandler) |
| Tests externes non automatisés | Validation manuelle uniquement | V2 (smoke test CI) |
| Pas de configuration multi-env | Profil unique | V3+ |
| Pas de documentation OpenAPI | API non documentée | V3+ |

---

## 9. 🔭 Continuité V1 → versions suivantes

| Élément V1 | Continuité |
|---|---|
| `HealthController` | Conservé et enrichi en V2/V3 |
| `/actuator/health` (liveness/readiness) | Réutilisé comme probes K8S en V4-A |
| Pipeline CI GitHub Actions | Enrichi en V2 (Docker), V3 (scans) |
| Stack Spring Boot / Java 21 | Base immuable de toutes les versions |
| Port 8080 | Conservé jusqu'en V4-A |
| `mvn clean verify` | Étape conservée dans tous les pipelines CI suivants |

---

## 10. 🟩 Conclusion

La V1 constitue une **base technique minimale validée**, permettant :

- de vérifier la stack Spring Boot opérationnelle,
- d'exposer un premier endpoint REST fonctionnel,
- de mettre en place un pipeline CI reproductible,
- de préparer l'introduction du métier, de la sécurité et du DevSecOps dans les versions suivantes.

Elle sert de point de départ documenté pour la chaîne :

```
V1 (stack + CI) → V2 (métier + Docker) → V3 (sécurité + DevSecOps) → V4-A (Kubernetes local) → V4-B (EKS)
```
