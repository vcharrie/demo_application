# SEC-CI-01 — Sécurisation du pipeline CI/CD

**Domaine :** Supply Chain / CI/CD Security  
**Couche :** Pipeline GitHub Actions (build → analyse → scan → publication)  
**Statut :** 🟩 Implémenté (V2)

---

## 1. Risque

### Menace

Un pipeline CI/CD non sécurisé devient un vecteur d'attaque majeur dans la supply chain logicielle. Un attaquant peut :

- injecter du code dans l'artefact final
- publier une image compromise dans le registry
- contourner les gates SAST/SCA
- falsifier un SBOM ou un rapport de sécurité
- exfiltrer des secrets via une action compromise
- pousser une image non scannée en production

### Vecteurs principaux

| Vecteur | Exemple concret |
|---|---|
| Actions non épinglées | Action compromise → exfiltration de secrets |
| Runner non maîtrisé | Exécution de code non isolé → fuite de secrets |
| Absence de traçabilité | Impossible de relier une image à un commit SHA |
| Build non reproductible | Artefact différent selon l'environnement |
| Artefacts CI exposés | SBOM ou logs accessibles publiquement |
| Push mutable (`latest`) | Écrasement silencieux d'une image existante |

### Impact

| Dimension | Conséquence |
|---|---|
| **Intégrité** | Artefact modifié, image compromise |
| **Confidentialité** | Fuite de secrets via logs ou actions tierces |
| **Disponibilité** | Pipeline bloqué par des CVE non gérées |
| **Traçabilité** | Impossibilité d'auditer un incident |

### Références

- NIST SSDF — PW.4, PW.5, RV.1, RV.2
- OWASP CI/CD Security Risks
- SLSA Framework (Levels 1–3)
- CIS Software Supply Chain Benchmark

---

## 2. Mesures de sécurité

| Mesure | Type | Principe |
|---|---|---|
| Épinglage strict des actions GitHub par SHA | Préventif | Immutabilité supply chain |
| Séparation des jobs (build / scan / publish) | Préventif | Isolation des responsabilités |
| Traçabilité SHA / digest / run ID | Détectif | Auditabilité complète |
| Build Docker reproductible + labels OCI | Préventif | Intégrité de l'artefact |
| SBOM CycloneDX obligatoire | Détectif | Inventaire des dépendances |
| Scan SCA (SBOM) + scan image (Trivy) | Détectif | Détection CVE multi-couches |
| Quality gate CRITICAL/HIGH | Préventif | Blocage des vulnérabilités |
| Publication immuable (tag = SHA) | Préventif | Non-mutabilité |
| Artefacts CI non versionnés dans Git | Préventif | Confidentialité |
| Archivage contrôlé via `upload-artifact` | Détectif | Preuve d'audit |

---

## 3. Implémentation — Pipeline

### 3.1. Épinglage strict des actions GitHub

**Principe :** un tag de version peut être réécrit silencieusement ; seul le SHA de commit est immuable.

```yaml
# ❌ Incorrect — tag mutable
uses: actions/checkout@v4

# ✅ Correct — SHA immuable
uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683  # v4.1.7
```

> Appliquer ce principe à **toutes** les actions du pipeline, y compris `upload-artifact`, `setup-java`, etc.

---

### 3.2. Séparation des jobs

**Principe :** empêcher un job compromis d'altérer les étapes suivantes.

```yaml
jobs:
  build:
    runs-on: ubuntu-latest

  sast:
    needs: build

  sca:
    needs: build

  image-scan:
    needs: build

  publish:
    needs: [sast, sca, image-scan]
```

---

### 3.3. Traçabilité SHA / digest / run ID

**Principe :** chaque build doit produire un fichier de métadonnées immuable.

```yaml
- name: Generate metadata
  run: |
    echo "sha=$GITHUB_SHA" >> metadata.txt
    echo "run_id=$GITHUB_RUN_ID" >> metadata.txt
    echo "date=$(date -u)" >> metadata.txt

- name: Capture image digest
  run: |
    IMAGE_DIGEST=$(docker inspect --format='{{index .RepoDigests 0}}' coreservice:${GITHUB_SHA})
    echo "digest=$IMAGE_DIGEST" >> metadata.txt
```

