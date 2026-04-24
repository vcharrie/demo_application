# Pull Request – CI/CD Pipeline Implementation (Build + Deploy)

## Title
Add full CI/CD pipeline: build, test, package, dockerize, deploy (smoke test)

---

## Summary
This pull request introduces the complete CI/CD pipeline for the CoreService application.  
It includes automated build, test execution, artifact publication, Docker packaging, image push to GHCR, and a deployment smoke test to validate the produced image.

This PR establishes the foundation for future DevSecOps enhancements such as quality gates, security scanning, and multi‑environment deployments.

---

## Description

### CI (Build Job)
The build job performs the following tasks:

- Checks out the repository  
- Sets up JDK 21 (Temurin)  
- Caches Maven dependencies  
- Builds the application and runs tests  
- Uploads test reports as artifacts  
- Uploads the built JAR  
- Authenticates to GitHub Container Registry (GHCR)  
- Builds the Docker image  
- Pushes the image to GHCR  

### CD (Deploy Job)
The deploy job validates the Docker image by:

- Authenticating to GHCR  
- Pulling the image produced by the build job  
- Running the container in detached mode  
- Waiting for Spring Boot to start  
- Performing a smoke test on `/actuator/health`  
- Stopping the container (always executed)  

This ensures that the image is functional before being used in future environments.

---

## Changes Included
- Added CI/CD workflow under `.github/workflows/ci-build.yml`
- Added build job (checkout, JDK setup, Maven cache, build, tests, artifacts)
- Added Docker image build and push to GHCR
- Added deploy job (pull image, run container, smoke test, cleanup)
- Added documentation:
  - `ci-build-pipeline.md`
  - `cd-build-pipeline.md`
  - `ci-cd-pipeline.md`
- Added internal PR documentation (`pr-ci-cd-build.md`)
- Added diff summary (`diff-ci-cd-build.md`)

---

## Motivation
This workflow ensures:

- deterministic builds  
- automated testing  
- reproducible Docker images  
- early detection of runtime issues  
- consistent delivery of validated artifacts  

It also prepares the repository for future CI/CD extensions such as:

- SonarQube Quality Gate  
- SAST / SCA security scanning  
- container vulnerability scanning  
- multi‑environment deployments  
- GitOps integration  

---

## How to Test

1. Push to a non-trigger branch (e.g., `feature/...`)  
   → No workflow should run.

2. Open a PR targeting `v3_dev` or `main`  
   → Workflow should trigger automatically.

3. Validate the following:
   - Build job completes successfully  
   - Test reports and JAR artifacts are uploaded  
   - Docker image builds and is pushed to GHCR  
   - Deploy job pulls the image  
   - Container starts correctly  
   - Smoke test returns HTTP 200  
   - Container is stopped cleanly  

4. Check GHCR to confirm the image exists:
