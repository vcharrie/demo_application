# 1. Architecture métier

## 1.1 Contexte métier
Le système s’inscrit dans un contexte de gestion de comptes et d’opérations financières.  
Il doit permettre aux utilisateurs de réaliser des opérations courantes (dépôt, retrait) ainsi que des opérations sensibles nécessitant une validation (virements).  
Les enjeux métier sont : fiabilité, cohérence des soldes, traçabilité, disponibilité et conformité réglementaire.

### Acteurs métier
- **Client** : titulaire d’un compte, initiateur des opérations simples et des virements.
- **Superviseur** : acteur habilité à valider les opérations sensibles.
- **Système bancaire** : référentiel des comptes, soldes et opérations.

### Périmètre métier
- Gestion des comptes
- Dépôts et retraits
- Virements (initiation + validation)
- Historisation des opérations

### Contraintes métier
- Cohérence stricte des soldes
- Historisation obligatoire
- Séparation des rôles (client / superviseur)
- Respect des workflows métier (ex : virement → initiation → validation)

## 1.2 Besoins métier
- **BM01 – Créer un compte**
- **BM02 – Déposer des fonds**
- **BM03 – Retirer des fonds**
- **BM04 – Initier un virement**
- **BM05 – Valider un virement**
- **BM06 – Consulter l’historique des opérations**

## 1.3 Cartographie des processus métier
Voir schéma lié.


# 2. Architecture fonctionnelle

## 2.1 Contexte fonctionnel
Les besoins métier sont traduits en use cases fonctionnels décrivant les interactions entre les acteurs et le système.

Use cases fonctionnels :
- **UC01F – Créer un compte**
- **UC02F – Dépôt**
- **UC03F – Retrait**
- **UC04F – Initier un virement**
- **UC05F – Valider un virement**
- **UC06F – Consulter l’historique**

## 2.2 Cartographie des fonctions métier
| Fonction métier | Description | UC associés |
|-----------------|-------------|-------------|
| **FM01 – Gestion des comptes** | Création et consultation des comptes | UC01F, UC06F |
| **FM02 – Gestion des dépôts** | Ajout de fonds | UC02F |
| **FM03 – Gestion des retraits** | Retrait de fonds | UC03F |
| **FM04 – Gestion des virements** | Initiation et validation | UC04F, UC05F |
| **FM05 – Historisation** | Enregistrement des opérations | Tous les UC |
| **FM06 – Supervision** | Validation des opérations sensibles | UC05F |

## 2.3 Exigences transverses (techniques + sécurité)
Les exigences techniques sont globales au système et ne dépendent pas des UC.

### Sécurité
- Authentification (authN)
- Autorisation (authZ / RBAC)
- Gestion des secrets
- Sécurité réseau / segmentation
- Chiffrement des données

### Intégrité
- Transactions ACID
- Idempotence
- Cohérence des données

### Disponibilité / Résilience
- Haute disponibilité
- Redondance
- Auto-réparation
- Health checks

### Scalabilité
- Scalabilité horizontale
- Autoscaling
- Partitionnement

### Observabilité
- Logs structurés
- Métriques
- Traces distribuées
- Dashboards
- Alerting

### Traçabilité / Audit
- Historisation des opérations
- Audit des actions sensibles

### CI/CD
- Build automatisé
- Tests automatisés
- Déploiement automatisé
- Rollback

### Réseau / Infrastructure
- Segmentation réseau
- RBAC infra
- Monitoring
- Gouvernance API

# 3.2 Architecture Applicative

Cette section décrit l’ensemble des composants applicatifs utilisés par l’application, leurs responsabilités, les exigences techniques couvertes, ainsi que les justifications des choix architecturaux. Les composants d’infrastructure (observability, monitoring, CI/CD, runtime, service mesh) ne sont pas inclus ici : ils appartiennent à la plateforme d’exécution et ne sont pas consommés directement par l’application. Seuls les flux applicatifs (logs, métriques, traces) seront représentés dans le schéma d’infrastructure.

---

## 3.2.0 CoreService (composant applicatif monolithique)

### Capacités offertes
- Implémentation des UC métier au sein d’un seul composant applicatif.
- Exposition des endpoints REST correspondant aux UC.
- Gestion centralisée des règles métier, validations et workflows.
- Accès cohérent au modèle de données métier.

