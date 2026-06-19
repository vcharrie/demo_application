# 📘 SPÉCIFICATION FONCTIONNELLE — VERSION 1 (V1)

**Application :** CoreService
**Version :** V1
**Date :** 2026-06-11
**Auteur :** Vincent

---

## 🎯 1. Objectif de la version

La version V1 de l'application CoreService constitue une release minimale fonctionnelle permettant :

- de vérifier que la stack Spring Boot est opérationnelle,
- d'exposer un endpoint technique standard (Actuator),
- d'exposer un endpoint applicatif démontrant la capacité à répondre à des appels REST,
- de valider le pipeline CI/CD minimaliste (build + tests).

Cette version ne contient aucune logique métier, aucun modèle métier, aucun service métier, et ne constitue pas encore la base de la spec métier.

---

## 📦 2. Périmètre fonctionnel de la V1

La V1 inclut uniquement :

- un endpoint technique : `/actuator/health`
- un endpoint applicatif : `/api/health`
- un pipeline CI/CD minimaliste (build + tests)

La V1 n'inclut pas :

- de logique métier,
- de modèle métier,
- de règles métier,
- de persistance,
- de sécurité applicative,
- de configuration avancée,
- de tests d'intégration,
- de déploiement automatisé.

---

## 🧩 3. Fonctionnalités détaillées

### FCT-V1-01 — Endpoint technique de santé (Actuator)

**Description :**
L'application expose un endpoint technique standard fourni par Spring Boot Actuator permettant de vérifier l'état interne de l'application.

**URL :**
`GET /actuator/health`

**Comportement attendu :**

- Retourne un statut global `UP` lorsque l'application fonctionne.
- Expose les groupes `liveness` et `readiness`.

**Exemple de réponse :**

```json
{
  "groups": ["liveness", "readiness"],
  "status": "UP"
}
```

**Critères d'acceptation :**

- Le statut doit être `UP` lorsque l'application démarre correctement.
- Le endpoint doit être accessible sans authentification.
- Le format JSON doit être conforme à Spring Boot Actuator.

---

### FCT-V1-02 — Endpoint applicatif de santé

**Description :**
L'application expose un endpoint REST applicatif permettant de vérifier que la couche API fonctionne et de retourner la version déployée.

**URL :**
`GET /api/health`

**Comportement attendu :**

- Retourne la version de l'application (`v1`).
- Retourne un statut applicatif `UP`.

**Exemple de réponse :**

```json
{
  "version": "v1",
  "status": "UP"
}
```

**Critères d'acceptation :**

- Le endpoint doit répondre en moins de 100 ms en local.
- Le champ `version` doit correspondre à la version déployée.
- Le champ `status` doit être `UP`.
- Le endpoint doit être accessible sans authentification.

---

### FCT-V1-03 — Pipeline CI/CD minimaliste

**Description :**
La V1 inclut un pipeline GitHub Actions permettant de valider automatiquement :

- la compilation du projet,
- l'exécution des tests unitaires,
- la génération du JAR.

**Comportement attendu :**

- Le pipeline s'exécute à chaque push et pull request.
- Le pipeline exécute `mvn clean test`.
- Le pipeline exécute `mvn package`.
- Le pipeline ne lance pas l'application.
- Le pipeline ne fait aucun appel REST.
- Le pipeline ne génère pas d'image Docker.

**Critères d'acceptation :**

- Le pipeline doit passer au vert si le code compile et les tests passent.
- Le pipeline doit échouer si un test échoue.
- Le pipeline doit produire un artefact JAR.

---

## 🚫 4. Hors périmètre de la V1

Les éléments suivants sont explicitement exclus :

- modèle métier
- règles métier
- services métier
- persistance (SQL/NoSQL)
- sécurité (authentification/autorisation)
- configuration avancée Spring
- Dockerfile
- build d'image Docker
- déploiement automatisé
- tests d'intégration
- monitoring avancé
- SBOM / Trivy / DevSecOps
- API métier
- gestion d'erreurs avancée

Ces éléments seront introduits en V2 ou ultérieur.

---

## 🔗 5. Dépendances fonctionnelles

La V1 dépend uniquement de :

- Spring Boot
- Spring Boot Actuator
- JDK 21
- Maven
- GitHub Actions (CI minimaliste)

Aucune dépendance métier.

---

## 🧪 6. Scénarios d'usage

### SUC-V1-01 — Vérification de l'état technique

**Acteur :** Administrateur / pipeline
**Pré-conditions :** L'application est démarrée

**Scénario :**

1. L'utilisateur appelle `GET /actuator/health`
2. L'application retourne `status = UP`
3. L'utilisateur valide que l'application est opérationnelle

---

### SUC-V1-02 — Vérification de l'état applicatif

**Acteur :** Développeur / pipeline
**Pré-conditions :** L'application est démarrée

**Scénario :**

1. L'utilisateur appelle `GET /api/health`
2. L'application retourne `version = v1` et `status = UP`
3. L'utilisateur valide que la couche REST fonctionne

---

## 📌 8. Conclusion

La V1 constitue une release technique minimale, permettant :

- de valider la stack Spring Boot,
- de valider la capacité à exposer des endpoints REST,
- de valider la CI/CD minimale,
- de préparer la V2 qui introduira le métier, la spec métier, et les fonctionnalités réelles.
