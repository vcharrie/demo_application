# SEC-IMG-01 — Durcissement du Dockerfile

**Domaine :** Container Security / Supply Chain  
**Couche :** Image Docker (build + runtime)  
**Statut :** ✅ Implémenté

---

## RISQUE

**Menace**
Une image container mal construite élargit la surface d'attaque de
l'application. Si un attaquant compromet le container (via une CVE
applicative ou une mauvaise configuration), une image non durcie lui
offre des leviers supplémentaires : accès root, outils réseau,
dépendances inutiles, image de base non maîtrisée.

**Vecteurs principaux**

| Vecteur | Exemple concret |
|---|---|
| Image de base non épinglée | Tag flottant `latest` mis à jour silencieusement avec une CVE |
| Processus root dans le container | Compromission → accès root à l'hôte via escape |
| Outils réseau inutiles (`curl`) | Surface CVE inutile + vecteur d'exfiltration |
| Image non multi-stage | JDK + outils de build présents en production |
| Glob `*.jar` au COPY | Copie accidentelle de plusieurs JARs |

**Impact**
- **Confidentialité** : outil réseau présent → exfiltration de données facilitée
- **Intégrité** : accès root → modification de fichiers système ou applicatifs
- **Disponibilité** : image lourde → surface CVE élargie, plus de vecteurs DoS

**Références**
- CWE-250 : Execution with Unnecessary Privileges
- OWASP Docker Security Cheat Sheet
- CIS Docker Benchmark
- NIST SP 800-190 : Application Container Security Guide

---

## MESURES DE SÉCURITÉ

| Mesure | Type | Principe |
|---|---|---|
| Épinglage des images de base sur digest SHA256 | Préventif | Immutabilité / Supply chain |
| Multi-stage build (build vs runtime) | Préventif | Réduction de surface |
| Image JRE (pas JDK) en runtime | Préventif | Moindre privilège |
| `--no-install-recommends` sur apt | Préventif | Réduction de surface |
| Suppression de `curl` | Préventif | Réduction de surface / CVE |
| Utilisateur non-root UID/GID fixes | Préventif | Moindre privilège |
| `--no-create-home --shell /bin/false` | Préventif | Moindre privilège |
| `--chown` sur le COPY | Préventif | Moindre privilège |
| `USER` numérique (pas nom) | Préventif | Compatibilité K8s `runAsNonRoot` |
| COPY avec nom explicite (pas glob) | Préventif | Intégrité de l'artefact |
| `JAVA_OPTS` avec `UseContainerSupport` | Préventif | Respect des cgroups |
| HEALTHCHECK sans curl | Préventif | Réduction de surface |

---

## IMPLÉMENTATION

```dockerfile
# ================================================================
# STAGE 1 — BUILD
# Épinglé sur digest SHA256 pour reproductibilité et supply chain.
# Récupérer le digest : docker pull maven:3.9.6-eclipse-temurin-21
# puis : docker inspect --format='{{index .RepoDigests 0}}' <image>
# ================================================================
FROM maven:3.9.6-eclipse-temurin-21@sha256:<digest> AS build

WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ================================================================
# STAGE 2 — RUNTIME
# JRE uniquement (pas JDK), épinglé sur digest SHA256.
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
COPY --from=build --chown=appuser:appgroup \
     /app/target/coreservice-*.jar app.jar

# UID numérique — requis pour Kubernetes runAsNonRoot
USER 10001:10001

ENV SPRING_PROFILES_ACTIVE=ci \
    JAVA_OPTS="-XX:+UseContainerSupport \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Healthcheck sans curl — wget présent dans jammy
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Forme exec — SIGTERM bien reçu par la JVM (arrêt propre)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
```

**Récupérer les digests SHA256**
```bash
docker pull maven:3.9.6-eclipse-temurin-21
docker inspect --format='{{index .RepoDigests 0}}' \
  maven:3.9.6-eclipse-temurin-21

docker pull eclipse-temurin:21-jre-jammy
docker inspect --format='{{index .RepoDigests 0}}' \
  eclipse-temurin:21-jre-jammy
```

