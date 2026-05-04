# ================================================================
# STAGE 1 — RUNTIME
# JRE uniquement (pas JDK), épinglé sur digest SHA256.
# ================================================================
FROM eclipse-temurin:21-jre-jammy@sha256:fa4854e6057665066cb79953616671c20f32b96a85d6dd8b78db0762203924c4 AS runtime

ARG BUILD_DATE
ARG VCS_REF
ARG VERSION

LABEL org.opencontainers.image.title="CoreService"
LABEL org.opencontainers.image.description="CoreService Spring Boot application"
LABEL org.opencontainers.image.source="https://github.com/<owner>/<repo>"
LABEL org.opencontainers.image.version="${VERSION}"
LABEL org.opencontainers.image.revision="${VCS_REF}"
LABEL org.opencontainers.image.created="${BUILD_DATE}"

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
COPY --chown=appuser:appgroup target/*.jar app.jar


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