# Spécification Métier – Version V2 (strictement métier)

## 1. Contexte métier

Le domaine métier de la version V2 concerne la gestion d’une entité simple appelée **Resource**.  
Une ressource représente une unité d’information identifiable, nommée et décrite.  
Le métier vise à définir ce qu’est une ressource, comment elle est caractérisée et quelles règles garantissent sa validité.

## 2. Modèle métier (Domain Model)

### 2.1. Entité métier : Resource

L’entité **Resource** est le concept central du domaine.

Elle est définie par les attributs métier suivants :

- **id** : identifiant métier unique de la ressource  
- **name** : nom métier de la ressource  
- **description** : description textuelle de la ressource  

Ces attributs constituent l’identité et la nature métier d’une ressource.

## 3. Invariants métier

Une ressource doit respecter les invariants suivants :

- **L’identifiant (id) est obligatoire.**  
  Une ressource ne peut exister sans identifiant.

- **Le nom (name) est obligatoire et doit être non vide.**  
  Une ressource doit être nommée pour être reconnue dans le domaine.

- **La description peut être vide, mais doit être définie.**

Ces invariants garantissent la validité d’une ressource dans le domaine métier.

## 4. Règles métier

Les règles métier applicables à l’entité Resource sont les suivantes :

- **Une ressource est identifiée de manière unique par son id.**  
  Deux ressources ne peuvent partager le même identifiant.

- **Une ressource doit être fournie dans un état valide**, c’est‑à‑dire conforme aux invariants.

- **Une ressource ne peut pas être considérée comme existante si son identifiant n’est pas connu du domaine.**

- **Une ressource ne peut pas être créée sans identifiant** (invariant métier V2).

Ces règles définissent le comportement attendu du domaine indépendamment de toute implémentation.

## 5. Glossaire métier

- **Resource** : unité métier identifiable, nommée et décrite.  
- **Identifiant métier (id)** : valeur unique permettant de distinguer une ressource.  
- **Nom (name)** : libellé métier permettant d’identifier la ressource.  
- **Description** : texte décrivant la ressource.

## 6. Hors périmètre métier V2

La version V2 ne définit pas :

- les opérations fonctionnelles (création, suppression, consultation, etc.)  
- les comportements techniques  
- les interactions avec des systèmes externes  
- la persistance ou le stockage  
- la sécurité  
- les formats d’échange  
- les erreurs techniques ou fonctionnelles  

Ces éléments relèvent des spécifications fonctionnelles et techniques, pas de la spécification métier.