**Vérification post-build**
## 1. Vérification de l’utilisateur non‑root

### Linux / PowerShell
```bash
docker run --rm --entrypoint whoami coreservice:test
```

**Résultat attendu :**
```text
10001
```
ou
````text
appuser
````

---

## 2. Vérification des permissions et du contenu de l’image

### Linux / PowerShell  
⚠️ Ne pas utiliser Git Bash (il réécrit /bin/bash).  
Utiliser PowerShell ou CMD.

```bash
docker run -it --rm --entrypoint /bin/bash coreservice:test
```

Dans le container :
```bash
whoami
id
ls -l /app
```

**Résultats attendus :**
- utilisateur = \`10001\`
- groupe = \`10001\`
- app.jar appartient à \`10001:10001\`
- aucun outil inutile (curl, apt, etc.)

---

## 3. Vérification du démarrage de l’application

### Linux / PowerShell
```bash
docker run --rm -p 8080:8080 coreservice:test
```

Dans un autre terminal :
```bash
curl -v http://localhost:8080/actuator/health
```

**Résultat attendu :**
```json
{"status":"UP"}
```

---

## 4. Vérification du Healthcheck Docker

### Linux / PowerShell
```bash
docker inspect --format='{{json .State.Health}}' coreservice:test
```

**Résultat attendu :**
```text
"Status": "healthy"
```

---

## 5. Vérification de la taille de l’image (efficacité multi‑stage)

### Linux / PowerShell
```bash
docker images coreservice:test
```

**Résultat attendu :**
- image finale nettement plus petite que l’image Maven (build stage)
- typiquement 150–250 MB pour Temurin 21 JRE

---

## 6. Génération du SBOM CycloneDX

### Linux / PowerShell
```bash
trivy image --format cyclonedx --output sbom.cdx coreservice:test
```

**Résultat attendu :**
- fichier sbom.cdx généré
- contient dépendances + métadonnées image

---

## 7. Scan Trivy (Quality Gate)

### Linux / PowerShell
```bash
trivy image --severity HIGH,CRITICAL --ignore-unfixed=false coreservice:test
```

**Résultat attendu :**
- 0 CRITICAL
- 0 HIGH (hors vulnérabilités documentées dans SEC‑DEP‑02)
- si vulnérabilités présentes → justification dans vulnerability-analysis-vX.md

---

## 8. Vérification des labels OCI

### Linux / PowerShell
```bash
docker inspect coreservice:test | jq '.[0].Config.Labels'
```

**Résultat attendu :**
- org.opencontainers.image.created
- org.opencontainers.image.revision
- org.opencontainers.image.version
- org.opencontainers.image.source

---

## RISQUES RÉSIDUELS

| Risque résiduel | Justification d'acceptation | Amélioration envisagée |
|---|---|---|
| CVE OS Ubuntu (glibc, libexpat...) | No fix upstream — documenté en SEC-DEP-02 (D2) | Migration vers image distroless ou Alpine (V4) |
| `wget` présent dans l'image | Nécessaire pour HEALTHCHECK — surface réduite vs curl | Healthcheck via java natif (complexité accrue) |
| Digests à maintenir manuellement | Mise à jour lors des rebuilds planifiés | Dependabot peut automatiser les mises à jour de digest |
| `sh -c` dans ENTRYPOINT | Nécessaire pour interpoler `$JAVA_OPTS` | Passer les options JVM en variables d'env fixes |

**Note sur curl vs wget**
La suppression de `curl` est motivée par le volume de CVE qu'il génère
(10 CVE HIGH en D1). `wget` présent dans l'image `jammy` est un compromis
acceptable pour le HEALTHCHECK — sa surface CVE historique est
significativement plus faible. Ce choix est réévalué à chaque scan Trivy.

**Mesures complémentaires**
- SEC-SCA-01 : scan Trivy de l'image finale (gate CVE)
- SEC-CI-01 : épinglage des actions GitHub dans le pipeline de build
- SEC-K8S-01 : Security Context K8s renforce les contraintes du Dockerfile
  au niveau orchestrateur (readOnlyRootFilesystem, capabilities drop)
