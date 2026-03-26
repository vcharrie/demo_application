# Technical Specification – v2

# **1. Technical Architecture Overview**

## **1.1 Application Type**

The application is a lightweight, sector‑neutral REST API built with a layered architecture.
It exposes CRUD operations on a single business entity called **Resource**.

The application is designed to be:

- simple to understand,
- easy to maintain,
- production‑ready in terms of structure,
- aligned with modern DevSecOps practices.

## **1.2 High‑Level Architecture**

The application follows a **four‑layer logical architecture**:

- **API Layer**: exposes REST endpoints and handles HTTP communication.
- **Service Layer**: implements functional rules and orchestrates operations.
- **Domain Layer**: contains the business entity and domain logic.
- **Infrastructure Layer**: provides persistence through a repository abstraction.

These layers are strictly separated to ensure clarity, testability, and maintainability.

## **1.3 Runtime Architecture**

The application runs inside a **Docker container**.

- **Local environment**: Docker Desktop is used to build and run the application.
- **CI environment**: GitHub Actions builds, tests, and scans the Docker image.
- **Execution model**: the application runs as a single container exposing an HTTP port.

This ensures consistent behavior across development and CI environments.

## **1.4 Persistence**

The application uses an **embedded H2 database** for v2.

- No external database is required.
- Data is stored in memory and reset at each startup.
- Persistence is abstracted through a `ResourceRepository` interface.

This keeps the implementation simple while demonstrating clean architectural boundaries.

## **1.5 API Style**

The API follows standard **REST** conventions:

- Resource‑oriented endpoints
- JSON request/response format
- Standard HTTP verbs (POST, GET, DELETE)
- Standard HTTP status codes

Endpoints:

- `POST /resources`
- `GET /resources`
- `GET /resources/{id}`
- `DELETE /resources/{id}`

## **1.6 Build & CI/CD**

The project includes a **GitHub Actions pipeline** that performs:

- code checkout
- dependency installation
- static build
- unit and integration tests
- Docker image build
- optional image scanning (introduced in v3)

This pipeline ensures code quality and reproducibility.

## **1.7 Security (v2 scope)**

Security in v2 is intentionally minimal:

- no authentication yet
- input validation
- safe error handling
- no sensitive data stored

Full security hardening is introduced in **v3**.

## **1.8 Observability (v2 scope)**

Basic logging is included:

- request handling logs
- error logs
- application startup logs

Advanced observability (metrics, correlation IDs, structured logs) arrives in **v4**.

# **2. API Technical Details**

## **2.1 API Style**

The API follows standard REST conventions:

- Resource‑oriented endpoints
- JSON request and response bodies
- Clear mapping between HTTP verbs and operations
- Predictable and consistent HTTP status codes
- Stateless interactions

Endpoints exposed:

- `POST /resources`
- `GET /resources`
- `GET /resources/{id}`
- `DELETE /resources/{id}`

## **2.2 Request and Response Format**

All requests and responses use **JSON** with UTF‑8 encoding.

### **Request body (POST /resources)**

json

`{
  "name": "string",
  "description": "string"
}`

### **Response body (Resource)**

json

`{
  "id": "string",
  "name": "string",
  "description": "string"
}`

### **Response body (List)**

json

`[
  {
    "id": "string",
    "name": "string",
    "description": "string"
  }
]`

## **2.3 Input Validation**

Input validation is performed at the API layer before invoking the service.

Validation rules:

- `name` is required
- `name` must not be empty
- `name` must not exceed 100 characters
- `description` is optional
- JSON must be syntactically valid

Invalid input results in a **400 Bad Request** response.

## **2.4 HTTP Status Codes**

The API uses standard, predictable HTTP status codes.

### **Success responses**

- **201 Created** — Resource successfully created
- **200 OK** — Resource retrieved or list returned
- **204 No Content** — Resource successfully deleted

### **Client errors**

- **400 Bad Request** — Invalid input
- **404 Not Found** — Resource does not exist
- **409 Conflict** — Resource with same name already exists (functional rule)

### **Server errors**

- **500 Internal Server Error** — Unexpected technical error

