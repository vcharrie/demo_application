# FICHE — TRANSACTIONS : ATOMICITÉ, CONCURRENCE, ISOLATION

## Comprendre les trois piliers de la cohérence dans les systèmes transactionnels

------------------------------------------------------------
1. OBJECTIF DE LA FICHE
------------------------------------------------------------

Clarifier :
- ce qu’est une transaction (atomicité),
- ce qu’est la concurrence d’accès (conflits d’écriture),
- ce qu’est l’isolation (cohérence de lecture),
- quand et pourquoi gérer la concurrence,
- quand et pourquoi ajuster le niveau d’isolation,
- comment distinguer les invariants locaux des invariants globaux,
- comment décider si un cas métier nécessite un contrôle de concurrence ou une isolation forte.

Cette fiche sert de guide de décision pour les architectes, tech leads, et responsables sécurité.

------------------------------------------------------------
2. LES TROIS PILIERS DES TRANSACTIONS
------------------------------------------------------------

### 2.1 Atomicité (transaction)
Définition :
- Une transaction regroupe un ensemble d’opérations métier et techniques.
- Soit TOUTES les opérations réussissent, soit AUCUNE n’est appliquée.
- La base de données garantit le rollback en cas d’erreur.

Important :
- L’atomicité concerne UNE opération métier.
- L’atomicité ne dit RIEN sur les accès concurrents.
- L’atomicité ne garantit pas la cohérence entre transactions.

### 2.2 Concurrence d’accès (conflits d’écriture)
Définition :
- Plusieurs transactions s’exécutent en même temps sur les mêmes données.
- Le risque est d’écraser une mise à jour, de lire un état obsolète, ou de violer un invariant global.

Important :
- La concurrence concerne PLUSIEURS transactions.
- La concurrence doit être gérée si plusieurs transactions simultanées peuvent créer un état métier invalide.

### 2.3 Isolation (cohérence de lecture)
Définition :
- L’isolation détermine ce qu’une transaction peut voir des autres transactions pendant son exécution.
- Elle contrôle la visibilité des modifications concurrentes.

Important :
- L’isolation concerne la cohérence de lecture.
- Le locking ne garantit PAS l’isolation.
- L’isolation ne garantit PAS l’absence de conflits d’écriture.

------------------------------------------------------------
3. INVARIANTS : LOCAL vs GLOBAL
------------------------------------------------------------

### 3.1 Invariant local
Définition :
- Règle métier vérifiée par une seule transaction.
- Si l’opération respecte la règle, l’invariant local est respecté.

Exemples :
- solde >= 0
- montant > 0
- statut valide
- email unique

### 3.2 Invariant global
Définition :
- Règle métier vérifiée sur l’ensemble des transactions.
- Si plusieurs transactions simultanées créent un état incohérent, l’invariant global est violé.

Exemples :
- le solde doit refléter la somme des opérations
- les transitions doivent être cohérentes
- les calculs cumulés doivent être exacts
- les numéros séquentiels doivent être uniques

------------------------------------------------------------
4. EXEMPLE CLÉ : DOUBLE DÉBIT CONCURRENT
------------------------------------------------------------

Solde initial : 100  
Débit A : 30  
Débit B : 40  

Sans gestion de concurrence :
- A lit 100 → calcule 70 → écrit 70
- B lit 100 → calcule 60 → écrit 60 (écrase 70)

Résultat final :
- Solde = 60
- Opérations enregistrées : 30 + 40
- Solde attendu = 30

Analyse :
- L’invariant local “solde >= 0” n’est PAS violé.
- L’invariant global “solde = somme des opérations” EST violé.

Conclusion :
- La concurrence doit être gérée non pas à cause de l’invariant local,
  mais à cause de l’invariant global.

------------------------------------------------------------
5. QUAND LA CONCURRENCE DOIT ÊTRE GÉRÉE
------------------------------------------------------------

La concurrence doit être gérée si ET SEULEMENT SI :

### 5.1 Plusieurs transactions simultanées peuvent violer un invariant global
Exemples :
- solde bancaire,
- total cumulé,
- compteur séquentiel,
- workflow métier,
- état dérivé de plusieurs opérations.

### 5.2 Plusieurs transactions simultanées peuvent créer un état impossible
Exemples :
- double validation d’un dossier,
- double transition de statut,
- double génération d’un identifiant unique.

### 5.3 Plusieurs transactions simultanées peuvent créer une incohérence métier
Exemples :
- solde faux,
- historique incohérent,
- calcul incorrect,
- état final non représentatif des actions effectuées.

