# SEC-CI-02 — Sécurisation des secrets GitHub

**Domaine :** CI/CD Security / Secrets Management  
**Couche :** GitHub Actions (secrets, variables, permissions)  
**Statut :** 🟩 Implémenté (V2)

---

## 1. Risque

### Menace

Les secrets utilisés dans le pipeline CI/CD (tokens GitHub, clés API, credentials de registry, tokens SonarCloud) sont des actifs critiques. S'ils sont exposés dans les logs, injectés dans des actions non fiables, stockés en clair dans le repo, accessibles à des forks, ou utilisés avec des permissions trop larges, ils deviennent un vecteur d'attaque majeur.

### Vecteurs principaux

| Vecteur | Exemple concret |
|---|---|
| Secret exposé dans un log CI | `echo $SONAR_TOKEN` → fuite immédiate |
| Secret injecté dans une action non épinglée | Action compromise → exfiltration |
| Secret accessible aux forks | Pull request malveillante → extraction |
| Secret dans `env:` global | Réutilisation involontaire dans un autre job |
| Permissions GitHub trop larges | `GITHUB_TOKEN` avec `write-all` → push non autorisé |
| Secret dans un fichier versionné | `.env`, `application.properties` |

### Impact

| Dimension | Conséquence |
|---|---|
| **Confidentialité** | Fuite de secrets → compromission du registry, SonarCloud, GitHub |
| **Intégrité** | Push non autorisé, modification d'artefacts |
| **Disponibilité** | Rotation forcée, pipeline bloqué |
| **Supply chain** | Compromission de l'image publiée |

### Références

- NIST SSDF — PW.9
- OWASP Secrets Management
- GitHub Actions Security Best Practices
- CIS Software Supply Chain Benchmark

---

## 2. Mesures de sécurité

| Mesure | Type | Principe |
|---|---|---|
| Stockage exclusif dans GitHub Secrets | Préventif | Aucun secret dans le repo |
| Masquage automatique des secrets | Préventif | Confidentialité |
| Permissions minimales (`GITHUB_TOKEN`) | Préventif | Moindre privilège |
| Injection step-level sécurisée | Préventif | Minimisation de l'exposition |
| Interdiction des secrets dans les logs | Préventif | Confidentialité |
| Secrets non accessibles aux forks | Préventif | Isolation |
| Rotation régulière | Correctif | Réduction du risque |
| Audit des accès secrets | Détectif | Traçabilité |

---

## 3. Implémentation

### 3.1. Stockage des secrets

**Principe :** aucun secret ne doit apparaître dans le code source, les fichiers de configuration, les logs ou les artefacts CI.

Configuration : **GitHub → Settings → Secrets and variables → Actions → New repository secret**

Exemples de secrets à déclarer :

- `GHCR_TOKEN`
- `SONAR_TOKEN`
- `REGISTRY_PASSWORD`

---

### 3.2. Injection step-level sécurisée

**Principe :** passer le secret par une variable d'environnement intermédiaire. La substitution directe `${{ secrets.X }}` dans `run:` résout la valeur avant exécution du shell, ce qui peut l'exposer dans les logs de substitution.

```yaml
# ❌ Incorrect — substitution directe exposée
- name: Login to GHCR
  run: echo ${{ secrets.GHCR_TOKEN }} | docker login ghcr.io -u USERNAME --password-stdin

# ✅ Correct — passage par variable d'environnement
- name: Login to GHCR
  env:
    TOKEN: ${{ secrets.GHCR_TOKEN }}
  run: echo "$TOKEN" | docker login ghcr.io -u USERNAME --password-stdin
```

---

### 3.3. Permissions minimales (`GITHUB_TOKEN`)

**Principe :** le token interne GitHub doit avoir uniquement les permissions nécessaires au job concerné.

```yaml
permissions:
  contents: read
  packages: write
  actions: read
```

```yaml
# ❌ Interdit
permissions: write-all
```

---

### 3.4. Secrets non accessibles aux forks

**Principe :** les secrets ne doivent pas être injectés dans les workflows déclenchés par des pull requests venant de forks.

```yaml
on:
  pull_request:
    branches: [ main ]
    types: [opened, synchronize]
    # Les secrets ne sont pas injectés automatiquement pour les forks
```

> Si un workflow nécessite des secrets sur PR, utiliser `pull_request_target` avec une extrême prudence et une validation explicite de la source.

---

### 3.5. Masquage dans les logs

**Principe :** forcer le masquage d'un secret si sa valeur est utilisée dans un contexte non standard.

```yaml
- name: Mask secret
  run: echo "::add-mask::${{ secrets.MY_SECRET }}"
```

> GitHub masque automatiquement toute valeur déclarée dans `secrets.*`. Ce step explicite est utile pour les valeurs dérivées ou reconstruites dynamiquement.

---

### 3.6. Rotation des secrets

**Principe :** limiter la fenêtre d'exploitation en cas de fuite.

- Rotation **trimestrielle** en conditions normales
- Rotation **immédiate** en cas de suspicion de fuite
- Rotation **automatique** si le fournisseur le supporte (AWS, Azure, GCP)

---

### 3.7. Audit des accès

**Principe :** tracer les usages et modifications des secrets.

**GitHub → Settings → Security → Audit log**

Filtres utiles : `secret.access`, `secret.update`, `secret.delete`

---

## 4. Risques résiduels

| Risque résiduel | Justification d'acceptation | Amélioration envisagée |
|---|---|---|
| Secrets non chiffrés en mémoire côté runner | GitHub masque mais ne chiffre pas en mémoire | Runner auto-hébergé + Vault (V4) |
| Actions tierces non auditées exhaustivement | Complexité de vérification | Liste blanche interne (V4) |
| Rotation manuelle | Charge opérationnelle | Rotation automatique (V5) |
| `pull_request_target` dangereux | Hors scope actuel | Politique stricte + sandbox (V5) |

---

## Références croisées

- **SEC-CI-01** — Sécurisation du pipeline CI/CD (épinglage des actions)
- **SEC-CI-03** — Provenance & intégrité supply chain
- **SEC-IMG-01** — Durcissement de l'image Docker
