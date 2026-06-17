# CD – Deploy Pipeline Documentation
*(File: `docs/ci-cd/cd-build-pipeline.md`)*

## Overview

This document describes the **deployment pipeline** for the CoreService application.  
The deployment job runs after the build job and performs a lightweight validation of the Docker image by:

- pulling the image from GitHub Container Registry (GHCR)  
- running the container locally on the GitHub Actions runner  
- performing a smoke test on the `/actuator/health` endpoint  
- stopping the container after validation  

This ensures that the Docker image is functional before being used in later environments.

---

## 1. Deployment Job Triggers

The deploy job runs automatically when:

- the **build job completes successfully**  
- the workflow is triggered by a push to `main` or `v3_dev`  
- the workflow is triggered by a pull request  

The job is defined with:

```yaml
needs: build
