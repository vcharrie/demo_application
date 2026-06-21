# CoreService — Résumé pour recruteur

## Présentation du projet
CoreService est un projet d’ingénierie logicielle moderne conçu pour démontrer un ensemble complet de compétences techniques, méthodologiques et DevSecOps, en s’appuyant sur une démarche itérative et une collaboration structurée entre un humain et deux IA (Copilot pour la production, Claude pour le contrôle).

Le projet met en valeur :
- une expérience préalable en architecture logicielle et gestion Agile,
- une montée en compétence sur les technologies modernes du marché,
- une capacité à piloter un cycle de développement complet,
- une intégration systématique de la sécurité (approche “security by design”).

La version détaillée du projet (spécifications, architectures, fiches sécurité, CI/CD, Kubernetes, EKS) est disponible dans la **Doc Chapeau** :  
`docs/doc_chapeau.md`

---

## Objectifs du projet
- Démontrer la maîtrise d’un cycle complet d’ingénierie logicielle moderne.
- Mettre en œuvre les bonnes pratiques DevSecOps.
- Déployer une application sur Kubernetes local (kind) puis cloud (EKS).
- Structurer un projet en releases cohérentes, documentées et sécurisées.
- Illustrer la capacité à travailler efficacement avec l’IA dans un contexte technique.

---

## Releases réalisées

### V1 — Backend
- Squelette Spring Boot 3
- Endpoints REST initiaux
- Première documentation

### V2 — Sécurité applicative
- Validation d’entrée
- Gestion centralisée des erreurs
- Logs sécurisés

### V3 — CI/CD sécurisé
- Pipeline GitHub Actions
- Build Maven + Docker
- SBOM CycloneDX
- Scan SCA + scan image (Trivy)
- Politique de blocage sur vulnérabilités

### V4 — Kubernetes (kind)
- Cluster kind
- Kustomize (base + overlays)
- Deployment, Service, Ingress
- Probes readiness/liveness
- Déploiement automatisé via CI/CD

---

## Roadmap immédiate (en cours)
### V5 — Déploiement EKS (AWS)
- Cluster EKS
- NodeGroups
- IRSA
- Security Groups
- Ingress Controller EKS
- Adaptation Kustomize

---

## Compétences démontrées

### Techniques
- Spring Boot 3, Java 21
- Docker, images sécurisées
- Kubernetes (kind), Kustomize
- CI/CD GitHub Actions
- SBOM, SCA, scans image

### DevSecOps
- Sécurité by design
- Politique de blocage CI/CD
- Fiches sécurité et mesures de risque
- Sécurisation du pipeline et des dépendances

### Architecture & Méthodologie
- Conception d’architectures applicatives et Kubernetes
- Structuration en releases
- Documentation professionnelle
- Gestion de projet Agile

### IA & Ingénierie
- Pilotage d’une IA principale (Copilot) pour la production
- Utilisation d’une IA secondaire (Claude) pour le contrôle
- Validation croisée humain–IA
- Capacité à diriger et challenger les propositions IA

---

## Conclusion
CoreService illustre la capacité à :
- concevoir, sécuriser et industrialiser un projet complet,
- intégrer les bonnes pratiques DevSecOps,
- déployer sur Kubernetes et EKS,
- produire une documentation professionnelle,
- piloter efficacement plusieurs IA dans un contexte d’ingénierie.

Pour les détails techniques, la démarche complète et l’ensemble des livrables :  
👉 **Voir la Doc Chapeau** : `docs/doc_chapeau.md`