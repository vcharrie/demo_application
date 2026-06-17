# CoreService – Version V1  
Service REST Spring Boot – Première version fonctionnelle

## 1. Présentation

La version **V1** de CoreService est la première version fonctionnelle du service.  
Elle introduit :

- un service REST minimal,
- un endpoint de santé applicative,
- une architecture Spring Boot simple,
- un packaging JAR classique (pas de Docker),
- un build Maven standard.

Cette version sert de base aux évolutions techniques des versions suivantes.

---

## 2. Architecture

### 2.1. Architecture logicielle

- Spring Boot 3.x
- Contrôleur principal : `/api/health`
- Tests unitaires simples
- Pas d’Actuator
- Pas de sécurité
- Pas de CI/CD

### 2.2. Architecture technique

- Build Maven local
- Exécution via `java -jar`
- Pas de containerisation
- Pas de pipeline DevSecOps

---

## 3. Build & Run

### 3.1. Build Maven

```
mvn clean package
```

Le JAR généré se trouve dans :

```
target/CoreServiceApplication.jar
```

### 3.2. Exécution

```
java -jar target/CoreServiceApplication.jar
```

### 3.3. Vérification

```
curl http://localhost:8080/api/health
```

---

## 4. Limitations

- Pas de Docker
- Pas de CI/CD
- Pas de SAST / SCA / SBOM
- Pas de gestion des vulnérabilités
- Architecture minimale

---

## 5. Documentation associée

- Release Notes V1
- Spécification Technique V1