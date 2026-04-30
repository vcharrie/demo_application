# SEC-SAST-04 — SonarCloud

**Domaine :** SAST / Quality Gate / Analyse consolidée  
**Couche :** Code source + rapports des outils précédents  
**Statut :** ✅ Implémenté

---

## Contexte

SonarCloud est une plateforme d'analyse statique de code hébergée (SaaS).
Dans ce pipeline, il joue un rôle **double** :

1. **Agrégateur** : importe les rapports Checkstyle, PMD et SpotBugs
   et les centralise dans une vue unique et historisée
2. **Analyseur propre** : applique ses propres règles de détection
   (bugs, vulnérabilités, Security Hotspots, dette technique, coverage)

SonarCloud s'exécute **après** les trois outils précédents dans le pipeline CI,
et constitue la **dernière gate de qualité** avant le packaging Docker.

---

## 1. RISQUE

**Menace**
Les outils locaux (Checkstyle, PMD, SpotBugs) détectent des problèmes
à l'instant t, mais sans vision longitudinale. Sans outil de consolidation :
- On ne sait pas si la qualité de sécurité du code **dégrade dans le temps**
- Les Security Hotspots (code à risque nécessitant une revue humaine)
  ne sont pas formalisés et peuvent passer inaperçus
- Le coverage de tests peut baisser sans alerte, réduisant la confiance
  dans la détection de régressions sécurité

**Vecteur**
- Accumulation silencieuse de dette technique sécurité au fil des commits
- Hotspot non revu exposé en production (ex: `Math.random()` pour générer
  un token, algorithme de hash MD5, désactivation CSRF)
- Coverage insuffisant sur les chemins d'authentification/autorisation

**Impact**
- **Confidentialité / Intégrité** : hotspot non revu → vulnérabilité exploitable
- **Intégrité** : régression de coverage → bug sécurité non détecté par les tests

**Références**
- CWE-330 : Use of Insufficiently Random Values (ex: Math.random)
- CWE-916 : Use of Password Hash With Insufficient Computational Effort (MD5)
- OWASP A02:2021 — Cryptographic Failures
- OWASP SAMM — Verification / Security Testing

---

## 2. MESURE DE PROTECTION

**Contrôle**
Centraliser l'analyse de sécurité statique dans SonarCloud avec un
Quality Gate bloquant, la détection des Security Hotspots, et
l'historisation des métriques pour détecter les dégradations.

**Type** : Préventif + Détectif + Correctif (via remédiation guidée)

**Principe de sécurité appliqué**
- **Défense en profondeur** : couche de synthèse au-dessus de Checkstyle,
  PMD et SpotBugs — chaque outil est complémentaire, pas redondant
- **Visibilité continue** : l'historisation permet de détecter les tendances
  de dégradation, pas seulement l'état instantané
- **Responsabilisation** : les Security Hotspots nécessitent une revue humaine
  explicite — on ne peut pas ignorer silencieusement

---

## 3. IMPLÉMENTATION

**Outil** : `sonar-maven-plugin` version `3.10.0.2594` + SonarCloud SaaS  
**Où** : `pom.xml` (plugin + properties) + `ci-build.yml` (étape CI)  
**Déclenchement** : après `mvn verify` dans le pipeline CI

**Configuration pom.xml**

```xml
<!-- Properties SonarCloud -->
<properties>
    <sonar.projectKey>vcharrie_demo_application</sonar.projectKey>
    <sonar.organization>vcharrie</sonar.organization>
    <sonar.host.url>https://sonarcloud.io</sonar.host.url>
</properties>

<!-- Plugin -->
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.10.0.2594</version>
</plugin>
```

**Configuration CI (ci-build.yml)**

```yaml
- name: SonarCloud Scan
  run: mvn -B verify sonar:sonar
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**Ce que SonarCloud analyse au-delà des outils locaux**

| Catégorie | Description | Lien sécurité |
|---|---|---|
| Vulnerabilities | Bugs de sécurité détectés (propres règles Sonar) | CWE direct |
| Security Hotspots | Code sensible nécessitant une revue humaine | OWASP Top 10 |
| Bugs | Erreurs logiques pouvant causer des comportements inattendus | Fiabilité |
| Code Smells | Dette technique dégradant la maintenabilité | Surface d'audit |
| Coverage | % de code couvert par les tests | Confiance dans la détection |
| Duplications | Code dupliqué = corrections de sécurité potentiellement partielles | Intégrité |

**Quality Gate — ce qui bloque le build**
Le Quality Gate SonarCloud est configuré sur sonarcloud.io.
Le pipeline échoue si le gate n'est pas passé (`sonar:sonar` retourne
un code d'erreur non-zero si le Quality Gate est en état `FAILED`).

Conditions typiques d'un Quality Gate sécurité :
- 0 nouvelle vulnérabilité
- 0 nouveau Security Hotspot non revu
- Coverage ≥ seuil défini
- Pas de régression de rating sécurité (A → B interdit)

**Vérification**
```bash
# Lancer l'analyse complète
mvn -B verify sonar:sonar -Dsonar.token=$SONAR_TOKEN

# Résultat : lien vers le dashboard SonarCloud dans les logs
# ex: ANALYSIS SUCCESSFUL, you can find the results at:
#     https://sonarcloud.io/dashboard?id=vcharrie_demo_application
```

**Import automatique des rapports des outils précédents**
SonarCloud importe automatiquement les rapports XML produits par :
- `target/checkstyle-result.xml` (Checkstyle)
- `target/pmd.xml` (PMD)
- `target/spotbugsXml.xml` (SpotBugs)

Cela signifie que les violations détectées par ces outils apparaissent
aussi dans SonarCloud, avec l'historique et la traçabilité.

---

## 4. LIMITES & RÉSIDUEL

**Ce que cette mesure ne couvre pas**
- SonarCloud analyse le code statiquement — il ne détecte pas les
  vulnérabilités de configuration à l'exécution (ex: Spring Security
  mal configuré au niveau des beans, variables d'env exposées)
- Il ne couvre pas les dépendances tierces (Trivy SCA — SEC-SCA-01)
- Les Security Hotspots nécessitent une **revue humaine** —
  SonarCloud les signale mais ne les résout pas automatiquement
- En mode solo (sans équipe), la revue des hotspots est de la
  responsabilité du développeur seul — risque de biais

**Risque résiduel accepté**
Les Security Hotspots marqués "reviewed / acknowledged" sans correction
représentent le risque résiduel explicitement accepté. Chaque décision
de type "won't fix" doit être justifiée dans SonarCloud.

**Note sur la redondance apparente avec Checkstyle/PMD/SpotBugs**
Ces outils ne sont PAS redondants avec SonarCloud — ils sont complémentaires :
- Checkstyle/PMD/SpotBugs : gates **locaux**, bloquent avant SonarCloud
- SonarCloud : gate **consolidé**, historisé, avec vision long terme
  Si SonarCloud était le seul outil, un développeur pourrait committer
  du code non analysé localement. La présence des gates locaux garantit
  que l'analyse est faite avant même que le code atteigne SonarCloud.

**Mesure complémentaire**
- SEC-SCA-01 (Trivy) couvre les dépendances tierces
- SEC-IMG-01 (Dockerfile durci) couvre la sécurité du runtime container
