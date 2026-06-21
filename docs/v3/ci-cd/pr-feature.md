Pull Request — Feature #2 : Local Quality Gate (Checkstyle, PMD, SpotBugs)
Summary

This PR implements the full Local Quality Gate for the CoreServiceApplication project.
It introduces automated static analysis using Checkstyle, PMD, and SpotBugs, with strict build‑breaking rules and clean reports across the entire codebase.

This ensures that every future commit and PR respects a consistent, maintainable, and secure code standard before moving to SonarCloud.
Scope of Work
✔ Checkstyle

    Added Checkstyle plugin with project‑wide ruleset

    Fixed all formatting and naming violations

    Ensured automatic formatting via VS Code integration

    Build now fails on any Checkstyle violation

    Report generated at: target/checkstyle-result.xml

✔ PMD

    Added PMD plugin with a moderated ruleset

    Fixed all PMD violations

    Verified that PMD scans all Java sources

    Build now fails on any PMD violation

    Report generated at: target/pmd.xml

✔ SpotBugs

    Added SpotBugs plugin with moderated configuration

    Added spotbugs-exclude.xml for Spring‑related false positives

    Fixed all real issues (null‑safety, Locale usage)

    Achieved 0 SpotBugs violations

    Report generated at: target/spotbugsXml.xml

Technical Changes
Maven

    Added Checkstyle plugin (verify phase)

    Added PMD plugin (verify phase)

    Added SpotBugs plugin (verify phase)

    Added exclusion filters for SpotBugs

    Ensured all reports are generated in XML format

Code Fixes

    Added null‑safety to ResourceApiMapper

    Replaced toUpperCase() with toUpperCase(Locale.ROOT)

    Cleaned formatting to satisfy Checkstyle

    No functional changes to business logic

CI/CD Integration (to be added next)

This PR prepares the ground for GitHub Actions integration:

    Upload Checkstyle report

    Upload PMD report

    Upload SpotBugs report

(Will be added in Feature #3)
Reviewer Checklist
Code Quality

    [ ] Code formatting follows Checkstyle rules

    [ ] No unused imports

    [ ] No PMD violations

    [ ] No SpotBugs violations

    [ ] No functional regressions

Build & Tooling

    [ ] mvn verify passes locally

    [ ] Reports are generated in target/

    [ ] No unnecessary exclusions

Documentation

    [ ] PR description is clear

    [ ] Commit history is linear (rebase‑based)

    [ ] No merge commits