## **2.5 Error Handling**

Errors follow a consistent JSON structure to ensure clarity and debuggability.

### **Error response format**

json

`{
  "error": "string",
  "message": "string",
  "timestamp": "string",
  "path": "string"
}`

### Examples

### **Resource not found**

json

`{
  "error": "NotFound",
  "message": "Resource with id '123' not found",
  "timestamp": "2026-03-25T18:42:10Z",
  "path": "/resources/123"
}`

### **Invalid input**

json

`{
  "error": "BadRequest",
  "message": "Field 'name' must not be empty",
  "timestamp": "2026-03-25T18:42:10Z",
  "path": "/resources"
}`

## **2.6 Idempotency**

- `GET /resources` → idempotent
- `GET /resources/{id}` → idempotent
- `DELETE /resources/{id}` → idempotent
- `POST /resources` → **not** idempotent (creates a new Resource each time)

## **2.7 API Versioning**

Versioning is not introduced in v2.
The API is exposed under a single namespace:

Code

`/resources`

Versioning may be added in future releases (v3+).

## **2.8 Security (v2 scope)**

Security is intentionally minimal in v2:

- No authentication
- No authorization
- No sensitive data
- Input validation and safe error handling only

Full security hardening is introduced in **v3**.

# **3. Application Architecture (Technical)**

## **3.1 Layered Architecture**

The application is structured into four clearly separated layers:

- **API Layer**
- **Service Layer**
- **Domain Layer**
- **Infrastructure Layer**

Each layer has a well‑defined responsibility and communicates only with the layer directly below it.
This ensures maintainability, testability, and clear separation of concerns.

## **3.2 API Layer**

The API layer exposes the REST endpoints and handles all HTTP‑related concerns.

**Responsibilities:**

- Receive HTTP requests
- Validate input payloads
- Map JSON to internal models
- Call the appropriate service method
- Map service results to HTTP responses
- Produce consistent error responses

**Components:**

- `ResourceController`

This layer contains **no business logic** and **no persistence logic**.

## **3.3 Service Layer**

The service layer implements the functional rules of the application.

**Responsibilities:**

- Apply functional rules for each use case
- Orchestrate operations involving the domain and repository
- Handle domain‑level errors
- Ensure consistency of operations
- Enforce functional constraints (e.g., uniqueness checks)

**Components:**

- `ResourceService`

This layer contains **no HTTP logic** and **no persistence details**.

## **3.4 Domain Layer**

The domain layer represents the core business model of the application.

**Responsibilities:**

- Represent the Resource entity
- Enforce intrinsic domain rules (e.g., name must not be empty)
- Provide a stable model independent of technical concerns

**Components:**

- `Resource` (domain entity)

This layer contains **no orchestration logic** and **no repository access**.

## **3.5 Infrastructure Layer**

The infrastructure layer provides the technical implementation of persistence.

**Responsibilities:**

- Store and retrieve Resource entities
- Abstract the underlying database
- Provide CRUD operations
- Hide persistence details from upper layers

**Components:**

- `ResourceRepository` (interface)
- `ResourceRepositoryImpl` (H2‑based implementation)

This layer contains **no business logic** and **no functional rules**.

## **3.6 Layer Interaction**

The layers interact in a strict top‑down manner:

Code

`API Layer → Service Layer → Domain Layer → Infrastructure Layer`

- The API layer never accesses the domain or repository directly.
- The service layer never handles HTTP or persistence details.
- The domain layer is pure and independent.
- The infrastructure layer is isolated and replaceable.

This structure ensures a clean, maintainable, and testable architecture.

## **3.7 Benefits of This Architecture**

- **High testability**: each layer can be tested independently.
- **Clear responsibilities**: no mixing of concerns.
- **Easy to evolve**: persistence or API can change without affecting the domain.
- **Professional structure**: aligns with industry standards and interview expectations.
- **DevSecOps‑friendly**: clean boundaries simplify scanning, testing, and containerization.

# **4. Persistence Layer**

## **4.1 Overview**

The persistence layer provides a simple and clean abstraction for storing and retrieving `Resource` entities.
In v2, persistence is intentionally minimal and uses an **embedded H2 database**, allowing the application to run without external dependencies.

