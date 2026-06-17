# Release Notes – CoreService V1

## 🎯 Objectif de la version
La version **V1** constitue la première version fonctionnelle et stable du service CoreService.  
Elle fournit un socle minimal mais propre, entièrement structuré, testé et packagé.

---

## ✨ Fonctionnalités livrées

### **Health Check Applicatif**
- Endpoint : `/api/health`  
- Retourne le statut applicatif et la version.

### **Health Check Technique**
- Exposition des métriques via : `/actuator/health`.

---

## 🧱 Architecture & Structure

- Architecture fonctionnelle définie et documentée.  
- Architecture applicative décrivant les endpoints exposés.  
- Architecture logique (API, application, configuration, tests).  
- Architecture logicielle incluant le packaging Spring Boot.  
- Architecture technique (Windows, JRE17, Postman, Git, GitHub Actions).  
- Document chapeau résumant les principes de construction des schémas.

---

## 🛠️ Implémentation

- Projet Spring Boot **4.0.3**  
- Java **17**  
- Packaging : `CoreServiceApplication.jar`  
- Build Maven : `mvn clean test package`  
- Tests unitaires : WebMvcTest  
- Structure de projet propre et minimaliste

---

## 🔧 CI

- Build GitHub Actions déclenché sur push / PR  
- Exécution des tests  
- Génération du JAR  
- Pas de déploiement automatisé en V1

---

## 📦 Artefacts

- `CoreServiceApplication.jar`  
- Documentation d’architecture complète (schémas + document chapeau)

---

## 🏷️ Version

**Tag Git : `v1`**