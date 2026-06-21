# CoreService – Version V2  
Service REST Spring Boot – Architecture complète + Spécifications métier/technique

## 1. Présentation

La version **V2** de CoreService introduit une structuration complète du projet :

- ajout des **spécifications métier**, **fonctionnelles**, **techniques** et **sécurité**,
- introduction d’une **architecture applicative** claire,
- ajout des **4 diagrammes d’architecture**,
- ajout d’un **service CRUD Resource**,
- ajout de l’Actuator `/actuator/health`,
- début de la structuration DevSecOps (mais pas encore de CI/CD complète),
- packaging toujours en JAR (pas encore Docker).

Cette version pose les fondations de la V3.

---

## 2. Architecture

### 2.1. Architecture logicielle

- Spring Boot 3.x
- Endpoints :
  - `/api/health`
  - `/api/resource` (CRUD)
- Actuator activé
- Architecture applicative documentée :
  - diagramme logique
  - diagramme de séquence
  - diagramme de classes
  - diagramme de déploiement (V2)

### 2.2. Architecture technique

- Build Maven local
- Exécution via `java -jar`
- Pas encore de Docker
- Pas encore de pipeline CI/CD
- Début de la réflexion DevSecOps (fiches sécurité)

---

## 3. Build & Run

### 3.1. Build Maven

```
mvn clean package
```

### 3.2. Exécution

```
java -jar target/CoreServiceApplication.jar
```

### 3.3. Vérification

```
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/resource
```

---

## 4. Sécurité

- Définition des exigences sécurité (fiches)
- Pas encore d’outils SAST/SCA/SBOM
- Pas encore de gestion CVE
- Pas encore de durcissement runtime

---

## 5. Limitations

- Pas de Docker
- Pas de CI/CD
- Pas de pipeline DevSecOps
- Pas de scan de vulnérabilités
- Architecture technique encore simple

---

## 6. Documentation associée

- Spécification métier V2
- Spécification fonctionnelle V2
- Spécification technique V2
- Spécification sécurité V2
- Release Notes 