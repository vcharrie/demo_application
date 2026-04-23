CI/CD – Build Pipeline Documentation

(File: docs/ci-cd/ci-build-pipeline.md)

Overview

This document describes the CI/CD build pipeline for the CoreService application.
The workflow automates the following tasks:

    Source checkout

    JDK setup

    Maven dependency caching

    Build + tests

    Artifact publication (test reports + JAR)

    Docker image build

    Docker image push to GitHub Container Registry (GHCR)

The pipeline is designed to be fast, reproducible, and aligned with DevSecOps best practices.
1. Workflow Triggers

The workflow runs automatically on:

    push events to main and v3_dev

    any pull_request

This ensures that:

    every commit is validated

    every PR is tested before merging

    the Docker image is always up-to-date on GHCR for deployment

2. Build Job Breakdown
Step 1 — Checkout

Retrieves the repository content so the runner can build the application.
Step 2 — JDK Setup

Installs Temurin JDK 21, ensuring consistent Java execution across all builds.
Step 3 — Maven Cache

Caches the local Maven repository (~/.m2) using a key derived from all pom.xml files.

Benefits:

    faster builds

    reduced network usage

    consistent dependency resolution

The cache is shared across all workflows in the repository.
Step 4 — Maven Build + Tests

Runs:
Code

mvn -DskipTests=false clean verify

This performs:

    dependency resolution

    compilation

    unit tests

    packaging

    integration test execution (if configured)

Step 5 — Upload Test Reports

Publishes Surefire test reports as build artifacts.
Useful for:

    debugging failing builds

    reviewing test output in PRs

    CI observability

Step 6 — Upload Built JAR

Stores the packaged application JAR as an artifact.
This JAR is used later in the deployment job.
Step 7 — GHCR Authentication

Logs into GitHub Container Registry using credentials stored in GitHub Secrets.
Step 8 — Docker Image Build

Builds the Docker image for the CoreService application:
Code

ghcr.io/<username>/coreservice:latest

Step 9 — Docker Image Push

Pushes the built image to GHCR so it can be pulled by the deploy job or external environments.
3. Produced Artifacts
Artifact	Description	Location
Test reports	Surefire XML reports	GitHub Actions artifacts
Application JAR	Packaged CoreService JAR	GitHub Actions artifacts
Docker image	ghcr.io//coreservice:latest	GitHub Container Registry
4. Security Considerations

    GHCR credentials are stored in GitHub Secrets

    Docker login uses --password-stdin to avoid leaking tokens

    No secrets appear in logs (GitHub masks them automatically)

    The workflow runs on ephemeral GitHub-hosted runners (no persistence)

5. Future Extensions (Planned)

This build pipeline is the foundation for upcoming CI/CD features:

    Quality Gate (SonarQube)

    Security Scans (Snyk, Trivy, Grype)

    Dependency Vulnerability Scanning

    Multi-stage Docker builds

    Release versioning

    Deployment to staging/production environments

Summary

The CI build pipeline ensures that every commit and PR is validated through:

    deterministic builds

    automated testing

    artifact generation

    Docker packaging

    registry publication

It provides a clean, maintainable foundation for future CI/CD enhancements.