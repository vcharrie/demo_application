Technical Specification – v3 (DIFF ONLY)
All new content introduced in v3 — nothing else
1. Technical Architecture Overview

<!-- 🆕 v3 addition -->
1.2 High‑Level Architecture — New Cross‑Cutting Components

    Secrets configuration validator

    Security configuration module

    Enhanced secure logging rules

    CI/CD security scanning workflows

<!-- 🆕 v3 addition -->
1.3 Runtime Architecture — Container Hardening

    Application must run as a non‑root user

    Docker image must include OCI labels

    Container image must be scanned using Trivy

<!-- 🆕 v3 addition -->
1.6 Build & CI/CD — Security Enhancements

    Add SAST (CodeQL or SonarCloud)

    Add SCA (OWASP Dependency Check)

    Add Container scanning (Trivy)

    Pipeline must fail on critical vulnerabilities

    Upload security reports as CI artifacts

    Introduce a dedicated security stage

<!-- 🆕 v3 addition -->
1.7 Security (v3 Scope)

    Mandatory externalized secrets

    Fail‑fast startup validation

    Secure logging rules

    Enforcement of standardized error behavior

2. API Technical Details

<!-- 🆕 v3 addition -->
2.3 Input Validation — Technical Guarantees

    Centralized interception of validation failures

    Standardized error mapping

    Validation logs must not include raw user input

<!-- 🆕 v3 addition -->
2.5 Error Handling — Security Reinforcement

    Stricter sanitization of error messages

    No internal exception messages exposed

    New security‑related error mappings

<!-- 🆕 v3 addition -->
2.8 Security — v3 Enhancements

    Mandatory externalized secrets

    Startup fail‑fast if secrets missing

    Secure logging rules

    No logging of sensitive data

    Enforced safe error responses

3. Application Architecture

<!-- 🆕 v3 addition -->
3.2 API Layer — New Responsibilities

    Centralized validation error mapping

    Sanitized logging of API errors

<!-- 🆕 v3 addition -->
3.3 Service Layer — New Security Enforcement

    Reject malformed UUIDs and invalid identifiers

5. Security Architecture (v3 Scope)

<!-- 🆕 v3 section -->
5.1 Security Philosophy (v3)

    Application‑level security hardening

    DevSecOps automation

    Safe defaults

    Code, dependency, and container scanning

<!-- 🆕 v3 addition -->
5.3 Input Validation — v3 Enhancements

    Strict UUID validation

    Centralized validation error mapping

    Sanitized validation logs

<!-- 🆕 v3 addition -->
5.4 Error Handling — v3 Enhancements

    Stricter sanitization

    No internal exception messages

    Standardized error codes

<!-- 🆕 v3 addition -->
5.5 Logging Security — v3 Enhancements

    No logging of raw user input

    No logging of secrets or config values

    Mandatory sanitization of error logs

    Enforced safe logging patterns

<!-- 🆕 v3 addition -->
5.7 Dependency Security — NEW

    Introduce SCA (OWASP Dependency Check)

    Fail on critical vulnerabilities

    Generate dependency vulnerability reports

<!-- 🆕 v3 addition -->
5.8 Container Security — NEW

    Non‑root container execution

    Trivy container scanning

    OCI labels

    Fail on critical container vulnerabilities

6. Error Handling Architecture

<!-- 🆕 v3 addition -->
6.1–6.7 v3 Enhancements

    Stricter sanitization

    New security‑related error mappings

    Enhanced logging rules

7. Logging & Observability

<!-- 🆕 v3 addition -->
7.1–7.9 v3 Enhancements

    Secure logging rules

    No sensitive data in logs

    Sanitized error logs

8. Testing Strategy

<!-- 🆕 v3 addition -->
8.2–8.5 v3 Enhancements

Add tests for:

    invalid UUID formats

    missing secrets (startup fail‑fast)

    standardized error responses

    validation edge cases

    security‑related behaviors

9. CI/CD Pipeline (v3)

<!-- 🆕 v3 section -->
9.1 Overview

Security scanning and enforcement added.

<!-- 🆕 v3 addition -->
9.3 Pipeline Stages (NEW)

    Security – SAST

    Security – SCA

    Build

    Test

    Docker Build

    Container Scan (Trivy)

    Run & Smoke Test

<!-- 🆕 v3 addition -->
9.4 Failure Conditions (NEW)

Pipeline must fail if:

    SAST finds critical vulnerabilities

    SCA finds critical vulnerabilities

    Trivy finds critical vulnerabilities

    Required secrets are missing

    Smoke tests fail

<!-- 🆕 v3 addition -->
9.5 Artifacts (NEW)

    SAST reports

    SCA reports

    Trivy reports

    Test reports

10. Containerization

<!-- 🆕 v3 addition -->
10.1–10.10 v3 Enhancements

    Non‑root execution

    OCI labels

    Mandatory Trivy scanning

    Improved multi‑stage Dockerfile security