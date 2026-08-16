# Guide documentaire — Structure et principes de la Spécification Métier V5

## Objectif du document

Ce guide explique comment la **spécification métier V5** de CoreService est structurée,  
quels sont les **concepts utilisés**,  
et comment ils s’articulent pour former une description métier complète, cohérente et testable.

Il sert de support méthodologique pour :
- comprendre la logique de modélisation,
- expliquer la démarche en entretien,
- justifier la structure documentaire,
- préparer les versions ultérieures (V6, RBAC, sécurité).

---

# 1. Les fondations de la modélisation métier

La spécification métier V5 repose sur **six piliers** :

1. **Exigences métier (EM)**  
2. **Règles métier (BR)**  
3. **Cas d’usage métier (UC)**  
4. **Scénarios métier**  
5. **Entités métier**  
6. **États métier**  
7. **Processus métier (BPMN simplifié)**  
8. **Matrice BR → Risques**

Ces éléments forment une chaîne cohérente qui permet :
- de comprendre le métier,
- de structurer les comportements attendus,
- de tracer les obligations métier,
- de préparer les tests fonctionnels,
- de garantir la conformité.

---

# 2. Exigences métier (EM)

Les **EM** sont les obligations métier de haut niveau.  
Elles répondent à la question :

> « Pourquoi le système doit-il exister et que doit-il garantir ? »

Caractéristiques :
- peu nombreuses,
- stables dans le temps,
- orientées métier,
- indépendantes de l’interface ou de la technique.

Exemples dans CoreService :
- intégrité financière,
- opérations simples sur un compte,
- virement interne,
- validation des opérations sensibles,
- traçabilité complète.

Les EM sont la **source** de toutes les règles métier et cas d’usage.

---

# 3. Règles métier (BR)

Les **BR** sont les contraintes métier qui doivent être respectées.  
Elles répondent à la question :

> « Quelles conditions doivent être vraies pour que le métier soit correct ? »

Caractéristiques :
- atomiques,
- testables,
- indépendantes des scénarios,
- appliquées dans plusieurs UC.

Exemples :
- solde non négatif,
- compte suspendu → aucune opération,
- montant positif,
- historisation obligatoire,
- auto‑virement interdit.

Les BR sont la **colonne vertébrale** de la logique métier.

---

# 4. Cas d’usage métier (UC)

Les **UC** décrivent les objectifs métier des acteurs.  
Ils répondent à la question :

> « Que veut faire l’acteur dans le système ? »

Caractéristiques :
- centrés sur l’acteur,
- indépendants de l’interface,
- mobilisent plusieurs BR,
- rattachés à une ou plusieurs EM.

Exemples :
- créer un compte,
- déposer,
- retirer,
- initier un virement,
- valider un virement,
- consulter l’historique.

Les UC sont le **point d’entrée** pour la modélisation fonctionnelle.

---

# 5. Scénarios métier

Les scénarios détaillent **comment** un UC se déroule.  
Ils répondent à la question :

> « Que se passe-t-il dans les cas normaux, alternatifs et d’erreur ? »

Caractéristiques :
- narratifs,
- exhaustifs,
- couvrent nominal / alternatif / erreur,
- permettent de dériver les tests fonctionnels.

Exemples :
- dépôt nominal,
- retrait avec solde insuffisant,
- virement > seuil → PENDING,
- auto‑virement → refus,
- compte suspendu → refus.

Les scénarios sont la **matière première** des tests fonctionnels.

---

# 6. Entités métier

Les entités décrivent les **objets métier manipulés** par le système.  
Elles répondent à la question :

> « Quels objets le métier manipule-t-il et quelles sont leurs propriétés ? »

Caractéristiques :
- structure de données métier,
- indépendantes de la technique,
- utilisées dans les UC et scénarios.

Exemples :
- Compte,
- Opération,
- Virement,
- Client,
- Agent.

Les entités sont la **base du modèle de domaine**.

---

# 7. États métier

Les états décrivent les **transitions possibles** des entités.  
Ils répondent à la question :

> « Dans quels états une entité peut-elle se trouver et comment évolue-t-elle ? »

Caractéristiques :
- transitions explicites,
- règles de passage d’un état à un autre,
- cohérence métier.

Exemples :
- Compte : Actif → Suspendu → Fermé  
- Opération : INITIATED → VALIDATED / REJECTED  
- Virement : INITIATED → COMPLETED / PENDING / FAILED

Les états garantissent la **cohérence du cycle de vie métier**.

---

# 8. Processus métier (BPMN simplifié)

Les processus décrivent les **enchaînements métier**.  
Ils répondent à la question :

> « Comment les UC s’enchaînent-ils dans un flux métier réel ? »

