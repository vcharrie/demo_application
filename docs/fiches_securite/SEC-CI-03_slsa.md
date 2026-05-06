# 📘 **FICHE 3 — SEC‑CI‑04 (V2)**  
### *Provenance / pré‑SLSA (corrigée, clarifiée)*

```markdown
# SEC‑CI‑04 — Provenance & Intégrité Supply Chain (V2)

**Domaine :** Supply Chain / Build Integrity  
**Statut :** 🟧 Partiellement implémenté (V2)

---

## 1. RISQUE

Sans provenance ni attestation, impossible de prouver :

- qui a construit l’artefact  
- avec quel pipeline  
- avec quelles dépendances  
- si le build a été altéré  

---

## 2. MESURES

| Mesure | Type | Principe |
|---|---|---|
| Provenance minimale (pré‑SLSA) | Détectif | Métadonnées vérifiables |
| Labels OCI | Détectif | Traçabilité |
| Hash des artefacts | Préventif | Intégrité |
| Archivage des métadonnées | Détectif | Audit |
| Séparation build/publish | Préventif | Isolation |
| Immutabilité (SHA) | Préventif | Non‑mutabilité |

---

## 3. IMPLÉMENTATION (V1 — pré‑SLSA)

### 3.1. Provenance minimale (corrigée)

```yaml
echo "sha=$GITHUB_SHA" > provenance.txt
echo "run_id=$GITHUB_RUN_ID" >> provenance.txt
echo "workflow=$GITHUB_WORKFLOW" >> provenance.txt
echo "runner=$RUNNER_NAME" >> provenance.txt
echo "date=$(date -u)" >> provenance.txt

(Note : ceci n’est pas une provenance SLSA — juste une provenance minimale.)
3.2. Labels OCI
yaml

docker build \
  --label org.opencontainers.image.revision=${GITHUB_SHA} \
  --label org.opencontainers.image.source="https://github.com/${GITHUB_REPOSITORY}" \
  -t coreservice:${GITHUB_SHA} .

3.3. Hash des artefacts
bash

sha256sum provenance.txt > provenance.txt.sha256
sha256sum bom.json > bom.json.sha256

3.4. Archivage
yaml

- uses: actions/upload-artifact@v4
  with:
    name: provenance
    path: |
      provenance.txt
      provenance.txt.sha256
      bom.json
      bom.json.sha256

4. ROADMAP (V2 → V3)
SLSA L1/L2 (réel)

    provenance JSON standardisée

    signature cryptographique

Cosign

    signature image

    signature SBOM

Admission controller

    vérification des signatures avant déploiement

5. RISQUES RÉSIDUELS
Risque	Amélioration
Provenance non standard	SLSA provenance
Pas de signature	Cosign
Pas de vérification	Admission controller