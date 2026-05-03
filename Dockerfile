# ================================================================
# STAGE 1 — BUILD
# Épinglé sur digest SHA256 pour reproductibilité et supply chain.
# Récupérer le digest : docker pull maven:3.9.6-eclipse-temurin-21
# puis : docker inspect --format='{{index .RepoDigests 0}}' <image>
# ================================================================
FROM maven:3.9.6-eclipse-temurin-21@sha256:8d63d4c1902cb12d9e79a70671b18ebe26358cb592561af33ca1808f00d935cb AS build

WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ================================================================
# STAGE 2 — RUNTIME
# JRE uniquement (pas JDK), épinglé sur digest SHA256.
# ================================================================
FROM eclipse-temurin:21-jre-jammy@sha256:fa4854e6057665066cb79953616671c20f32b96a85d6dd8b78db0762203924c4 AS runtime

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
     /app/target/CoreServiceApplication-*.jar app.jar

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