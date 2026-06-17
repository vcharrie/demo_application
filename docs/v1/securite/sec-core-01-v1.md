# 🛡️ SEC-CORE-01 — Mesures de sécurité implicites de la V1 (CoreService)

**Version :** V1
**Périmètre :** Application CoreService (version minimale)
**Objectif :** Documenter les mesures de sécurité déjà présentes dans la V1, même sans DevSecOps avancé.

---

## 1. 🎯 Objectif de la fiche

Décrire les mesures de sécurité déjà effectives dans la V1, bien qu'elles soient "techniques" et non explicitement "sécurité".

Ces mesures :

- réduisent des risques,
- empêchent certains scénarios d'exploitation,
- améliorent la robustesse,
- garantissent la traçabilité,
- assurent la reproductibilité du build.

---

## 2. ⚠️ Menaces & risques couverts

| ID | Risque / Menace | Description |
|---|---|---|
| R1 | Code non maîtrisé | Absence de versioning, modifications non tracées, code non reproductible. |
| R2 | Build non reproductible | Artefacts générés manuellement, impossibles à auditer. |
| R3 | Régression non détectée | Absence de tests → comportements non maîtrisés. |
| R4 | Application non observable | Impossible de vérifier l'état runtime. |
| R5 | Absence de contrôle d'intégrité | Pas de validation du comportement après packaging. |
| R6 | Absence de séparation des responsabilités | Pas de distinction entre tests internes et externes. |
| R7 | Absence de pipeline | Build manuel → risque d'erreur humaine, artefacts non fiables. |

---

## 3. 🛠️ Mesures de sécurité implémentées en V1

### M1 — Gestion de configuration sécurisée via Git

- Le code est versionné dans un dépôt Git.
- Chaque modification est tracée (commit SHA).
- Les PR et issues (process décrit dans un document séparé) assurent la revue et la validation.
- Le code source ne peut pas être modifié sans trace.

**Risques couverts :** R1, R6

---

### M2 — Build automatisé (local + CI/CD)

- Le build Maven est reproductible (`mvn clean test`, `mvn package`).
- Le pipeline CI exécute automatiquement les tests et le packaging.
- Le build ne dépend pas de l'environnement local du développeur.

**Risques couverts :** R2, R7

---

### M3 — Tests unitaires

- Tests unitaires sur le contrôleur.
- Validation du comportement attendu.
- Détection de régressions simples.

**Risques couverts :** R3

---

### M4 — Tests d'intégration internes (Spring Boot Test)

- Démarrage du contexte Spring Boot.
- Validation de l'intégration interne des composants.
- Détection d'erreurs de configuration.

**Risques couverts :** R3, R6

---

### M5 — Tests d'intégration externes (JAR packagé + curl)

- Exécution du JAR packagé.
- Appels HTTP réels via `curl`.
- Validation du comportement runtime réel.
- Vérification que l'application fonctionne comme un client externe l'utiliserait.

**Risques couverts :** R3, R5

---

### M6 — Endpoint technique Actuator

- `/actuator/health` expose l'état interne de l'application.
- Permet de vérifier la disponibilité et la santé du runtime.
- Permet une supervision minimale.

**Risques couverts :** R4

---

### M7 — Endpoint applicatif de santé

- `/api/health` expose la version et l'état applicatif.
- Permet de vérifier que la couche API fonctionne.
- Permet de valider le comportement après packaging.

**Risques couverts :** R4, R5

---

## 4. 🧩 Synthèse des mesures

| Mesure | Description | Risques couverts |
|---|---|---|
| M1 | Gestion de conf Git | R1, R6 |
| M2 | Build automatisé | R2, R7 |
| M3 | Tests unitaires | R3 |
| M4 | Tests intégration internes | R3, R6 |
| M5 | Tests intégration externes | R3, R5 |
| M6 | Actuator health | R4 |
| M7 | API health | R4, R5 |

---

## 5. 🟠 Risques résiduels (V1)

| ID | Risque résiduel | Commentaire |
|---|---|---|
| RR1 | Pas de sécurité applicative | Endpoints ouverts, pas d'authentification. |
| RR2 | Pas de DevSecOps | Pas de SAST, SCA, SBOM, DAST. |
| RR3 | Pas de persistance sécurisée | Pas de DB, donc pas de chiffrement, pas de gestion des secrets. |
| RR4 | Pas de gestion d'erreurs avancée | Pas de normalisation des erreurs. |
| RR5 | Pas de monitoring avancé | Actuator minimaliste uniquement. |
| RR6 | Tests externes non automatisés | Tests manuels → risque d'oubli. |

---

## 6. 🟩 Conclusion

Même en V1 minimaliste, l'application met déjà en place :

- une chaîne de build reproductible,
- une gestion de configuration sécurisée,
- des tests internes et externes,
- des endpoints de supervision,
- un processus de traçabilité (décrit dans un document séparé).

Ces éléments constituent une base de sécurité solide, sur laquelle les futures fiches (V2) ajouteront :

- SAST
- SCA
- SBOM
- DAST
- secrets management
- durcissement CI/CD
- durcissement runtime
- architecture sécurisée
