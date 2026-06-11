# Technical Specification – v3 (Full Document with v3 Additions Marked)

1. **Technical Architecture Overview**
1.1 Application Type
1.2 High‑Level Architecture
1.3 Runtime Architecture
1.4 Persistence
1.5 API Style
1.6 Build & CI/CD
1.7 Security (v2 scope)
1.8 Observability (v2 scope)
2. **API Technical Details**
2.1 API Style
2.2 Request and Response Format
2.3 Input Validation
2.4 HTTP Status Codes
2.5 Error Response Format
2.6 Idempotency
2.7 API Versioning
2.8 Security (v2 scope)
3. **Application Architecture (Technical)**
3.1 Layered Architecture
3.2 API Layer
3.3 Service Layer
3.4 Domain Layer
3.5 Infrastructure Layer
3.6 Layer Interaction
3.7 Benefits
4. **Persistence Layer**
4.1 Overview
4.2 Data Model
4.3 Identifier Strategy
4.4 Repository Abstraction
4.5 Repository Implementation
4.6 H2 Database Behavior
4.7 Persistence Constraints
4.8 Benefits
5. **Security Architecture (v2 Scope)**
5.1 Security Philosophy
5.2 Authentication & Authorization
5.3 Input Validation
5.4 Error Handling & Information Exposure
5.5 Logging Security
5.6 Transport Security
5.7 Dependency Security
5.8 Container Security
5.9 Limitations
6. **Error Handling Architecture**
6.1 Overview
6.2 Error Categories
6.3 Global Exception Handler
6.4 Error Response Format
6.5 Exception Mapping
6.6 Security Considerations
6.7 Logging Strategy
6.8 Benefits
7. **Logging & Observability (v2 Scope)**
7.1 Overview
7.2 Logging Objectives
7.3 Logging Scope
7.4 Log Levels
7.5 Log Format
7.6 Observability Scope
7.7 CI Logging
7.8 Security Considerations
7.9 Benefits
8. **Testing Strategy**
8.1 Overview
8.2 Types of Tests
8.3 Coverage Expectations
8.4 Test Data Strategy
8.5 Error Scenario Testing
8.6 Mocking Strategy
8.7 CI Integration
8.8 Benefits
9. **CI/CD Pipeline (v2)**
9.1 Overview
9.2 Objectives
9.3 Triggers
9.4 Pipeline Stages
9.5 Pipeline Structure
9.6 Artefacts
9.7 Failure Conditions
9.8 Limitations
9.9 Benefits
10. **Containerization**
10.1 Overview
10.2 Objectives
10.3 Dockerfile Structure
10.4 Runtime Configuration
10.5 Local Execution
10.6 CI Execution
10.7 Image Optimization
10.8 Security Considerations
10.9 Limitations
10.10 Benefits

## **1. Technical Architecture Overview**

### **1.1 Application Type**

The application remains a lightweight, sector‑neutral REST API built with a layered architecture.
It exposes CRUD operations on a single business entity called **Resource**.

It is designed to be:

- simple to understand,
- easy to maintain,
- production‑ready in terms of structure,
- aligned with modern DevSecOps practices.

<!-- 🆕 v3 addition -->
**v3 introduces security‑focused technical components and DevSecOps tooling**, strengthening the application’s robustness and security posture.

### **1.2 High‑Level Architecture**

The application continues to follow a **four‑layer logical architecture**:

- **API Layer**
- **Service Layer**
- **Domain Layer**
- **Infrastructure Layer**

<!-- 🆕 v3 addition -->
v3 adds **cross‑cutting technical components**:

- Secrets configuration validator
- Security configuration module
- Enhanced logging rules
- CI/CD security scanning workflows

These additions do not modify the layered structure but enhance its behavior.

### **1.3 Runtime Architecture**

The application runs inside a Docker container.

<!-- 🆕 v3 addition -->
v3 introduces:

- **non‑root container execution**,
- **OCI labels**,
- **mandatory container image scanning**.

### **1.4 Persistence**

Unchanged from v2: embedded H2 database, in‑memory, reset at startup.

### **1.5 API Style**

REST conventions remain unchanged.

### **1.6 Build & CI/CD**

The GitHub Actions pipeline performs:

- checkout
- build
- tests
- Docker image build

<!-- 🆕 v3 addition -->
v3 adds:

- **SAST (CodeQL or SonarCloud)**
- **SCA (OWASP Dependency Check)**
- **Container scanning (Trivy)**
- **Fail‑on‑critical‑vulnerabilities policy**
- **Security reports as CI artifacts**
- **Dedicated “security” stage in the pipeline**

### **1.7 Security (v2 scope)**

v2 included minimal security.

<!-- 🆕 v3 addition -->
v3 introduces **full application‑level security hardening**, including:

- strict secrets management
- fail‑fast startup validation
- secure logging rules
- standardized error behavior enforcement
- DevSecOps scanning tools

### **1.8 Observability (v2 scope)**

Basic logging remains.

<!-- 🆕 v3 addition -->
v3 strengthens logging security (no sensitive data, sanitized logs).
Structured JSON logs remain planned for v4.

# **2. API Technical Details**

## **2.1 API Style**

Unchanged.

## **2.2 Request and Response Format**

Unchanged.

## **2.3 Input Validation**

Validation rules remain identical to v2.

