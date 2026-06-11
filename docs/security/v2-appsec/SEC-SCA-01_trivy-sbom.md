# SEC-SCA-01 — Trivy SCA + SBOM CycloneDX

**Domaine :** Supply Chain / Software Composition Analysis  
**Couche :** Dépendances Maven (pom.xml) + Image container  
**Statut :** ✅ Implémenté — deux workflows complémentaires

---

## Contexte

L'application CoreService est un Spring Boot fat JAR qui embarque des
dizaines de dépendances tierces (Spring, Tomcat, Jackson, Logback...).
Ces dépendances sont des vecteurs de vulnérabilités indépendants du code
applicatif — une CVE sur Tomcat ou Spring Security peut rendre l'application
exploitable sans qu'une seule ligne de code métier soit en cause.

Deux workflows complémentaires de SCA ont été mis en place, dans un ordre
chronologique précis qui reflète leur complémentarité :

```
WORKFLOW 1 — ci-build.yml
  Trivy scan sur l'image container finale (OS + Java)
  → Première couche de détection, post-build

WORKFLOW 2 — trivy-sbom.yml (implémenté après)
  Génération SBOM CycloneDX depuis le pom.xml
  Trivy scan sur le SBOM
  → Détection en amont du build, sur les déclarations Maven
  → A permis de détecter des CVE non visibles dans le scan image
    (CVE-2026-22731, CVE-2026-22733 sur Spring Boot Actuator)
```

---

## 1. RISQUE

**Menace**
Une dépendance tierce embarquée dans le fat JAR peut contenir une
vulnérabilité connue (CVE publiée), exploitable via l'application
sans que le code métier soit directement en cause.

Exemples de CVE détectées dans ce projet :
- **CVE-2026-29145** — Tomcat : authentication bypass via CLIENT_CERT soft fail
  → contournement d'authentification sans credential valide
- **CVE-2024-38816 / 38819** — Spring WebMVC : path traversal
  → accès à des fichiers hors du répertoire web
- **CVE-2025-22228** — spring-security-crypto : BCrypt max length bypass
  → mots de passe longs acceptés sans vérification complète
- **CVE-2026-22731 / 22733** — Spring Boot Actuator : authentication bypass
  → endpoints de gestion accessibles sans authentification

**Vecteur**
- Dépendance vulnérable embarquée dans le JAR deployé en production
- CVE publiée et indexée → exploits publics disponibles rapidement
- Sans scan automatique : la vulnérabilité peut rester des mois en production

**Impact**
- **Confidentialité** : path traversal → lecture de fichiers sensibles
- **Intégrité** : authentication bypass → accès non autorisé à des fonctions
- **Disponibilité** : DoS via Tomcat multipart upload (CVE-2025-48988)

**Références**
- OWASP A06:2021 — Vulnerable and Outdated Components
- CWE-1357 : Reliance on Insufficiently Trustworthy Component
- NIST SP 800-218 (SSDF) — RV.1 / RV.2
- CISA — Vulnerability Exploitability eXchange (VEX)

---

## 2. MESURE DE PROTECTION

**Contrôle**
Analyser automatiquement les dépendances à deux niveaux — avant et après
build — avec une gate bloquante sur CRITICAL et HIGH, et un processus
de décision documenté pour chaque CVE détectée (patch, VEX, trivyignore).

**Type** : Préventif + Détectif

**Principe de sécurité appliqué**
- **Shift-left** : le scan SBOM détecte les CVE sur le pom.xml avant
  que l'image soit construite
- **Défense en profondeur** : deux scans complémentaires (pom + image)
  se complètent mutuellement
- **Décision consciente et tracée** : aucune CVE n'est ignorée
  silencieusement — chaque entrée trivyignore a une justification
- **Fail-secure** : le pipeline échoue si une CVE CRITICAL ou HIGH
  non justifiée est détectée

---

## 3. IMPLÉMENTATION

### 3.1 Workflow 1 — Scan image container (ci-build.yml)

**Quand :** après build et push de l'image Docker  
**Ce qu'il scanne :** OS packages Ubuntu + Java JARs embarqués  
**Gate :** CRITICAL et HIGH bloquants

```yaml
- name: Trivy Image Scan (CRITICAL/HIGH gate)
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ env.COMMIT_SHA }}
    format: table
    exit-code: 1
    vuln-type: 'os,library'
    severity: 'CRITICAL,HIGH'
    ignore-unfixed: true
    args: --ignorefile config/trivy/.trivyignore.yaml
```

**Ce que ce scan couvre**

| Catégorie | Exemples détectés |
|---|---|
| OS packages Ubuntu | curl, dpkg, glibc, libexpat (D1, D2) |
| Java embedded JARs | Tomcat, Spring Security, Spring MVC (D3, D4) |

