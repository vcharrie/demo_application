# 📘 Document Chapeau — Vision, démarche, releases et roadmap

## 1. Présentation générale du projet

Le projet *CoreService* s’inscrit dans une démarche visant à démontrer et actualiser un ensemble de compétences déjà acquises dans un parcours professionnel antérieur, incluant :

- l’architecture logicielle,
- la conception de processus d’ingénierie logicielle complets,
- le pilotage de projets en méthodologie Agile,
- la mise en place de démarches itératives et incrémentales.

Ce socle a été enrichi par une montée en compétence sur des technologies et pratiques modernes du marché :

- Java 21 / Spring Boot 3,
- Docker et images sécurisées,
- CI/CD GitHub Actions,
- Kubernetes (kind puis EKS),
- sécurité by design (fiches sécurité, mesures de risque, DevSecOps),
- copilotage IA dans toutes les phases du cycle de développement.

Le projet vise à **développer et démontrer l’ensemble des compétences attendues** dans la mise en œuvre d’un projet d’ingénierie logicielle moderne, sécurisé et industrialisé.

---

## 2. Ambitions pédagogiques

Le projet a pour objectif de :

- consolider les compétences existantes en architecture et gestion de projet,
- maîtriser les fondamentaux du développement backend moderne,
- intégrer les bonnes pratiques de sécurité applicative et DevSecOps,
- mettre en place un pipeline CI/CD sécurisé,
- déployer une application sur Kubernetes local (kind) puis cloud (EKS),
- produire une documentation technique professionnelle,
- démontrer une capacité à piloter un projet complet en interaction avec une IA.

---

## 3. Démarche itérative adoptée

Le projet suit une approche incrémentale, organisée en releases successives.  
Chaque release comporte :

- un périmètre fonctionnel et technique défini,
- une spécification technique dédiée,
- une architecture associée,
- une implémentation contrôlée,
- une validation humaine,
- une documentation mise à jour.

Cette démarche reflète une pratique professionnelle issue de l’expérience en architecture logicielle et en gestion de projet Agile.

---

## 4. Interaction humain–IA dans le processus de développement

Le projet a été réalisé en collaboration continue entre un humain et deux IA, chacune ayant un rôle distinct.

### 4.1 IA principale : Copilot (production)
Copilot a été utilisée comme **IA maître**, responsable de :

- la production des spécifications techniques,
- la conception d’architectures,
- la génération de code et de fichiers de configuration,
- la rédaction de la documentation,
- la proposition d’améliorations et de corrections.

Toutes les productions ont été **validées, ajustées et contrôlées** par l’humain.

### 4.2 IA secondaire : Claude (contrôle et challenge)
Une seconde IA (Claude) a été utilisée comme **outil de contrôle**, notamment pour :

- challenger les propositions de Copilot,
- vérifier la cohérence des spécifications techniques et fiches sécurité,
- confronter les choix techniques,
- identifier des points d’amélioration.

Cette double interaction IA a permis un **processus de validation croisé**, garantissant la qualité et la robustesse des livrables.

### 4.3 Processus global humain–IA
- L’humain définit les objectifs, le périmètre et les contraintes,
- Copilot produit une première version,
- L’humain contrôle, corrige, demande des ajustements,
- Claude challenge les propositions et apporte un regard alternatif,
- Copilot consolide et finalise.

Ce processus démontre une capacité à **piloter plusieurs IA dans un contexte d’ingénierie logicielle**, tout en conservant la maîtrise du cycle de développement.

---

## 5. Releases réalisées

### **V1 — Socle applicatif minimal & CI de base**
La première version pose les fondations du projet :

- Healthcheck applicatif (`/api/health`) et technique (`/actuator/health`)
- Architecture fonctionnelle, logique, logicielle et technique documentée
- Projet Spring Boot 4.0.3 / Java 17
- Packaging Maven propre (`mvn clean test package`)
- Tests unitaires WebMvcTest
- Première CI GitHub Actions :
  - build Maven
  - exécution des tests
  - génération du JAR
- Documentation d’architecture complète (schémas + document chapeau)

