# SEC-BUILD-01 — Maven Enforcer Plugin

**Domaine :** Build Integrity / Reproductibilité  
**Couche :** Build Maven (pom.xml)  
**Statut :** ✅ Implémenté

---

## Contexte

Avant toute compilation, Maven exécute des plugins dans un ordre défini.
Sans contraintes explicites sur l'environnement de build, rien n'empêche
un développeur ou un runner CI de compiler le projet avec Java 11 ou Maven 3.6,
produisant un artefact dont le comportement diffère de celui attendu en production.

---

## 1. RISQUE

**Menace**
Un artefact compilé dans un environnement non contrôlé (mauvaise version de JDK,
Maven obsolète) peut introduire des comportements non testés, des incompatibilités
de bytecode, ou des failles liées à des versions anciennes du compilateur.

**Vecteur**
- Développeur local avec un JDK différent de celui du CI
- Runner GitHub Actions dont l'image de base a été mise à jour silencieusement
- Fork du projet buildé dans un environnement tiers non maîtrisé

**Impact**
- **Intégrité** : l'artefact produit n'est pas celui attendu
- **Disponibilité** : incompatibilités runtime détectées trop tard (en production)
- **Sécurité** : des versions anciennes de Java ont des vulnérabilités connues
  (ex: désérialisation Java < 17, algorithmes cryptographiques dépréciés)

**Références**
- CWE-1357 : Reliance on Insufficiently Trustworthy Component
- OWASP A06:2021 — Vulnerable and Outdated Components
- NIST SP 800-218 (SSDF) — PW.4 : Reuse existing, well-secured software

---

## 2. MESURE DE PROTECTION

**Contrôle**
Imposer des pré-conditions sur l'environnement de build via Maven Enforcer,
et faire échouer le build immédiatement si ces conditions ne sont pas remplies.

**Type** : Préventif

**Principe de sécurité appliqué**
- **Fail-secure** : le build échoue explicitement plutôt que de produire
  un artefact dans un état indéterminé
- **Reproductibilité** : même environnement = même artefact (principe d'immutabilité du build)
- **Shift-left** : le problème est détecté au plus tôt dans le pipeline,
  avant compilation, tests, scan de sécurité

---

## 3. IMPLÉMENTATION

**Outil** : `maven-enforcer-plugin` version `3.4.1`  
**Où** : `pom.xml` → section `<build><plugins>`  
**Phase Maven** : s'exécute automatiquement avant `validate` (phase la plus précoce)

**Configuration implémentée**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>3.4.1</version>
    <executions>
        <execution>
            <id>enforce-rules</id>
            <goals><goal>enforce</goal></goals>
            <configuration>
                <rules>
                    <!-- Java 21 minimum obligatoire -->
                    <requireJavaVersion>
                        <version>[21,)</version>
                    </requireJavaVersion>
                    <!-- Maven 3.9 minimum obligatoire -->
                    <requireMavenVersion>
                        <version>[3.9,)</version>
                    </requireMavenVersion>
                </rules>
                <!-- Fail immédiat si violation -->
                <fail>true</fail>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Ce que chaque règle adresse**

| Règle | Risque adressé |
|---|---|
| `requireJavaVersion [21,)` | Empêche la compilation avec un JDK ancien porteur de CVE |
| `requireMavenVersion [3.9,)` | Garantit des résolutions de dépendances déterministes (reproductible builds) |
| `<fail>true</fail>` | Bloque le pipeline immédiatement, pas d'artefact produit en cas de violation |

**Vérification**
```bash
# Simuler un build avec une version Java incorrecte (si vous avez plusieurs JDK)
JAVA_HOME=/path/to/java11 mvn validate
# Résultat attendu : BUILD FAILURE avec message "Detected JDK Version: 11.x.x"

# Vérification normale
mvn validate
# Résultat attendu : BUILD SUCCESS, règles satisfaites
```

Dans le pipeline CI (`ci-build.yml`), `actions/setup-java@v4` garantit Java 21,
ce qui rend la règle Enforcer cohérente avec l'environnement déclaré.

---

## 4. LIMITES & RÉSIDUEL

**Ce que cette mesure ne couvre pas**
- Elle ne vérifie pas que le JDK utilisé est lui-même exempt de CVE
  (Java 21.0.1 vs 21.0.4 — Enforcer accepte les deux)
- Elle ne contrôle pas les plugins Maven tiers téléchargés pendant le build
  (risque supply chain Maven Central — adressé par SEC-SCA-01)
- Elle ne garantit pas la reproductibilité bit-à-bit de l'artefact
  (pour ça : Maven Reproducible Builds + `project.build.outputTimestamp`)

**Risque résiduel accepté**
Une CVE dans une sous-version de Java 21 pourrait être utilisée sans
que l'Enforcer ne le détecte. Ce risque est partiellement mitigé par
le scan Trivy sur l'image finale (SEC-IMG-01) qui détecte les CVE JRE.

**Mesure complémentaire**
- SEC-SCA-01 (Trivy SCA) couvre les dépendances Maven
- SEC-IMG-01 (Trivy Image Scan) couvre les CVE du JRE dans l'image finale
