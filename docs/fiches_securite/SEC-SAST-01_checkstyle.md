# SEC-SAST-01 — Checkstyle

**Domaine :** SAST / Qualité de code  
**Couche :** Code source Java (analyse statique)  
**Statut :** ✅ Implémenté

---

## Contexte

Checkstyle analyse le code source Java **avant compilation**, en vérifiant
des règles de style, de structure et de bonnes pratiques d'écriture.
Il opère sur le texte source (`.java`), pas sur le bytecode compilé.

Dans le pipeline, il s'exécute à la phase `verify` de Maven, avant les étapes
de packaging et de scan de sécurité.

---

## 1. RISQUE

**Menace**
Un code mal structuré, illisible ou non conforme aux conventions augmente
la probabilité d'introduire des bugs et des vulnérabilités — non pas directement,
mais parce qu'il rend les revues de code inefficaces et les erreurs difficiles
à détecter. C'est un risque de **dette technique créatrice de surface d'attaque**.

Exemples concrets de ce que Checkstyle peut détecter avec un impact sécurité :
- Des blocs `catch` vides qui avalent silencieusement des exceptions
  (masque des erreurs d'authentification ou d'autorisation)
- Des imports génériques (`import java.util.*`) qui rendent les dépendances
  opaques et les revues difficiles
- L'absence de modificateurs de visibilité explicites (`public`/`private`)
  qui peut exposer des champs ou méthodes par inadvertance
- Des longueurs de méthode excessives qui compliquent l'audit de sécurité

**Vecteur**
Développeur qui introduit du code non conforme, non détecté en revue
manuelle, qui finit en production.

**Impact**
- **Intégrité** : code difficile à auditer = vulnérabilités non détectées
- Risque indirect : pas de CVE directe, mais augmentation de la surface
  d'exposition aux erreurs humaines

**Références**
- CWE-390 : Detection of Error Condition Without Action (catch vide)
- CWE-1076 : Insufficient Adherence to Coding Guidelines
- OWASP Code Review Guide
- NIST SP 800-218 (SSDF) — PW.2 : Review software design to address security

---

## 2. MESURE DE PROTECTION

**Contrôle**
Appliquer un ensemble de règles Checkstyle personnalisées sur l'ensemble
du code source, et faire échouer le build si une violation est détectée.

**Type** : Préventif + Détectif

**Principe de sécurité appliqué**
- **Shift-left** : détection au plus tôt, avant compilation et tests
- **Automatisation de la revue** : réduit la dépendance à la vigilance humaine
  pour les règles mécaniques
- **Cohérence de base de code** : code uniforme = revues plus rapides et fiables

---

## 3. IMPLÉMENTATION

**Outil** : `maven-checkstyle-plugin` version `3.3.1`  
**Où** : `pom.xml` → phase `verify` + fichier `config/checkstyle/checkstyle.xml`  
**Phase Maven** : `verify` (avant packaging)

**Configuration implémentée**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <executions>
        <execution>
            <id>checkstyle-validation</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <!-- Règles personnalisées (pas le Google/Sun style par défaut) -->
                <configLocation>config/checkstyle/checkstyle.xml</configLocation>
                <!-- Fail du build si violation -->
                <failOnViolation>true</failOnViolation>
                <!-- Rapport XML pour CI/intégration SonarCloud -->
                <outputFile>target/checkstyle-result.xml</outputFile>
                <outputFileFormat>xml</outputFileFormat>
                <consoleOutput>false</consoleOutput>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Points clés de la configuration**

| Paramètre | Valeur | Pourquoi |
|---|---|---|
| `configLocation` | fichier custom | Règles adaptées au projet, pas un style générique |
| `failOnViolation` | `true` | Le build échoue — gate bloquant, pas un avertissement |
| `outputFileFormat` | `xml` | Consommable par SonarCloud et les outils CI |
| `phase` | `verify` | S'exécute avant `package`, donc avant la création du JAR |

**Relation avec SonarCloud**
Le rapport XML produit par Checkstyle (`target/checkstyle-result.xml`)
est importé automatiquement par SonarCloud lors du scan `sonar:sonar`.
Checkstyle bloque le build localement ; SonarCloud agrège et historise
les violations dans le temps (SEC-SAST-04).

**Vérification**
```bash
# Introduire volontairement une violation (ex: ligne trop longue, import générique)
# puis lancer :
mvn checkstyle:check
# Résultat attendu : BUILD FAILURE avec la liste des violations

# Build normal
mvn verify
# Résultat attendu : BUILD SUCCESS si aucune violation
```

---

## 4. LIMITES & RÉSIDUEL

**Ce que cette mesure ne couvre pas**
- Checkstyle n'analyse **pas la logique** du code — il ne détecte pas
  de null pointer, d'injection SQL, ou de race condition
- Il ne détecte pas les vulnérabilités dans les bibliothèques tierces
- Les règles custom (`checkstyle.xml`) doivent être maintenues :
  si elles sont trop permissives, la gate ne sert à rien
- Il opère sur le source uniquement — pas sur le bytecode ni les dépendances

**Risque résiduel accepté**
Un développeur peut contourner une règle avec `@SuppressWarnings`
ou une annotation Checkstyle-suppress. Ce contournement doit faire
l'objet d'une revue manuelle (non automatisée ici).

**Mesure complémentaire**
- SEC-SAST-02 (PMD) couvre la logique et les mauvaises pratiques de code
- SEC-SAST-03 (SpotBugs) couvre les bugs et vulnérabilités dans le bytecode
- SEC-SAST-04 (SonarCloud) agrège et historise toutes les violations
