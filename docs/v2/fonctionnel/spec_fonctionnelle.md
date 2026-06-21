# Spécification Fonctionnelle – Version V2

## 1. Objet du document

La présente spécification fonctionnelle décrit les **fonctionnalités offertes par la version V2** de l’application CoreService.  
Elle définit les comportements attendus du système du point de vue fonctionnel, indépendamment des aspects métier, techniques ou de sécurité.

Elle constitue la référence fonctionnelle pour la release V2.

---

## 2. Périmètre fonctionnel de la V2

La V2 fournit un ensemble minimal de fonctionnalités permettant :

- la consultation de l’état de santé de l’application ;
- la consultation de l’ensemble des ressources ;
- la consultation d’une ressource par identifiant ;
- la suppression d’une ressource par identifiant.

La création de ressource est **présente dans l’API**, mais **non fonctionnelle en V2** (voir section 7 — Limites fonctionnelles).

---

## 3. Fonctionnalités

### 3.1. Consultation de l’état de santé de l’application

**Description :**  
L’utilisateur peut interroger l’application pour vérifier qu’elle est opérationnelle.

**Endpoint fonctionnel :**  
`GET /api/health`

**Comportement attendu :**
- Retourne un message indiquant que l’application est active.
- Retourne la version fonctionnelle courante (V2).

---

### 3.2. Consultation de toutes les ressources

**Description :**  
L’utilisateur peut obtenir la liste complète des ressources connues du système.

**Endpoint fonctionnel :**  
`GET /resources`

**Comportement attendu :**
- Retourne une liste de ressources.
- Si aucune ressource n’existe, retourne une liste vide.

---

### 3.3. Consultation d’une ressource par identifiant

**Description :**  
L’utilisateur peut consulter une ressource spécifique à partir de son identifiant.

**Endpoint fonctionnel :**  
`GET /resources/{id}`

**Comportement attendu :**
- Si la ressource existe : retourne la ressource.
- Si la ressource n’existe pas : retourne une erreur fonctionnelle indiquant que la ressource est introuvable.

---

### 3.4. Suppression d’une ressource par identifiant

**Description :**  
L’utilisateur peut supprimer une ressource existante.

**Endpoint fonctionnel :**  
`DELETE /resources/{id}`

**Comportement attendu :**
- Si la ressource existe : la ressource est supprimée.
- Si la ressource n’existe pas : retourne une erreur fonctionnelle indiquant que la ressource est introuvable.

---

### 3.5. Création d’une ressource (fonctionnalité non opérationnelle en V2)

**Description :**  
L’utilisateur peut tenter de créer une ressource via l’API.

**Endpoint fonctionnel :**  
`POST /resources`

**Comportement attendu en V2 :**
- La fonctionnalité est **présente** mais **non fonctionnelle**.
- Toute tentative de création entraîne une erreur interne.
- Le comportement attendu sera corrigé en V3.

---

## 4. Règles fonctionnelles

- Une ressource est identifiée fonctionnellement par son identifiant fourni dans l’URL.
- Une ressource inexistante ne peut pas être consultée ni supprimée.
- La suppression est idempotente :  
  - supprimer une ressource inexistante renvoie une erreur fonctionnelle,  
  - supprimer une ressource existante la supprime définitivement.
- La création n’est pas utilisable en V2.

---

## 5. Scénarios fonctionnels

### 5.1. Consultation de l’état de santé
1. L’utilisateur appelle `GET /api/health`.
2. Le système retourne un message indiquant que l’application fonctionne.

### 5.2. Consultation de toutes les ressources
1. L’utilisateur appelle `GET /resources`.
2. Le système retourne la liste des ressources existantes (ou une liste vide).

### 5.3. Consultation d’une ressource inexistante
1. L’utilisateur appelle `GET /resources/{id}` avec un identifiant inconnu.
2. Le système retourne une erreur fonctionnelle indiquant que la ressource est introuvable.

### 5.4. Suppression d’une ressource existante
1. L’utilisateur appelle `DELETE /resources/{id}`.
2. Le système supprime la ressource.
3. Le système retourne une confirmation de suppression.

### 5.5. Suppression d’une ressource inexistante
1. L’utilisateur appelle `DELETE /resources/{id}` avec un identifiant inconnu.
2. Le système retourne une erreur fonctionnelle indiquant que la ressource est introuvable.

### 5.6. Tentative de création d’une ressource
1. L’utilisateur appelle `POST /resources` avec un corps valide.
2. Le système retourne une erreur interne (fonctionnalité non opérationnelle en V2).

---

## 6. Messages fonctionnels

Les messages fonctionnels renvoyés par l’API sont :

- **Ressource introuvable** : lorsque l’identifiant fourni ne correspond à aucune ressource.
- **Erreur interne** : lors d’une tentative de création en V2.
- **Liste vide** : lorsque aucune ressource n’est enregistrée.

---

## 7. Limites fonctionnelles de la V2

- La création de ressource est **non fonctionnelle**.
- Les messages d’erreur sont retournés en **texte brut**, non en JSON.
- Aucun mécanisme fonctionnel de validation avancée n’est présent.
- Aucun mécanisme fonctionnel de mise à jour n’est disponible.
- Aucun mécanisme fonctionnel de pagination, filtrage ou tri n’est disponible.

Ces limites seront levées dans les versions ultérieures.

---

## 8. Hors périmètre fonctionnel V2

- Sécurité (authentification, autorisation, filtrage, sanitizing)
- Persistance durable
- Gestion des identifiants
- Format d’erreur JSON
- Mise à jour de ressource
- Documentation OpenAPI
- Monitoring avancé
- Observabilité

Ces éléments relèvent de la spécification technique ou des versions futures.