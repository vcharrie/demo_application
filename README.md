# CoreService — Projet d’architecture logicielle & DevSecOps

CoreService est un projet complet visant à démontrer une maîtrise moderne de l’ingénierie logicielle :  
architecture, sécurité, CI/CD, conteneurisation, Kubernetes, documentation professionnelle et pilotage d’IA.

Le projet a été construit **release par release**, avec une complexité croissante, en suivant une démarche itérative inspirée des pratiques professionnelles d’architecture et de gestion de projet Agile.

---

## 📘 1. Document central — Vision & démarche

Le document chapeau présente :

- la vision globale du projet,  
- la démarche pédagogique,  
- l’approche itérative,  
- la collaboration humain–IA,  
- les releases réalisées,  
- la roadmap,  
- les compétences acquises.

👉 **Lire le document chapeau :**  
[`/docs/doc-chapeau.md`](docs/doc-chapeau.md)

Ce document est la **porte d’entrée principale** pour comprendre la logique du projet.

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

## 📂 7. Documentation par release

Toute la documentation est organisée par version dans :  
👉 [`/docs/`](docs/)

Chaque release contient :

- spécification technique,  
- architecture,  
- fiches sécurité,  
- release note,  
- README dédié.

---

## 👤 8. À propos de moi

👉 **Présentation courte (pitch professionnel)**  
[`/about/about.md`](about/about.md)

👉 **CV & documents complets**  
[`/about/`](about/)

---

## 📬 9. Contact

- **Email** : vincent.xxx@mail.com  
- **LinkedIn** : https://linkedin.com/in/xxxx  
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