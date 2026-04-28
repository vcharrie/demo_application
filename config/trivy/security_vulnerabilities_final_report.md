Security Scan – Final Consolidated Report (Trivy + Java + Container)

Application : CoreService  
Version : v3.x  
Date : 28/04/2026  
Scope : Container Image + Java Dependencies (Spring Boot)
1. Objective

This report summarizes all vulnerabilities identified during the Trivy scanning phase (OS + Java + runtime), the actions taken to remediate them, the VEX‑justified ignored findings, and the dependency upgrades applied to the pom.xml to ensure a fully patched and secure build.
2. Summary of Findings
2.1 OS‑Level Vulnerabilities

    Initial scan detected multiple HIGH/CRITICAL CVEs in the base image (Ubuntu packages, libc, openssl, zlib…)

    Actions:

        Updated base image to latest patch level

        Added .trivyignore.yaml entries for non‑exploitable OS CVEs (no network exposure, no attack surface)

        Monthly review policy applied

➡️ Status: All OS CVEs resolved or VEX‑justified
2.2 Runtime Vulnerabilities (Tomcat)

    Several CRITICAL CVEs detected in embedded Tomcat (10.1.x)

    Actions:

        Forced Tomcat version to 10.1.54 (latest patched)

        Verified compatibility with Spring Boot 3.4.x

➡️ Status: All Tomcat CVEs resolved
2.3 Java Dependencies (Spring Boot / Framework / Security)
Identified CVEs
Component	CVE	Severity	Fixed Version
spring-core	CVE‑2025‑41249	HIGH	6.2.11
spring-security-core	CVE‑2025‑41232	CRITICAL	6.4.6
spring-security-core	CVE‑2025‑41248	HIGH	6.4.10
spring-security-web	CVE‑2026‑22732	CRITICAL	6.5.9
Actions Taken

    Upgraded Spring Boot → 3.4.5 (BOM update)

    Overrode Spring Framework Core → 6.2.11

    Overrode Spring Security Core → 6.4.10

    Overrode Spring Security Web → 6.5.9

➡️ Status: All Java CVEs resolved
3. VEX Justifications (Ignored CVEs)
3.1 OS CVEs (Ignored via .trivyignore.yaml)

    CVEs with no attack surface in a minimal container:

        No shell access

        No package manager

        No exposed vulnerable binaries

        No network‑reachable vector

    Monthly review policy applied

3.2 Java CVEs

    None ignored  
    All Java CVEs were remediated via dependency upgrades.

4. Dependency Upgrades Applied (pom.xml)
4.1 Spring Boot
xml

<version>3.4.5</version>

4.2 Spring Framework Core
xml

spring-core:6.2.11

4.3 Spring Security
xml

spring-security-core:6.4.10
spring-security-web:6.5.9

4.4 Tomcat
xml

tomcat-embed-core:10.1.54

➡️ All patched versions are compatible and validated by build + deploy
5. Final Status
Layer	Status
OS	✔️ Clean (patched + VEX)
Tomcat	✔️ Clean
Spring Boot	✔️ Clean
Spring Framework	✔️ Clean
Spring Security	✔️ Clean
Container Image	✔️ Clean
Build	✔️ Passed
Deploy	✔️ Passed

➡️ The application is now fully patched, stable, and compliant.