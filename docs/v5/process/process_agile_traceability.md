# Process Agile & Traçabilité — Modèle de référence et simplification CoreService

## Objectif de ce document

Ce document a deux parties :

1. **Le process idéal** — celui qu'on trouverait dans une organisation avec des rôles séparés (Business Analyst, Product Owner, développeurs, QA), sur un projet soumis à des exigences de conformité (ISO 27001, NIS2, audit réglementaire). C'est le modèle de référence, complet, avec tous les niveaux de traçabilité.
2. **Le choix de simplification pour CoreService** — ce qu'on retient, ce qu'on allège, et pourquoi, compte tenu du contexte réel du projet (démonstrateur solo, objectif de démonstration de compétence plutôt que de conformité opposable).

L'idée n'est pas d'opposer les deux, mais de montrer que la version simplifiée est un choix conscient et documenté, pas une lacune. C'est précisément ce type de justification qui a de la valeur en entretien : montrer qu'on connaît le modèle complet et qu'on sait l'adapter au contexte.

---

## Partie 1 — Le process idéal (multi-rôles, orienté conformité)

### 1.1 Les rôles impliqués

| Rôle | Responsabilité | Niveaux portés |
|---|---|---|
| Sponsor / Métier | Exprime le besoin stratégique | Exigences métier (EM) |
| Business Analyst (BA) | Formalise les règles et cas d'usage métier | Règles métier (BR), UC métier |
| Product Owner (PO) | Découpe en valeur livrable, priorise | Epic (regroupement de chaîne de valeur) |
| PO / UX | Précise l'interaction utilisateur | UC fonctionnel (EF intégrées) |
| Équipe de développement | Réalise et découpe techniquement | User Story, tâches techniques |
| QA / Testeur | Valide fonctionnellement | Critères d'acceptation, tests de validation fonctionnelle |
| Développeur | Valide techniquement | Tests unitaires, tests d'intégration |
| Auditeur / RSSI | Vérifie la chaîne de bout en bout | Matrice de traçabilité complète |

Cette séparation des rôles justifie l'existence de niveaux intermédiaires distincts : chaque rôle a besoin d'un artefact à sa granularité pour travailler sans dépendre en permanence des autres.

### 1.2 Deux axes à ne pas confondre : traçabilité et planning

Un point de méthode central, qui a structuré toutes les corrections de ce document : il existe **deux axes orthogonaux**, pas une seule chaîne hiérarchique à sens unique.

```
Axe TRAÇABILITÉ (exigence → réalisation → test)
   EM ↔ BR ↔ UC métier  →  EF/UC fonctionnel  →  US  →  tests
   Relations souvent N-N : granulaire, c'est la preuve.

Axe PLANNING / LIVRAISON
   UC métier  →  Epic
   Un Epic regroupe les UC métier qui forment ENSEMBLE une chaîne de valeur
   complète — c'est ça, et seulement ça, sa définition. Deux critères possibles
   pour ce regroupement (détaillés en 1.3) :
     - dépendance dure : les UC ne produisent de valeur QUE réunis
     - cohérence de capacité produit : les UC sont livrables séparément,
       mais relèvent de la même fonction du point de vue utilisateur
   La composition d'un Epic est stable (déterminée par la logique métier),
   seul son échéancier de livraison varie selon la vélocité.

   ⚠ Un Epic n'est PAS un synonyme de "Fonctionnalité". Il arrive très
   souvent que la composition d'un Epic coïncide avec une Fonctionnalité
   (capacité produit), notamment quand le critère de regroupement est la
   cohérence de capacité — mais ce n'est qu'une coïncidence fréquente,
   pas la définition. Le critère qui définit l'Epic est TOUJOURS la
   chaîne de valeur (dure ou par cohérence), jamais la capacité produit
   prise isolément. Voir 1.3 pour la distinction complète.
```

L'Epic n'est **jamais** un maillon de la matrice de traçabilité formelle — c'est une métadonnée de regroupement, utile pour piloter et pour présenter, mais la preuve de conformité passe toujours directement de l'exigence à la US, sans obligation de transiter par l'Epic.

### 1.3 La définition de l'Epic — un seul critère, deux variantes

