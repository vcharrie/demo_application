🟦 1) Description générale des composants Kubernetes

Ci-dessous la listee des composants apparaissant sur le schéma :
🟩 A. Composants système (dans le container Docker kind-control-plane)

Ce sont des processus Linux, qui s'exécutent directement dans le container Docker qui représente le node Kubernetes.

    kube-apiserver  :
      - Point d’entrée du cluster. Reçoit les manifests, expose l’API, valide les objets.

    etcd : 
      - Base de données clé-valeur. Stocke tous les objets Kubernetes (Deployments, Services, Pods…).

    kube-scheduler :  
      - Choisit sur quel node placer les Pods.

    kube-controller-manager  
      - Applique la logique déclarative : crée/maintient les Pods selon les Deployments.

    kubelet  
      - Agent du node. Lance les containers via containerd, surveille les Pods, monte les Secrets.

🟦 B. Runtime de containers (dans le container Docker)

    containerd  
      - Le moteur qui exécute les containers des Pods.
      - Il s'exécute dans le container Docker, et c’est lui qui lance les containers des Pods.

🟧 C. Pods natifs (exécutés via containerd)

Ce sont des Pods “de base” nécessaires au fonctionnement du cluster.

    kube-proxy  
      - Configure iptables/IPVS. Applique les Services. Route le trafic vers les Pods.

    coredns  
      - DNS interne du cluster. Résout les noms des Services.

🟩 D. Pods d’infrastructure (exécutés via containerd)

Ce sont les Pods installés pour ajouter des fonctionnalités aux pods applicatifs :

    ingress-nginx-controller  
      - Lit les objets Ingress. Configure NGINX. Fait le reverse proxy HTTP.

    sealed-secrets-controller  
       - Déchiffre les SealedSecrets. Crée les Secrets Kubernetes.

🟨 E. Pods applicatifs (exécutés via containerd)

    Pod coreservice  
      : Pod créé par le Deployment applicatif.

    Container coreservice (Spring Boot)  
      : Le container qui exécute réellement le code.

🟪 F. Objets Kubernetes (stockés dans etcd)

Ce sont les fichiers de déclarations qui vivent dans etcd et sont appliqués par les contrôleurs.

    Deployment  
      Décrit combien de Pods doivent exister.

    Service  
      IP virtuelle stable. kube-proxy applique les règles réseau.

    Ingress  
      Règles HTTP. ingress-nginx applique la config.

    SealedSecret  
      Objet chiffré. Déchiffré par sealed-secrets-controller.

    Secret  
      Objet déchiffré. Monté dans les Pods par kubelet.

🟦 G. Outils côté client (ne tournent pas dans le cluster)

    Kustomize  
      Assemble les YAML, applique les patches, génère les manifests finaux.

    kubectl  
      Envoie les manifests au kube-apiserver.

    Fichiers YAML  
      Déclarations statiques. Restent côté client.

🟦 2) Séquence : création du cluster kind

Les étapes réalisées lorsqu'on fait un :
  - kind create cluster

1. Docker crée un container kind-control-plane  
Ce container est le node Kubernetes.

2. À l’intérieur du container, kind démarre :

    - kube-apiserver

    - etcd

    - kube-scheduler

    - kube-controller-manager

    - kubelet

    - containerd

3. kubelet démarre les Pods natifs :

    Pod kube-proxy

    Pod coredns

4. Le cluster est opérationnel

→ API server répond
→ etcd stocke l’état
→ kube-proxy route
→ coredns résout les noms


🟦 3) Séquence : déploiement de ton application

Lorsque la commande de déploiement des pods applicatifs suivantes est exécutée :

kubectl apply -k k8s/overlays/local

1. Kustomize assemble les YAML

→ génère les manifests finaux
2. kubectl envoie les manifests au kube-apiserver

→ via l’API Kubernetes
3. kube-apiserver stocke les objets dans etcd

    Deployment

    Service

    Ingress

    SealedSecret

4. Les contrôleurs réagissent
kube-controller-manager

→ voit le Deployment
→ crée le Pod coreservice

sealed-secrets-controller

→ voit le SealedSecret
→ crée le Secret déchiffré

ingress-nginx-controller

→ voit l’Ingress
→ configure NGINX

kube-proxy

→ voit le Service
→ configure iptables/IPVS

5. kubelet demande à containerd de lancer le container Spring Boot

→ l'application démarre et s'exécute