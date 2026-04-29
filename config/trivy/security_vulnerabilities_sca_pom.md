Security Vulnerability Assessment Report

CoreService — Java Dependencies (Embedded JARs)  
Version: 8f72ce6  
Date: 28/04/2026 – 18:55
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

4. Summary of Findings

    Total CRITICAL vulnerabilities: 0

    Total HIGH vulnerabilities: 2

    All HIGH vulnerabilities have a fix available.

    No VEX justification is required (both are exploitable conditions in Actuator).

    Remediation requires upgrading Spring Boot, which will transitively upgrade Spring Security, Spring Framework, and Spring MVC.

5. Detailed Vulnerability Table
5.1 CRITICAL Vulnerabilities

(None detected in this scan)
5.2 HIGH Vulnerabilities
Spring Boot Actuator
CVE	Package	Installed Version	Fixed Version(s)	Title	Decision	Action
CVE‑2026‑22731	spring-boot-starter-actuator	3.4.5	3.4.15	Authentication bypass when endpoint path overlaps with Health Group path	Patch	Upgrade Spring Boot
CVE‑2026‑22733	spring-boot-starter-actuator	3.4.5	3.4.15	Authentication bypass via CloudFoundry Actuator endpoints	Patch	Upgrade Spring Boot
6. Remediation Plan

    Upgrade Spring Boot from 3.4.5 to 3.4.13.  
    This upgrade automatically pulls fixed versions of:

        Spring Security

        Spring Framework

        Spring MVC

        Tomcat 10.1.x (compatible with Jakarta EE 10)

    Rebuild the application and re-run Trivy.

    Update this document with the results of the next scan (D4).

7. Approval

This document is approved by the Security Lead and the DevSecOps team as part of the vulnerability management process.