------------------------------------------------------------
6. QUAND LA CONCURRENCE N’EST PAS NÉCESSAIRE
------------------------------------------------------------

La concurrence n’est PAS nécessaire si :

### 6.1 Les modifications concurrentes ne touchent pas un invariant global
Exemples :
- modification d’adresse,
- modification de téléphone,
- modification de description,
- modification de tags,
- modification de préférences utilisateur.

### 6.2 Les modifications concurrentes sont indépendantes
Exemples :
- Alice modifie l’adresse,
- Bob modifie le numéro de téléphone.

### 6.3 Le métier accepte “last write wins”
Exemples :
- champ commentaire,
- champ note interne,
- champ description libre.

------------------------------------------------------------
7. MÉCANISMES DE GESTION DE CONCURRENCE (LOCKING)
------------------------------------------------------------

### 7.1 Optimistic locking (version)
- Pas de blocage.
- Conflit détecté à l’écriture.
- L’utilisateur doit recharger et recommencer.
- Idéal pour les systèmes métier (édition de dossier, workflow).
- Exclusion logique, a posteriori.

### 7.2 Pessimistic locking (verrou DB)
- Blocage immédiat (SELECT FOR UPDATE).
- Empêche les autres transactions de lire ou modifier la ligne.
- Idéal pour les opérations atomiques (débit/crédit).
- Exclusion physique, a priori.

### 7.3 Lock applicatif (token d’édition)
- Empêche deux utilisateurs d’éditer un dossier en même temps.
- Verrou métier, pas technique.

### 7.4 Last-write-wins
- Acceptable si le métier l’autorise.

### 7.5 Merge intelligent
- Fusion des modifications indépendantes.

------------------------------------------------------------
8. ISOLATION DES TRANSACTIONS (COHÉRENCE DE LECTURE)
------------------------------------------------------------

L’isolation détermine ce qu’une transaction voit des autres transactions pendant son exécution.

### 8.1 READ UNCOMMITTED
- Peut lire des données non validées (dirty reads).
- Cohérence faible.
- Jamais utilisé en métier critique.

### 8.2 READ COMMITTED
- Ne lit que des données committées.
- Dirty reads impossibles.
- Non-repeatable reads possibles.
- Niveau par défaut dans de nombreux SGBD.

### 8.3 REPEATABLE READ
- Snapshot stable pour les lignes lues.
- Dirty reads impossibles.
- Non-repeatable reads impossibles.
- Phantom reads possibles.

### 8.4 SERIALIZABLE
- Transactions exécutées comme si elles étaient séquentielles.
- Isolation maximale.
- Coût élevé.
- Indispensable en finance, inventaire, calcul cumulatif.

### 8.5 Pourquoi l’isolation ≠ locking ?
- Le locking protège les ÉCRITURES sur une ligne.
- L’isolation protège les LECTURES sur l’ensemble de la transaction.
- Même avec un lock pessimiste, on peut avoir :
  - des lectures fantômes,
  - des lectures non répétables,
  - des lectures incohérentes sur d’autres lignes,
  - des incohérences dans les agrégats.

------------------------------------------------------------
9. GRILLE DE DÉCISION : FAUT-IL GÉRER LA CONCURRENCE ?
------------------------------------------------------------

Répondre OUI ou NON aux questions suivantes :

1. Plusieurs transactions simultanées peuvent-elles violer un invariant global ?
2. Plusieurs transactions simultanées peuvent-elles créer un état impossible ?
3. Plusieurs transactions simultanées peuvent-elles créer une incohérence métier ?
4. Le métier exige-t-il une exactitude stricte ?
5. Le métier exige-t-il une cohérence stricte ?
6. Le métier interdit-il les écrasements ?
7. Le métier exige-t-il une transition unique ?
8. Le métier exige-t-il une atomicité globale ?

Si au moins UNE réponse est OUI → concurrence obligatoire.  
Si TOUTES les réponses sont NON → concurrence inutile.

------------------------------------------------------------
10. SYNTHÈSE FINALE
------------------------------------------------------------

- **Atomicité = cohérence interne d’une transaction (tout ou rien).**
- **Concurrence = cohérence d’écriture entre transactions (locking).**
- **Isolation = cohérence de lecture entre transactions (READ COMMITTED, SERIALIZABLE…).**

- Atomicité ≠ Concurrence ≠ Isolation.
- Le locking protège les écritures.
- L’isolation protège les lectures.
- La bonne décision dépend du métier, pas de la technique.

------------------------------------------------------------
FIN DE LA FICHE
------------------------------------------------------------