### Justification du choix monolithe
- Cohérence transactionnelle forte : les UC métier nécessitent des transactions ACID locales sur plusieurs entités, sans orchestration distribuée.
- Domaine fonctionnel maîtrisé et de taille raisonnable : pas de besoin immédiat de découpage en multiples services indépendants.
- Équipe et organisation orientées sur un cycle de livraison unifié : un artefact applicatif unique simplifie le déploiement et la maintenance.
- Simplicité opérationnelle : pas de complexité liée au maillage de microservices (discovery, resilience patterns, versioning inter-services).
- Évolutivité maîtrisée : le monolithe reste structuré en modules internes, permettant un futur découpage si nécessaire.

### Exigences techniques transverses couvertes
- Cohérence métier et technique au sein d’un même runtime.
- Simplicité de déploiement (un artefact applicatif).
- Observabilité unifiée (logs, métriques, traces sur un seul composant applicatif).

### Critères spécifiques liés aux fonctions applicatives
- UC métier nécessitant des validations synchrones et des mises à jour atomiques de plusieurs entités.
- Besoin de réponses immédiates pour les opérations critiques (dépôt, retrait, virement, validations).
- Absence de besoin de scalabilité indépendante par sous-domaine métier à ce stade.

### Alternatives rejetées
- Architecture microservices :
  - Complexité accrue (orchestration, sagas, compensation, gestion des échecs partiels).
  - Nécessité de gérer la cohérence distribuée (eventual consistency) pour des UC qui exigent une cohérence forte.
  - Surcoût opérationnel (monitoring, déploiement, versioning, résilience) non justifié par le périmètre actuel.
- Modular monolith non structuré :
  - Risque de dérive vers un “big ball of mud”.
  - Le CoreService est explicitement structuré en modules internes pour éviter ce risque.

---

## 3.2.1 API Gateway

### Capacités offertes
- Point d’entrée unique pour les clients.
- Routage vers les services applicatifs internes.
- Gestion des quotas, throttling, rate limiting.
- Centralisation des politiques de sécurité.
- Versioning des API.

### Exigences techniques transverses couvertes
- Sécurité (auth, autorisation, protection contre attaques).
- Gouvernance des API.
- Disponibilité et résilience.

### Critères spécifiques liés aux fonctions applicatives
- Exposition cohérente des UC via endpoints REST.
- Découplage entre clients et services internes.

### Alternatives rejetées
- Appels directs aux services → couplage fort, absence de gouvernance.

---

## 3.2.2 IAM interne

### Capacités offertes
- Authentification des services internes et back-office.
- Gestion des rôles et permissions internes.
- Émission de tokens courts adaptés aux interactions backend.

### Exigences techniques transverses couvertes
- Sécurité.
- Gouvernance des identités internes.

### Critères spécifiques liés aux fonctions applicatives
- Protection des endpoints internes sensibles.
- Séparation stricte des identités internes/externe.

### Alternatives rejetées
- Utilisation de l’IAM externe pour les identités internes → mélange des contextes.

---

## 3.2.3 IAM externe

### Capacités offertes
- Authentification des utilisateurs finaux via OIDC/OAuth2.
- Gestion des comptes clients.
- Flows PKCE / Authorization Code.
- Émission de tokens JWT pour les clients.

### Exigences techniques transverses couvertes
- Sécurité.
- Conformité aux standards modernes d’authentification.

### Critères spécifiques liés aux fonctions applicatives
- Intégration avec les applications front.
- Gestion des sessions utilisateurs.

### Alternatives rejetées
- Authentification custom → non conforme, non sécurisée.

---

## 3.2.4 Base de données

### Capacités offertes
- Stockage persistant des données métier.
- Transactions ACID locales.
- Modèle relationnel adapté aux règles métier.

### Exigences techniques transverses couvertes
- Cohérence des données.
- Disponibilité.
- Gouvernance du modèle de données.

### Critères spécifiques liés aux fonctions applicatives
- Stockage des entités métier.
- Respect des invariants métier via contraintes relationnelles.

### Alternatives rejetées
- Base NoSQL pour des données fortement relationnelles.
- Base partagée entre services → couplage fort.

---

## 3.2.5 Secrets Manager

### Capacités offertes
- Stockage sécurisé des secrets (DB, API externes, certificats).
- Rotation automatique.
- Distribution contrôlée des secrets.

### Exigences techniques transverses couvertes
- Sécurité.
- Gouvernance des secrets.