**Définition (à retenir en priorité) : un Epic regroupe les UC métier qui, ensemble, forment une chaîne de valeur métier complète.** Ce n'est jamais "un ensemble de fonctionnalités du même thème" pris comme critère de départ — c'est toujours la question "ces UC ont-ils besoin d'être réunis pour produire quelque chose d'utilisable ?" qui tranche.

Deux variantes de cette même définition :

**Variante 1 — dépendance dure.** Les UC ne produisent AUCUNE valeur isolément.
Exemple : UC04 (initier un virement) + UC05 (valider un virement). Un virement qui reste en PENDING pour toujours, parce que personne ne peut jamais le valider, n'a aucune utilité. Le processus BPMN "Exécution d'un virement interne" ne se termine que si les deux UC sont réalisés ensemble → **Epic "Virement interne"**.

**Variante 2 — cohérence de capacité produit.** Les UC sont livrables indépendamment (chacun a une valeur utilisable seul), mais on choisit de les regrouper parce qu'ils relèvent de la même fonction vue par l'utilisateur final.
Exemple : UC02 (dépôt) + UC03 (retrait). Tu pourrais livrer UC02 seul et ça apporterait déjà de la valeur. Ils sont regroupés parce qu'ils partagent le même processus BPMN "Dépôt/Retrait" et la même cohérence fonctionnelle (opérations simples sur un compte) → **Epic "Opérations compte"**.

C'est uniquement dans ce second cas que l'Epic **coïncide** avec ce qu'on appellerait une "Fonctionnalité" au sens catalogue produit (capacité que le système offre). Cette coïncidence est une conséquence du critère de regroupement choisi, pas la définition de l'Epic elle-même. Dans le premier cas (dépendance dure), parler de "Fonctionnalité" a moins de sens : UC04 seul n'est pas une fonctionnalité utilisable, donc c'est l'Epic entier (UC04+UC05) qui correspond à une capacité produit, jamais un sous-ensemble.

**Résumé à retenir : Epic = chaîne de valeur (critère). Fonctionnalité = lecture "catalogue produit" de cette même chaîne, une fois construite — jamais l'inverse.**

### 1.4 La chaîne complète

```
Exigence métier (EM-xx)
   │
   │  EM ↔ N-N ↔ BR-xx        (une EM peut justifier plusieurs BR, une BR peut découler de plusieurs EM)
   │  EM ↔ N-N ↔ UC métier    (une EM peut couvrir plusieurs UC, un UC peut répondre à plusieurs EM)
   │  BR ↔ N-N ↔ UC métier    (une règle peut s'appliquer à plusieurs UC, un UC peut mobiliser plusieurs BR)
   │
   ▼
UC métier (UC-xx)
   objectif d'un acteur, indépendant de l'interface
   │
   ▼
Epic
   regroupe les UC métier qui forment une chaîne de valeur complète
   (dépendance dure, ex : initier + valider un virement,
   ou cohérence de capacité produit, ex : dépôt + retrait — voir 1.3)
   composition stable ; seul l'échéancier de livraison varie avec la vélocité
   │
   ▼
UC fonctionnel  (EF fusionnées dedans dans la version simplifiée — voir Partie 2)
   même objectif que le UC métier, narré comme interaction utilisateur/système
   (écrans, API, messages, comportements aux limites)
   Dans le modèle complet : N EF ↔ N-N ↔ UC fonctionnel (une exigence fonctionnelle
   atomique EF-xx peut être embarquée dans plusieurs UC fonctionnels)
   │
   ▼
User Story
   tranche de valeur livrable en un sprint, dérivée du UC fonctionnel
   (découpage par scénario nominal/alternatif, ou par incrément de complexité — story slicing)
   rattachée à un Epic (planning) et tracée vers un UC métier / des BR (preuve)
   │
   ├──→ Critère d'acceptation (Given/When/Then)
   │         │
   │         ▼
   │    Test de validation fonctionnelle (recette)
   │
   ├──→ Test d'intégration
   │         valide la US comme un tout, composants assemblés
   │
   └──→ Tâche technique
             │
             ▼
        Test unitaire
             logique isolée
```

### 1.5 Pourquoi chaque niveau existe

