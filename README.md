# CoreService — Projet d’architecture logicielle & DevSecOps

CoreService est un projet complet visant à démontrer une maîtrise moderne de l’ingénierie logicielle :  
architecture, sécurité, CI/CD, conteneurisation, Kubernetes, documentation professionnelle et pilotage d’IA.

Le projet a été construit **release par release**, avec une complexité croissante, en suivant une démarche itérative inspirée des pratiques professionnelles d’architecture et de gestion de projet Agile.

---

## 📘 1. Document central — Vision & démarche

La documentation du projet CoreService est **versionnée** : chaque release introduit de nouveaux éléments (architecture, CI/CD, sécurité, Kubernetes, etc.) et la documentation évolue en parallèle.  
Les dossiers présents dans `/docs/v1`, `/docs/v2`, `/docs/v3`, `/docs/v4` reflètent l’état réel du projet à chaque étape.

Deux documents transverses permettent de comprendre cette progression :

### 🔹 Document recruteur — Vue d’ensemble non technique
[`/docs/doc-recruteur.md`](docs/doc-recruteur.md)

Document destiné à un public **non opérationnel** (RH, managers stratégiques, décideurs).  
Il présente :
- la finalité du projet,
- son périmètre,
- les compétences démontrées,
- la logique d’évolution.

Il fournit une **vision synthétique et accessible**, sans entrer dans les détails techniques.

### 🔹 Document chapeau — Vision approfondie et opérationnelle
[`/docs/doc-chapeau.md`](docs/doc-chapeau.md)

Document destiné à un public **technique ou opérationnel** (architectes, tech leads, consultants, managers engineering).  
Il décrit :
- la démarche complète,
- les choix d’architecture,
- les patterns utilisés,
- les pratiques DevSecOps,
- les releases détaillées,
- la logique d’évolution version par version.

Il constitue la **référence principale** pour comprendre la construction du projet et son niveau de maturité.

---

## 🧩 2. Architecture & conception

L’architecture du projet évolue à chaque release :

- structure backend Spring Boot 3,
- architecture modulaire,
- sécurisation progressive,
- conteneurisation Docker,
- déploiement Kubernetes (kind puis EKS),
- Kustomize (base + overlays),
- CI/CD GitHub Actions,
- SBOM, SCA, scans image,
- bonnes pratiques DevSecOps.

Les détails techniques sont décrits dans les documents de chaque release (voir section 4).

---

## 🔄 3. Démarche itérative (releases)

Le projet suit une progression incrémentale :

- **V1** — Structure backend  
- **V2** — Sécurité applicative  
- **V3** — CI/CD & sécurité  
- **V4** — Déploiement Kubernetes (kind)  
- **V5** — Déploiement EKS (en cours)  
- **V6+** — Persistance, sécurité avancée, observabilité, scalabilité, industrialisation

Chaque release contient :

- une spécification technique,  
- une architecture dédiée,  
- des fiches sécurité,  
- une release note,  
- un README spécifique.

👉 Tous les documents sont accessibles dans :  
[`/docs/`](docs/)

---

## 🤖 4. Collaboration humain–IA

Le projet illustre une démarche innovante :

- **Copilot** : IA principale (production, architecture, code, documentation)  
- **Claude** : IA secondaire (challenge, contrôle, cohérence sécurité)  
- **Humain** : pilotage, validation, arbitrage, corrections, décisions

Cette approche démontre une capacité à **diriger plusieurs IA** dans un contexte d’ingénierie logicielle réelle.

Les détails sont décrits dans le document chapeau.

---

## 🧪 5. CI/CD & sécurité

Le pipeline CI/CD inclut :

- build Maven,  
- build Docker,  
- SBOM CycloneDX,  
- scan SCA (Trivy),  
- scan image (Trivy),  
- politique de blocage HIGH/CRITICAL,  
- déploiement automatisé Kubernetes (kind puis EKS).

Les pipelines sont visibles dans `.github/workflows/`.

---

## ☸️ 6. Déploiement Kubernetes

### kind (local)
- cluster local,
- Kustomize (base + overlays),
- Deployment, Service, Ingress,
- probes readiness/liveness,
- patch dynamique de l’image via SHA.

### EKS (cloud)
- cluster EKS,
- NodeGroups,
- IRSA,
- Security Groups,
- Ingress Controller,
- adaptation Kustomize.

---

## 📚 7. Documentation

### 🔹 Documents transverses
- Document recruteur : [`/docs/doc-recruteur.md`](docs/doc-recruteur.md)  
- Document chapeau : [`/docs/doc-chapeau.md`](docs/doc-chapeau.md)

### 🔹 Documentation versionnée

La documentation détaillée est organisée **par version**, afin de refléter l’évolution réelle du projet :

#### 📁 V1 — Socle applicatif & premières pratiques
[`/docs/v1`](docs/v1)
- Architecture V1  
- Spécification fonctionnelle V1  
- Processus DevOps & traçabilité V1  
- Sécurité V1  
- Spécification technique V1  
- Release Note V1  

#### 📁 V2 — Architecture en couches, CRUD & containerisation
[`/docs/v2`](docs/v2)
- Architecture V2  
- Spécification fonctionnelle V2  
- Spécification métier V2  
- Sécurité V2  
- Spécification technique V2  
- Release Note V2  

#### 📁 V3 — DevSecOps, durcissement & CI/CD avancée
[`/docs/v3`](docs/v3)
- Architecture V3  
- CI/CD (pipelines, PR, build)  
- Sécurité (SAST, SCA, SBOM, Dockerfile durci, SLSA, secrets)  
- Spécification technique V3  
- Release Note V3  

#### 📁 V4 — Kubernetes, Kustomize & déploiement automatisé
[`/docs/v4`](docs/v4)
- Architecture Kubernetes (base + overlay local)  
- Spécification technique V4  
- Release Note V4

---

## 👤 8. À propos de moi

👉 **Présentation courte (pitch professionnel)**  
[`/about/about.md`](about/about.md)

👉 **CV & documents complets**  
[`/about/`](about/)

---

## 📬 9. Contact

- **Email** : charrier_vincent@proton.me  
- **LinkedIn** : https://www.linkedin.com/in/vincentcharrier/  
- **GitHub** : https://github.com/xxxx  

---

## 🏁 10. Conclusion

CoreService est un projet complet démontrant :

- une démarche d’architecture moderne,  
- une intégration forte de la sécurité,  
- une industrialisation CI/CD avancée,  
- un déploiement Kubernetes local et cloud,  
- une documentation professionnelle,  
- une collaboration humain–IA structurée.

Il constitue une preuve de maîtrise des pratiques actuelles d’ingénierie logicielle, DevSecOps et architecture cloud.

---

## 📄 11. Licence

Ce projet est publié sous licence **Apache 2.0**, afin de permettre la consultation et l’inspiration tout en protégeant l’attribution.

Voir le fichier [`LICENSE`](LICENSE) à la racine du dépôt.