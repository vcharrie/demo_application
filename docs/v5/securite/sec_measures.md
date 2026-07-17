# Catalogue des Mesures de Sécurité — CoreService (V5)

Chaque mesure couvre un ou plusieurs risques et découle d’un arbitrage.

---

## SEC-VAL-01 — Validation d’entrée côté backend
**Risque(s) couvert(s) :** RISK-VAL-01, RISK-INT-01  
**Arbitrage :** ARB-003  
**Objectif :** Empêcher les montants invalides  
**Implémentation :** Voir spec technique §3.2.2  
**Référentiels :** OWASP ASVS V5, NIST SP 800‑53 SI‑10

---

## SEC-AUTHZ-01 — Blocage des comptes suspendus
**Risque(s) couvert(s) :** RISK-LOG-01  
**Arbitrage :** ARB-002  
**Implémentation :** Vérification statut ACTIF dans AccountService  
**Référentiels :** OWASP ASVS V4

---

## SEC-IMMUT-01 — Immutabilité des opérations validées
**Risque(s) couvert(s) :** RISK-INT-02  
**Arbitrage :** ARB-004  
**Implémentation :** Interdiction de modifier Operation VALIDATED  
**Référentiels :** NIST SP 800‑53 AU‑9

---

## SEC-TXN-01 — Gestion du risque de double dépense
**Risque(s) couvert(s) :** RISK-LOG-02  
**Arbitrage :** ARB-005  
**Implémentation :** Transaction ACID + TODO V6 (verrou optimiste)  
**Référentiels :** OWASP ASVS V10

---

## SEC-AUTHZ-02 — Validation des virements élevés
**Risque(s) couvert(s) :** RISK-AUTHZ-01  
**Arbitrage :** ARB-006  
**Implémentation :** Vérification rôle SUPERVISEUR  
**Référentiels :** NIST SP 800‑63

---

## SEC-AUDIT-01 — Historisation obligatoire
**Risque(s) couvert(s) :** RISK-AUDIT-01  
**Arbitrage :** ARB-007  
**Implémentation :** Persistance systématique Operation/Transfer  
**Référentiels :** NIST SP 800‑53 AU‑2

---

## SEC-WORKFLOW-01 — Workflow strict des virements
**Risque(s) couvert(s) :** RISK-LOG-03  
**Arbitrage :** ARB-008  
**Implémentation :** Respect strict des transitions métier  
**Référentiels :** OWASP ASVS V4

---

## SEC-VAL-02 — Interdiction des auto-virements
**Risque(s) couvert(s) :** RISK-LOG-04  
**Arbitrage :** ARB-009  
**Implémentation :** Vérification compteSource ≠ compteDestination  
**Référentiels :** OWASP ASVS V5

---

## SEC-VAL-03 — Vérification du statut du compte destination
**Risque(s) couvert(s) :** RISK-LOG-05  
**Arbitrage :** ARB-010  
**Implémentation :** Vérification statut ≠ Fermé  
**Référentiels :** OWASP ASVS V5