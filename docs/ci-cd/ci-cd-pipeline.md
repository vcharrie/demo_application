CI/CD Pipeline – Global Documentation

(File: docs/ci-cd/ci-cd-pipeline.md)
Overview

This document provides a complete overview of the CI/CD pipeline for the CoreService application.
The pipeline is implemented using GitHub Actions and automates:

    continuous integration (CI)

    continuous delivery (CD)

    Docker packaging

    artifact publication

    smoke testing

The pipeline is designed to be modular, maintainable, and aligned with DevSecOps best practices.
1. Pipeline Structure

The workflow consists of two jobs:
1. Build (CI)

Responsible for:

    checking out the code

    setting up JDK

    caching Maven dependencies

    building the application

    running tests

    publishing test reports

    building a Docker image

    pushing the image to GHCR

Documented in:
➡️ ci-build-pipeline.md
2. Deploy (CD)

Responsible for:

    pulling the Docker image

    running it locally

    performing a smoke test

    stopping the container

Documented in:
➡️ cd-build-pipeline.md
2. Workflow Triggers

The pipeline runs on:

    pushes to main

    pushes to v3_dev

    all pull requests

This ensures:

    every commit is validated

    every PR is tested before merging

    the Docker image is always up‑to‑date

3. Artifact Flow
Build job produces:
Artifact	Purpose
Test reports	Debugging failing builds
Application JAR	Used for deployment
Docker image	Published to GHCR
Deploy job consumes:
Input	Source
Docker image	GHCR
Application JAR	Build job artifacts (optional future use)
4. Security Considerations

    GHCR credentials stored in GitHub Secrets

    Docker login uses --password-stdin

    Secrets masked in logs

    Runners are ephemeral (no persistence)

    No credentials stored in the repository

Future enhancements:

    secret scanning

    dependency vulnerability scanning

    container image scanning

5. Future Roadmap
CI Enhancements

    SonarQube Quality Gate

    SAST scanning

    Dependency scanning

    Multi-stage Docker builds

    Build caching improvements

CD Enhancements

    staging environment deployment

    production deployment

    Kubernetes integration

    GitOps workflow

    blue/green or canary deployments

    automated rollback

Summary

The CI/CD pipeline provides a solid foundation for automated build, test, packaging, and validation of the CoreService application.
It is intentionally modular to support future DevSecOps enhancements and production‑grade deployment workflows.