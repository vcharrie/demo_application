BLOC 1 — CI SÉCURISÉ (fin de V3)         ← on est ici
  SEC-IMG-01  Dockerfile durci
  SEC-CI-01   Pipeline GitHub Actions durci
              (épinglage, ordre scan/push,
               permissions GITHUB_TOKEN,
               OIDC sans token statique)

BLOC 2 — SÉCURITÉ APPLICATIVE JAVA       ← formalisation
  SEC-APP-01  Architecture hexagonale & DDD
              (isolation des couches, surface d'attaque)
  SEC-APP-02  Composants Spring Security
              (AuthN, AuthZ, CSRF, CORS, headers)
  SEC-APP-03  Validation des entrées & gestion des erreurs

BLOC 3 — KUBERNETES SÉCURISÉ (V4-A)
  SEC-K8S-01  Security Context
              (runAsNonRoot, readOnly, capabilities)
  SEC-K8S-02  Secrets K8s (Sealed Secrets ou SOPS)
  SEC-K8S-03  Network Policies
  SEC-K8S-04  RBAC K8s

BLOC 4 — CLOUD & IaC SÉCURISÉS (V4-B/C)
  SEC-IAC-01  Terraform — state sécurisé + scanning
              (tfsec/checkov, S3+DynamoDB backend)
  SEC-IAC-02  AWS IAM — least privilege
  SEC-IAC-03  GitHub OIDC → AWS
              (suppression credentials statiques)
  SEC-IAC-04  EKS — configuration sécurisée
              (node groups, IRSA, logging)
  SEC-CD-01   Pipeline CD sécurisé
              (vérification digest, deploy gate)
			  
Couverture compétences :

Code sécurisé        ✅ BLOC 2
Build sécurisé       ✅ BLOC 1 (déjà largement couvert)
Image sécurisée      ✅ BLOC 1
Pipeline sécurisé    ✅ BLOC 1
Infra sécurisée      ✅ BLOC 4
Runtime sécurisé     ✅ BLOC 3
Supply chain         ✅ Couvert (SEC-SCA-01, SEC-DEP-*)
Secrets management   ✅ BLOCS 1, 3, 4