### Critères spécifiques liés aux fonctions applicatives
- Protection des accès aux ressources critiques (DB, services externes).

### Alternatives rejetées
- Secrets en clair dans fichiers ou variables d’environnement.

---

## 3.2.6 Config Server

### Capacités offertes
- Centralisation de la configuration applicative.
- Distribution dynamique de la configuration.
- Versioning et rollback.
- Feature flags.

### Exigences techniques transverses couvertes
- Gouvernance.
- Disponibilité.
- Sécurité (intégration avec Secrets Manager).

### Critères spécifiques liés aux fonctions applicatives
- Paramétrage métier non codé : seuils, limites, comportements dynamiques des services applicatifs implémentant les UC métier.
- Gestion multi-environnements (DEV, INT, PREPROD, PROD).

### Alternatives rejetées
- Configuration embarquée → redéploiement nécessaire.
- Variables d’environnement → pas de versioning, pas de gouvernance.

---

## 3.2.7 Cache (Redis)

### Capacités offertes
- Cache distribué en mémoire.
- TTL configurable.
- Structures de données avancées (hashes, sets, sorted sets).
- Pub/sub léger.

### Exigences techniques transverses couvertes
- Performance.
- Scalabilité.
- Résilience.

### Critères spécifiques liés aux fonctions applicatives
- Cache des lectures fréquentes.
- Stockage d’états transitoires (OTP, tokens techniques).
- Gestion de quotas ou limites via compteurs.
- Décharge de la base de données.

### Alternatives rejetées
- Cache local → non distribué, incohérences.
- Base de données utilisée comme cache → surcharge, absence de TTL.

---

## 3.2.8 Messaging (Event Bus / Queue)

### Capacités offertes
- Transport asynchrone de messages.
- Découplage fort entre producteurs et consommateurs.
- Retry, DLQ, gestion des erreurs.
- Pub/sub pour diffusion d’événements métier.

### Exigences techniques transverses couvertes
- Résilience.
- Scalabilité.
- Performance.

### Critères spécifiques liés aux fonctions applicatives
- Traitements asynchrones (emails, notifications, documents).
- Propagation d’événements métier.
- Déport de charge hors REST.
- Robustesse des workflows.

### Alternatives rejetées
- REST synchrone pour traitements lourds → timeouts, saturation.
- Batch pour événements temps réel → latence.

---

## 3.2.9 Object Storage

### Capacités offertes
- Stockage de documents, pièces jointes, exports.
- URLs temporaires sécurisées.
- Durabilité élevée.
- Versioning optionnel.

### Exigences techniques transverses couvertes
- Performance.
- Scalabilité.
- Sécurité.
- Gouvernance du cycle de vie.

### Critères spécifiques liés aux fonctions applicatives
- Stockage des documents générés par les UC.
- Pièces justificatives.
- Archivage long terme.
- Accès contrôlé via URLs temporaires.

### Alternatives rejetées
- BLOB en base → surcharge, fragmentation.
- Stockage local → non scalable, non durable.

---

## 3.2.10 Batch / Scheduled Jobs

### Capacités offertes
- Exécution planifiée de traitements.
- Déport des traitements volumétriques.
- Orchestration simple de workflows techniques.
- Historisation et monitoring des exécutions.

### Exigences techniques transverses couvertes
- Performance.
- Résilience.
- Gouvernance.

### Critères spécifiques liés aux fonctions applicatives
- Génération de relevés, clôtures journalières.
- Purge de données temporaires.
- Rattrapage de traitements en attente.
- Traitements nécessitant un temps d’exécution long.

### Alternatives rejetées
- REST synchrone pour traitements longs.
- Cron sur serveurs → non gouverné, non observable.

---

# Synthèse

Les composants applicatifs retenus assurent la sécurité, la performance, la résilience et la gouvernance de l’application. Ils couvrent l’ensemble des besoins fonctionnels et techniques : exposition des API, gestion des identités, stockage, configuration, cache, messaging, stockage de documents et traitements planifiés.

Le CoreService constitue le composant applicatif central, implémenté sous forme de monolithe structuré, pour garantir une cohérence transactionnelle forte et une simplicité opérationnelle adaptée au périmètre fonctionnel actuel.

Les composants d’infrastructure (observability, monitoring, CI/CD, runtime, service mesh) ne sont pas inclus dans cette section. Les flux applicatifs vers la stack d’observabilité (logs, métriques, traces) seront représentés dans le schéma d’infrastructure.
