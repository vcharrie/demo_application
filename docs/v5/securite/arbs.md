# Journal des Arbitrages — CoreService (V5)

Chaque arbitrage documente une décision structurante, ses impacts, et les risques résiduels acceptés.

---

## ARB-001 — Contrôle strict du solde avant opération
**Risque concerné :** RISK-INT-01  
**Contexte :** Prévenir les incohérences de solde.  
**Options :**  
1. Contrôle côté frontend — rejeté (non fiable).  
2. Contrôle côté backend — retenu.  
**Impacts :**  
- Métier : cohérence garantie  
- Fonctionnel : aucun  
- Technique : validation systématique  
- Sécurité : intégrité (I) renforcée  
**Décision :** Contrôle backend obligatoire  
**Mesure dérivée :** SEC-VAL-01

---

## ARB-002 — Blocage strict des comptes suspendus
**Risque concerné :** RISK-LOG-01  
**Décision :** Vérification systématique du statut ACTIF  
**Mesure dérivée :** SEC-AUTHZ-01

---

## ARB-003 — Validation d’entrée côté backend
**Risque concerné :** RISK-VAL-01  
**Décision :** Validation backend obligatoire  
**Mesure dérivée :** SEC-VAL-01

---

## ARB-004 — Immutabilité des opérations validées
**Risque concerné :** RISK-INT-02  
**Décision :** Interdiction de modifier une opération VALIDATED  
**Mesure dérivée :** SEC-IMMUT-01

---

## ARB-005 — Gestion du risque de double dépense
**Risque concerné :** RISK-LOG-02  
**Options :**  
1. Verrou pessimiste — lourd  
2. Verrou optimiste — retenu pour V6  
3. Sérialisation stricte — trop coûteuse  
**Décision :** Risque résiduel accepté en V5  
**Mesure dérivée :** SEC-TXN-01

---

## ARB-006 — Validation des virements élevés
**Risque concerné :** RISK-AUTHZ-01  
**Décision :** Validation par agent SUPERVISEUR obligatoire  
**Mesure dérivée :** SEC-AUTHZ-02

---

## ARB-007 — Historisation obligatoire
**Risque concerné :** RISK-AUDIT-01  
**Décision :** Historisation systématique  
**Mesure dérivée :** SEC-AUDIT-01

---

## ARB-008 — Workflow strict des virements
**Risque concerné :** RISK-LOG-03  
**Décision :** Respect strict des transitions métier  
**Mesure dérivée :** SEC-WORKFLOW-01

---

## ARB-009 — Interdiction des auto-virements
**Risque concerné :** RISK-LOG-04  
**Décision :** Vérification obligatoire des comptes distincts  
**Mesure dérivée :** SEC-VAL-02

---

## ARB-010 — Vérification du statut du compte destination
**Risque concerné :** RISK-LOG-05  
**Décision :** Vérification obligatoire du statut ≠ Fermé  
**Mesure dérivée :** SEC-VAL-03