**👉 V1 = un socle propre, stable, documenté, prêt pour l’itération.**

---

### **V2 — Architecture en couches, CRUD métier & containerisation**
Cette version introduit la première vraie logique applicative :

- Architecture en couches complète :
  - API (controllers + exception handler)
  - Application (service)
  - Domaine (entité métier + exceptions)
  - Infrastructure (repository en mémoire + mapper)
- Nouveau modèle métier `Resource`
- CRUD complet :
  - GET/POST/PUT/DELETE `/api/resources`
- Gestion centralisée des erreurs (GlobalExceptionHandler)
- Containerisation complète :
  - Dockerfile multi‑stage
  - Exécution locale via Docker Desktop
- CI/CD enrichie :
  - build Maven
  - build Docker multi-stage
  - push GHCR
  - smoke test containerisé

**👉 V2 = première vraie application + première chaîne CI/CD complète + containerisation.**

---

### **V3 — Durcissement sécurité, DevSecOps complet & pipeline avancé**
La V3 marque une rupture : passage à une chaîne DevSecOps professionnelle.

#### Sécurité & packaging
- Dockerfile durci :
  - base image Temurin 21 JRE (digest épinglé)
  - user non‑root
  - healthcheck
  - labels OCI
- Suppression du multi‑stage : build Maven externe obligatoire

#### CI/CD DevSecOps
Pipeline complet et strict :

- Build Maven (`mvn clean verify`)
- SAST : Checkstyle, PMD, SpotBugs
- SBOM CycloneDX
- SCA Trivy (fail HIGH/CRITICAL)
- Scan image Docker (fail HIGH/CRITICAL)
- Exécution du container en CI (healthcheck + smoke test)
- Push GHCR (digest immuable)
- Analyse SonarCloud (Quality Gate)

#### Gestion des vulnérabilités
- Processus CVE structuré
- `.trivyignore` (VEX)
- Montées de versions Spring Boot / Tomcat / Spring Security

**👉 V3 = pipeline DevSecOps complet, durcissement sécurité, gestion CVE, qualité logicielle.**

---

### **V4 — Déploiement Kubernetes (kind), Kustomize & CI/CD de déploiement**
La V4 introduit l’infrastructure Kubernetes et le déploiement automatisé.

#### Kubernetes (kind)
- Cluster local kind
- Namespace `coreservice`
- Ingress NGINX + routage `/api/*`
- Probes readiness/liveness
- Requests/limits CPU & RAM
- Container non‑root

#### Kustomize
- Base générique (`k8s/base`)
- Overlay local (`k8s/overlays/local`)
- Patch dynamique de l’image via SHA
- Patch probes & ressources

#### CI/CD Kubernetes
- Build Maven
- Build Docker
- SBOM CycloneDX
- SCA Trivy
- Scan image
- Push GHCR (SHA)
- Déploiement automatique sur kind
- Vérification du rollout

**👉 V4 = première infrastructure Kubernetes + déploiement automatisé + sécurité renforcée.**

---

## 6. Roadmap et releases futures envisagées

La roadmap vise à couvrir **l’ensemble des compétences attendues** pour un projet moderne sécurisé :  
Java sécurisé, CI/CD sécurisé, Kubernetes sécurisé, Cloud sécurisé.

### Release V5 — Déploiement EKS (immédiate, en cours)
- Création d’un cluster EKS,
- NodeGroups,
- IAM Roles for Service Accounts (IRSA),
- Sécurisation réseau (Security Groups),
- Ingress Controller EKS,
- Adaptation Kustomize pour EKS.

### Release V6 — Persistance et base de données
- Passage à PostgreSQL,
- Secrets Kubernetes (SealedSecrets),
- Configuration sécurisée.

### Release V7 — Sécurité Kubernetes / EKS
- NetworkPolicies,
- PodSecurityStandards,
- Sécurisation des images,
- Admission controllers (OPA/Gatekeeper),
- Analyse de posture de sécurité.

### Release V8 — Observabilité
- Prometheus,
- Grafana,
- Metrics Spring Boot,
- Logs structurés centralisés,
- Dashboards opérationnels.

