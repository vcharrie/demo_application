# 📘 Document Chapeau – Présentation des Schémas d'Architecture (V1)

## 1. Présentation générale

Les schémas d'architecture produits pour CoreService V1 ont été construits selon un principe simple :

👉 chaque schéma représente un angle de vue spécifique du système, sans mélange entre les niveaux.

L'objectif est de fournir une vision claire, progressive et cohérente du service, depuis ce qu'il fait jusqu'à la manière dont il s'exécute sur l'environnement local.

Les schémas couvrent les aspects suivants :

- **Architecture fonctionnelle** : ce que fait le service.
- **Architecture applicative** : les services exposés et leurs protocoles.
- **Architecture logique** : l'organisation interne en couches.
- **Architecture logicielle** : la stack logicielle et le packaging.
- **Architecture technique** : l'environnement d'exécution et les outils utilisés.

Chaque schéma se concentre sur son propre périmètre, sans redondance avec les autres.

---

## 2. Détail des schémas

### 2.1. Architecture fonctionnelle

Ce schéma décrit les fonctions offertes par le service, indépendamment de toute technologie.

Il présente :

- les acteurs qui utilisent le service,
- les fonctions exposées (ex. : health checks),
- les interactions fonctionnelles.

Il répond à la question :

👉 *« Qu'est-ce que le service permet de faire ? »*

---

### 2.2. Architecture applicative

Ce schéma décrit les services applicatifs exposés et leurs protocoles d'échange.

Il présente :

- les endpoints REST,
- les formats d'échange (JSON),
- les interactions entre clients et API.

Il répond à la question :

👉 *« Quels services expose l'application et comment y accède-t-on ? »*

---

### 2.3. Architecture logique

Ce schéma décrit l'organisation interne du code en couches et composants.

Il présente :

- la couche API,
- la couche application,
- la configuration,
- les tests internes.

Il répond à la question :

👉 *« Comment l'application est-elle structurée en interne ? »*

---

### 2.4. Architecture logicielle

Ce schéma décrit la stack logicielle et le packaging final.

Il présente :

- le JAR final,
- le code applicatif,
- les dépendances Spring Boot,
- les artefacts de test (séparés),
- le rôle du build Maven,
- le socle d'exécution (JRE 17).

Il répond à la question :

👉 *« Avec quels composants logiciels l'application est-elle construite et empaquetée ? »*

---

### 2.5. Architecture technique

Ce schéma décrit l'environnement d'exécution réel du service.

Il présente :

- le poste Windows local,
- le JRE 17,
- l'exécution du JAR,
- Postman pour les appels REST,
- Git local,
- GitHub et GitHub Actions,
- les flux réseau (`localhost:8080`).

Il répond à la question :

👉 *« Où et comment l'application s'exécute-t-elle dans l'environnement réel ? »*

---

## 3. Synthèse

Les schémas ont été construits selon trois principes simples :

1. **Un schéma = un périmètre clair**
   Pas de mélange entre fonctionnel, applicatif, logique, logiciel ou technique.

2. **Représenter uniquement ce qui existe réellement en V1**
   Pas d'anticipation, pas de complexité inutile.

3. **Favoriser la lisibilité et la cohérence**
   Même style, même granularité, même logique de découpage.
