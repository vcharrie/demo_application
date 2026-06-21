# Spécification Technique — CoreService V3

**Version :** 3.0  
**Statut :** Implémenté  
**Date :** 2026-06  
**Auteur :** Vincent Charrie

---

## 1. Objet du document

Cette spécification décrit l'implémentation technique réelle de la version V3 de CoreService.

Elle documente :

- le packaging Docker (image durcie, runtime only, multi-stage supprimé),
- la chaîne CI/CD GitHub Actions (structure, jobs, dépendances),
- l'intégration des mécanismes SAST, SBOM, SCA, scan image,
- la gestion des vulnérabilités (CVE + processus VEX),
- les conventions techniques (nommage, épinglage, immutabilité),
- les artefacts produits à chaque étape,
- les interactions entre composants.

**Relation avec les fiches sécurité**

Les fiches sécurité (SEC-*) définissent les exigences, les justifications de risque et les décisions d'acceptation. Cette spécification décrit comment ces exigences sont implémentées concrètement. En cas de divergence, les fiches font foi.

---

## 2. Périmètre et contexte

### 2.1. Périmètre V3

La version V3 introduit les évolutions suivantes par rapport à V2 :

- image Docker durcie, runtime only (eclipse-temurin:21-jre, digest épinglé),
- build Maven externalisé hors Dockerfile (suppression du multi-stage),
- pipeline CI/CD complet : SAST → SBOM → SCA → scan image → smoke test → publish → SonarCloud,
- processus CVE formalisé : Trivy + .trivyignore.yaml justifié (format VEX),
- montées de versions : Spring Boot 3.4.13, Tomcat 10.1.54, Spring Security 6.4.x/6.5.x,
- traçabilité complète : labels OCI, provenance minimale, digest immuable GHCR.

### 2.2. Hors périmètre V3

- aucune évolution fonctionnelle ni métier,
- aucune persistance (prévue V6),
- pas de déploiement Kubernetes (prévu V4),
- pas de signature cryptographique Cosign (prévue V3/V4 — voir SEC-CI-03),
- pas de runner auto-hébergé (prévu V5).

---

## 3. Architecture logicielle

L'application CoreService est un **fat JAR Spring Boot 3.4.13** autonome embarquant toutes ses dépendances.

Composants runtime :

- `CoreServiceApplication.jar` — fat JAR Spring Boot
- image de base : `eclipse-temurin:21-jre-jammy` (digest SHA256 épinglé — voir §5.2)
- utilisateur non-root : UID/GID `10001` (appuser:appgroup)
- healthcheck exposé via `/actuator/health` (profil CI uniquement — désactivé en production)
- entrypoint exec avec `JAVA_OPTS` (support cgroups — voir SEC-IMG-01)
- labels OCI normalisés (revision, source, created, version)

Les tests unitaires et rapports SAST ne sont **pas embarqués** dans l'image finale.

---

## 4. Architecture technique

| Composant | Technologie | Version |
|---|---|---|
| Langage | Java | 21 (JDK build, JRE runtime) |
| Framework | Spring Boot | 3.4.13 |
| Serveur embarqué | Tomcat embed | 10.1.54 |
| Sécurité | Spring Security | 6.4.x / 6.5.x |
| Build | Maven | 3.9+ |
| Container | Docker | runtime only |
| Registry | GHCR | digest immuable |
| CI/CD | GitHub Actions | runner ubuntu-latest |
| SAST consolidé | SonarCloud | SaaS |
| Poste développeur | Windows 10/11 + Docker Desktop | — |

**Contraintes d'environnement**

Maven Enforcer (SEC-BUILD-01) impose :

- JDK ≥ 21 (`requireJavaVersion [21,)`)
- Maven ≥ 3.9 (`requireMavenVersion [3.9,)`)

Le build échoue immédiatement si ces contraintes ne sont pas satisfaites (fail-secure).

---

## 5. Packaging et build

### 5.1. Build Maven (obligatoirement externe)

Conformément à SEC-IMG-01, le build Maven est réalisé **hors Dockerfile**. L'image Docker ne contient pas de JDK ni d'outils de build.

**Commande CI/CD (et locale) :**

```bash
mvn -B clean verify
```

La phase `verify` est obligatoire — elle déclenche l'ensemble des gates SAST avant packaging :

