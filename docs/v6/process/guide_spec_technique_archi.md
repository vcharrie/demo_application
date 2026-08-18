# Guide documentaire — Structure et principes de la Spécification Technique V6

## Objectif du document

Ce guide définit :
- la place de la spécification technique dans le SDLC,
- la distinction entre exigences métier, fonctionnelles et techniques,
- la distinction entre fonctions métier/fonctionnelles et fonctions techniques,
- la manière dont les exigences techniques conduisent aux choix d’architecture,
- la manière dont la solution technique est structurée,
- les référentiels d’ingénierie logicielle sur lesquels repose la démarche.

Il sert de base méthodologique pour :
- structurer la spécification technique V6,
- justifier les choix d’architecture,
- préparer les US techniques et les tâches techniques,
- garantir la conformité sécurité et la traçabilité.

---

# 1. Rôle de la spécification technique

La spécification technique décrit :

> **La solution technique permettant d’implémenter les use cases fonctionnels, en répondant aux exigences techniques locales et transverses.**

Elle contient :
- les **exigences techniques** (besoins techniques),
- les **choix d’architecture** (réponses techniques),
- les **fonctions techniques** du système (capacités techniques),
- les **guidelines de mise en œuvre** (comment appliquer la solution).

Elle ne contient pas :
- les exigences métier,
- les exigences fonctionnelles,
- les use cases,
- les schémas métier ou fonctionnels,
- les détails d’implémentation du code.

---

# 2. Articulation documentaire

La spécification technique s’inscrit dans la chaîne :

`Spec métier → Spec fonctionnelle → Exigences techniques → Choix d’architecture → Spec technique → Architecture (DAT) → US techniques → Tâches techniques`

## 2.1 Ce qui dérive du métier
- exigences métier (EM),
- scénarios métier,
- exigences fonctionnelles (EF),
- use cases fonctionnels (UC).

## 2.2 Ce qui dérive de la technique
- exigences techniques locales (liées aux UC),
- exigences techniques transverses (liées au système),
- choix d’architecture,
- fonctions techniques,
- guidelines de mise en œuvre.

---

# 3. Les trois types de fonctions dans un système

## 3.1 Fonctions métier
Capacités métier indépendantes du système.
Exemples :
- initier un virement,
- créer un compte.

Elles apparaissent uniquement dans :
- la **spec métier**,
- les **schémas métier** (BPMN).

Elles n’apparaissent jamais dans :
- la spec technique,
- l’architecture applicative ou technique.

---

## 3.2 Fonctions fonctionnelles
Comportements du système pour réaliser les capacités métier.
Exemples :
- vérifier le solde,
- historiser une opération,
- appliquer un workflow de validation.

Elles apparaissent uniquement dans :
- la **spec fonctionnelle**,
- les **UC fonctionnels**,
- les **schémas fonctionnels** (UML).

Elles n’apparaissent jamais dans :
- la spec technique,
- l’architecture applicative ou technique.

---

## 3.3 Fonctions techniques
Capacités techniques nécessaires pour que le système fonctionne.
Exemples :
- authentification,
- autorisation,
- audit,
- logging,
- transactions,
- persistance,
- conteneurisation,
- supervision,
- scalabilité,
- sécurité réseau,
- gestion des secrets.

Elles apparaissent uniquement dans :
- la **spec technique**,
- l’**architecture applicative**,
- l’**architecture technique**.

Elles n’apparaissent jamais dans :
- la spec métier,
- la spec fonctionnelle.

---

# 4. Les deux familles d’exigences techniques

## 4.1 Exigences techniques locales (liées aux UC)
Ce sont les contraintes techniques nécessaires pour implémenter un UC fonctionnel.

Exemples :
- UC04F → audit obligatoire,
- UC05F → RBAC métier SUPERVISEUR,
- UC03F → protection contre double dépense,
- UC02F → transaction ACID.

Elles dérivent :
- des scénarios fonctionnels,
- des règles métier,
- des permissions métier.

---

## 4.2 Exigences techniques transverses (liées au système)
Ce sont les contraintes techniques nécessaires pour que le système existe, indépendamment des UC.

Exemples :
- observabilité (logs, métriques, traces),
- supervision infra (Kubernetes, Prometheus),
- conteneurisation Docker,
- CI/CD sécurisé,
- gestion des secrets (Vault),
- disponibilité globale,
- scalabilité globale,
- sécurité réseau,
- sauvegardes / restauration,
- reprise sur incident.

Elles dérivent :
- des contraintes cloud,
- des contraintes de sécurité,
- des contraintes d’exploitation,
- des contraintes de performance,
- des contraintes de disponibilité.

---

# 5. Distinction entre exigence technique et choix d’architecture