The persistence layer is fully encapsulated behind a repository interface, ensuring that the rest of the application remains independent from the underlying storage technology.

## **4.2 Data Model**

The application manages a single entity:

### **Resource**

- `id` (String) — unique identifier
- `name` (String) — required, max 100 characters
- `description` (String) — optional

This model is mapped directly to a relational table in H2.

### **Database Table (conceptual)**

| Column | Type | Constraints |
| --- | --- | --- |
| id | VARCHAR | Primary key |
| name | VARCHAR | Not null, max length 100 |
| description | VARCHAR | Nullable |

The schema is generated automatically by Spring Boot based on the entity definition.

## **4.3 Identifier Strategy**

The application uses a **String‑based unique identifier** generated at creation time.

Characteristics:

- generated in the service layer
- unique across all resources
- independent from the database
- stable across environments

This avoids database‑specific ID generation strategies and keeps the domain model portable.

## **4.4 Repository Abstraction**

The persistence layer exposes a single repository interface:

### **ResourceRepository**

Responsibilities:

- Save a Resource
- Retrieve a Resource by ID
- Retrieve all Resources
- Delete a Resource by ID
- Check for name uniqueness

The repository interface hides all persistence details from the service layer.

## **4.5 Repository Implementation**

In v2, the repository is implemented using **Spring Data JPA** with an embedded H2 database.

Characteristics:

- no manual SQL required
- automatic CRUD implementation
- in‑memory storage
- data reset at each application startup
- ideal for development and CI environments

This implementation is replaceable in future releases (e.g., PostgreSQL, MySQL, MongoDB).

## **4.6 H2 Database Behavior**

The H2 database is configured in **in‑memory mode**:

- data exists only while the application is running
- no persistence across restarts
- ideal for tests and local development
- no external dependency required

This keeps v2 lightweight and easy to run in Docker and CI.

## **4.7 Persistence Constraints**

The following constraints are enforced at the persistence level:

- primary key uniqueness
- non‑null constraint on `name`
- maximum length constraint on `name`

Functional constraints (e.g., name uniqueness across resources) are enforced in the **service layer**, not in the database.

## **4.8 Benefits of This Persistence Approach**

- **Simple**: no external database to install
- **Portable**: works identically in local and CI environments
- **Fast**: ideal for development and testing
- **Replaceable**: repository abstraction allows easy migration to a real database
- **Clean architecture**: domain and service layers remain independent from storage details

# **5. Security Architecture (v2 Scope)**

## **5.1 Security Philosophy for v2**

Security in v2 is intentionally minimal.
The goal is to:

- keep the API simple,
- avoid premature complexity,
- prepare the foundation for the full security hardening introduced in v3.

The v2 security model focuses on **input validation**, **safe error handling**, and **secure defaults**, without authentication or authorization.

## **5.2 Authentication and Authorization**

There is **no authentication** and **no authorization** in v2.

Reasons:

- v2 is a pedagogical release focused on architecture and API correctness
- no sensitive data is stored or exposed
- security mechanisms will be introduced in v3 (Basic Auth or equivalent)

This is explicitly documented to avoid ambiguity.

## **5.3 Input Validation**

Input validation is the primary security mechanism in v2.

Validation rules include:

- JSON must be syntactically valid
- `name` must not be empty
- `name` must not exceed 100 characters
- `description` is optional
- invalid input returns a **400 Bad Request**

This prevents malformed or malicious payloads from propagating into the system.

## **5.4 Error Handling and Information Exposure**

The API ensures that error responses:

- do **not** expose stack traces
- do **not** leak internal implementation details
- do **not** reveal database or framework information
- follow a consistent JSON structure

Example of safe error response:

json

`{
  "error": "BadRequest",
  "message": "Field 'name' must not be empty",
  "timestamp": "2026-03-25T18:42:10Z",
  "path": "/resources"
}`

This prevents attackers from gathering information about the system.

## **5.5 Logging Security**

Logs in v2 follow basic security principles:

- no sensitive data is logged
- no stack traces in normal logs
- errors are logged at the appropriate level
- logs do not contain user‑provided content without sanitization

Advanced logging (correlation IDs, structured logs) will be introduced in v4.

## **5.6 Transport Security**

Transport security (HTTPS) is **not enforced at the application level** in v2.

Rationale:

- v2 is executed locally and in CI only
- HTTPS termination is typically handled by infrastructure (reverse proxy, ingress, API gateway)
- full runtime security will be addressed in later releases

## **5.7 Dependency Security**

Although full SCA (Software Composition Analysis) is introduced in v3, v2 already follows good practices:

- dependencies are minimal
- no deprecated or vulnerable libraries are intentionally used
- Spring Boot manages dependency versions through its BOM
- no custom cryptography or unsafe libraries

This ensures a clean baseline before adding DevSecOps tooling.

## **5.8 Container Security (v2 scope)**

The Docker image follows basic security principles:

- minimal base image
- non‑root execution (optional in v2, mandatory in v3)
- no exposed secrets
- no unnecessary tools installed

Full container hardening and image scanning will be implemented in v3.

## **5.9 Summary of Security Limitations in v2**

v2 explicitly does **not** include:

- authentication
- authorization
- rate limiting
- audit logging
- secrets management
- encryption at rest
- advanced transport security
- SAST / SCA / container scanning

These features are introduced progressively in v3 and v4.

# **6. Error Handling Architecture**

## **6.1 Overview**

The application implements a centralized and consistent error‑handling mechanism.
All errors—whether functional, domain‑related, or technical—are captured and transformed into a predictable JSON response.

Objectives:

- avoid leaking internal details
- provide clear and actionable error messages
- ensure consistent HTTP status codes
- simplify debugging and client integration
- maintain separation between layers

Error handling is implemented through a **global exception handler**.

## **6.2 Error Categories**

Errors are grouped into three categories:

### **1. Validation Errors (API Layer)**

Triggered when:

- input JSON is invalid
- required fields are missing
- field constraints are violated

Mapped to **400 Bad Request**.

### **2. Functional Errors (Service Layer)**

Triggered by functional rules, for example:

- resource name already exists
- resource not found

Mapped to:

- **404 Not Found**
- **409 Conflict**

### **3. Technical Errors (Infrastructure Layer or unexpected failures)**

Triggered by:

- database issues
- unexpected exceptions
- internal failures

Mapped to **500 Internal Server Error**.

These errors never expose stack traces or internal details.

## **6.3 Global Exception Handler**

A global exception handler intercepts all exceptions thrown by the application.

Responsibilities:

- map exceptions to HTTP status codes
- produce a consistent JSON error structure
- prevent internal details from leaking
- log errors appropriately

This ensures that all errors follow the same format regardless of where they originate.

## **6.4 Error Response Format**

All errors follow a unified JSON structure:

json

`{
  "error": "string",
  "message": "string",
  "timestamp": "string",
  "path": "string"
}`

### Field definitions:

- **error**: short error type (e.g., `BadRequest`, `NotFound`, `Conflict`)
- **message**: human‑readable explanation
- **timestamp**: ISO‑8601 timestamp
- **path**: the request path that triggered the error

This structure is stable and predictable for clients.

## **6.5 Mapping Exceptions to HTTP Status Codes**

| Exception Type | Description | HTTP Status |
| --- | --- | --- |
| ValidationException | Invalid input | 400 |
| ResourceNotFoundException | Resource does not exist | 404 |
| ResourceConflictException | Name already exists | 409 |
| GenericException | Unexpected error | 500 |

This mapping ensures clarity and consistency.

## **6.6 Security Considerations**

Error handling is designed to avoid leaking sensitive information:

- no stack traces in responses
- no database or framework details
- no internal identifiers
- no raw exception messages

Only safe, user‑friendly messages are returned.

Technical details are logged internally but never exposed to clients.

## **6.7 Logging Strategy for Errors**

Errors are logged with:

- appropriate log level (`WARN` for functional errors, `ERROR` for technical errors)
- sanitized messages
- correlation with the request path

Advanced logging (correlation IDs, structured logs) will be introduced in v4.