| Phase Maven | Ce qui s'exécute |
|---|---|
| `validate` | Maven Enforcer (SEC-BUILD-01) — vérifie JDK ≥ 21 et Maven ≥ 3.9 |
| `compile` | Compilation Java |
| `test` | Tests unitaires |
| `verify` | Checkstyle, PMD, SpotBugs (SEC-SAST-01/02/03) |
| `package` | Production du fat JAR |

> ⚠️ Ne jamais utiliser `mvn clean package -DskipTests` en CI : cette commande contourne les gates SAST et les tests. Réservée au build local de développement uniquement.

**Artefacts produits :**

```
target/
├── CoreServiceApplication.jar       # fat JAR Spring Boot
├── bom.json                         # SBOM CycloneDX (généré séparément — §5.3)
├── checkstyle-result.xml            # rapport Checkstyle (importé par SonarCloud)
├── pmd.xml                          # rapport PMD (importé par SonarCloud)
└── spotbugsXml.xml                  # rapport SpotBugs (importé par SonarCloud)
```

---

### 5.2. Dockerfile (runtime only)

Le Dockerfile implémente l'ensemble des exigences de SEC-IMG-01.

```dockerfile
# ================================================================
# RUNTIME ONLY — pas de stage build dans le Dockerfile V3
# Le JAR est produit par Maven en amont (voir §5.1)
# ================================================================
FROM eclipse-temurin:21-jre-jammy@sha256:<digest> AS runtime

ARG BUILD_DATE
ARG VCS_REF
ARG VERSION

LABEL org.opencontainers.image.created=$BUILD_DATE \
      org.opencontainers.image.revision=$VCS_REF \
      org.opencontainers.image.version=$VERSION \
      org.opencontainers.image.source="https://github.com/vcharrie/demo_application"

WORKDIR /app

# Mises à jour sécurité OS — sans curl, sans recommandations inutiles
RUN apt-get update \
 && apt-get upgrade -y --no-install-recommends \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

# Utilisateur non-root — UID/GID fixes, pas de home, pas de shell
RUN groupadd --gid 10001 appgroup \
 && useradd --uid 10001 --gid appgroup \
            --no-create-home --shell /bin/false appuser

# COPY avec --chown et nom explicite (pas de glob *.jar)
COPY --chown=appuser:appgroup target/CoreServiceApplication.jar app.jar

# UID numérique — requis pour Kubernetes runAsNonRoot (V4)
USER 10001:10001

ENV SPRING_PROFILES_ACTIVE=ci \
    JAVA_OPTS="-XX:+UseContainerSupport \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Healthcheck sans curl (wget présent dans jammy — justification SEC-IMG-01)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Forme exec — SIGTERM bien reçu par la JVM
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
```

**Récupérer le digest SHA256 de l'image de base :**

```bash
docker pull eclipse-temurin:21-jre-jammy
docker inspect --format='{{index .RepoDigests 0}}' eclipse-temurin:21-jre-jammy
```

Remplacer `<digest>` dans le Dockerfile par la valeur retournée. À mettre à jour lors de chaque rebuild planifié.

---

### 5.3. Génération du SBOM CycloneDX

Le SBOM est généré depuis le `pom.xml` via le plugin CycloneDX Maven, **dans le job `build`** avant le scan SCA :

```bash
mvn -B org.cyclonedx:cyclonedx-maven-plugin:2.7.9:makeAggregateBom
```

Produit : `target/bom.json` (format CycloneDX 1.5) — inventaire complet de toutes les dépendances Maven avec leurs versions exactes.

---

### 5.4. Build local (développeur)

Séquence complète pour reproduire le build CI en local :

```bash
# 1. Build Maven avec gates SAST
mvn -B clean verify

# 2. Génération SBOM
mvn -B org.cyclonedx:cyclonedx-maven-plugin:2.7.9:makeAggregateBom

# 3. Build image Docker
docker build \
  --build-arg BUILD_DATE=$(date -u +%Y-%m-%dT%H:%M:%SZ) \
  --build-arg VCS_REF=$(git rev-parse HEAD) \
  --build-arg VERSION=local \
  -t coreservice:local .
```

---

## 6. Chaîne CI/CD V3

### 6.1. Vue d'ensemble du pipeline

Le pipeline `ci-build.yml` implémente les exigences des fiches SEC-CI-01, SEC-CI-02, SEC-CI-03, SEC-SAST-01/02/03/04, SEC-SCA-01, SEC-IMG-01.

