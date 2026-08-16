# Guide documentaire — Structure et principes de la Spécification Fonctionnelle V6

## Objectif du document

Ce guide explique comment la **spécification fonctionnelle V6** de CoreService est structurée,  
sur quels **standards d’ingénierie logicielle** elle repose,  
quels **concepts fonctionnels** elle utilise,  
et comment ces concepts sont **liés aux concepts métier** définis dans `spec_metier_v6.md`.

Il sert de support méthodologique pour :
- comprendre la logique de modélisation fonctionnelle,
- expliquer la démarche en entretien,
- justifier la structure documentaire,
- préparer le backlog (User Stories),
- démontrer la traçabilité métier → fonctionnel → tests.

---

# 1. Standards d’ingénierie logicielle utilisés

La spécification fonctionnelle V6 repose sur plusieurs standards internationaux reconnus :

## 1.1 UML (Unified Modeling Language — OMG)
Apporte :
- la notion de **Use Case fonctionnel** (UC fonctionnel),
- la structuration des **scénarios** (nominal, alternatif, erreur),
- la séparation entre **UC métier** et **UC fonctionnel**.

## 1.2 IEEE 830 / ISO/IEC/IEEE 29148 (Software Requirements Specification)
Apporte :
- la structuration des exigences fonctionnelles,
- la notion de **comportement testable**,
- la séparation entre **exigence fonctionnelle** et **règle fonctionnelle**,
- la traçabilité fonctionnelle.

## 1.3 BABOK (Business Analysis Body of Knowledge — IIBA)
Apporte :
- la distinction entre **exigence métier** et **exigence fonctionnelle**,
- la notion de **UC dérivé du métier**,
- la logique de transformation métier → fonctionnel.

## 1.4 OWASP SAMM / BSIMM (Secure-by-Design)
Apporte :
- l’intégration des **permissions métier** dans les préconditions fonctionnelles,
- la notion de **traçabilité des permissions**,
- la séparation entre **contrôle métier** et **contrôle technique**.

## 1.5 IAM / IGA (Identity Governance & Administration)
Apporte :
- la notion de **permission métier**,
- la notion de **rôle métier**,
- la gouvernance des habilitations,
- l’intégration du RBAC dans les UC fonctionnels.

---

# 2. Concepts fonctionnels utilisés

La spécification fonctionnelle V6 utilise les concepts suivants :

## 2.1 UC fonctionnel (Use Case fonctionnel)
Décrit :
- **le comportement du système**,
- **du point de vue de l’utilisateur**,  
- **sans aucune notion technique**.

Il est dérivé **1:1** d’un UC métier.

## 2.2 Règles fonctionnelles (RF)
Décrivent :
- les **contraintes fonctionnelles** appliquées dans le UC,
- dérivées des **règles métier (BR)**,
- et des **permissions métier (RBAC)**.

Elles sont testables et structurent le comportement du système.

## 2.3 Données fonctionnelles manipulées
Décrivent :
- les **données métier** utilisées dans le UC,
- les **états fonctionnels**,
- les **transitions**.

Elles sont dérivées des **entités métier**.

## 2.4 Préconditions fonctionnelles
Décrivent :
- les conditions nécessaires pour exécuter le UC,
- incluant les **permissions métier** (RBAC),
- et les **conditions métier** (statuts, montants, etc.).

## 2.5 Postconditions fonctionnelles
Décrivent :
- l’état du système après exécution du UC,
- les **effets métier**,
- les **effets fonctionnels** (historisation, traçabilité).

## 2.6 Scénarios fonctionnels
Décrivent :
- le déroulement du UC,
- en **nominal**, **alternatif**, **erreur**,
- dérivés des scénarios métier.

## 2.7 Traçabilité fonctionnelle
Relie :
- UC fonctionnel → UC métier → BR → EM → Permission.

Elle garantit la conformité métier et la cohérence fonctionnelle.

---

# 3. Lien entre concepts métier et concepts fonctionnels

La spécification fonctionnelle est une **projection** de la spécification métier :

| Concept métier | Concept fonctionnel | Rôle |
|----------------|---------------------|------|
| EM (Exigence métier) | Objectif du UC fonctionnel | Justifie le comportement |
| BR (Règle métier) | RF (Règle fonctionnelle) | Contrainte appliquée dans le UC |
| UC métier | UC fonctionnel | Déclinaison fonctionnelle |
| Scénarios métier | Scénarios fonctionnels | Déclinaison narrative |
| Entités métier | Données fonctionnelles | Structure manipulée |
| États métier | États fonctionnels | Cycle de vie fonctionnel |
| Processus métier | Enchaînement des UC fonctionnels | Vision globale |
| Matrice BR → Risques | Traçabilité fonctionnelle | Preuve de conformité |

La spec fonctionnelle **ne crée rien de nouveau** :  
elle **décline**, **structure**, **opérationnalise** le métier.

---

# 4. Exigence fonctionnelle (EF) vs Règle fonctionnelle (RF)

## 4.1 Définition d’une exigence fonctionnelle (EF)
Selon IEEE 29148 :

> **Une EF est un comportement atomique, testable, que le système doit fournir, indépendamment des scénarios.**

Caractéristiques :
- atomique,
- indépendante du UC,
- indépendante de l’interface,
- testable isolément.

## 4.2 Définition d’une règle fonctionnelle (RF)
Selon UML + BABOK :

> **Une RF est une contrainte fonctionnelle appliquée dans un UC fonctionnel, dérivée d’une règle métier (BR).**

Caractéristiques :
- dépend du UC,
- exprime une contrainte métier,
- peut regrouper plusieurs EF,
- testable dans le contexte du UC.

## 4.3 Différence EF vs RF

| Concept | Nature | Granularité | Dépend du UC ? | Sert à quoi ? |
|--------|--------|-------------|----------------|---------------|
| **EF** | obligation fonctionnelle | atomique | ❌ non | testabilité, audit |
| **RF** | contrainte appliquée dans un UC | composite | ✔ oui | logique du UC |

Dans CoreService :
- les RF sont **déjà atomiques**,  
- les UC sont **simples**,  
- les scénarios sont **courts**,  
- les permissions sont **claires**,  
→ donc **les EF sont fusionnées dans les RF**, conformément aux standards.

---

# 5. Pourquoi les EF sont fusionnées dans CoreService ?

Parce que :
- le périmètre fonctionnel est simple,
- le nombre de UC est faible,
- les RF sont déjà atomiques,
- les scénarios sont courts,
- ajouter un niveau EF serait redondant,
- cela n’apporterait aucune granularité supplémentaire,
- cela alourdirait la documentation inutilement.

C’est exactement ce que recommandent :
- UML (Use Case simple),
- IEEE 29148 (EF optionnelles dans les petits périmètres),
- BABOK (fusion EF/UC fonctionnel dans les projets à faible complexité).

---

# 6. Synthèse

La spécification fonctionnelle V6 de CoreService :

- applique les standards UML, IEEE, BABOK, OWASP, IAM/IGA,  
- décline fidèlement les concepts métier,  
- intègre le RBAC métier dans les préconditions,  
- fusionne EF et RF pour éviter la sur‑documentation,  
- garantit une traçabilité complète EM → BR → UC → RF → Permission,  
- fournit une base solide pour le backlog (User Stories) et les tests.

Elle est **simple**, **professionnelle**, **standardisée**, et **parfaitement défendable en entretien**.