**Exigence métier (EM)** — le niveau que réclame un auditeur en premier : "pourquoi cette fonctionnalité existe-t-elle, à quelle obligation répond-elle ?" Sans EM, BR-01 (solde non négatif) ressemble à un choix de conception arbitraire ; avec l'EM associée, elle devient une réponse tracée à une exigence de contrôle interne. Peu nombreuses (5-15 pour un projet), stables dans le temps.

**Règle métier (BR) et UC métier** — le cœur de la spec métier. La BR pose la contrainte, le UC métier pose l'objectif d'usage. Une règle sans contexte d'usage est difficile à tester ; un cas d'usage sans règles explicites est difficile à valider.

**Epic / Fonctionnalité** — niveau de gestion de projet et de présentation produit, pas de preuve. Il sert à planifier des livraisons cohérentes et à décrire ce que le système sait faire — pas à démontrer la conformité. Composition stable, indépendante de la vélocité.

**Exigence fonctionnelle (EF)** — permet à la QA de tester sans avoir à interpréter un scénario narratif entier. C'est l'unité atomique qu'on coche "conforme / non conforme" en recette. Sans EF, seul le UC fonctionnel complet peut être validé en bloc, trop grossier pour une certification.

**UC fonctionnel** — le pont entre le métier et l'implémentation, et surtout un document qui a une valeur propre indépendante du découpage en sprints : c'est la vision fonctionnelle globale et persistante du système, utile pour l'onboarding, la cohérence UX/API, la maintenance, un audit — même si un seul UC fonctionnel ne produit qu'une seule User Story, sa valeur ne vient pas de la réutilisation mais de cette permanence documentaire.

**User Story et story slicing** — le format qui rend le travail livrable en sprint. Sans lui, l'équipe devrait livrer un UC fonctionnel entier d'un coup, ce qui casse le principe même de l'itératif.

**Les quatre niveaux de test** — chacun valide une couche différente, aucun ne remplace un autre : le test unitaire prouve que le code fait ce qu'il est censé faire (pas que le comportement métier est respecté), le test d'intégration prouve que les composants communiquent correctement à l'échelle de la US (pas que la règle métier est bien interprétée), et seul le test de validation fonctionnelle, dérivé des critères d'acceptation eux-mêmes dérivés des EF, prouve la conformité métier.

### 1.6 Ce que produit ce modèle complet

Une matrice de traçabilité où chaque ligne remonte du test jusqu'à l'exigence métier, l'Epic n'apparaissant que comme métadonnée de regroupement, pas comme maillon de preuve :

| EM | BR | UC métier | Epic (info) | EF | US | Test |
|---|---|---|---|---|---|---|
| EM-01 | BR-05, BR-06 | UC04, UC05 | Virement interne | EF-012, EF-013 | US-042, US-043 | TU-089, TI-034, TV-021 |

C'est cet artefact précis qu'un audit ISO 27001 ou une revue Secure SDLC va réclamer : la preuve que rien n'est codé sans exigence, et que rien n'est livré sans test.

---

## Partie 2 — Le choix de simplification pour CoreService

### 2.1 Le contexte qui justifie de s'écarter du modèle complet

CoreService n'est pas un projet réel avec équipe et audit externe — c'est un démonstrateur solo, dont l'objectif est de prouver une maîtrise méthodologique en entretien, pas de produire une preuve de conformité opposable.

- **Un seul rôle porte tous les niveaux** — pas besoin d'artefacts séparés pour permettre à des rôles différents de travailler indépendamment.
- **Personne ne va auditer formellement la matrice** — l'objectif est de savoir l'expliquer et de montrer une structure cohérente, pas de produire un document opposable devant un organisme de certification.
- **Le volume fonctionnel est volontairement restreint** (6 UC métier) — un modèle appliqué strictement avec autant de documents séparés générerait plus de documentation que de code.

### 2.2 Le principe de simplification retenu

**On garde le raisonnement complet, on réduit le nombre de documents et de numérotations séparées — jamais la substance.**