**Limitation de ce scan seul**
Le scan image ne peut détecter que ce qui est packagé dans l'image.
Si une CVE affecte une version déclarée dans le pom.xml mais que
l'image n'a pas encore été rebuildée avec cette version, la CVE
n'est pas visible. → C'est la raison d'être du scan SBOM.

---

### 3.2 Workflow 2 — Scan SBOM (trivy-sbom.yml)

**Quand :** sur push et pull_request touchant pom.xml ou src/  
**Ce qu'il scanne :** SBOM CycloneDX généré depuis le pom.xml Maven  
**Gate :** CRITICAL et HIGH bloquants, `ignore-unfixed: false`

**Étape 1 — Génération du SBOM CycloneDX**
```yaml
- name: Generate SBOM (CycloneDX via Maven)
  run: mvn -B org.cyclonedx:cyclonedx-maven-plugin:2.7.9:makeAggregateBom
```
Produit `target/bom.json` — inventaire complet de toutes les dépendances
déclarées dans le pom.xml avec leurs versions exactes.

**Étape 2 — Scan Trivy sur le SBOM**
```yaml
- name: Scan SBOM with Trivy (fail on HIGH/CRITICAL)
  run: |
    trivy sbom \
      --severity HIGH,CRITICAL \
      --ignorefile "config/trivy/.trivyignore" \
      --ignore-unfixed=false \
      --exit-code 1 \
      --format table \
      target/bom.json
```

**Différence clé : `ignore-unfixed=false`**

```
ci-build.yml   ignore-unfixed: true
  → Les CVE sans fix sont exclues automatiquement de la gate
  → Logique : réduire le bruit sur l'image finale
  → Risque : CVE sans fix exclues silencieusement

trivy-sbom.yml  ignore-unfixed=false  ← plus strict
  → Toutes les CVE sont visibles, avec ou sans fix
  → Logique : on veut TOUT voir sur les dépendances Maven
  → Les CVE sans fix doivent être explicitement justifiées (VEX)
  → C'est ce mode qui a forcé l'analyse VEX de CVE-2026-22731/22733
```

**Recommandation d'alignement**
Pour une cohérence maximale, `ci-build.yml` devrait également utiliser
`ignore-unfixed: false` avec un trivyignore complet et justifié.
Cela garantit que chaque CVE ignorée est une décision consciente,
pas une exclusion automatique silencieuse.

---

### 3.3 Complémentarité des deux scans — cas réel

Le parcours de remédiation de ce projet illustre parfaitement pourquoi
les deux scans sont nécessaires :

```
Image scan seul (D1 → D4)
  → Détecte : CVE OS (curl, glibc...) + CVE Java (Tomcat, Spring Security)
  → Remédiations : apt-get upgrade + overrides dependencyManagement
  → Résultat D4 : 2 CRITICAL, 5 HIGH restants sur Spring Boot 3.2.5

SBOM scan implémenté après (SCA-1)
  Spring Boot 3.4.5
  → Détecte : CVE-2026-22731 et CVE-2026-22733 sur spring-boot-actuator
  → Ces CVE n'étaient PAS détectées par l'image scan (différence de version)
  → Décision : upgrade Spring Boot → 3.4.13

SBOM scan (SCA-2)
  Spring Boot 3.4.13
  → Mêmes CVE présentes — fix uniquement en 3.5.12 / 4.0.4 (hors branche 3.4.x)
  → Analyse d'exploitabilité → VEX "Not Affected"
  → Justification architecturale :
      - Actuator désactivé en production
      - Seul /actuator/health exposé en CI via profil dédié
      - CloudFoundry endpoint désactivé dans tous les profils
```

---

### 3.4 Le trivyignore.yaml — état actuel et recommandation

**État actuel** (format bare — fonctionnel mais non justifié)
```
CVE-2026-27456
CVE-2026-2219
CVE-2026-4046
CVE-2026-4437
CVE-2026-4438
CVE-2025-66382
CVE-2024-2236
CVE-2026-22731
CVE-2026-22733
```

**Format recommandé** (justifié, avec expiration et référence au document)