```
build ──┬── sca ──────────────────┐
        └── (rapports SAST)       ├── docker-build ── docker-scan ── smoke-test ── publish
                                  │
                                  └── (SBOM produit dans build)

publish ── sonar
```

Structure des jobs et dépendances :

```yaml
jobs:
  build:
    runs-on: ubuntu-latest

  sca:
    needs: build

  docker-build:
    needs: build

  docker-scan:
    needs: docker-build

  smoke-test:
    needs: docker-scan

  publish:
    needs: [sca, smoke-test]

  sonar:
    needs: publish
```

La publication n'est possible que si le scan SCA **et** le smoke test sont tous deux passés. SonarCloud s'exécute en dernier, après publication.

---

### 6.2. Épinglage des actions GitHub (SEC-CI-01 §3.1)

Toutes les actions GitHub sont épinglées par **SHA de commit**, pas par tag. Un tag peut être réécrit silencieusement ; le SHA est immuable.

```yaml
# ❌ Incorrect — tag mutable
uses: actions/checkout@v4

# ✅ Correct — SHA immuable
uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683  # v4.2.2
```

Actions épinglées dans le pipeline :

| Action | SHA | Version de référence |
|---|---|---|
| `actions/checkout` | `11bd71901bbe5b1630ceea73d27597364c9af683` | v4.2.2 |
| `actions/setup-java` | SHA à compléter | v4.x |
| `actions/upload-artifact` | `6f51ac03b9356f520e9adb1b1b7802705f340c2b` | v4.5.0 |
| `aquasecurity/trivy-action` | SHA à compléter | dernière stable |

---

### 6.3. Gestion des secrets (SEC-CI-02)

Les secrets sont stockés exclusivement dans **GitHub → Settings → Secrets and variables → Actions**.

Secrets déclarés :

- `GHCR_TOKEN` — token d'accès au registry GHCR
- `SONAR_TOKEN` — token d'authentification SonarCloud

**Permissions minimales du `GITHUB_TOKEN` :**

```yaml
permissions:
  contents: read
  packages: write
  actions: read
```

**Injection sécurisée des secrets (pas de substitution directe dans `run:`) :**

```yaml
# ✅ Correct
- name: Login to GHCR
  env:
    TOKEN: ${{ secrets.GHCR_TOKEN }}
  run: echo "$TOKEN" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
```

---

### 6.4. Job `build` — Maven + SAST + SBOM

```yaml
- name: Setup Java 21
  uses: actions/setup-java@<sha>
  with:
    java-version: '21'
    distribution: 'temurin'

- name: Build Maven (verify — gates SAST inclus)
  run: mvn -B clean verify

- name: Generate SBOM
  run: mvn -B org.cyclonedx:cyclonedx-maven-plugin:2.7.9:makeAggregateBom

- name: Upload SAST reports
  uses: actions/upload-artifact@6f51ac03b9356f520e9adb1b1b7802705f340c2b
  if: always()
  with:
    name: sast-reports
    retention-days: 30
    path: |
      target/checkstyle-result.xml
      target/pmd.xml
      target/spotbugsXml.xml
      target/bom.json
```

`mvn -B clean verify` exécute dans l'ordre : Enforcer → compile → test → Checkstyle → PMD → SpotBugs → package. Le pipeline échoue à la première violation bloquante.

---

### 6.5. Job `sca` — Scan SBOM Trivy (SEC-SCA-01 workflow 2)

```yaml
- name: Scan SBOM with Trivy
  run: |
    trivy sbom \
      --severity HIGH,CRITICAL \
      --ignorefile config/trivy/.trivyignore.yaml \
      --ignore-unfixed=false \
      --exit-code 1 \
      --format table \
      target/bom.json
```

> **Point clé :** `--ignore-unfixed=false` — toutes les CVE sont visibles, avec ou sans correctif disponible. Les CVE sans fix doivent être explicitement justifiées dans `.trivyignore.yaml` (VEX). Ce mode plus strict est celui qui a permis de détecter CVE-2026-22731/22733 sur Spring Boot Actuator (voir SEC-SCA-01 et SEC-DEP-02).

---

### 6.6. Job `docker-build` — Build image