## 5.1 Exigence technique = besoin technique
Formulation :  
**« Le système doit… »**

Exemples :
- Le système doit authentifier les utilisateurs.
- Le système doit tracer les actions sensibles.
- Le système doit garantir l’intégrité des transactions.
- Le système doit être observable.
- Le système doit gérer les secrets de manière sécurisée.

---

## 5.2 Choix d’architecture = solution technique
Formulation :  
**« Nous utiliserons… »**

Exemples :
- OAuth2 + JWT RSA via Spring Security,
- Docker multi-stage + distroless,
- Kubernetes stateless + HPA,
- Vault pour les secrets,
- logs JSON + Prometheus + OpenTelemetry.

👉 **Les choix d’architecture sont une réponse aux exigences techniques.**

---

# 6. Représentation applicative des use cases

Les use cases :
- n’apparaissent jamais dans l’architecture applicative ou logique,
- n’apparaissent jamais dans la spec technique.

Ce qui apparaît dans l’architecture applicative :
- les **controllers REST** (adapters entrants),
- les **services applicatifs** (composants techniques),
- les **ports** (interfaces),
- les **adapters techniques** (sortants),
- les **repositories**.

Les endpoints REST sont des **interfaces fonctionnelles**, pas des composants architecturaux.

Les microservices portent des **noms fonctionnels**, mais sont des **artefacts techniques**.

---

# 7. Comment définir les exigences techniques ?

## Étape 1 — Partir des UC fonctionnels  
Identifier les contraintes techniques locales :
- sécurité,
- intégrité,
- performance,
- disponibilité,
- audit,
- transaction,
- validation,
- volumétrie,
- latence,
- asynchronisme.

## Étape 2 — Ajouter les exigences techniques transverses  
Identifier les contraintes techniques globales :
- observabilité,
- conteneurisation,
- déploiement Kubernetes,
- CI/CD,
- sécurité réseau,
- gestion des secrets,
- scalabilité,
- disponibilité,
- résilience,
- sauvegardes,
- reprise sur incident.

## Étape 3 — Définir les choix d’architecture  
Répondre aux exigences techniques :
- architecture hexagonale,
- Spring Security + OAuth2/JWT,
- Docker multi-stage,
- Kubernetes stateless,
- Vault,
- logs JSON,
- Prometheus,
- OpenTelemetry,
- transactions ACID,
- RBAC métier + RBAC technique.

## Étape 4 — Définir les fonctions techniques  
Formaliser les capacités techniques du système.

## Étape 5 — Définir les guidelines de mise en œuvre  
Décrire comment appliquer la solution technique :
- patterns,
- conventions,
- règles de sécurité,
- règles CI/CD,
- règles de logging,
- règles de tests,
- règles d’intégration.

---

# 8. Référentiels d’ingénierie logicielle utilisés

La démarche repose sur les standards suivants :

## 8.1 IEEE 29148 — Software Requirements Specification
- distingue exigences fonctionnelles et techniques,
- impose la formulation “Le système doit…”,
- structure la traçabilité EF → ET → Architecture.

## 8.2 IEEE 42010 — Architecture Description
- distingue concerns, requirements, architecture decisions,
- impose que les choix d’architecture soient justifiés par des exigences.

## 8.3 TOGAF ADM
- structure Business → Application → Technology,
- distingue requirements vs decisions,
- clarifie que microservices / événements sont des **choix d’architecture**, jamais des exigences.

## 8.4 OWASP SAMM / BSIMM
- définissent les exigences techniques de sécurité,
- imposent audit, gestion des secrets, RBAC technique.

## 8.5 NIST SP 800‑160
- structure les exigences techniques transverses,
- impose la traçabilité risque → contrôle → implémentation.

## 8.6 UML / Architecture Hexagonale / DDD
- structurent Domain / Application / Infrastructure,
- définissent les fonctions techniques,
- clarifient la séparation métier / technique.

## 8.7 CNCF / DevOps
- conteneurisation (Docker),
- orchestration (Kubernetes),
- observabilité (Prometheus, OpenTelemetry),
- CI/CD sécurisé.

---

# 9. Synthèse

> **Le métier et le fonctionnel décrivent des comportements (use cases).  
> La technique décrit des capacités (fonctions techniques).  
>  
> Les exigences techniques expriment les besoins techniques.  
> Certaines dérivent des UC (locales).  
> D’autres sont transverses (globales).  
>  
> Les choix d’architecture sont la réponse à ces exigences.  
>  
> L’architecture représente les composants techniques qui implémentent les UC,  
> jamais les UC eux-mêmes.**

Ce guide fournit la structure conceptuelle nécessaire pour rédiger la spécification technique V6.