<!-- 🆕 v3 addition -->
v3 enforces **technical guarantees**:

- validation failures must be intercepted centrally
- validation errors must map to standardized error responses
- validation logs must not include raw user input

## **2.4 HTTP Status Codes**

Unchanged.

## **2.5 Error Handling**

Unchanged structure.

<!-- 🆕 v3 addition -->
v3 enforces:

- stricter sanitization of error messages
- prohibition of leaking internal exception messages
- consistent mapping for all new security‑related errors

## **2.6 Idempotency**

Unchanged.

## **2.7 API Versioning**

Unchanged.

## **2.8 Security (v2 scope)**

v2 had minimal security.

<!-- 🆕 v3 addition -->
v3 introduces:

- mandatory externalized secrets
- startup validation of required secrets
- secure logging rules
- prohibition of logging sensitive data
- enforcement of safe error responses

# **3. Application Architecture (Technical)**

## **3.1 Layered Architecture**

Unchanged.

## **3.2 API Layer**

Unchanged responsibilities.

<!-- 🆕 v3 addition -->
v3 adds:

- centralized validation error mapping
- sanitized logging of API errors

## **3.3 Service Layer**

Unchanged.

<!-- 🆕 v3 addition -->
v3 adds:

- enforcement of functional security rules (e.g., rejecting malformed IDs)

## **3.4 Domain Layer**

Unchanged.

## **3.5 Infrastructure Layer**

Unchanged.

## **3.6 Layer Interaction**

Unchanged.

## **3.7 Benefits**

Unchanged.

## **3.8 Architectural Principles Applied**

Unchanged.

# **4. Persistence Layer**

Unchanged from v2.

# **5. Security Architecture (v3 Scope)**

<!-- 🆕 v3 section -->

## **5.1 Security Philosophy (v3)**

v3 introduces **application‑level security hardening** and **DevSecOps automation**, focusing on:

- preventing insecure configurations
- enforcing safe defaults
- scanning code, dependencies, and container images
- ensuring predictable and safe behavior under invalid input

## **5.2 Authentication & Authorization**

Unchanged (still none in v3).

## **5.3 Input Validation**

Already implemented in v2.

<!-- 🆕 v3 addition -->
v3 adds:

- strict validation of UUID formats
- centralized mapping of validation errors
- sanitized validation logs

## **5.4 Error Handling & Information Exposure**

Already implemented in v2.

<!-- 🆕 v3 addition -->
v3 adds:

- stricter sanitization rules
- prohibition of exposing internal exception messages
- enforcement of standardized error codes

## **5.5 Logging Security**

v2 had basic logging security.

<!-- 🆕 v3 addition -->
v3 adds:

- prohibition of logging user‑provided raw input
- prohibition of logging secrets or configuration values
- mandatory sanitization of error logs
- enforcement of safe logging patterns

## **5.6 Transport Security**

Unchanged (still handled externally).

## **5.7 Dependency Security**

<!-- 🆕 v3 addition -->
v3 introduces **Software Composition Analysis (SCA)**:

- OWASP Dependency Check
- fail on critical vulnerabilities
- dependency vulnerability reports

## **5.8 Container Security**

<!-- 🆕 v3 addition -->
v3 introduces:

- non‑root container execution
- Trivy container scanning
- OCI labels
- fail on critical container vulnerabilities

## **5.9 Limitations**

v3 still does not include:

- authentication
- authorization
- DAST
- rate limiting
- audit logging

# **6. Error Handling Architecture**

Unchanged from v2.

<!-- 🆕 v3 addition -->
v3 adds:

- stricter sanitization
- mapping of new security‑related errors
- enhanced logging rules

# **7. Logging & Observability**

Unchanged from v2.

<!-- 🆕 v3 addition -->
v3 adds:

- secure logging rules
- prohibition of logging sensitive data
- sanitized error logs

Structured logs remain planned for v4.

# **8. Testing Strategy**

Unchanged from v2.

<!-- 🆕 v3 addition -->
v3 adds:

- tests for security behaviors (invalid UUID, missing secrets, etc.)
- tests for standardized error responses
- tests for validation edge cases

# **9. CI/CD Pipeline (v3)**

<!-- 🆕 v3 section -->

## **9.1 Overview**

v3 significantly enhances the CI/CD pipeline with security scanning and enforcement.

## **9.2 Objectives**

- detect vulnerabilities early
- prevent insecure builds
- enforce DevSecOps best practices

## **9.3 Pipeline Stages**

<!-- 🆕 v3 addition -->
v3 pipeline stages:

1. **Security – SAST**
2. **Security – SCA**
3. **Build**
4. **Test**
5. **Docker Build**
6. **Container Scan (Trivy)**
7. **Run & Smoke Test**

## **9.4 Failure Conditions**

<!-- 🆕 v3 addition -->
The pipeline must fail if:

- SAST detects critical vulnerabilities
- SCA detects critical vulnerabilities
- Trivy detects critical vulnerabilities
- required secrets are missing
- smoke tests fail

## **9.5 Artifacts**

<!-- 🆕 v3 addition -->
Artifacts include:

- SAST reports
- SCA reports
- Trivy reports
- test reports

# **10. Containerization**

Unchanged from v2.

<!-- 🆕 v3 addition -->
v3 adds:

- non‑root execution
- OCI labels
- mandatory Trivy scanning
- improved multi‑stage Dockerfile security