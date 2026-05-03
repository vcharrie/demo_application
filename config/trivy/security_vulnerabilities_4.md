Security Vulnerability Assessment Report

CoreService — Java Dependencies (Embedded JARs)
Version: ab1e841  
Date: 27/04/2026 – 16:52
1. Purpose

This document provides a formal assessment of all CRITICAL and HIGH Java‑level vulnerabilities identified in the CoreService application JAR during the third security scan (D3).
This scan was performed after OS‑level remediation and focuses exclusively on Java dependencies embedded in the Spring Boot application.
2. Scope

The assessment covers:

    Vulnerabilities detected in embedded JARs (Spring Boot, Spring Security, Spring Framework, Spring MVC)

    Severity levels: CRITICAL and HIGH

    Only Java dependencies packaged inside the application JAR

    Excludes OS‑level vulnerabilities (covered in separate documents D1 and D2)

3. Security Policy

    CRITICAL vulnerabilities must be remediated immediately.

    HIGH vulnerabilities must be remediated within the current development sprint.

    When a fix exists, the dependency must be updated.

    Responsibility for remediation lies with the application layer.

4. Summary of Findings (D3)

    Total CRITICAL vulnerabilities: 2

    Total HIGH vulnerabilities: 5

    All vulnerabilities have a fix available.

    No VEX justification is required.

    Remediation requires upgrading Spring Boot, which will transitively upgrade Spring Security, Spring Framework, and Spring MVC.

5. Detailed Vulnerability Table
5.1 CRITICAL Vulnerabilities
CVE	Package	Installed Version	Fixed Version(s)	Title	Decision	Action
CVE‑2024‑38821	spring-security-web	6.2.x	6.2.7	Authorization bypass in WebFlux static resources	Patch	Upgrade Spring Security
CVE‑2026‑22732	spring-security-web	6.2.x	6.5.9	Security policy bypass & information disclosure	Patch	Upgrade Spring Security
5.2 HIGH Vulnerabilities
Spring Boot
CVE	Package	Installed Version	Fixed Version(s)	Decision	Action
CVE‑2025‑22235	spring-boot	3.2.5	3.3.11 / 3.4.5	Patch	Upgrade Spring Boot
Spring Security (Crypto)
CVE	Package	Installed Version	Fixed Version(s)	Decision	Action
CVE‑2025‑22228	spring-security-crypto	6.2.4	6.2.10 / 6.3.8 / 6.4.4	Patch	Upgrade Spring Security
Spring Framework (Core)
CVE	Package	Installed Version	Fixed Version(s)	Decision	Action
CVE‑2025‑41249	spring-core	6.1.6	6.2.11	Patch	Upgrade Spring Framework
Spring MVC
CVE	Package	Installed Version	Fixed Version(s)	Decision	Action
CVE‑2024‑38816	spring-webmvc	6.1.x	6.1.13	Patch	Upgrade Spring MVC
CVE‑2024‑38819	spring-webmvc	6.1.x	6.1.14	Patch	Upgrade Spring MVC
6. Remediation Plan (D3)

    Upgrade Spring Boot from 3.2.5 to 3.3.11 (recommended) or 3.4.5.

        This upgrade automatically pulls fixed versions of:

            Spring Security

            Spring Framework

            Spring MVC

            Tomcat 10.1.x (compatible with Jakarta EE 10)

    Remove any manual overrides for Spring Security / Framework unless required.

    Keep the Tomcat override to 10.1.54 (compatible with Spring Boot 3.3.x and 3.4.x).

    Rebuild the application and re-run Trivy.

    Update this document with the results of the next scan (D4).

7. Approval

This document is approved by the Security Lead and the DevSecOps team as part of the vulnerability management process.