# SEC-SAST-03 — SpotBugs

**Domaine :** SAST / Analyse de bytecode  
**Couche :** Bytecode Java compilé (.class)  
**Statut :** ✅ Implémenté

---

## Contexte

SpotBugs (successeur de FindBugs) analyse le **bytecode compilé** — pas
le source — pour détecter des patterns de bugs et de vulnérabilités.
Cette approche lui permet de voir le code *tel qu'il sera réellement exécuté*,
y compris le code généré par des processeurs d'annotations comme Lombok,
ce que Checkstyle et PMD ne voient pas.

SpotBugs intègre le plugin **Find Security Bugs** qui étend son analyse
à des vulnérabilités de sécurité applicative connues (injection, XXE, etc.).

---

## 1. RISQUE

**Menace**
Des vulnérabilités de sécurité applicative classiques peuvent être introduites
dans le code Java sans être détectées par une analyse de surface (style/logique).
SpotBugs cible des patterns directement exploitables :

Catégories de détection à impact sécurité direct (plugin Find Security Bugs) :
- **Injection SQL** : construction de requêtes par concaténation de chaînes
- **Path Traversal** : utilisation non contrôlée de chemins de fichiers
- **XXE (XML External Entity)** : parseurs XML mal configurés
- **Deserialisation non sécurisée** : objets Java désérialisés sans validation
- **Gestion des mots de passe** : stockage ou comparaison non sécurisé
- **Hardcoded credentials** : secrets en dur dans le code
- **Null Dereference** : NPE potentiels sur des chemins d'exécution critiques
- **Race conditions** : accès concurrents non synchronisés sur des ressources partagées

**Vecteur**
Code en production avec des chemins d'exécution vulnérables, activables
par des entrées utilisateur malveillantes ou des conditions de concurrence.

**Impact**
- **Confidentialité** : injection SQL → exfiltration de données
- **Intégrité** : désérialisation → exécution de code arbitraire (RCE)
- **Disponibilité** : null dereference → crash applicatif

**Références**
- CWE-89 : SQL Injection
- CWE-22 : Path Traversal
- CWE-611 : Improper Restriction of XML External Entity Reference
- CWE-502 : Deserialization of Untrusted Data
- CWE-798 : Use of Hard-coded Credentials
- OWASP A03:2021 — Injection
- OWASP Top 10

---

## 2. MESURE DE PROTECTION

**Contrôle**
Analyser le bytecode compilé avec SpotBugs en mode `effort:max`
et seuil de détection `threshold:Low`, avec un filtre d'exclusion
pour les faux positifs documentés, et bloquer le build en cas de détection.

**Type** : Préventif + Détectif

**Principe de sécurité appliqué**
- **Défense en profondeur** : troisième couche d'analyse statique,
  complémentaire à Checkstyle (source/style) et PMD (source/logique)
- **Analyse au plus proche de l'exécution** : le bytecode est ce qui
  tourne réellement — pas le source (différence importante avec Lombok)
- **Fail-secure** : threshold `Low` = détection maximale,
  aucune vulnérabilité connue n'est silencieusement ignorée

---

## 3. IMPLÉMENTATION

**Outil** : `spotbugs-maven-plugin` version `4.8.3.0`  
**Où** : `pom.xml` → phase `verify` + fichier `config/spotbugs-exclude.xml`  
**Phase Maven** : `verify` (après compilation, sur le bytecode)

**Configuration implémentée**

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.3.0</version>
    <configuration>
        <!-- Effort maximum : analyse la plus profonde possible -->
        <effort>max</effort>
        <!-- Seuil bas : détecte même les bugs de confiance faible -->
        <threshold>Low</threshold>
        <!-- Fichier d'exclusions documentées (faux positifs justifiés) -->
        <excludeFilterFile>config/spotbugs-exclude.xml</excludeFilterFile>
        <!-- Rapport XML pour SonarCloud -->
        <xmlOutput>true</xmlOutput>
        <!-- Gate bloquant -->
        <failOnError>true</failOnError>
    </configuration>
    <executions>
        <execution>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
        </execution>
    </executions>
</plugin>
```

**Explication des paramètres critiques**

| Paramètre | Valeur | Impact sécurité |
|---|---|---|
| `effort` | `max` | Analyse inter-procédurale profonde — détecte les vulnérabilités dans les chaînes d'appel |
| `threshold` | `Low` | Aucun bug potentiel ignoré — même les détections à faible confiance |
| `excludeFilterFile` | fichier versionné | Les exclusions sont explicites et auditables, pas silencieuses |
| `failOnError` | `true` | Gate bloquant — pas d'artefact si détection |

**Pourquoi SpotBugs là où PMD s'arrête**

PMD analyse le source → ne voit pas le code généré par Lombok.
SpotBugs analyse le bytecode → voit exactement ce qui sera exécuté,
y compris les getters/setters/builders générés par Lombok.

```
Code source (.java)
    ↓ Checkstyle, PMD (analyse du texte source)
Compilation javac + traitement annotations (Lombok)
    ↓ SpotBugs (analyse du bytecode .class)
Bytecode (.class)
    ↓ packaging
JAR
```

**Dans le pipeline CI**
SpotBugs est exécuté en deux temps dans `ci-build.yml` :
1. Via `mvn verify` (goal `check`) → gate bloquant
2. Via `mvn spotbugs:spotbugs` → génération du rapport XML seul

Cette séparation permet d'avoir à la fois le gate et le rapport
même si le gate échoue (`if: always()` sur l'upload du rapport).

**Vérification**
```bash
# Lancer SpotBugs seul (génère le rapport sans bloquer)
mvn spotbugs:spotbugs

# Lancer le check bloquant
mvn spotbugs:check

# Consulter le rapport
cat target/spotbugsXml.xml

# Exemple de violation à tester : 
# String sql = "SELECT * FROM users WHERE id = " + userId;
# SpotBugs détectera SQL_INJECTION_JDBC
```

---

## 4. LIMITES & RÉSIDUEL

**Ce que cette mesure ne couvre pas**
- SpotBugs ne détecte pas les vulnérabilités dans les **dépendances tierces**
  (Trivy SCA — SEC-SCA-01 couvre cela)
- Le threshold `Low` génère des faux positifs — chaque exclusion dans
  `spotbugs-exclude.xml` doit être justifiée et revue périodiquement
- SpotBugs ne couvre pas les vulnérabilités de configuration
  (Spring Security misconfiguration, CORS trop permissif)
- Il ne remplace pas les tests de sécurité dynamiques (DAST)

**Risque résiduel accepté**
Les exclusions documentées dans `spotbugs-exclude.xml` représentent
le risque résiduel explicitement accepté. Ce fichier doit être
traité avec la même rigueur qu'un fichier `trivyignore.yaml` —
chaque entrée doit avoir une justification et une date de révision.

**Mesure complémentaire**
- SEC-SAST-04 (SonarCloud) consolide les résultats SpotBugs
  et ajoute ses propres règles de sécurité (Security Hotspots)
- SEC-SCA-01 (Trivy) couvre les dépendances tierces