Caractéristiques :
- séquences d’étapes,
- conditions métier,
- transitions d’état,
- règles mobilisées.

Exemples :
- processus de virement interne,
- processus dépôt/retrait.

Les processus donnent une **vision globale** du fonctionnement métier.

---

# 9. Matrice BR → Risques

La matrice relie chaque règle métier à un risque.  
Elle répond à la question :

> « Quel risque métier est couvert par cette règle ? »

Caractéristiques :
- justification métier,
- couverture des risques,
- support d’audit,
- alignement ISO/NIS2.

Exemples :
- solde non négatif → incohérence comptable,
- compte suspendu → contournement des statuts,
- historisation obligatoire → absence de traçabilité.

La matrice est la **preuve de conformité métier**.

---

# 10. Synthèse : pourquoi cette structure ?

La structure V5 permet :

- une **modélisation métier complète**,  
- une **traçabilité EM → BR → UC → scénarios → tests**,  
- une **séparation claire métier / technique**,  
- une **base solide pour la spec fonctionnelle**,  
- une **base solide pour l’intégration RBAC en V6**,  
- une **preuve de maîtrise méthodologique** en entretien.

Elle est volontairement :
- simple,
- stable,
- cohérente,
- exhaustive,
- adaptée à un démonstrateur solo,
- conforme aux standards (BABOK, UML, BPMN, ISO 27001).

---

# 11. Ce que prépare ce guide

Ce guide sert de base pour :

- la **spec métier V6** (avec RBAC),
- la **spec fonctionnelle V6**,
- le **backlog V6**,
- la **matrice de traçabilité V6**,
- l’**architecture sécurité V6**.

Il permet d’expliquer clairement **comment** et **pourquoi** la spec métier est structurée ainsi.

# 12. Référentiels utilisés

La structuration de la spécification métier (exigences métier, règles métier, cas d’usage, scénarios, entités, états, processus, matrice de risques) repose sur un ensemble de standards internationaux reconnus en ingénierie logicielle, analyse métier et sécurité.

## 12.1 BABOK (Business Analysis Body of Knowledge — IIBA)
Référentiel international pour l’analyse métier.  
Apporte :
- la notion d’exigence métier (Business Requirement),
- la notion de règle métier (Business Rule),
- la structuration des cas d’usage métier,
- la séparation métier / solution,
- la traçabilité des exigences.

## 12.2 UML (Unified Modeling Language — OMG)
Standard mondial de modélisation logicielle.  
Apporte :
- les Use Cases (UC),
- les scénarios nominal / alternatif / erreur,
- les diagrammes d’états (State Machine),
- les diagrammes de classes (entités métier).

## 12.3 BPMN 2.0 (Business Process Model and Notation — OMG)
Standard pour la modélisation des processus métier.  
Apporte :
- la représentation des processus métier,
- les transitions d’état,
- les conditions métier,
- les séquences d’activités.

## 12.4 ISO 27001 / ISO 27002 (Sécurité de l’information)
Normes internationales de sécurité.  
Apportent :
- la notion d’exigence métier de sécurité,
- la nécessité de tracer les règles vers les risques,
- la justification des contrôles métier,
- la structuration de la matrice BR → Risques.

## 12.5 NIST SP 800‑30 / SP 800‑53 / SP 800‑160
Référentiels américains de sécurité et d’ingénierie système.  
Apportent :
- la notion de risque métier,
- la séparation exigence métier / exigence technique / solution technique,
- la structuration des contrôles,
- la justification des règles métier de sécurité.

## 12.6 OWASP SAMM / BSIMM (Secure Software Maturity Models)
Référentiels de maturité Secure-by-Design.  
Apportent :
- l’intégration de la sécurité dans les cas d’usage métier,
- la traçabilité des exigences métier vers les tests,
- la structuration des règles métier de sécurité,
- la gouvernance des habilitations.

## 12.7 IAM / IGA (Identity Governance & Administration)
Référentiels de gouvernance des identités.  
Apportent :
- la notion de permission métier,
- la notion de rôle métier,
- la gouvernance des habilitations,
- la traçabilité des permissions utilisées.

## 12.8 IEEE 830 / ISO/IEC/IEEE 29148 (Software Requirements Specification)
Standards pour la rédaction des spécifications.  
Apportent :
- la structuration des exigences,
- la séparation exigence / règle / comportement / donnée,
- la notion de testabilité,
- la notion de traçabilité.

---

**Synthèse :**  
La spécification métier V5/V6 de CoreService est construite en conformité avec les standards BABOK, UML, BPMN, ISO 27001, NIST, OWASP SAMM, IAM/IGA et IEEE.  
Elle applique les bonnes pratiques internationales de modélisation métier, de traçabilité, de gouvernance des exigences et de sécurité.