```yaml
- name: Build Docker image
  run: |
    docker build \
      --build-arg BUILD_DATE=$(date -u +%Y-%m-%dT%H:%M:%SZ) \
      --build-arg VCS_REF=${GITHUB_SHA} \
      --build-arg VERSION=${GITHUB_SHA} \
      -t coreservice:${GITHUB_SHA} .

- name: Generate metadata
  run: |
    echo "sha=${GITHUB_SHA}" > metadata.txt
    echo "run_id=${GITHUB_RUN_ID}" >> metadata.txt
    echo "workflow=${GITHUB_WORKFLOW}" >> metadata.txt
    echo "runner=${RUNNER_NAME}" >> metadata.txt
    echo "date=$(date -u)" >> metadata.txt
```

Aucun tag `latest` n'est appliqué (SEC-CI-01 §3.8 — immutabilité).

---

### 6.7. Job `docker-scan` — Scan image Trivy (SEC-SCA-01 workflow 1)

```yaml
- name: Trivy image scan (CRITICAL/HIGH gate)
  run: |
    trivy image \
      --severity HIGH,CRITICAL \
      --ignorefile config/trivy/.trivyignore.yaml \
      --ignore-unfixed=true \
      --vuln-type os,library \
      --exit-code 1 \
      --format table \
      coreservice:${GITHUB_SHA}
```

> **Différence avec le scan SBOM :** `--ignore-unfixed=true` est utilisé ici pour réduire le bruit sur les CVE OS sans correctif disponible (responsabilité Canonical). Les CVE OS sans fix sont malgré tout documentées dans `.trivyignore.yaml`. L'alignement vers `--ignore-unfixed=false` est prévu (voir SEC-SCA-01 §4).

---

### 6.8. Job `smoke-test` — Validation container

```yaml
- name: Run container
  run: docker run -d --name cs coreservice:${GITHUB_SHA}

- name: Wait for healthcheck
  run: |
    for i in $(seq 1 12); do
      STATUS=$(docker inspect --format='{{.State.Health.Status}}' cs 2>/dev/null || echo "starting")
      echo "Attempt $i: $STATUS"
      [ "$STATUS" = "healthy" ] && exit 0
      sleep 5
    done
    echo "Container did not become healthy"
    docker logs cs
    exit 1

- name: Smoke test HTTP
  run: |
    curl -sf http://localhost:8080/actuator/health \
      | grep -q '"status":"UP"' \
      || (echo "Smoke test failed" && exit 1)

- name: Stop and remove container
  if: always()
  run: docker rm -f cs || true
```

---

### 6.9. Job `publish` — Push GHCR (SEC-CI-01 §3.8)

```yaml
- name: Login to GHCR
  env:
    TOKEN: ${{ secrets.GHCR_TOKEN }}
  run: echo "$TOKEN" | docker login ghcr.io -u ${{ github.actor }} --password-stdin

- name: Push image (digest immuable)
  run: |
    docker tag coreservice:${GITHUB_SHA} ghcr.io/${{ github.repository }}/coreservice:${GITHUB_SHA}
    docker push ghcr.io/${{ github.repository }}/coreservice:${GITHUB_SHA}

- name: Capture image digest
  run: |
    IMAGE_DIGEST=$(docker inspect \
      --format='{{index .RepoDigests 0}}' \
      ghcr.io/${{ github.repository }}/coreservice:${GITHUB_SHA})
    echo "digest=${IMAGE_DIGEST}" >> metadata.txt

- name: Hash artefacts de provenance
  run: |
    sha256sum metadata.txt > metadata.txt.sha256
    sha256sum target/bom.json > bom.json.sha256

- name: Upload provenance
  uses: actions/upload-artifact@6f51ac03b9356f520e9adb1b1b7802705f340c2b
  with:
    name: provenance
    retention-days: 30
    path: |
      metadata.txt
      metadata.txt.sha256
      target/bom.json
      bom.json.sha256
```

Tags publiés : `coreservice:${GITHUB_SHA}` uniquement. Pas de tag `latest`.

---

### 6.10. Job `sonar` — SonarCloud (SEC-SAST-04)

