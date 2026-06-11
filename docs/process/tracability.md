# Traceability Model

This project implements a complete DevSecOps traceability chain.
The goal is to ensure that any stakeholder can navigate from a business requirement to the corresponding code, tests, CI results, artifacts, and deployed version — and back.

Traceability is defined through three pillars:

1. **Traceability Needs**
2. **Traceability Rules**
3. **Traceability Entry Points**

# 1) 🔍 **Traceability Needs**

For each object in the development lifecycle, we define **what information must be reachable** and **why**.

## **1. Issue → What should I be able to navigate to?**

From an Issue, I must be able to reach:

- The Pull Request that implements it
- The commits linked to the issue
- The CI runs validating the implementation
- The generated artifacts (JAR, test reports)
- The version (tag or branch) where the issue is included

**Why:**  
To validate that the requirement has been implemented, tested, and delivered.

## **2. Commit → What should I be able to navigate to?**

From a Commit, I must be able to reach:

- The Issue it implements
- The Pull Request containing the commit
- The CI run triggered by this commit
- The artifacts produced by the CI
- The final version containing this commit

**Why:**  
To audit code changes and understand their purpose and impact.

## **3. Pull Request → What should I be able to navigate to?**

From a PR, I must be able to reach:

- The Issue(s) it resolves
- The commits included
- The CI checks (build + tests)
- The artifacts generated during the PR validation
- The merge commit and final version

**Why:**  
To validate the integration process and ensure quality gates were respected.

## **4. CI Run → What should I be able to navigate to?**

From a CI run, I must be able to reach:

- The commit that triggered the run
- The PR (if applicable)
- The artifacts generated
- The test results
- The issue indirectly linked through the commit/PR

**Why:**  
To verify build reproducibility and test coverage.

## **5. Artifact → What should I be able to navigate to?**

From an artifact (JAR, test report), I must be able to reach:

- The CI run that produced it
- The commit used to build it
- The PR and Issue associated
- The version in which the artifact was released

**Why:**  
To ensure that any binary can be traced back to its source code and requirement.

## **6. Version / Tag → What should I be able to navigate to?**

From a version, I must be able to reach:

- The list of issues included
- The PRs merged
- The commits included
- The artifacts associated with the release

**Why:**  
To understand the content and scope of a release.

# 2) 📏 **Traceability Rules**

To guarantee consistent navigation across objects, the following rules apply:

### **1. Commit messages must reference the Issue number**

Format:
`feat: implement health endpoint (#2)`

### **2. Pull Requests must close Issues automatically**

Use GitHub keywords:
`Fixes #2Closes #3`

### **3. CI must run on every commit and every PR**

This ensures:

- build reproducibility
- test validation
- artifact generation

### **4. Artifacts must be linked to the commit SHA**

Example naming:
`core-service-v1-<commit-sha>.jar`

### **5. PRs must not be merged unless CI is green**

This enforces quality gates.

### **6. Releases must list Issues and PRs included**

This ensures release-level traceability.

# 3) 🧭 **Traceability Entry Points**

This section explains **how to navigate the traceability chain starting from any object**.

## **Starting from an Issue**

You can navigate to:

- PR → via “Linked Pull Requests”
- Commits → via PR
- CI Runs → via PR checks
- Artifacts → via CI
- Version → via PR merge

## **Starting from a Commit**

You can navigate to:

- Issue → via commit message reference
- PR → via “Included in Pull Request”
- CI → via “Checks”
- Artifacts → via CI
- Version → via merge history

## **Starting from a CI Run**

You can navigate to:

- Commit → via run metadata
- PR → if the run is associated with a PR
- Artifacts → via run artifacts
- Issue → via commit → PR → Issue

## **Starting from an Artifact**

You can navigate to:

- CI Run → via artifact metadata
- Commit → via CI run
- PR → via commit
- Issue → via PR
- Version → via merge

## **Starting from a Version / Tag**

You can navigate to:

- PRs included in the release
- Issues closed by those PRs
- Commits included
- Artifacts attached to the release