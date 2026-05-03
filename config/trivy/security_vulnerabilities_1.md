# Security Vulnerability Assessment Report  
CoreService — Ubuntu 24.04 Base Image  
Version: <commit SHA>  
Date: <BUILD_DATE>

## 1. Purpose
This document provides a formal assessment of all **CRITICAL** and **HIGH** security vulnerabilities identified in the CoreService container image based on Ubuntu 24.04.  
The objective is to ensure traceability, accountability, and compliance with internal security policies and industry standards.

## 2. Scope
The assessment covers:
- Operating system–level vulnerabilities detected by Trivy
- Severity levels: **CRITICAL** and **HIGH**
- Packages originating from the Ubuntu 24.04 base image
- Excludes Medium and Low vulnerabilities, as they are not considered blocking under the current security policy

Application‑level vulnerabilities are out of scope for this document.

## 3. Security Policy
- **CRITICAL vulnerabilities must be remediated immediately**, unless no fix is available.  
- **HIGH vulnerabilities must be remediated within the current development sprint.**  
- When no fix is available, a **VEX (Vulnerability Exploitability eXchange) justification** must be documented.  
- Responsibility for remediation lies with the **OS layer (Ubuntu base image)**.

## 4. Summary of Findings
- Total CRITICAL vulnerabilities: **3**  
- Total HIGH vulnerabilities: **14**  
- All vulnerabilities originate from OS packages (curl, dpkg, gpgv, util-linux).  
- No vulnerabilities affect application code.

## 5. Detailed Vulnerability Table

### 5.1 CRITICAL Vulnerabilities

| CVE | Package | Installed Version | Fixed Version | Decision | Responsibility | Action | Deadline |
|-----|---------|------------------|---------------|----------|----------------|--------|----------|
| CVE-2026-27456 | bsdutils | 1:2.39.3-9ubuntu6.1 | — | **VEX: No Fix Available** | OS | Temporarily ignored via `.trivyignore.yaml` | Monthly review |
| CVE-2025-14017 | curl | 8.5.0-2ubuntu10.4 | 8.5.0-2ubuntu10.7 | Patch | OS | Update curl when fix is available in Ubuntu repositories | Immediate |
| CVE-2026-1965 | curl | 8.5.0-2ubuntu10.4 | 8.5.0-2ubuntu10.8 | Patch | OS | Update curl when fix is available in Ubuntu repositories | Immediate |

### 5.2 HIGH Vulnerabilities

| CVE | Package | Installed Version | Fixed Version | Decision | Responsibility | Action | Deadline |
|-----|---------|------------------|---------------|----------|----------------|--------|----------|
| CVE-2026-3783 | curl | 8.5.0-2ubuntu10.4 | 10.8 | Patch | OS | Update curl | Current sprint |
| CVE-2024-11053 | curl | 8.5.0-2ubuntu10.4 | 10.6 | Patch | OS | Update curl | Current sprint |
| CVE-2024-9681 | curl | 8.5.0-2ubuntu10.4 | 10.5 | Patch | OS | Update curl | Current sprint |
| CVE-2025-0167 | curl | 8.5.0-2ubuntu10.4 | 10.8 | Patch | OS | Update curl | Current sprint |
| CVE-2025-10148 | curl | 8.5.0-2ubuntu10.4 | 10.7 | Patch | OS | Update curl | Current sprint |
| CVE-2025-14524 | curl | 8.5.0-2ubuntu10.4 | 10.7 | Patch | OS | Update curl | Current sprint |
| CVE-2025-14819 | curl | 8.5.0-2ubuntu10.4 | 10.7 | Patch | OS | Update curl | Current sprint |
| CVE-2025-15079 | curl | 8.5.0-2ubuntu10.4 | 10.7 | Patch | OS | Update curl | Current sprint |
| CVE-2025-15224 | curl | 8.5.0-2ubuntu10.4 | 10.7 | Patch | OS | Update curl | Current sprint |
| CVE-2026-3784 | curl | 8.5.0-2ubuntu10.4 | 10.8 | Patch | OS | Update curl | Current sprint |
| CVE-2026-2219 | dpkg | 1.22.6ubuntu6.1 | — | Patch | OS | Apply fix when available | Current sprint |
| CVE-2025-6297 | dpkg | 1.22.6ubuntu6.1 | 6.5 | Patch | OS | Update dpkg | Current sprint |
| CVE-2025-68973 | gpgv | 2.4.4-2ubuntu17 | 17.4 | Patch | OS | Update gpgv | Current sprint |
| CVE-2025-30258 | gpgv | 2.4.4-2ubuntu17 | 17.2 | Patch | OS | Update gpgv | Current sprint |

## 6. Remediation Plan
1. Rebuild the container image once updated Ubuntu packages become available.  
2. Ensure the Dockerfile includes: apt-get update && apt-get upgrade -y
3. Re-scan the updated image using Trivy.  
4. Update this document accordingly.  
5. Review VEX entries monthly until fixes are published.

## 7. Approval
This document is approved by the Security Lead and the DevSecOps team as part of the vulnerability management process.  
