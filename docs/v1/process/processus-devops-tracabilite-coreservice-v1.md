# 📘 PROCESSUS DEVOPS & TRAÇABILITÉ — VERSION 1 (V1)

**Application :** CoreService
**Version :** V1
**Auteur :** Vincent
**Date :** 2026-06-11

---

## 🎯 1. Objectif du document

Décrire le processus DevOps V1 mis en place autour de l'application CoreService, incluant :

- la stratégie de branches Git,
- le workflow de développement,
- le processus de build local,
- le processus CI/CD GitHub Actions,
- le processus de release,
- la traçabilité technique et fonctionnelle basée sur le commit SHA.

Ce document complète la spec technique et la spec fonctionnelle.

---

## 🏷️ 2. Stratégie de branches Git

La stratégie V1 repose sur trois branches principales :

### 2.1. Branche main

- Contient uniquement les versions stables.
- Chaque merge vers `main` correspond à une release.
- Chaque release est taguée (`v1`, `v1.1`, etc.).

### 2.2. Branche release_dev

- Branche d'intégration continue interne.
- Regroupe les fonctionnalités prêtes à être stabilisées.
- Sert de base pour préparer une release.

### 2.3. Branches feature/*

- Une branche par fonctionnalité.
- Créée depuis `release_dev`.
- Contient le développement local + tests locaux.
- Sert de base pour une Pull Request vers `release_dev`.

---

## 🔄 3. Workflow de développement

### Étape 1 — Développement local

1. Création d'une branche `feature/<nom>` depuis `release_dev`.
2. Développement de la fonctionnalité.
3. Build local :

```
mvn clean test
mvn package
```

4. Tests d'intégration externes (JAR + curl) en local.

👉 Aucun build CI n'est déclenché lors d'un push sur une branche feature.

---

### Étape 2 — Push de la branche feature

1. Push de la branche `feature/*` vers GitHub.
2. Ouverture d'une Pull Request vers `release_dev`.

---

### Étape 3 — Build CI/CD sur Pull Request

Lors de l'ouverture de la PR :

- GitHub Actions exécute automatiquement :
  - `mvn clean test`
  - `mvn package`
- Le build doit être vert pour autoriser le merge.
- La PR doit être liée à une issue.

👉 La CI tourne UNIQUEMENT sur Pull Request vers `release_dev`.

---

### Étape 4 — Merge vers release_dev

Une fois la PR validée :

- Merge de `feature/*` → `release_dev`.
- Aucun build CI n'est déclenché sur ce merge.

---

### Étape 5 — Préparation de la release

Quand toutes les features prévues sont intégrées :

- Vérification finale du build local.
- Tests externes (JAR + curl).
- Mise à jour éventuelle des specs.

---

### Étape 6 — Merge vers main

Une fois la release validée :

- Merge `release_dev` → `main`.
- GitHub Actions exécute un build final.
- Création d'un tag Git (`v1`, `v1.1`, etc.).
- Rédaction des release notes.

👉 La CI tourne sur push/merge vers `main`.

---

## 🚀 4. Processus CI/CD GitHub Actions

### 4.1. Déclencheurs (V1)

Le workflow CI/CD est déclenché :

✔ Sur Pull Request vers `release_dev`

```yaml
on:
  pull_request:
    branches: [ "release_dev" ]
```

✔ Sur push/merge vers `main`

```yaml
on:
  push:
    branches: [ "main" ]
```

❌ Pas de build sur push d'une branche feature

→ conforme à ton process V1.

---

### 4.2. Étapes exécutées

1. Checkout du code
2. Installation du JDK
3. Cache Maven
4. `mvn clean test`
5. `mvn package`

---

### 4.3. Objectifs

- Garantir un build reproductible
- Valider les tests internes
- Générer un artefact JAR associé au commit SHA
- Valider les PR avant intégration
- Valider les releases avant tagging

---

## 🏷️ 5. Processus de release

### 5.1. Conditions pour release

- Toutes les features prévues sont mergées dans `release_dev`.
- Build local OK.
- Tests externes OK.
- CI OK sur PR.

### 5.2. Merge vers main

- Merge `release_dev` → `main`.
- Build CI final.

### 5.3. Tagging

Création d'un tag Git :

```
git tag v1
git push origin v1
```

### 5.4. Release notes

Contiennent :

- liste des issues incluses
- liste des PR mergées
- résumé des changements
- commit SHA de référence
- artefact généré

---

## 🔗 6. Traçabilité technique

La V1 met en place une traçabilité complète basée sur le commit SHA.

### 6.1. Pivot de traçabilité : le commit SHA

Le commit SHA identifie :

- le code source
- les tests associés
- le build Maven
- le JAR généré
- la PR associée
- l'issue associée
- la version taguée
- les specs versionnées

👉 Le commit SHA est la source de vérité.

### 6.2. Traçabilité code → build

Pour chaque commit :

- CI exécute `mvn clean test`
- CI exécute `mvn package`
- Le build est associé au SHA
- Les logs CI sont archivés

### 6.3. Traçabilité build → tests

- Tests unitaires exécutés pour ce SHA
- Tests d'intégration internes exécutés pour ce SHA
- Tests externes (JAR + curl) exécutés en local pour ce SHA

### 6.4. Traçabilité build → artefact

- Le JAR généré contient le SHA dans `pom.properties`
- Le JAR est l'artefact officiel de la version

### 6.5. Traçabilité fonctionnelle

- Les issues sont liées aux specs
- Les PR sont liées aux issues
- Les commits sont liés aux PR
- Le tag est lié au commit final

---

## 🟩 7. Synthèse du processus

| Élément | Description |
|---|---|
| Branches | `main`, `release_dev`, `feature/*` |
| Build local | Tests + packaging |
| CI/CD | PR → `release_dev` ; push → `main` |
| Tests externes | JAR + curl |
| Release | Merge → `main` + tag + notes |
| Traçabilité | SHA → build → tests → artefact → specs → issues → PR |
