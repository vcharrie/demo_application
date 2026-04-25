# ====== Build stage ======
# This stage uses a Maven image to build the application. 
# It first copies the pom.xml to leverage Docker's caching for dependencies, then copies the source code and builds the JAR file.
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom and download dependencies (offline mode)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B clean package -DskipTests

# ====== Runtime stage ======
# This stage uses a lightweight JRE image to run the application. 
# It creates a non-root user for security, copies the built JAR from the build stage, and sets up a health check.
FROM eclipse-temurin:21.0.4_7-jre
WORKDIR /app

# Create non-root user for security
RUN useradd -m appuser
USER appuser

# Build metadata
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION

# Add OCI labels for better image metadata
LABEL org.opencontainers.image.created=$BUILD_DATE \
      org.opencontainers.image.revision=$VCS_REF \
      org.opencontainers.image.version=$VERSION \
      org.opencontainers.image.source="https://github.com/vcharrie/demo_application"

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Healthcheck (optional but recommended)
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
