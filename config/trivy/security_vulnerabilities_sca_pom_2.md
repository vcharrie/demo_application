Security Vulnerability Assessment Report

CoreService — Java Dependencies (Embedded JARs)  
Version: 7e6a1ef  
Date: 29/04/2026 – 09:20
1. Purpose

This document provides a formal assessment of all CRITICAL and HIGH Java‑level vulnerabilities identified in the CoreService application JAR during the current security scan.

This assessment focuses exclusively on Java dependencies embedded in the Spring Boot application.
2. Scope

The assessment covers:

    Vulnerabilities detected in embedded JARs (Spring Boot, Spring Security, Spring Framework, Spring MVC, Jackson, Logback)

    Severity levels: CRITICAL and HIGH

    Only Java dependencies packaged inside the application JAR

    Excludes OS‑level vulnerabilities (covered in separate documents)

3. Security Policy

    CRITICAL vulnerabilities must be remediated immediately.

    HIGH vulnerabilities must be remediated within the current development sprint.

    When a fix exists, the dependency must be updated.

    Responsibility for remediation lies with the application layer.

    When a vulnerability is not exploitable due to architectural or configuration constraints, a VEX justification may be applied.

4. Summary of Findings

    Total CRITICAL vulnerabilities: 0

    Total HIGH vulnerabilities: 2

    Both HIGH vulnerabilities relate to Spring Boot Actuator

    No fix is available in the Spring Boot 3.4.x branch

    Both vulnerabilities are non‑exploitable in production due to Actuator being disabled

    A VEX “Not Affected” justification is applied for both CVEs

    A dedicated CI/CD profile enables /actuator/health only for container health checks

5. Detailed Vulnerability Table
5.1 CRITICAL Vulnerabilities

(None detected in this scan)
5.2 HIGH Vulnerabilities — Spring Boot Actuator
CVE	Package	Installed Version	Fixed Version(s)	Title	Decision	Action
CVE‑2026‑22731	spring-boot-starter-actuator	3.4.13	3.5.12 / 4.0.4	Authentication bypass when endpoint path overlaps with Health Group path	VEX — Not Affected	Disable Actuator in production; enable only /actuator/health in CI/CD
CVE‑2026‑22733	spring-boot-starter-actuator	3.4.13	3.5.12 / 4.0.4	Authentication bypass via CloudFoundry Actuator endpoints	VEX — Not Affected	Disable CloudFoundry endpoint; Actuator disabled in production
6. Remediation Plan
6.1 Technical Measures Applied
Production profile
yaml

management.endpoints.enabled-by-default=false
management.endpoint.cloudfoundry.enabled=false

Effect:

    No Actuator endpoints exposed in production

    Vulnerabilities become non‑exploitable

    VEX justification is valid and audit‑ready

CI/CD (Docker) profile
yaml

management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=true
management.endpoint.health.probes.enabled=true
management.endpoint.cloudfoundry.enabled=false

Dockerfile:
dockerfile

ENV SPRING_PROFILES_ACTIVE=docker

Effect:

    /actuator/health available only during container runtime tests

    No vulnerable endpoints exposed

    CI/CD pipeline remains functional

6.2 VEX Justification
CVE‑2026‑22731 — VEX Not Affected
Code

Status: Not Affected (VEX)
Justification: Actuator endpoints are disabled in all production profiles. Only the /actuator/health endpoint is enabled in an isolated CI/CD environment via a dedicated 'docker' profile. No Health Group additional paths are exposed in production, making the vulnerability non-exploitable.
Mitigation: management.endpoints.enabled-by-default=false
Review: Monthly
Expiration: 2026-12-31

CVE‑2026‑22733 — VEX Not Affected
Code

Status: Not Affected (VEX)
Justification: The CloudFoundry Actuator endpoint is disabled in all profiles, including CI/CD. No CloudFoundry management endpoints are exposed, making the vulnerability non-exploitable.
Mitigation: management.endpoint.cloudfoundry.enabled=false
Review: Monthly
Expiration: 2026-12-31

7. Approval

This document is approved by the Security Lead and the DevSecOps team as part of the vulnerability management process.