### Release V9 — Scalabilité
- Horizontal Pod Autoscaler (HPA),
- Tests de charge,
- Optimisation des ressources.

### Release V10 — Industrialisation
- Helm chart,
- Promotion multi‑environnements,
- Observabilité cloud,
- Sécurité cloud avancée.

---

## 7. Compétences acquises

### 7.1 Compétences techniques
- Développement backend Spring Boot 3,
- Architecture logicielle modulaire,
- Docker et images sécurisées,
- Kubernetes (kind) : Deployments, Services, Ingress, Probes,
- Kustomize (base + overlays),
- CI/CD GitHub Actions,
- SBOM, SCA, scan image.

### 7.2 Compétences DevSecOps
- Intégration de la sécurité dans toutes les étapes du cycle,
- Analyse et correction des vulnérabilités,
- Politique de blocage CI/CD,
- Sécurisation du déploiement Kubernetes.

### 7.3 Compétences d’architecture
- Conception d’architectures applicatives et Kubernetes,
- Production de diagrammes techniques,
- Structuration d’un projet en releases.

### 7.4 Compétences méthodologiques
- Travail itératif,
- Validation continue,
- Documentation professionnelle,
- Gestion de projet Agile.

### 7.5 Compétences liées à l’usage de l’IA
- Pilotage d’une IA dans un contexte d’ingénierie,
- Co‑construction de spécifications, architectures et documents,
- Interaction humain–IA structurée,
- Utilisation d’une IA secondaire pour challenger les propositions.

---

## 8. Caractère innovant de la démarche

La démarche adoptée est innovante car elle :

- utilise l’IA comme **copilote d’ingénierie**,  
- repose sur un **pilotage humain systématique**,  
- met en œuvre un **double contrôle IA** (production + challenge),  
- produit un **projet complet**, pas un ensemble de fragments,  
- intègre l’IA dans un **cycle de développement réel**,  
- démontre une capacité à **diriger l’IA**, pas à la suivre.

---

## 9. Liens vers les documents par release

### Release V1
- Spécification technique V1 : `docs/specification-technique-v1.md`
- Architecture V1 : `docs/architecture-v1.md`
- Fiches sécurité V1 : `docs/securite-v1/`
- Release Note V1 : `docs/release-notes/release-v1.md`
- README V1 : `docs/readme-v1.md`

### Release V2
- Spécification technique V2 : `docs/specification-technique-v2.md`
- Architecture V2 : `docs/architecture-v2.md`
- Fiches sécurité V2 : `docs/securite-v2/`
- Release Note V2 : `docs/release-notes/release-v2.md`
- README V2 : `docs/readme-v2.md`

### Release V3
- Spécification technique V3 : `docs/specification-technique-v3.md`
- Architecture V3 : `docs/architecture-v3.md`
- Fiches sécurité V3 : `docs/securite-v3/`
- Release Note V3 : `docs/release-notes/release-v3.md`
- README V3 : `docs/readme-v3.md`

### Release V4
- Spécification technique V4 : `docs/specification-technique-v4.md`
- Architecture V4 : `docs/architecture-v4.md`
- Fiches sécurité V4 : `docs/securite-v4/`
- Release Note V4 : `docs/release-notes/release-v4.md`
- README V4 : `docs/readme-v4.md`

### Release V5 (EKS — en cours)
- Spécification technique V5 : `docs/specification-technique-v5.md`
- Architecture V5 : `docs/architecture-v5.md`
- Fiches sécurité V5 : `docs/securite-v5/`
- Release Note V5 : `docs/release-notes/release-v5.md`
- README V5 : `docs/readme-v5.md`

---

## 10. Conclusion

Le projet CoreService constitue un exemple concret de **développement moderne assisté par IA**, combinant :

- une démarche itérative,
- une architecture maîtrisée,
- une sécurité intégrée,
- un déploiement Kubernetes complet,
- une documentation professionnelle,
- une collaboration humain–IA structurée et contrôlée.

Il s’agit d’un projet à la fois **technique**, **pédagogique** et **méthodologique**