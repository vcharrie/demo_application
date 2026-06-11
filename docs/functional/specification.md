# Spec v1 – Secure Mini‑Application – Initial Version (Basic CI)

## 1. Context

This project is a **pedagogical secure mini‑application** designed to:

- illustrate a clean and modular **Java / Spring Boot architecture**,
- serve as a foundation for implementing a **DevSecOps‑oriented CI/CD pipeline** using GitHub Actions,
- be presented during **technical interviews** as a concrete example of:
  - architecture design,
  - CI/CD implementation,
  - progressive integration of security practices.

This Spec v1 describes the **first iteration** of the project. It is intentionally limited and will serve as the baseline for future versions (v2, v3, …).

---

## 2. Goals of version v1

- Provide a **minimal Java 17 / Spring Boot project** that:
  - compiles successfully,
  - exposes a simple REST endpoint,
  - includes unit tests.
- Set up a **basic CI pipeline** with GitHub Actions:
  - Maven build,
  - test execution,
  - artifact publication (JAR, test reports),
  - Maven caching.
- Establish the **initial architectural foundations** (packages, layers) to be expanded later.

---

## 3. Functional scope of v1

Very limited, purely educational features:

- Expose a REST endpoint:
  - `GET /api/health`
  - Response example:
    ```json
    { "status": "UP", "version": "v1" }
    ```
- No business domain logic yet.
- The goal is to **validate the project structure and CI pipeline**, not implement functional features.

---

## 4. Logical architecture v1

- **Planned layers** (some still empty or minimal in v1):
  - `api`: REST controllers  
  - `service`: business logic (placeholder for v1)  
  - `domain`: domain entities (placeholder for
