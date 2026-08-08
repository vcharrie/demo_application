# Error Handling — Release v5

## 1. Objectifs

La gestion des erreurs vise à garantir :

- une API cohérente et prédictible,
- une séparation stricte entre erreurs métier, erreurs applicatives et erreurs techniques,
- une traçabilité complète,
- une compatibilité ascendante,
- une robustesse face aux évolutions du modèle métier et du schéma de base de données.

Toutes les erreurs exposées à l’API sont normalisées sous forme JSON.

---

## 2. Typologie des erreurs

L’architecture distingue quatre catégories d’erreurs, selon leur origine et leur nature.

---

## 2.1 Business Exceptions (Domain Layer)

Origine :
- Les règles métier immuables situées dans les entités du domaine.
- Les invariants métier.

Nature :
- Erreurs levées par les méthodes du domaine lorsqu’un invariant est violé.

Exemples :
- solde négatif interdit,
- montant d’opération non valide,
- opération interdite selon l’état du compte.

Caractéristiques :
- levées dans le domaine,
- ne connaissent pas HTTP,
- ne connaissent pas Spring,
- ne connaissent pas la base de données.

Propagation :
- remontent au service applicatif,
- converties en FunctionalException.

Raison :
- le domaine ne doit jamais exposer directement une erreur API.

---

## 2.2 Functional Exceptions (Application Layer)

Origine :
- La logique métier applicative (services),
- Les règles métier contextuelles non invariantes.

Nature :
- Erreurs liées à l’orchestration métier.

Exemples :
- compte introuvable,
- solde insuffisant (si la règle est dans le service),
- opération interdite selon le statut du compte,
- validation métier dépendante du contexte.

Caractéristiques :
- levées dans les services applicatifs,
- exposées à l’API sous forme d’erreurs métier,
- converties en JSON via le ControllerAdvice.

Propagation :
- remontent au ControllerAdvice,
- converties en HTTP 400 ou 409.

---

## 2.3 Technical Exceptions maîtrisées (Application Layer)

Origine :
- Erreurs techniques anticipées et maîtrisées dans la logique applicative.

Nature :
- Erreurs techniques qui ne sont pas des erreurs métier,
- mais qui doivent être capturées et traitées explicitement.

Exemples :
- échec d’un update DB,
- violation d’un index unique,
- contrainte d’intégrité violée,
- problème de persistance anticipé.

Caractéristiques :
- levées dans les services applicatifs,
- converties en TechnicalException,
- exposées à l’API comme erreurs techniques uniformisées.

Propagation :
- remontent au ControllerAdvice,
- converties en HTTP 409 ou 500 selon le cas.

Raison :
- ce sont des erreurs techniques maîtrisées, mais pas des erreurs métier.

---

## 2.4 Technical Exceptions non maîtrisées (Infrastructure + Spring)

Origine :
- Spring MVC (binding, validation),
- JPA/Hibernate,
- base de données,
- JSON parsing,
- erreurs système,
- exceptions inattendues.

Nature :
- Erreurs techniques non prévues par le code métier.

Exemples :
- UUID invalide,
- JSON mal formé,
- NullPointerException,
- IllegalStateException,
- Timeout,
- erreurs réseau.

Caractéristiques :
- ne doivent jamais remonter brutes,
- doivent être capturées par un handler générique,
- doivent être converties en une erreur API uniforme.

Propagation :
- capturées par le ControllerAdvice,
- converties en HTTP 400 ou 500 selon le cas.

---

## 3. ControllerAdvice global

Le ControllerAdvice centralise la gestion des erreurs exposées à l’API.

Principes :
- Ne jamais exposer une stack trace.
- Ne jamais exposer une erreur technique brute.
- Ne jamais renvoyer une exception Java non maîtrisée.
- Toujours renvoyer un JSON structuré.
- Toujours distinguer métier / applicatif / technique.

Le ControllerAdvice contient :
- un handler pour FunctionalException,
- un handler pour TechnicalException,
- un handler pour les erreurs de binding (UUID invalide),
- un handler pour les violations de validation,
- un handler pour les erreurs DB,
- un fallback pour les erreurs internes.

---

## 4. Structure des erreurs exposées

Toutes les erreurs exposées suivent la structure :

    {
      "error": "<CODE>",
      "message": "<DESCRIPTION>"
    }

Règles :
- error : code stable, versionné, compatible ascendant,
- message : description lisible, non technique,
- jamais de stack trace,
- jamais de message interne de la base de données,
- jamais d’exception Java brute.

---

## 5. Compatibilité ascendante

Les codes d’erreurs métier et techniques sont versionnés et ne doivent jamais être modifiés ou supprimés sans migration.

Les erreurs techniques doivent rester stables pour garantir :
- la compatibilité avec les clients existants,
- la stabilité des tests d’intégration,
- la prédictibilité des comportements API.

---

## 6. Tests associés

Tests d’intégration couvrent :
- BusinessException → FunctionalException,
- FunctionalException,
- TechnicalException maîtrisée,
- erreurs de validation,
- erreurs de binding (UUID invalide),
- erreurs DB,
- erreurs internes (fallback).

Tests unitaires couvrent :
- conversion des exceptions,
- cohérence des codes d’erreurs,
- mapping métier → API.

---

## 7. Synthèse

La gestion des erreurs repose sur :

- une séparation stricte Domain / Application / Infrastructure,
- une distinction claire entre BusinessException, FunctionalException et TechnicalException,
- un ControllerAdvice robuste,
- des codes d’erreurs stables,
- une compatibilité ascendante,
- une couverture de tests complète.

Elle garantit une API fiable, prédictible et adaptée à un produit métier évolutif.