```yaml
- name: SonarCloud analysis
  run: mvn -B verify sonar:sonar
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

`mvn -B verify sonar:sonar` dans la même commande garantit que SonarCloud importe les rapports XML produits par les gates locales :

| Rapport importé | Source |
|---|---|
| `target/checkstyle-result.xml` | Checkstyle (SEC-SAST-01) |
| `target/pmd.xml` | PMD (SEC-SAST-02) |
| `target/spotbugsXml.xml` | SpotBugs (SEC-SAST-03) |

SonarCloud applique en plus ses propres règles (Security Hotspots, vulnérabilités Sonar, coverage, duplications) et constitue la **gate de qualité consolidée**.

Le pipeline échoue si le Quality Gate SonarCloud retourne `FAILED` (zéro nouvelle vulnérabilité, zéro nouveau Security Hotspot non revu, pas de régression de rating sécurité).

---

## 7. Gestion des vulnérabilités (CVE)

### 7.1. Processus

```
1. Détection
   ├── Scan SBOM (trivy-sbom.yml) — sur pom.xml, ignore-unfixed=false
   └── Scan image (ci-build.yml) — sur image finale, ignore-unfixed=true

2. Qualification
   ├── CVE exploitable ? Vecteur d'attaque applicable à CoreService ?
   ├── Fix disponible ? Dans quelle version ?
   └── Impact architectural ? (Actuator désactivé, endpoint non exposé...)

3. Décision
   ├── Fix disponible → montée de version (SEC-DEP-01)
   ├── No fix upstream → acceptation temporaire + entrée trivyignore
   └── VEX Not Affected → justification architecturale + entrée trivyignore

