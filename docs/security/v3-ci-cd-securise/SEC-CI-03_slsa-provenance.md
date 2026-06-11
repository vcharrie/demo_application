# SEC-CI-03 — Provenance & Intégrité Supply Chain

**Domaine :** Supply Chain / Build Integrity  
**Couche :** CI/CD (provenance, intégrité des artefacts, traçabilité)  
**Statut :** 🟧 Partiellement implémenté (V2)

---

## 1. Risque

### Menace

Dans une chaîne CI/CD moderne, un attaquant peut viser l'intégrité du build plutôt que le code source. Même si le code est propre, un build compromis peut produire un artefact modifié, une image contenant du code injecté, un SBOM falsifié, ou une image poussée par un acteur non autorisé.

Sans mécanisme de provenance et d'attestation, il est impossible de prouver :

- **qui** a construit l'artefact
- **avec quel pipeline**
- **avec quelles dépendances**
- **dans quel environnement**
- **si le build a été altéré**

### Vecteurs principaux

| Vecteur | Exemple concret |
|---|---|
| Build non attesté | Impossible de prouver que l'image vient du pipeline officiel |
| Attaque sur le runner | Injection de code dans l'image au moment du build |
| SBOM falsifié | Masquage d'une dépendance vulnérable |
| Pipeline modifié | Contournement des gates SAST/SCA |
| Absence de signature | Artefact malveillant poussé sous un tag légitime |

### Impact

| Dimension | Conséquence |
|---|---|
| **Intégrité** | Artefact compromis, image falsifiée |
| **Traçabilité** | Impossibilité d'auditer un incident supply chain |
| **Conformité** | Absence de preuve de provenance (SLSA, NIST SSDF) |
| **Sécurité** | Contournement des gates SAST/SCA |

### Références

- SLSA Framework (Supply-chain Levels for Software Artifacts)
- NIST SSDF — PW.4, PW.5, PS.3
- CIS Software Supply Chain Benchmark
- OWASP CI/CD Security Risks

---

## 2. Mesures de sécurité

| Mesure | Type | Principe |
|---|---|---|
| Provenance minimale (pré-SLSA) | Détectif | Métadonnées vérifiables |
| Hash des artefacts | Préventif | Intégrité |
| Archivage des métadonnées | Détectif | Audit |
| Séparation build / publish | Préventif | Isolation |
| Immutabilité des images (tag = SHA) | Préventif | Non-mutabilité |

> **Note :** les labels OCI sont définis dans **SEC-CI-01 §3.4** et ne sont pas répétés ici.

---

## 3. Implémentation (V2 — pré-SLSA)

> Cette implémentation constitue une **provenance minimale**, pas une provenance SLSA au sens strict. Le format SLSA officiel (JSON signé, standardisé) est prévu en V3 — voir section Roadmap.

---

### 3.1. Provenance minimale

**Principe :** produire un fichier de métadonnées traçant les conditions du build.

```yaml
- name: Generate provenance
  run: |
    echo "sha=$GITHUB_SHA" > provenance.txt
    echo "run_id=$GITHUB_RUN_ID" >> provenance.txt
    echo "workflow=$GITHUB_WORKFLOW" >> provenance.txt
    echo "runner=$RUNNER_NAME" >> provenance.txt
    echo "date=$(date -u)" >> provenance.txt
```

---

### 3.2. Hash des artefacts

**Principe :** garantir que les artefacts n'ont pas été modifiés après le build.

```bash
sha256sum provenance.txt > provenance.txt.sha256
sha256sum target/bom.json > bom.json.sha256
```

---

### 3.3. Archivage des métadonnées

**Principe :** conserver les preuves d'audit hors du repo Git.

```yaml
- name: Upload provenance
  uses: actions/upload-artifact@6f51ac03b9356f520e9adb1b1b7802705f340c2b  # v4.5.0
  with:
    name: provenance
    retention-days: 30
    path: |
      provenance.txt
      provenance.txt.sha256
      target/bom.json
      bom.json.sha256
```

---

### 3.4. Séparation build / publish

**Principe :** empêcher un job compromis de publier une image sans validation des gates.

```yaml
publish:
  needs: [sast, sca, image-scan]
```

> Défini dans **SEC-CI-01 §3.2** — rappelé ici pour la cohérence supply chain.

---

### 3.5. Immutabilité des images

**Principe :** une image = un SHA = un digest. Aucun tag mutable (`latest`).

```yaml
- name: Push image
  run: |
    docker tag coreservice:${GITHUB_SHA} ghcr.io/<repo>/coreservice:${GITHUB_SHA}
    docker push ghcr.io/<repo>/coreservice:${GITHUB_SHA}
```

> Défini dans **SEC-CI-01 §3.8** — rappelé ici pour la cohérence supply chain.

---

## 4. Roadmap

### V3 — SLSA L1/L2 (provenance standardisée)

- Provenance au format JSON SLSA officiel
- Signature cryptographique via token OIDC GitHub

```yaml
# Prérequis : permission OIDC
permissions:
  id-token: write
  contents: read
```

### V3 — Cosign (signature image + SBOM)

```bash
# Signature de l'image
cosign sign --key env://COSIGN_KEY ghcr.io/<repo>/coreservice:${GITHUB_SHA}

# Signature du SBOM
cosign attest --predicate target/bom.json ghcr.io/<repo>/coreservice:${GITHUB_SHA}
```

### V4 — Vérification automatique des signatures

- Admission controller (OPA / Gatekeeper / Kyverno)
- Vérification de signature avant déploiement en cluster

---

## 5. Risques résiduels

| Risque résiduel | Justification d'acceptation | Amélioration envisagée |
|---|---|---|
| Provenance non standardisée | Format maison, non vérifiable automatiquement | SLSA provenance JSON (V3) |
| Pas de signature cryptographique | Complexité + outillage | Cosign (V3) |
| Pas de vérification automatique | Hors scope actuel | Admission controller (V4) |
| Runner GitHub non attesté | Contrainte SaaS | Runner auto-hébergé + enclaves (V4) |

---

## Références croisées

- **SEC-CI-01** — Sécurisation du pipeline CI/CD (labels OCI §3.4, séparation jobs §3.2, immutabilité §3.8)
- **SEC-CI-02** — Sécurisation des secrets GitHub
- **SEC-IMG-01** — Durcissement de l'image Docker
- **SEC-SCA-01** — SBOM + Trivy
- **SEC-DEP-02** — Gestion CVE / VEX