```yaml
# =================================================================
# TRIVY IGNORE — CoreService
# Toute entrée doit référencer le document de décision (SEC-DEP-02)
# Révision mensuelle obligatoire
# =================================================================

vulnerabilities:

  # --- OS Layer — Ubuntu 24.04 — No fix available (voir D2) ------

  - id: CVE-2026-27456
    statement: "No fix available in Ubuntu 24.04 at scan time (D2 — util-linux/bsdutils)"
    expiration: "2026-12-31"

  - id: CVE-2026-2219
    statement: "No fix available in Ubuntu 24.04 at scan time (D2 — dpkg)"
    expiration: "2026-12-31"

  - id: CVE-2026-4046
    statement: "No fix available in Ubuntu 24.04 at scan time (D2 — glibc)"
    expiration: "2026-12-31"

  - id: CVE-2026-4437
    statement: "No fix available in Ubuntu 24.04 at scan time (D2 — glibc)"
    expiration: "2026-12-31"

  - id: CVE-2026-4438
    statement: "No fix available in Ubuntu 24.04 at scan time (D2 — glibc)"
    expiration: "2026-12-31"

  - id: CVE-2025-66382
    statement: "No fix available in Ubuntu 24.04 at scan time (D2 — libexpat)"
    expiration: "2026-12-31"

  - id: CVE-2024-2236
    statement: "No fix available in Ubuntu 24.04 at scan time (D2 — libgcrypt)"
    expiration: "2026-12-31"

  # --- Java Layer — VEX Not Affected (voir SCA-2) ----------------

  - id: CVE-2026-22731
    statement: >
      VEX Not Affected (SCA-2). Actuator endpoints disabled in all production
      profiles (management.endpoints.enabled-by-default=false). Only
      /actuator/health enabled in isolated CI/CD profile. No Health Group
      additional paths exposed. Fix available only in Spring Boot 3.5.12+,
      outside current 3.4.x branch.
    expiration: "2026-12-31"

  - id: CVE-2026-22733
    statement: >
      VEX Not Affected (SCA-2). CloudFoundry Actuator endpoint explicitly
      disabled in all profiles (management.endpoint.cloudfoundry.enabled=false).
      Vulnerability non-exploitable in current configuration.
      Fix available only in Spring Boot 3.5.12+, outside current 3.4.x branch.
    expiration: "2026-12-31"
```

---

### 3.5 Vérification

```bash
# Scan SBOM local
mvn -B org.cyclonedx:cyclonedx-maven-plugin:2.7.9:makeAggregateBom
trivy sbom \
  --severity HIGH,CRITICAL \
  --ignorefile config/trivy/.trivyignore \
  --ignore-unfixed=false \
  --exit-code 1 \
  target/bom.json

# Scan image local
trivy image \
  --severity HIGH,CRITICAL \
  --ignorefile config/trivy/.trivyignore \
  --vuln-type os,library \
  ghcr.io/<registry>/coreservice:<sha>

# Vérifier le contenu du SBOM généré
cat target/bom.json | python3 -m json.tool | grep -A3 '"name"'
```

---

## 4. LIMITES & RÉSIDUEL

**Ce que ces scans ne couvrent pas**
- Les CVE publiées **entre deux runs CI** — fenêtre d'exposition
  entre un commit et le prochain déclenchement du pipeline
- Les vulnérabilités de **logique applicative** — Trivy détecte les CVE
  connues, pas les failles dans votre propre code (couvert par SAST)
- Les CVE **MEDIUM et LOW** — hors scope de la gate actuelle.
  Un attaquant peut chaîner plusieurs CVE de criticité moyenne
  pour obtenir un impact élevé (élévation de privilèges par étapes)
- La **validité des VEX dans le temps** — une VEX "Not Affected"
  peut devenir incorrecte si la configuration change
  (ex: Actuator réactivé sans mise à jour du trivyignore)

**Risque résiduel accepté**

| CVE | Justification d'acceptation |
|---|---|
| CVE-2026-22731/22733 | Actuator désactivé en prod — non exploitable architecturalement |
| CVE OS (glibc, libexpat...) | No fix upstream — responsabilité Canonical |

**Point de vigilance VEX Actuator**
La validité des VEX CVE-2026-22731/22733 repose sur le maintien de la
configuration `management.endpoints.enabled-by-default=false` en production.
Tout changement de configuration Actuator doit déclencher une réévaluation
de ces VEX. Ce couplage configuration/sécurité doit être documenté dans
les guidelines de contribution du projet.

**Améliorations envisagées**
- Migrer vers une image de base `distroless` ou Alpine pour éliminer
  la majorité des CVE OS (à traiter en V4)
- Mettre en place Dependabot pour les alertes CVE automatiques
  sur les dépendances Maven
- Aligner `ignore-unfixed` sur `false` dans ci-build.yml pour une
  politique cohérente entre les deux workflows

**Mesure complémentaire**
- SEC-DEP-01 : épinglage des versions (levier correctif)
- SEC-DEP-02 : processus de décision CVE (historique des runs)
- SEC-IMG-01 : Dockerfile durci (suppression curl → réduction surface OS)
