# Guide de rédaction du mini-DAT
Structure conforme aux standards TOGAF ADM et ISO/IEC/IEEE 42010.

La justification des choix suit le fil conducteur :
**Critère → Capacité → Exigences transverses → Composants TOGAF (Application / Logical / Physical / Execution / Technology).**

---

# 1. Architecture métier

## 1.1 Contexte métier
- Enjeux métier
- Acteurs
- Périmètre métier
- Contraintes métier

## 1.2 Besoins métier
- Liste des besoins métier

## 1.3 Cartographie des processus métier
- Voir schéma PUML lié

---

# 2. Architecture fonctionnelle

## 2.1 Contexte fonctionnel
- Use cases fonctionnels
- Scénarios fonctionnels
- Flux fonctionnels

## 2.2 Cartographie des fonctions métier
- Fonctions qui réalisent les UC
- Relations entre fonctions

## 2.3 Exigences transverses (techniques + sécurité)
Les exigences techniques sont globales au système et ne dépendent pas des UC.

### Sécurité
- Authentification, Autorisation (RBAC), Secrets, Sécurité réseau, Chiffrement

### Intégrité
- ACID, Idempotence, Cohérence

### Disponibilité / Résilience
- HA, Redondance, Auto-réparation

### Scalabilité
- Scalabilité horizontale, Autoscaling, Partitionnement

### Observabilité
- Logs, Métriques, Traces, Dashboards, Alerting

### Traçabilité / Audit
- Historisation, Audit des actions sensibles

### CI/CD
- Build, Tests, Déploiement, Rollback

### Réseau / Infrastructure
- Segmentation réseau, RBAC infra, Monitoring, Gouvernance API

---

# 3. Architecture applicative

## 3.1 Schéma d’architecture applicative
- Modules applicatifs (Application Components)
- API exposées
- Flux (REST, events, batch)
- Découpage fonctionnel

## 3.2 Justification des choix applicatifs
Pour chaque choix :
- **Critère fonctionnel / contexte UC**
- **Capacité de la solution : fonction applicative**
- **Exigences transverses couvertes**
- **Composants mis en œuvre : Application Component**

Exemples :
- REST pour UC simples et synchrones
- Kafka pour UC volumétriques ou asynchrones

---

# 4. Architecture logique (Logical Application Architecture)

## 4.1 Schéma d’architecture logique
Modèles en couches et responsabilités des couches pour chaque composant applicatif :
- Architecture hexagonale
- Domain-Driven Design
- Ports / Adapters
- Services métier
- Repositories

## 4.2 Justification des choix d’architecture logique
Pour chaque choix :
- **Critère applicatif / contexte composant applicatif**
- **Capacité de la solution logique**
- **Exigences transverses couvertes**
- **Composants mis en œuvre : Logical Application Component**

---

# 5. Architecture logicielle (Application Software Architecture)

## 5.1 Schéma d’architecture logicielle
Liste de la stack technique (Physical Application Components) :
- Frameworks (Spring Boot)
- Langage (Java 21)
- Serveur embarqué (Tomcat)
- Artefacts de déploiement (image Docker)

## 5.2 Justification des choix logiciels
Pour chaque choix :
- **Critère logique / contexte composant logique (structure logique) à rendre exécutable**
- **Capacité de la solution logicielle**
- **Exigences transverses couvertes**
- **Composants mis en œuvre : Physical Application Component**

Exemples :
- Java + Spring Boot + Tomcat + Docker

---

# 6. Architecture système / exécution (Technology Architecture)

## 6.1 Schéma d’exécution
- Monolithe backend vs microservices
- Conteneurs Docker (Technology Nodes)
- Pods / Deployments Kubernetes (Execution Nodes)
- Services Kubernetes

## 6.2 Justification des choix d’exécution
Pour chaque choix :
- **Critère applicatif / contexte Application Component**
- **Capacité de la solution d’exécution**
- **Exigences transverses couvertes**
- **Composants mis en œuvre : Execution Node / Technology Node**

Exemples :
- Monolithe pour cohérence transactionnelle
- Microservices pour scalabilité indépendante
- Kubernetes pour disponibilité et CI/CD

---

# 7. Architecture infrastructure / réseau / sécurité

## 7.1 Schéma infra / réseau
- Zones réseau
- Segmentation
- Ingress
- RBAC
- Observabilité
- CI/CD

## 7.2 Justification des choix infra
Pour chaque choix :
- **Critère de sécurité / contexte de déploiement**
- **Capacité de la solution infra**
- **Exigences transverses couvertes**
- **Composants mis en œuvre : Technology Components / Infrastructure Services**

Exemples :
- Segmentation réseau pour Zero Trust
- RBAC pour moindre privilège
- Observabilité pour supervision
- CI/CD pour fiabilité et répétabilité

---

# 8. Fil conducteur du DAT

Pour chaque choix d’architecture :
- Critère
- Capacité
- Exigences transverses couvertes
- Composants TOGAF mis en œuvre

Structure conforme TOGAF ADM et ISO/IEC/IEEE 42010.
