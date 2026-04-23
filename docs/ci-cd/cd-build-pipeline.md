CD – Deploy Pipeline Documentation

(File: docs/ci-cd/cd-build-pipeline.md)
Overview

This document describes the deployment pipeline for the CoreService application.
The deployment job runs after the build job and performs a lightweight validation of the Docker image by:

    pulling the image from GitHub Container Registry (GHCR)

    running the container locally on the GitHub Actions runner

    performing a smoke test on the /actuator/health endpoint

    stopping the container after validation

This ensures that the Docker image is functional before being used in later environments.
1. Deployment Job Triggers

The deploy job runs automatically when:

    the build job completes successfully

    the workflow is triggered by a push to main or v3_dev

    the workflow is triggered by a pull request

The job is defined with:
yaml

needs: build

This guarantees strict sequencing:
➡️ Deploy only runs if Build succeeds.
2. Deployment Job Breakdown
Step 1 — Authenticate to GHCR

Logs into GitHub Container Registry using credentials stored in GitHub Secrets.

Purpose:

    pull private images

    ensure secure access

    avoid exposing credentials in logs

Step 2 — Pull Docker Image

Downloads the image built in the previous job:
Code

ghcr.io/<username>/coreservice:latest

This ensures the deploy job always tests the exact image produced by the build.
Step 3 — Run Container (Detached Mode)

Runs the container in the background:
Code

docker run -d --name coreservice -p 8080:8080 <image>

Key points:

    detached mode allows the workflow to continue without blocking

    port 8080 is mapped to the host for smoke testing

    the container runs inside the ephemeral GitHub runner

    a short delay (sleep 12) gives Spring Boot time to start

Step 4 — Smoke Test

Sends a request to the health endpoint:
Code

curl -f http://localhost:8080/actuator/health

The test validates:

    the container started correctly

    the application is reachable

    the Spring Boot health indicator reports UP

If the endpoint returns a non‑2xx status, the job fails.
Step 5 — Stop Container

Stops the container even if the smoke test fails:
Code

if: always()

This ensures:

    clean resource usage

    no leftover containers

    predictable runner state

3. Purpose of the Deploy Job

This job is not a production deployment.
It is a validation stage that ensures:

    the Docker image is runnable

    the application starts correctly

    the health endpoint responds

    the image pushed to GHCR is functional

It acts as a pre‑deployment gate for future environments (staging, production, Kubernetes, etc.).
4. Future Extensions (Planned)

    container security scanning (Trivy, Grype)

    multi‑environment deployments (dev/staging/prod)

    Kubernetes deployment (GitOps or direct apply)

    automated rollback logic

    versioned image tagging

    blue/green or canary deployments

Summary

The deploy job validates the Docker image produced by the build pipeline by running it and performing a health check.
It ensures that only functional images reach the registry and prepares the foundation for future CD stages.