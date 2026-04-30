# SEC-DEP-01 — Épinglage explicite des versions de dépendances (dependencyManagement)

**Domaine :** Supply Chain / Gestion des dépendances  
**Couche :** Build Maven (pom.xml)  
**Statut :** ✅ Implémenté (partiel — voir Limites)

---

## Contexte

Spring Boot utilise un système de **BOM (Bill of Materials)** via son
`spring-boot-starter-parent` : il gère automatiquement les versions
de toutes ses dépendances transitives. C'est pratique, mais cela signifie
que la version effective d'une bibliothèque comme Tomcat ou Spring Security
est décidée par Spring Boot, pas par vous.

Quand une CVE est publiée sur une version de Tomcat ou Spring Security,
Spring Boot peut ne pas avoir encore publié une version corrigée de son BOM.
La section `<dependencyManagement>` permet de **surcharger** ces versions
pour appliquer un correctif de sécurité indépendamment du cycle de release
de Spring Boot.

---

## 1. RISQUE

**Menace**
Une dépendance transitive (tirée automatiquement par Spring Boot) peut
être vulnérable. Sans surcharge explicite, vous êtes bloqué par le
calendrier de release de Spring Boot pour obtenir la version corrigée.
Pendant cette fenêtre, votre application est exposée à une CVE connue
et publique.

**Vecteur**
- CVE publiée sur Tomcat embed ou Spring Security
- Spring Boot n'a pas encore mis à jour son BOM
- Votre application continue de tourner avec la version vulnérable
- Trivy détecte la CVE sur votre image → gate CI bloqué OU CVE ignorée

**Exemples réels**
- CVE sur `tomcat-embed-core` : exécution de code arbitraire via
  des requêtes HTTP malformées
- CVE sur `spring-security-web` : contournement d'authentification

**Impact**
- **Confidentialité / Intégrité / Disponibilité** : selon la CVE exploitée,
  impact potentiellement critique (RCE, authentification bypassée)

**Références**
- OWASP A06:2021 — Vulnerable and Outdated Components
- CWE-1357 : Reliance on Insufficiently Trustworthy Component
- NIST SP 800-218 (SSDF) — PW.4

---

## 2. MESURE DE PROTECTION

**Contrôle**
Surcharger explicitement les versions des dépendances critiques dans
`<dependencyManagement>` pour pouvoir appliquer des correctifs de sécurité
indépendamment du cycle de release de Spring Boot.

**Type** : Préventif + Correctif

**Principe de sécurité appliqué**
- **Contrôle explicite** : vous choisissez consciemment chaque version
  plutôt que de déléguer à Spring Boot
- **Réactivité** : dès qu'une CVE est corrigée dans une dépendance,
  vous pouvez l'appliquer sans attendre la prochaine release Spring Boot
- **Traçabilité** : les versions surchargées sont visibles et versionnées
  dans le pom.xml — auditables

---

## 3. IMPLÉMENTATION

**Où** : `pom.xml` → section `<dependencyManagement>`

**Configuration implémentée**

```xml
<dependencyManagement>
    <dependencies>
        <!-- Tomcat embed : surcharge pour corriger des CVE avant la mise à jour Spring Boot -->
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-core</artifactId>
            <version>10.1.54</version>
        </dependency>
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-websocket</artifactId>
            <version>10.1.54</version>
        </dependency>
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-el</artifactId>
            <version>10.1.54</version>
        </dependency>

        <!-- Spring Security : surcharge explicite -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-web</artifactId>
            <version>6.5.9</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-core</artifactId>
            <version>6.4.10</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>6.2.11</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Comment vérifier les versions effectives**
```bash
# Voir toutes les versions résolues par Maven (y compris transitives)
mvn dependency:tree

# Vérifier qu'une surcharge est bien appliquée
mvn dependency:tree | grep tomcat-embed-core
# Résultat attendu : org.apache.tomcat.embed:tomcat-embed-core:jar:10.1.54

# Vérifier qu'il n'y a pas de conflit de version
mvn dependency:tree -Dverbose | grep "omitted for conflict"
```

**Vérification cohérence avec Trivy**
Après un `mvn package`, Trivy SCA analyse le JAR final et ses dépendances.
Si la surcharge est correctement appliquée, Trivy ne doit plus détecter
les CVE corrigées dans les nouvelles versions.

```bash
trivy fs --scanners vuln target/*.jar
```

---

## 4. LIMITES & RÉSIDUEL

**Ce que cette mesure ne couvre pas**
- Elle ne couvre que les dépendances **explicitement surchargées** —
  les autres transitives restent gérées par Spring Boot BOM
- Les versions épinglées peuvent elles-mêmes devenir vulnérables
  si elles ne sont pas maintenues à jour régulièrement
- Il n'y a pas de mécanisme automatique d'alerte quand une version
  épinglée est dépassée (Dependabot ou Renovate pourraient couvrir cela)

**Incohérence à noter**
`spring-security-web` est épinglé en `6.5.9` et `spring-security-core`
en `6.4.10` — ce sont deux versions mineures différentes dans la même
famille Spring Security. Cela peut fonctionner mais mérite vérification :
idéalement, toutes les dépendances Spring Security devraient être
sur la même version mineure pour garantir la compatibilité.

**Risque résiduel accepté**
Les dépendances non surchargées (gérées par Spring Boot BOM) peuvent
contenir des CVE non encore corrigées par Spring Boot. Ce risque est
partiellement mitigé par le scan Trivy (SEC-SCA-01) qui les détectera.

**Mesure complémentaire**
- SEC-SCA-01 (Trivy SCA) détecte les CVE sur toutes les dépendances,
  y compris celles non surchargées — filet de sécurité indispensable
- Envisager Dependabot (GitHub) ou Renovate pour automatiser
  les mises à jour de versions et recevoir des alertes CVE
