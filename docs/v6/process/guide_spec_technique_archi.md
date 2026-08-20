# Guide documentaire — Structure et principes de la Spécification Technique V6
## Version mise à jour — intégration du pattern Exigence → Fonction → Solution → Composant

## Objectif du document

Ce guide définit :
- la place de la spécification technique dans le SDLC,
- la distinction entre exigences métier, fonctionnelles et techniques,
- la distinction entre fonctions métier/fonctionnelles et fonctions techniques,
- la manière dont les exigences techniques conduisent aux choix d’architecture,
- la manière dont la solution technique est structurée,
- les référentiels d’ingénierie logicielle sur lesquels repose la démarche,
- le pattern canonique : **Exigence → Fonction → Solution → Composant**.

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
- les **exigences techniques** (besoins formulés en *« Le système doit… »*),
- les **fonctions techniques** (capacités formulées en *« Le système assure… »*),
- les **choix d’architecture** (réponses formulées en *« Nous utiliserons… »*),
- les **composants techniques** (implémentations concrètes),
- les **guidelines de mise en œuvre**.

Elle ne contient pas :
- les exigences métier,
- les exigences fonctionnelles,
- les use cases,
- les schémas métier ou fonctionnels,
- les détails d’implémentation du code.

---

# 2. Articulation documentaire

La chaîne documentaire suit le pattern canonique :

> **Exigence → Fonction → Solution → Composant**

Et s’inscrit dans :

`Spec métier → Spec fonctionnelle → Exigences techniques → Fonctions techniques → Choix d’architecture → Spec technique → Architecture (DAT) → US techniques → Tâches techniques`

## 2.1 Ce qui dérive du métier
- exigences métier (EM),
- scénarios métier,
- exigences fonctionnelles (EF),
- use cases fonctionnels (UC).

## 2.2 Ce qui dérive de la technique
- exigences techniques locales (liées aux UC),
- exigences techniques transverses (liées au système),
- fonctions techniques,
- choix d’architecture,
- composants techniques,
- guidelines de mise en œuvre.

---

# 3. Les trois types de fonctions dans un système

## 3.1 Fonctions métier
Capacités métier indépendantes du système.  
Elles apparaissent uniquement dans :
- la spec métier,
- les schémas métier (BPMN).

## 3.2 Fonctions fonctionnelles
Comportements du système pour réaliser les capacités métier.  
Elles apparaissent uniquement dans :
- la spec fonctionnelle,
- les UC fonctionnels,
- les schémas fonctionnels (UML).

## 3.3 Fonctions techniques
Capacités techniques nécessaires pour que le système fonctionne.  
Formulation : **« Le système assure… »**

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
- la spec technique,
- l’architecture applicative,
- l’architecture technique.

---

# 4. Les deux familles d’exigences techniques

## 4.1 Exigences techniques locales (liées aux UC)
Formulation : **« Le système doit… »**

Ce sont les contraintes techniques nécessaires pour implémenter un UC fonctionnel.  
Conformes à :
- IEEE 29148 (Functional Requirements),
- TOGAF ADM (Application Requirements).

Exemples :
- UC04F → *Le système doit tracer les actions sensibles.*
- UC05F → *Le système doit appliquer le RBAC métier.*
- UC03F → *Le système doit garantir l’intégrité des opérations.*
- UC02F → *Le système doit exécuter une transaction ACID.*

## 4.2 Exigences techniques transverses (liées au système)
Formulation : **« Le système doit… »**

Ce sont les contraintes techniques nécessaires pour que le système existe, indépendamment des UC.  
Conformes à :
- IEEE 29148 (Non‑Functional Requirements),
- IEEE 42010 (Architecture Requirements),
- TOGAF ADM (Technology Requirements).

Exemples :
- *Le système doit être observable.*
- *Le système doit être conteneurisé.*
- *Le système doit être déployé sur Kubernetes.*
- *Le système doit gérer les secrets de manière sécurisée.*
- *Le système doit appliquer une segmentation réseau.*
- *Le système doit implémenter un RBAC technique cloud/Kubernetes.*
- *Le système doit être résilient et disponible.*

---

# 5. Distinction entre exigence, fonction, solution et composant

## 5.1 Exigence technique = besoin
Formulation : **« Le système doit… »**

## 5.2 Fonction technique = capacité
Formulation : **« Le système assure… »**

## 5.3 Choix d’architecture = solution
Formulation : **« Nous utiliserons… »**

## 5.4 Composant technique = implémentation
Formulation : **nom du composant**

### Exemple complet
- **Exigence** : *Le système doit segmenter le réseau Pod‑to‑Pod.*
- **Fonction** : *Le système assure la segmentation réseau interne.*
- **Solution** : *Nous utiliserons des NetworkPolicies Kubernetes.*
- **Composant** : *CNI Calico/Cilium.*

---

# 6. Représentation applicative des use cases

Les use cases :
- n’apparaissent jamais dans l’architecture applicative ou logique,
- n’apparaissent jamais dans la spec technique.

Ce qui apparaît dans l’architecture applicative :
- controllers REST (adapters entrants),
- services applicatifs,
- ports (interfaces),
- adapters techniques (sortants),
- repositories.

Les endpoints REST sont des interfaces fonctionnelles, pas des composants architecturaux.  
Les microservices portent des noms fonctionnels, mais sont des artefacts techniques.

---

# 7. Comment définir les exigences techniques ?

## Étape 1 — Identifier les exigences techniques locales
Formulation : **« Le système doit… »**

## Étape 2 — Identifier les exigences techniques transverses
Formulation : **« Le système doit… »**

## Étape 3 — Définir les fonctions techniques
Formulation : **« Le système assure… »**

## Étape 4 — Définir les choix d’architecture
Formulation : **« Nous utiliserons… »**

## Étape 5 — Définir les composants techniques
Formulation : **nom du composant**

## Étape 6 — Définir les guidelines de mise en œuvre
Formulation : **comment appliquer la solution**

---

# 8. Référentiels d’ingénierie logicielle utilisés

## IEEE 29148 — Software Requirements Specification
## IEEE 42010 — Architecture Description
## TOGAF ADM — Business / Application / Technology
## OWASP SAMM / BSIMM — Sécurité applicative
## NIST SP 800‑160 — Ingénierie de la sécurité
## UML / Architecture Hexagonale / DDD
## CNCF / DevOps — Cloud native, Kubernetes, CI/CD, observabilité

---

# 9. Synthèse

> **Les exigences techniques expriment les besoins (“Le système doit…”).  
> Les fonctions techniques expriment les capacités (“Le système assure…”).  
> Les choix d’architecture expriment les solutions (“Nous utiliserons…”).  
> Les composants techniques implémentent les solutions.  
>  
> Les exigences techniques locales dérivent des UC.  
> Les exigences techniques transverses dérivent des contraintes cloud, sécurité, exploitation.  
>  
> Ce pattern garantit la traçabilité, la justification des choix,  
> et la conformité sécurité.**

Ce guide fournit la structure conceptuelle nécessaire pour rédiger la spécification technique V6.