---

### 3.4. Build Docker reproductible + labels OCI

**Principe :** garantir l'intégrité et la traçabilité de chaque image produite.

```yaml
- name: Build image
  run: |
    docker build \
      --label org.opencontainers.image.revision=${GITHUB_SHA} \
      --label org.opencontainers.image.source="https://github.com/${GITHUB_REPOSITORY}" \
      --label org.opencontainers.image.created="$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
      -t coreservice:${GITHUB_SHA} .
```

---

### 3.5. SBOM CycloneDX

**Principe :** inventaire complet des dépendances avant scan SCA.

```yaml
- name: Generate SBOM
  run: mvn -B org.cyclonedx:cyclonedx-maven-plugin:2.7.9:makeAggregateBom
```

---

### 3.6. Scan SCA (SBOM)

**Principe :** détecter les CVE sur les dépendances Maven avant build de l'image.

```yaml
- name: Scan SBOM
  run: |
    trivy sbom \
      --severity HIGH,CRITICAL \
      --ignore-unfixed=false \
      --exit-code 1 \
      target/bom.json
```

---

### 3.7. Scan image (Trivy)

**Principe :** détecter les CVE OS + Java dans l'image finale.

```yaml
- name: Scan image
  run: |
    trivy image \
      --severity HIGH,CRITICAL \
      --ignorefile config/trivy/.trivyignore.yaml \
      --ignore-unfixed=true \
      --exit-code 1 \
      coreservice:${GITHUB_SHA}
```

---

### 3.8. Publication immuable

**Principe :** une image = un SHA = un digest. Pas de tag `latest` mutable.

```yaml
- name: Push image
  run: |
    docker tag coreservice:${GITHUB_SHA} ghcr.io/<repo>/coreservice:${GITHUB_SHA}
    docker push ghcr.io/<repo>/coreservice:${GITHUB_SHA}
```

---

## 4. Gestion des artefacts CI

> Les artefacts produits par le pipeline (SBOM, rapports Trivy, métadonnées) contiennent des informations sensibles. Ils ne doivent jamais être versionnés dans Git.

### 4.1. Artefacts à ne jamais versionner

Ajouter au `.gitignore` :

```gitignore
# Artefacts CI — ne jamais versionner
target/
bom.json
metadata.txt
trivy-*.json
trivy-*.txt
provenance.txt
*.sha256
```

---

### 4.2. Archivage contrôlé

**Principe :** archiver hors du repo Git avec accès restreint aux membres du projet.

```yaml
- name: Upload CI artifacts
  uses: actions/upload-artifact@6f51ac03b9356f520e9adb1b1b7802705f340c2b  # v4.5.0
  with:
    name: ci-artifacts
    retention-days: 30
    path: |
      metadata.txt
      target/bom.json
      trivy-image.txt
      trivy-sbom.txt
```

> **Note :** l'action `upload-artifact` est elle-même épinglée par SHA, conformément au principe §3.1.

---

### 4.3. Hash optionnel des artefacts exportés

**Principe :** garantir l'intégrité des artefacts transmis hors du pipeline.

```bash
sha256sum metadata.txt > metadata.txt.sha256
sha256sum target/bom.json > bom.json.sha256
```

---

## 5. Risques résiduels

| Risque résiduel | Justification d'acceptation | Amélioration envisagée |
|---|---|---|
| CVE OS sans fix disponible | Responsabilité Canonical | Migration distroless (V4) |
| `ignore-unfixed=true` sur le scan image | Réduction du bruit OS | Alignement complet avec SBOM (V4) |
| Dépendance au runner GitHub SaaS | Contrainte opérationnelle | Runner auto-hébergé (V5) |
| Pas de signature cryptographique | Hors scope V1 | Cosign — voir SEC-CI-03 |
| Absence de DAST | Hors scope actuel | Intégration ZAP ou StackHawk |

---

## Références croisées

- **SEC-CI-02** — Sécurisation des secrets GitHub
- **SEC-CI-03** — Provenance & intégrité supply chain (SLSA)
- **SEC-IMG-01** — Durcissement de l'image Docker
- **SEC-SCA-01** — SBOM + Trivy
- **SEC-DEP-02** — Gestion CVE / VEX