## **6.8 Benefits of This Error Architecture**

- **Predictable**: clients always receive the same structure
- **Secure**: no leakage of internal details
- **Maintainable**: centralized logic simplifies evolution
- **Testable**: error scenarios can be tested independently
- **Professional**: aligns with industry standards for REST APIs

# **7. Logging & Observability (v2 Scope)**

## **7.1 Overview**

In v2, the application implements **basic logging and minimal observability**.
The goal is to provide enough visibility for debugging and monitoring during development and CI execution, without introducing advanced instrumentation.

Observability will be significantly expanded in v4 (structured logs, correlation IDs, metrics, health probes).

## **7.2 Logging Objectives**

Logging in v2 aims to:

- trace the main application events
- support debugging during development
- record errors in a consistent way
- avoid logging sensitive or unnecessary information
- remain lightweight and easy to maintain

Logs are written using the default Spring Boot logging framework (SLF4J + Logback).

## **7.3 Logging Scope**

The following events are logged:

### **Application Startup**

- application initialization
- environment information (non‑sensitive)
- successful startup message

### **API Requests (minimal)**

The API layer logs:

- entry into controller methods
- request path
- request outcome (success or error)

No request body is logged to avoid noise and potential exposure of user input.

### **Service Layer**

The service layer logs:

- functional errors (e.g., resource not found, name conflict)
- important functional events (e.g., resource created)

### **Error Logging**

Errors are logged with:

- log level `ERROR` for unexpected technical failures
- log level `WARN` for functional errors (e.g., not found)
- sanitized messages only

Stack traces are logged **only on the server side**, never returned to clients.

## **7.4 Log Levels**

The application uses the following log level strategy:

| Level | Usage |
| --- | --- |
| **INFO** | Startup, shutdown, successful operations |
| **WARN** | Functional errors (e.g., not found, conflict) |
| **ERROR** | Unexpected technical errors |
| **DEBUG** | Optional debugging during development |
| **TRACE** | Not used in v2 |

This ensures clarity and avoids log pollution.

## **7.5 Log Format**

The default Spring Boot log format is used:

Code

`timestamp level logger message`

Example:

Code

`2026-03-25 18:42:10 INFO  ResourceController - Creating new resource
2026-03-25 18:42:11 WARN  ResourceService - Resource with id '123' not found
2026-03-25 18:42:12 ERROR ResourceService - Unexpected error while deleting resource`

Structured logs (JSON format) will be introduced in v4.

## **7.6 Observability Scope (v2)**

Observability in v2 is intentionally minimal:

- basic logs
- error logs
- startup logs
- no metrics
- no tracing
- no correlation IDs
- no health endpoints beyond the default Spring Boot actuator (optional)

Advanced observability will be introduced in v4.

## **7.7 CI/CD Logging**

During CI execution:

- build logs
- test logs
- Docker build logs

are captured automatically by GitHub Actions.

No additional configuration is required in v2.

## **7.8 Security Considerations**

Logs must not contain:

- sensitive data
- stack traces in API responses
- raw exception messages
- user‑provided content without sanitization

This ensures safe logging practices even in a minimal setup.

## **7.9 Benefits of This Logging Approach**

- **Simple**: minimal configuration, easy to maintain
- **Safe**: avoids leaking sensitive information
- **Useful**: enough visibility for debugging
- **Extensible**: ready for advanced observability in v4
- **Professional**: aligns with industry standards for early‑stage services

# **8. Testing Strategy**

## **8.1 Overview**

The testing strategy for v2 focuses on ensuring correctness, stability, and maintainability of the application.
Tests are designed to validate:

- functional behavior
- domain rules
- API correctness
- persistence interactions
- error handling

The strategy follows a layered approach, with each type of test targeting a specific part of the architecture.

## **8.2 Types of Tests**

### **1. Unit Tests**

Unit tests validate the behavior of individual components in isolation.

**Scope:**

- Service layer logic
- Domain rules
- Validation logic (when applicable)

**Characteristics:**

- no Spring context
- no database
- no HTTP layer
- use of mocks for repository interactions

**Objectives:**

