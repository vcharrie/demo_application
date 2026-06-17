# SEC-SAST-02 — PMD

**Domaine :** SAST / Analyse statique de code  
**Couche :** Code source Java (analyse logique)  
**Statut :** ✅ Implémenté

---

## Contexte

PMD analyse le **code source Java** pour détecter des problèmes de logique,
des mauvaises pratiques de programmation, et du code potentiellement dangereux.
Contrairement à Checkstyle qui vérifie le style, PMD s'intéresse à ce que
le code *fait* (ou ne fait pas correctement).

PMD opère sur l'AST (Abstract Syntax Tree) du source — il comprend
la structure logique du programme, pas seulement son apparence textuelle.

---

## 1. RISQUE

**Menace**
Du code Java mal écrit peut introduire des vulnérabilités exploitables :
gestion incorrecte des exceptions, ressources non fermées, code mort
masquant des chemins d'exécution non testés, comparaisons incorrectes
d'objets sensibles (ex: comparaison de chaînes de mots de passe avec `==`).

Exemples de détections PMD à impact sécurité direct :
- `EmptyCatchBlock` : exception avalée silencieusement — masque des erreurs
  d'authentification, d'autorisation ou de validation
- `CloseResource` : connexions, streams ou sockets non fermés
  — risque de déni de service par épuisement de ressources
- `UseEqualsToCompareStrings` : comparaison de Strings avec `==`
  — comportement indéterminé pour des tokens, mots de passe, identifiants
- `UnusedPrivateMethod` / code mort : surface d'attaque inutile,
  vecteur potentiel si réactivé par réflexion
- `AvoidUsingHardCodedIP` : IPs en dur dans le code

**Vecteur**
Code de production déployé avec des chemins d'exécution défaillants
non détectés par les tests (code mort, branches jamais testées).

**Impact**
- **Confidentialité** : gestion silencieuse d'exceptions d'authentification
- **Disponibilité** : fuite de ressources → épuisement → déni de service
- **Intégrité** : comportement indéterminé sur les comparaisons de données sensibles

**Références**
- CWE-390 : Detection of Error Condition Without Action
- CWE-400 : Uncontrolled Resource Consumption
- CWE-561 : Dead Code
- CWE-597 : Use of Wrong Operator in String Comparison
- OWASP A04:2021 — Insecure Design

---

## 2. MESURE DE PROTECTION

**Contrôle**
Analyser le code source avec un ruleset PMD personnalisé ciblant
les patterns dangereux, et bloquer le build en cas de violation.

**Type** : Préventif + Détectif

**Principe de sécurité appliqué**
- **Shift-left** : détection avant compilation et packaging
- **Réduction de la surface d'attaque** : suppression du code mort et des
  patterns dangereux dès leur introduction
- **Défense en profondeur** : couche complémentaire à Checkstyle
  (style) et SpotBugs (bytecode)

---

## 3. IMPLÉMENTATION

**Outil** : `maven-pmd-plugin` version `3.21.0`  
**Où** : `pom.xml` → phase `verify` + fichier `config/pmd-ruleset.xml`  
**Phase Maven** : `verify`

**Configuration implémentée**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.21.0</version>
    <configuration>
        <!-- Ruleset personnalisé (pas le défaut PMD) -->
        <rulesets>
            <ruleset>config/pmd-ruleset.xml</ruleset>
        </rulesets>
        <!-- Gate bloquant -->
        <failOnViolation>true</failOnViolation>
        <!-- Affiche les violations dans la console CI -->
        <printFailingErrors>true</printFailingErrors>
    </configuration>
    <executions>
        <execution>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
        </execution>
    </executions>
</plugin>
```

**Points clés de la configuration**

| Paramètre | Valeur | Pourquoi |
|---|---|---|
| `ruleset` | fichier custom | Règles adaptées — le défaut PMD est trop permissif pour un contexte sécurité |
| `failOnViolation` | `true` | Gate bloquant — pas d'artefact si violation |
| `printFailingErrors` | `true` | Visibilité immédiate dans les logs CI sans avoir à ouvrir le rapport |

**Différence avec Checkstyle**

| Checkstyle | PMD |
|---|---|
| Analyse textuelle (style) | Analyse logique (AST) |
| "Est-ce que le code est bien écrit ?" | "Est-ce que le code fait quelque chose de dangereux ?" |
| Import génériques, longueur de ligne | Catch vide, ressources non fermées, code mort |
| Niveau : lisibilité et conventions | Niveau : comportement et patterns à risque |

**Vérification**
```bash
# Lancer PMD seul
mvn pmd:check

# Exemple de violation à tester : ajouter un bloc catch vide dans une classe
try {
    someOperation();
} catch (Exception e) {
    // vide intentionnellement
}
# Résultat attendu : BUILD FAILURE — EmptyCatchBlock
```

---

## 4. LIMITES & RÉSIDUEL

**Ce que cette mesure ne couvre pas**
- PMD n'analyse pas le bytecode — il ne voit pas ce que font les
  bibliothèques tierces ni les annotations traitées à la compilation
  (ex: Lombok génère du code que PMD ne voit pas toujours correctement)
- PMD ne détecte pas les vulnérabilités dans les dépendances
- Les faux positifs sur du code Lombok nécessitent des suppressions
  manuelles (`@SuppressWarnings("PMD.xxx")`) à documenter
- PMD ne remplace pas une revue de code manuelle pour la logique métier

**Risque résiduel accepté**
Le code généré par Lombok (getters, builders, constructeurs) peut produire
des faux positifs PMD. Ces suppressions sont acceptées à condition d'être
explicitement annotées et revues.

**Mesure complémentaire**
- SEC-SAST-03 (SpotBugs) analyse le **bytecode compilé**, y compris
  le code généré par Lombok — couche complémentaire indispensable
- SEC-SAST-04 (SonarCloud) fournit la vue consolidée et l'historique
