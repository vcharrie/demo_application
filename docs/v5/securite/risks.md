# Registre des Risques — CoreService (V5)

Ce registre est la source d’autorité unique pour tous les risques du projet.  
Chaque risque est rattaché à un actif métier ou technique, et référencé dans les arbitrages (ARB‑xxx) et mesures de sécurité (SEC‑xxx).

---

## RISK-INT-01 — Incohérence de solde
**Origine :** Métier  
**Actif touché :** Compte.solde  
**Description :** Une opération pourrait conduire à un solde incohérent ou négatif.  
**Probabilité :** Moyenne  
**Impact :** Fort  
**Criticité :** Haute  
**Exigence liée :** BR-01, EM-01  
**Arbitrages liés :** ARB-001  
**Mesures liées :** SEC-VAL-01, SEC-LOGIC-01  
**Risque résiduel :** Faible  
**Référentiels :** OWASP ASVS V5, NIST SP 800‑53 (SI‑10)

---

## RISK-LOG-01 — Contournement des statuts de compte
**Origine :** Métier  
**Actif touché :** Compte.statut  
**Description :** Un compte suspendu pourrait émettre ou recevoir des opérations.  
**Probabilité :** Faible  
**Impact :** Moyen  
**Criticité :** Moyenne  
**Exigence liée :** BR-02  
**Arbitrages liés :** ARB-002  
**Mesures liées :** SEC-AUTHZ-01  
**Référentiels :** OWASP ASVS V4, CIS Controls

---

## RISK-VAL-01 — Injection de montants invalides
**Origine :** Métier  
**Actif touché :** Operation.montant  
**Description :** Un montant négatif ou non numérique pourrait être accepté.  
**Probabilité :** Moyenne  
**Impact :** Moyen  
**Criticité :** Moyenne  
**Exigence liée :** BR-03  
**Arbitrages liés :** ARB-003  
**Mesures liées :** SEC-VAL-01  
**Référentiels :** OWASP ASVS V5, NIST SP 800‑53 (SI‑10)

---

## RISK-INT-02 — Modification d’une opération validée
**Origine :** Métier  
**Actif touché :** Operation.etat  
**Description :** Une opération validée pourrait être modifiée a posteriori.  
**Probabilité :** Faible  
**Impact :** Fort  
**Criticité :** Haute  
**Exigence liée :** BR-04  
**Arbitrages liés :** ARB-004  
**Mesures liées :** SEC-IMMUT-01  
**Référentiels :** OWASP ASVS V4, NIST SP 800‑53 (AU‑9)

---

## RISK-LOG-02 — Double dépense (race condition)
**Origine :** Technique  
**Actif touché :** Compte.solde  
**Description :** Deux transactions simultanées peuvent lire un solde suffisant avant débit.  
**Probabilité :** Moyenne  
**Impact :** Fort  
**Criticité :** Haute  
**Exigence liée :** BR-05  
**Arbitrages liés :** ARB-005  
**Mesures liées :** SEC-TXN-01  
**Risque résiduel :** Moyen (V5)  
**Référentiels :** NIST SP 800‑53 (SC‑28), OWASP ASVS V10

---

## RISK-AUTHZ-01 — Validation non autorisée d’un virement
**Origine :** Métier  
**Actif touché :** Agent.rôle  
**Description :** Un agent non SUPERVISEUR pourrait valider un virement élevé.  
**Probabilité :** Moyenne  
**Impact :** Fort  
**Criticité :** Haute  
**Exigence liée :** BR-06, BR-09  
**Arbitrages liés :** ARB-006  
**Mesures liées :** SEC-AUTHZ-02  
**Référentiels :** OWASP ASVS V4, NIST SP 800‑63

---

## RISK-AUDIT-01 — Absence de traçabilité
**Origine :** Métier  
**Actif touché :** Historisation  
**Description :** Une opération pourrait ne pas être historisée.  
**Probabilité :** Faible  
**Impact :** Fort  
**Criticité :** Moyenne  
**Exigence liée :** BR-07  
**Arbitrages liés :** ARB-007  
**Mesures liées :** SEC-AUDIT-01  
**Référentiels :** NIST SP 800‑53 (AU‑2), OWASP ASVS V7

---

## RISK-LOG-03 — Contournement du workflow de virement
**Origine :** Métier  
**Actif touché :** Transfer.etat  
**Description :** Un virement pourrait passer à COMPLETED sans respecter les règles.  
**Probabilité :** Faible  
**Impact :** Moyen  
**Criticité :** Moyenne  
**Exigence liée :** BR-08  
**Arbitrages liés :** ARB-008  
**Mesures liées :** SEC-WORKFLOW-01  
**Référentiels :** OWASP ASVS V4

---

## RISK-LOG-04 — Fraude interne (auto-virement)
**Origine :** Métier  
**Actif touché :** Transfer  
**Description :** Un utilisateur pourrait effectuer un virement vers son propre compte.  
**Probabilité :** Faible  
**Impact :** Moyen  
**Criticité :** Moyenne  
**Exigence liée :** BR-10  
**Arbitrages liés :** ARB-009  
**Mesures liées :** SEC-VAL-02  
**Référentiels :** OWASP ASVS V5

---

## RISK-LOG-05 — Virement vers un compte invalide
**Origine :** Métier  
**Actif touché :** Transfer  
**Description :** Un virement pourrait être envoyé vers un compte fermé.  
**Probabilité :** Faible  
**Impact :** Moyen  
**Criticité :** Moyenne  
**Exigence liée :** BR-11  
**Arbitrages liés :** ARB-010  
**Mesures liées :** SEC-VAL-03  
**Référentiels :** OWASP ASVS V5