- verify functional rules (e.g., name uniqueness)
- verify domain constraints
- ensure predictable behavior

### **2. Integration Tests**

Integration tests validate the interaction between multiple components.

**Scope:**

- Repository layer
- Persistence with H2
- Spring context initialization

**Characteristics:**

- real Spring Boot context
- real H2 database
- no mocks

**Objectives:**

- verify repository behavior
- validate schema generation
- ensure correct mapping between domain and database

### **3. API (Web) Tests**

API tests validate the REST layer end‑to‑end.

**Scope:**

- HTTP endpoints
- JSON serialization/deserialization
- validation
- error handling
- status codes

**Characteristics:**

- use Spring’s MockMvc or WebTestClient
- no real network calls
- real controller + service + repository

**Objectives:**

- ensure API correctness
- verify error responses
- validate HTTP status codes
- test request/response formats

## **8.3 Test Coverage Expectations**

The v2 release aims for:

- **High coverage on service layer** (functional rules)
- **Full coverage on domain rules**
- **Coverage of all API endpoints**
- **Coverage of repository behavior**

Exact numeric coverage is not enforced, but the goal is to ensure:

- all functional paths are tested
- all error paths are tested
- no untested business logic

## **8.4 Test Data Strategy**

Test data is:

- minimal
- deterministic
- isolated per test
- reset between tests

The H2 database is recreated for each integration test class to ensure consistency.

## **8.5 Error Scenario Testing**

Error scenarios are explicitly tested:

- invalid input → 400
- resource not found → 404
- name conflict → 409
- unexpected errors → 500

This ensures the error‑handling architecture behaves as expected.

## **8.6 Mocking Strategy**

Mocks are used **only** in unit tests.

- Repository is mocked in service tests
- No mocks in integration or API tests
- No mocking of domain objects

This ensures a clean separation between test types.

## **8.7 CI Integration**

All tests run automatically in the CI pipeline:

- unit tests
- integration tests
- API tests

The pipeline fails if:

- any test fails
- the application cannot start
- the Docker image cannot be built

This guarantees stability and prevents regressions.

## **8.8 Benefits of This Testing Approach**

- **Clear separation of concerns**
- **High confidence in functional correctness**
- **Reliable API behavior**
- **Predictable persistence behavior**
- **Fast feedback during development**
- **Strong foundation for future releases (v3–v5)**

# **9. CI/CD Pipeline (v2)**

## **9.1 Overview**

The CI/CD pipeline for v2 is implemented using **GitHub Actions**.
Its purpose is to ensure that:

- the application builds successfully
- all tests pass
- the Docker image can be built
- the application runs consistently in a containerized environment

The pipeline is intentionally simple in v2, focusing on correctness and reproducibility.
Security scanning and advanced DevSecOps steps will be introduced in v3.

## **9.2 Pipeline Objectives**

The v2 pipeline ensures:

- **automated build** of the application
- **automated execution of unit and integration tests**
- **Docker image build** using the project’s Dockerfile
- **fast feedback** on code changes
- **consistent execution** across local and CI environments

This provides a solid foundation for future enhancements.

## **9.3 Pipeline Triggers**

The pipeline runs automatically on:

- every push to the repository
- every pull request targeting the main branch

This ensures continuous validation of all changes.

## **9.4 Pipeline Stages**

### **1. Checkout**

Retrieves the repository content.

### **2. JDK Setup**

Installs the required Java version (e.g., Temurin 21).

### **3. Build**

Runs the Maven or Gradle build:

- dependency resolution
- compilation
- static build checks

### **4. Unit & Integration Tests**

Executes all tests:

- unit tests
- integration tests
- API tests

The pipeline fails if any test fails.

### **5. Docker Build**

Builds the Docker image using the project’s Dockerfile.

This ensures:

- the Dockerfile is valid
- the application can run in a container
- the runtime environment is reproducible

### **6. Optional: Docker Run Smoke Test**

The pipeline may run a minimal smoke test:

- start the container
- check that the application starts successfully
- stop the container

This step is optional in v2 but recommended.

## **9.5 Pipeline Structure (Conceptual)**

Code

