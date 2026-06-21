# CI – Build Pipeline Documentation
*(File: `docs/ci-cd/ci-build-pipeline.md`)*

## Overview
This document describes the CI build pipeline for the **CoreService** application.  
The workflow automates the following tasks:

- Source checkout  
- JDK setup  
- Maven dependency caching  
- Build + tests  
- Artifact publication (test reports + JAR)  
- Docker image build  
- Docker image push to GitHub Container Registry (GHCR)

The pipeline is designed to be fast, reproducible, and aligned with DevSecOps best practices.

---

## 1. Workflow Triggers

The workflow runs automatically on:

- `push` events to `main` and `v3_dev`
- any `pull_request`

This ensures that:

- every commit is validated  
- every PR is tested before merging  
- the Docker image is always up-to-date on GHCR for deployment

---

## 2. Build Job Breakdown

### Step 1 — Checkout

Retrieves the repository content so the runner can build the application.

### Step 2 — JDK Setup

Installs **Temurin JDK 21**, ensuring consistent Java execution across all builds.

### Step 3 — Maven Cache

Caches the local Maven repository (`~/.m2`) using a key derived from all `pom.xml` files.

Benefits:

- faster builds  
- reduced network usage  
- consistent dependency resolution  

The cache is shared across all workflows in the repository.

### Step 4 — Maven Build + Tests

Runs:

```bash
mvn -DskipTests=false clean verify
