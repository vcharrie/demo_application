Security Vulnerability Assessment Report

CoreService — Ubuntu 24.04 Base Image
Version: ad0fc6bff1635dd477b01a8a3b406b9091672e7a
Date: <BUILD_DATE>
1. Purpose

This document provides a formal assessment of all CRITICAL and HIGH security vulnerabilities identified in the CoreService container image based on Ubuntu 24.04 after applying all available OS package updates.

This represents the second scan, performed after remediation actions from the first scan.
2. Scope

The assessment covers:

    Operating system–level vulnerabilities detected by Trivy

    Severity levels: CRITICAL and HIGH

    Packages originating from the Ubuntu 24.04 base image

    Excludes Medium and Low vulnerabilities

Application‑level vulnerabilities remain out of scope.
3. Security Policy

    CRITICAL vulnerabilities must be remediated immediately, unless no fix is available.

    HIGH vulnerabilities must be remediated within the current development sprint.

    When no fix is available, a VEX justification must be documented.

    Responsibility for remediation lies with the OS layer (Ubuntu base image).

4. Summary of Findings (Second Scan)

    Total CRITICAL vulnerabilities: 3

    Total HIGH vulnerabilities: 14

    All vulnerabilities originate from OS packages (util-linux, dpkg, glibc, libexpat, libgcrypt).

    All vulnerabilities have no fix available in Ubuntu 24.04 at scan time.

    No vulnerabilities affect application code.

5. Detailed Vulnerability Table (Second Scan)
5.1 CRITICAL Vulnerabilities
CVE	Package	Installed Version	Fixed Version	Decision	Responsibility	Action	Deadline
CVE‑2026‑27456	bsdutils	1:2.39.3‑9ubuntu6.5	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑2219	dpkg	1.22.6ubuntu6.5	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑27456	libblkid1	2.39.3‑9ubuntu6.5	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
5.2 HIGH Vulnerabilities
CVE	Package	Installed Version	Fixed Version	Decision	Responsibility	Action	Deadline
CVE‑2026‑4046	libc-bin	2.39‑0ubuntu8.7	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑4437	libc-bin	2.39‑0ubuntu8.7	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑4438	libc-bin	2.39‑0ubuntu8.7	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑4046	libc6	2.39‑0ubuntu8.7	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑4437	libc6	2.39‑0ubuntu8.7	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑4438	libc6	2.39‑0ubuntu8.7	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2025‑66382	libexpat1	2.6.1‑2ubuntu0.4	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2024‑2236	libgcrypt20	1.10.3‑2build1	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑27456	libmount1	2.39.3‑9ubuntu6.5	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑27456	libsmartcols1	2.39.3‑9ubuntu6.5	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑27456	libuuid1	2.39.3‑9ubuntu6.5	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑4046	locales	2.39‑0ubuntu8.7	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
CVE‑2026‑4437	locales	2.39‑0ubuntu8.7	—	VEX: No Fix Available	OS	Temporarily ignored via .trivyignore.yaml	Monthly review
6. Remediation Plan (Second Scan)

    All vulnerabilities identified in this scan currently have no upstream fix in Ubuntu 24.04.

    All entries are temporarily ignored using .trivyignore.yaml with monthly review.

    Continue monitoring Ubuntu Security Notices (USN).

    Rebuild the image and re-scan as soon as fixes become available.

    Maintain traceability between first and second scan documents.

7. Approval

This document is approved by the Security Lead and the DevSecOps team as part of the vulnerability management process.