| Niveau du modèle idéal | Décision pour CoreService | Où ça vit concrètement |
|---|---|---|
| Exigence métier (EM) | **Conservée, en version simplifiée** (quelques lignes, pas de fiche BABOK complète) | En tête de `spec_metier.md`, avant les BR |
| Règle métier (BR) | Conservée telle quelle | `spec_metier.md`, section Règles métier |
| UC métier | Conservé tel quel | `spec_metier.md`, section Cas d'usage |
| Epic (regroupement par chaîne de valeur ; coïncide souvent avec une Fonctionnalité) | Conservé, composition stable, avec mention "capacité produit" quand pertinent | `backlog.md`, section Epics |
| Exigence fonctionnelle (EF) | **Fusionnée dans le UC fonctionnel**, pas de numérotation EF-xx séparée | Directement dans le texte de chaque UC fonctionnel |
| UC fonctionnel | **Conservé comme document séparé, non optionnel** — sa valeur tient à la permanence de la vue fonctionnelle, pas au volume de réutilisation | `spec_fonctionnelle.md`, un UC fonctionnel par UC métier |
| User Story | Conservée telle quelle | `backlog.md`, section User Stories, rattachée à un Epic |
| Critère d'acceptation | Conservé tel quel | Sur chaque US, format Given/When/Then |
| Test de validation fonctionnelle | Conservé, sans automatisation Gherkin systématique | Exécuté manuellement ou en test d'intégration selon le cas |
| Test unitaire / test d'intégration | Conservés tels quels | Code source, déjà couverts par les specs techniques v3/v4 |

### 2.3 Ce qui ne change pas malgré la simplification

1. **La traçabilité EM/BR/UC → US reste explicite**, même sous forme de colonnes dans un tableau plutôt qu'une matrice complète à six niveaux. C'est le minimum qui permet de dire, en entretien : "je peux vous montrer, pour n'importe quelle US, quelle exigence métier, quelle règle et quel cas d'usage elle réalise."

2. **La distinction Epic ≠ UC métier est maintenue**, parce qu'elle démontre la compréhension du principe de regroupement par chaîne de valeur (UC04+UC05 indissociables pour le virement, UC02+UC03 regroupés par cohérence de capacité produit) plutôt qu'un simple mapping 1:1 qui trahirait une compréhension superficielle du découpage Agile.

3. **Le UC fonctionnel reste un document à part entière**, pas absorbé dans les User Stories — c'est la vue fonctionnelle stable du système, indépendante du découpage en sprints.

### 2.4 Schéma simplifié retenu pour CoreService

L'ordre ci-dessous suit la dépendance logique réelle : l'Epic (regroupement de UC métier) précède la spec fonctionnelle, qui elle-même précède la rédaction des User Stories — puisqu'une US est une tranche du UC fonctionnel, elle ne peut être écrite qu'une fois ce dernier posé.

```
spec_metier.md (déjà rédigé, à compléter avec les EM)
   EM-xx (simplifiées) ↔ BR-xx ↔ UC métier
        │
        ▼
   backlog.md — section Epics (3-4)
   composition stable, mention "capacité produit" par Epic
        │
        ▼
   spec_fonctionnelle.md (à créer — document à part entière, non optionnel)
   UC fonctionnels — comportement précis par UC, EF fusionnées dedans
        │
        ▼
   backlog.md — section User Stories
   dérivées des UC fonctionnels (par scénario ou story slicing)
   rattachées à un Epic, tracées vers UC métier source / BR appliquées
        │
        ▼
   spec_technique_v3/v4.md (déjà rédigées)
   tâches techniques, tests unitaires, tests d'intégration
```

`backlog.md` porte donc deux sections rédigées à des moments différents de la chaîne : les Epics juste après la spec métier, les User Stories seulement après la spec fonctionnelle — mais les deux sections cohabitent dans le même fichier physique.

### 2.5 Ce qu'il faudrait rétablir si le projet changeait de nature

Si CoreService devait un jour servir de brique dans un contexte réel avec obligation de conformité (audit client, certification), les éléments encore allégés (numérotation EF séparée, matrice de traçabilité formelle à six colonnes) redeviendraient nécessaires. Le choix actuel est réversible et documenté comme tel — savoir dimensionner un process selon le contexte, plutôt que d'appliquer un modèle unique par défaut, est une compétence à part entière côté AppSec/Secure SDLC.
