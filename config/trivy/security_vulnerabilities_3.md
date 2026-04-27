Security Vulnerability Assessment Report

CoreService — Java Dependencies (Embedded JARs)
Version: ab1e841  
Date: 27/04/2026 – 16:52
1. Purpose

This document provides a formal assessment of all CRITICAL and HIGH Java‑level vulnerabilities identified in the CoreService application JAR.
This represents the second scan, performed after OS‑level remediation and focusing exclusively on Java dependencies embedded in the Spring Boot application.
2. Scope

The assessment covers:

    Vulnerabilities detected in embedded JARs (Tomcat, Spring Boot, Spring Security, Spring Framework)

    Severity levels: CRITICAL and HIGH

    Only Java dependencies packaged inside the application JAR

    Excludes OS‑level vulnerabilities (covered in a separate document)

3. Security Policy

    CRITICAL vulnerabilities must be remediated immediately.

    HIGH vulnerabilities must be remediated within the current development sprint.

    When a fix exists, the dependency must be updated.

    Responsibility for remediation lies with the application layer.

4. Summary of Findings

    Total CRITICAL vulnerabilities: 3

    Total HIGH vulnerabilities: 13

    All vulnerabilities have a fix available.

    No VEX justification is required for this scan.

🔎 Tomcat Version Selection Note

For all Tomcat vulnerabilities, the selected fixed version corresponds to the latest 10.1.x release listed in the Trivy report.

This is required because:

    Spring Boot 3.2.5 is built on Jakarta EE 10

    Jakarta EE 10 requires Tomcat 10.1.x

    Tomcat 9.0.x (Servlet API) and 11.0.x (Jakarta EE 11) are incompatible with Spring Boot 3.x

Therefore, only 10.1.x fixed versions are used in the table below.
5. Detailed Vulnerability Table
5.1 CRITICAL Vulnerabilities
CVE	Package	Installed Version	Fixed Version (Selected)	Title	Decision	Responsibility	Action	Deadline
CVE‑2026‑29145	tomcat‑embed‑core	10.1.35	10.1.53	Authentication bypass due to CLIENT_CERT soft fail	Patch	App	Override Tomcat version in Maven	Immediate
CVE‑2024‑38821	spring‑security‑web	6.2.x (via Boot 3.2.5)	6.2.7	Authorization bypass in WebFlux static resources	Patch	App	Upgrade Spring Security	Immediate
CVE‑2026‑22732	spring‑security‑web	6.2.x	6.5.9	Security policy bypass & info disclosure	Patch	App	Upgrade Spring Security	Immediate
5.2 HIGH Vulnerabilities
🔹 Tomcat (embedded)
CVE	Package	Installed Version	Fixed Version (Selected)	Title	Decision	Responsibility	Action	Deadline
CVE‑2025‑48988	tomcat‑embed‑core	10.1.35	10.1.42	DoS in multipart upload	Patch	App	Override Tomcat version	Current sprint
CVE‑2025‑48989	tomcat‑embed‑core	10.1.35	10.1.44	HTTP/2 “MadeYouReset” DoS	Patch	App	Override Tomcat version	Current sprint
CVE‑2025‑52520	tomcat‑embed‑core	10.1.35	10.1.43	Denial of service	Patch	App	Override Tomcat version	Current sprint
CVE‑2025‑53506	tomcat‑embed‑core	10.1.35	10.1.43	Denial of service	Patch	App	Override Tomcat version	Current sprint
CVE‑2025‑55752	tomcat‑embed‑core	10.1.35	10.1.45	Directory traversal with possible RCE	Patch	App	Override Tomcat version	Current sprint
CVE‑2026‑24734	tomcat‑embed‑core	10.1.35	10.1.52	Certificate revocation bypass (OCSP)	Patch	App	Override Tomcat version	Current sprint
CVE‑2026‑34483	tomcat‑embed‑core	10.1.35	10.1.54	Information disclosure via JsonAccessLogValve	Patch	App	Override Tomcat version	Current sprint
CVE‑2026‑34487	tomcat‑embed‑core	10.1.35	10.1.54	Information disclosure via log files	Patch	App	Override Tomcat version	Current sprint
🔹 Spring Boot
CVE	Package	Installed Version	Fixed Version	Title	Decision	Responsibility	Action	Deadline
CVE‑2025‑22235	spring‑boot	3.2.5	3.3.11 / 3.4.5	EndpointRequest matcher vulnerability	Patch	App	Upgrade Spring Boot	Current sprint
🔹 Spring Security (Crypto)
CVE	Package	Installed Version	Fixed Version	Title	Decision	Responsibility	Action	Deadline
CVE‑2025‑22228	spring‑security‑crypto	6.2.4	6.2.10 / 6.3.8 / 6.4.4	BCryptPasswordEncoder max length bypass	Patch	App	Upgrade Spring Security	Current sprint
🔹 Spring Framework (Core)
CVE	Package	Installed Version	Fixed Version	Title	Decision	Responsibility	Action	Deadline
CVE‑2025‑41249	spring‑core	6.1.6	6.2.11	Annotation detection vulnerability	Patch	App	Upgrade Spring Framework	Current sprint
🔹 Spring MVC
CVE	Package	Installed Version	Fixed Version	Title	Decision	Responsibility	Action	Deadline
CVE‑2024‑38816	spring‑webmvc	6.1.x	6.1.13	Path traversal in RouterFunctions	Patch	App	Upgrade Spring MVC	Current sprint
CVE‑2024‑38819	spring‑webmvc	6.1.x	6.1.14	Path traversal in functional web frameworks	Patch	App	Upgrade Spring MVC	Current sprint
6. Remediation Plan

    Override Tomcat embedded to the latest compatible 10.1.x version.

    Upgrade Spring Boot, Spring Security, Spring Framework, and Spring MVC to the fixed versions listed above.

    Rebuild the application and re-scan with Trivy.

    Validate compatibility through integration tests.

    Document the updated dependency versions in the release notes.

7. Approval

This document is approved by the Security Lead and the DevSecOps team as part of the vulnerability management process.