`CI Pipeline (v2)
 ├── Checkout code
 ├── Setup JDK
 ├── Build application
 ├── Run tests
 ├── Build Docker image
 └── (Optional) Run container smoke test`

This structure is simple, clean, and easy to extend in v3.

## **9.6 Artefacts**

The v2 pipeline may produce the following artefacts:

- build logs
- test reports
- Docker image (local to the CI runner)

The image is **not** pushed to a registry in v2.
This will be introduced in v5.

## **9.7 Failure Conditions**

The pipeline fails if:

- the build fails
- any test fails
- the Docker image cannot be built
- the application fails to start in the smoke test (if enabled)

This ensures that only stable code reaches the main branch.

## **9.8 CI/CD Limitations in v2**

The v2 pipeline does **not** include:

- SAST (static code analysis)
- SCA (dependency vulnerability scanning)
- container image scanning
- deployment steps
- registry publishing
- environment promotion
- secrets management

These capabilities will be introduced progressively in v3, v4, and v5.

## **9.9 Benefits of This CI/CD Approach**

- **Reproducible builds**
- **Automated testing**
- **Container‑ready application**
- **Fast feedback loop**
- **Solid foundation for DevSecOps**
- **Easy to extend in future releases**

# **10. Containerization**

## **10.1 Overview**

The application is packaged and executed as a **Docker container**.
Containerization ensures:

- consistent runtime across environments
- simplified local development
- reproducible CI execution
- isolation from host dependencies
- alignment with modern DevSecOps practices

The Docker image is built using a **multi‑stage Dockerfile** to keep the final image lightweight and secure.

## **10.2 Containerization Objectives**

The v2 containerization strategy aims to:

- provide a clean and minimal runtime environment
- ensure fast and reproducible builds
- avoid unnecessary dependencies in the final image
- support both local execution and CI execution
- prepare the foundation for security scanning in v3

## **10.3 Dockerfile Structure**

The Dockerfile follows a **multi‑stage build** pattern:

### **Stage 1 — Build Stage**

- uses a JDK base image
- compiles the application
- runs tests (optional in v2)
- produces an executable JAR

### **Stage 2 — Runtime Stage**

- uses a lightweight JRE base image
- copies only the final JAR
- exposes the application port
- defines the entrypoint

This approach reduces the final image size and attack surface.

## **10.4 Runtime Configuration**

The container exposes:

- **Port 8080** (default Spring Boot port)

Runtime behavior:

- the application starts automatically when the container starts
- logs are written to stdout/stderr (container‑friendly)
- no external configuration is required in v2

Environment variables may be added in future releases (v3+).

## **10.5 Local Execution**

Developers can run the application locally using:

Code

`docker build -t coreservice .
docker run -p 8080:8080 coreservice`

This ensures that:

- the local environment matches the CI environment
- no local Java installation is required
- the application behaves identically everywhere

## **10.6 CI Execution**

In GitHub Actions:

- the Dockerfile is built during the pipeline
- the image is validated
- optional smoke tests may run the container

This guarantees that:

- the Dockerfile is always valid
- the application can run in a container
- the runtime environment is reproducible

## **10.7 Image Size and Optimization**

The v2 image is optimized through:

- multi‑stage build
- minimal runtime base image
- exclusion of build tools from the final image
- no unnecessary OS packages

Further optimizations (non‑root user, distroless images) will be introduced in v3.

## **10.8 Security Considerations (v2 scope)**

Security is minimal in v2:

- no secrets baked into the image
- no sensitive environment variables
- no root filesystem modifications
- no exposed internal ports

Full container hardening and image scanning will be introduced in v3.

## **10.9 Limitations in v2**

The v2 containerization does **not** include:

- non‑root execution
- distroless base images
- image signing
- vulnerability scanning
- registry publishing
- multi‑architecture builds

These features will be progressively added in v3–v5.

## **10.10 Benefits of This Containerization Approach**

- **Reproducible**: same runtime everywhere
- **Lightweight**: minimal final image
- **Simple**: easy to build and run
- **Professional**: aligns with industry standards
- **Extensible**: ready for DevSecOps enhancements