4. Suivi
   └── Révision mensuelle du trivyignore.yaml (dates d'expiration)
```

---

### 7.2. Fichier `.trivyignore.yaml` — format et conventions

Le fichier est localisé en `config/trivy/.trivyignore.yaml`.

**Format obligatoire (justifié avec expiration) :**

```yaml
# =================================================================
# TRIVY IGNORE — CoreService
# Toute entrée doit référencer le document de décision (SEC-DEP-02)
# Révision mensuelle obligatoire
# =================================================================

vulnerabilities:

  # --- OS Layer — Ubuntu 24.04 — No fix available ---------------

  - id: CVE-2026-27456
    statement: "No fix available in Ubuntu 24.04 at scan time (D2 — util-linux/bsdutils)"
    expiration: "2026-12-31"

  - id: CVE-2026-4046
    statement: "No fix available in Ubuntu 24.04 at scan time (D2 — glibc)"
    expiration: "2026-12-31"

  # --- Java Layer — VEX Not Affected ----------------------------

  - id: CVE-2026-22731
    statement: >
      VEX Not Affected (SCA-2). Actuator endpoints disabled in all production
      profiles (management.endpoints.enabled-by-default=false). Only
      /actuator/health enabled in isolated CI/CD profile. Fix available only
      in Spring Boot 3.5.12+, outside current 3.4.x branch.
    expiration: "2026-12-31"

  - id: CVE-2026-22733
    statement: >
      VEX Not Affected (SCA-2). CloudFoundry Actuator endpoint explicitly
      disabled in all profiles (management.endpoint.cloudfoundry.enabled=false).
      Fix available only in Spring Boot 3.5.12+, outside current 3.4.x branch.
    expiration: "2026-12-31"
```

> **Format bare interdit.** Une liste nue d'identifiants CVE sans justification ne constitue pas un processus de décision — c'est une exclusion silencieuse. Chaque entrée doit avoir : `id`, `statement` référençant SEC-DEP-02, `expiration`.

**Point de vigilance VEX Actuator**

La validité des VEX CVE-2026-22731/22733 repose sur le maintien de `management.endpoints.enabled-by-default=false` en production. Tout changement de configuration Actuator doit déclencher une réévaluation immédiate de ces entrées.

---

### 7.3. Montées de versions (SEC-DEP-01)

Les dépendances critiques sont surchargées dans `<dependencyManagement>` du `pom.xml` pour corriger des CVE indépendamment du cycle de release Spring Boot :

| Dépendance | Version épinglée | Motif |
|---|---|---|
| `tomcat-embed-core` | 10.1.54 | CVE-2026-29145 (auth bypass) |
| `tomcat-embed-websocket` | 10.1.54 | alignement Tomcat |
| `tomcat-embed-el` | 10.1.54 | alignement Tomcat |
| `spring-security-web` | 6.5.9 | CVE-2025-22228 (BCrypt bypass) |
| `spring-security-core` | 6.4.10 | CVE sur spring-security-core |
| `spring-core` | 6.2.11 | CVE-2024-38816/38819 (path traversal) |

> **Note de cohérence :** `spring-security-web` (6.5.9) et `spring-security-core` (6.4.10) sont sur des versions mineures différentes. Ce point mérite vérification de compatibilité lors de la prochaine mise à jour Spring Boot.

---

## 8. Artefacts CI/CD et traçabilité

### 8.1. Artefacts produits par le pipeline

| Artefact | Job | Rétention | Usage |
|---|---|---|---|
| `target/bom.json` | build | 30 jours | Scan SCA + SonarCloud |
| `target/checkstyle-result.xml` | build | 30 jours | SonarCloud import |
| `target/pmd.xml` | build | 30 jours | SonarCloud import |
| `target/spotbugsXml.xml` | build | 30 jours | SonarCloud import |
| `metadata.txt` | publish | 30 jours | Provenance audit |
| `metadata.txt.sha256` | publish | 30 jours | Intégrité provenance |
| `bom.json.sha256` | publish | 30 jours | Intégrité SBOM |

### 8.2. Artefacts à ne jamais versionner dans Git

```gitignore
# Artefacts CI — ne jamais versionner
target/
bom.json
metadata.txt
trivy-*.json
trivy-*.txt
provenance.txt
*.sha256
```

### 8.3. Provenance minimale (SEC-CI-03)

La provenance produite en V3 est une **provenance minimale** (pré-SLSA) — pas une provenance SLSA au sens strict (format JSON signé standardisé). Elle contient : SHA du commit, run ID, nom du workflow, runner, date, digest image.

La provenance SLSA officielle (signature OIDC, format JSON standardisé) est prévue en V3/V4 via Cosign — voir roadmap SEC-CI-03.

---

## 9. Limitations et contraintes techniques

| Contrainte | Justification | Évolution prévue |
|---|---|---|
| Build Maven obligatoirement externe | SEC-IMG-01 — pas de JDK en runtime | Permanent |
| `ignore-unfixed=true` sur scan image | Réduction du bruit CVE OS | Alignement `false` (V4) |
| `wget` présent dans l'image | Nécessaire pour HEALTHCHECK | Healthcheck natif Java (V4) |
| Digests SHA256 maintenus manuellement | Mise à jour lors des rebuilds | Dependabot digest (V4) |
| `sh -c` dans ENTRYPOINT | Nécessaire pour `$JAVA_OPTS` | Variables d'env fixes (V4) |
| Provenance non signée | Hors scope V3 | Cosign (V3/V4) |
| Runner GitHub SaaS non attesté | Contrainte opérationnelle | Runner auto-hébergé (V5) |
| Pas de DAST | Hors scope V3 | ZAP ou StackHawk (V5+) |

---

## 10. Références croisées

| Fiche | Périmètre | Section(s) de cette spec |
|---|---|---|
| SEC-BUILD-01 | Maven Enforcer | §4, §5.1 |
| SEC-CI-01 | Pipeline CI/CD, épinglage, immutabilité | §6.1, §6.2, §6.6, §6.9 |
| SEC-CI-02 | Secrets GitHub | §6.3 |
| SEC-CI-03 | Provenance, SLSA | §6.9, §8.3 |
| SEC-DEP-01 | Épinglage versions dépendances | §7.3 |
| SEC-DEP-02 | Processus CVE, historique runs | §7.1, §7.2 |
| SEC-IMG-01 | Dockerfile durci | §5.2 |
| SEC-SAST-01 | Checkstyle | §5.1, §6.4 |
| SEC-SAST-02 | PMD | §5.1, §6.4 |
| SEC-SAST-03 | SpotBugs | §5.1, §6.4 |
| SEC-SAST-04 | SonarCloud | §6.10 |
| SEC-SCA-01 | Trivy SCA + SBOM | §5.3, §6.5, §6.7 |

---

## 11. Journal des versions

| Version | Date | Évolutions |
|---|---|---|
| 3.0 | 2026-06 | Image durcie runtime only, build Maven externe, pipeline complet V3, processus VEX formalisé, trivyignore justifié, montées de versions SB 3.4.13 / Tomcat 10.1.54 |
| 2.0 | — | Pipeline V2, secrets GitHub, épinglage SHA actions |
| 1.0 | — | Version initiale |
