# Analyse des Corrélations Crypto-Stock avec Architecture Modulaire

## 🚀 Description

Ce projet implémente une infrastructure modulaire pour analyser et visualiser les corrélations entre les cryptomonnaies (Bitcoin, Ethereum) et les actions Nvidia. Il combine des flux de données en temps réel et des données historiques pour fournir une analyse approfondie et une visualisation interactive via des tableaux de bord.

---

## 📊 Fonctionnalités Principales

- **Acquisition de données en temps réel** depuis les APIs Binance et Finnhub.

- **Stockage** des données en temps réel avec Kafka et des données historiques avec Elasticsearch.

- **Traitement des données** avec Apache Flink/Spark pour calculer des corrélations.

- **Visualisation avancée** avec Tableau, permettant une analyse approfondie des tendances historiques et des relations en temps réel.

---

## 🏗 Architecture

L'architecture repose sur une approche modulaire avec les composants suivants :

- **NiFi** : Récupération des données des APIs.

- **Kafka** : Gestion des flux de données en temps réel.

- **Elasticsearch** : Stockage et interrogation des données.

- **Apache Flink/Spark** : Traitement des données en streaming.

- **Tableau** : Création de graphiques interactifs et dynamiques.

---

## ⚙️ Étapes de Lancement du Projet

### Chapitre 1 : Clonage et Lancement du Projet

1. **Cloner le projet :**
   ```bash
   git clone https://github.com/Cherkani/project_data_pipeline.git
   ```


Cela téléchargera une copie du projet localement.

2. Accéder au répertoire :

  ```bash
  cd project_data_pipeline
  ```

3. Démarrer les services avec Docker Compose :

```bash
docker-compose up
```

Cette commande initialise les conteneurs Docker nécessaires et garantit que les dépendances (Kafka, Elasticsearch, etc.) sont opérationnelles.


## Configurer le Flux de Données avec Apache NiFi
1. Accédez à l'interface NiFi et configurez les flux suivants :

  - InvokeHTTP : Configurez des requêtes pour collecter les données depuis les APIs (Binance et Finnhub).
  
  - GenerateFlowFile : Synchronisez les données collectées.
  
  - PublishKafka_2_6 : Publiez les données dans des topics Kafka.
  
  - Configurez les URLs des APIs :

2. Binance API : Récupère les prix des cryptomonnaies.
```bash
https://api.binance.com/api/v3/ticker/price?symbols=%5B%22BTCUSDT%22,%22ETHUSDT%22%5D
```

Finnhub API : Récupère les données Nvidia.

```bash
https://finnhub.io/api/v1/quote?symbol=NVDA&token=<votre_token>
```

## Gestion des Topics Kafka

1. Connectez-vous à l'interface Kafka.

2. Créez les topics nécessaires :

- my_topic

- topic2

Configurez les partitions et le facteur de réplication selon vos besoins.

Traitement des Données avec Apache Flink
Compilez et empaquetez le projet avec Maven :

```bash
mvn clean package
```

Ajoutez un job dans l'interface Flink :

Chargez le fichier JAR généré.
Configurez la classe principale et soumettez le job.
Analyse avec Apache Spark




1. Déployer le script Spark : Transférez le fichier Spark vers le conteneur :

```bash
docker cp /path/to/script.py spark-master:/opt/bitnami/spark/
```


2. Exécuter le script Spark :

```bash
spark-submit /opt/bitnami/spark/script.py
```

3. Vérifiez l’état des jobs dans l’interface Spark.

## Intégration avec Elasticsearch et Kibana

1. Assurez-vous qu'Elasticsearch est opérationnel.

2. Accédez à Kibana via l'URL configurée et ouvrez l'onglet Discover.

3. Recherchez les indices (par exemple, btc, eth) pour analyser les données en temps réel.

4. Répétez ces étapes pour chaque index utilisé afin de valider les enregistrements.



## Visualisation avec Tableau

1. Configurez une connexion avec Elasticsearch dans Tableau en utilisant l'URL suivante :

```bash
http://localhost:9200
```

2. Créez des graphiques interactifs avec les données des indices (btc, eth, nvda).

📄 Structure du Projet

- docker-compose.yml : Définit tous les services nécessaires.
- nifi/ : Flux NiFi pour collecter et publier les données.
- spark/ : Scripts pour le traitement en temps réel avec Spark.
- dashboards/ : Modèles de tableau de bord pour Tableau.

🌐 APIs Utilisées

- Binance API : Prix des cryptomonnaies en temps réel.

- Finnhub API : Données des actions Nvidia.

📊 Tableaux de Bord

Les tableaux de bord incluent :

- Variations des prix BTC, ETH, NVDA.

- Corrélations entre cryptomonnaies et actions.

- Visualisation des tendances historiques.


🛡 Contributions

Les contributions sont les bienvenues ! Pour contribuer :

1. Forkez le dépôt.
2. Créez une branche pour vos modifications.
3. Soumettez une Pull Request.




















# Analyse des Corrélations Crypto-Stock avec Architecture Modulaire

## 🚀 Description


Ce projet implémente une infrastructure modulaire pour analyser et visualiser les corrélations entre les cryptomonnaies (Bitcoin, Ethereum) et les actions Nvidia. Il combine des flux de données en temps réel et des données historiques pour fournir une analyse approfondie et une visualisation interactive via des tableaux de bord.


# 📊 Fonctionnalités Principales

## Acquisition de données en temps réel depuis les APIs Binance et Finnhub.

Stockage des données en temps réel avec Kafka et des données historiques avec Elasticsearch.

Traitement des données avec Apache Flink/Spark pour calculer des corrélations.

Visualisation avancée avec Tableau, permettant une analyse approfondie des tendances historiques et des relations en temps réel.

# video

https://github.com/user-attachments/assets/92fd0eba-f6c6-42ab-ad52-1b28de5eedba


# 🏗 Architecture

![data pipeline](https://github.com/user-attachments/assets/3f7eea15-9a67-4aa8-b661-d22235c440d4)


## L'architecture repose sur une approche modulaire avec les composants suivants :

NiFi : Récupération des données des APIs.

Kafka : Gestion des flux de données en temps réel.

Elasticsearch : Stockage et interrogation des données.

Apache Flink/Spark : Traitement des données en streaming.

Tableau : Création de graphiques interactifs et dynamiques.

# 📈 Pipeline de Traitement

## Collecte des données : API Binance (BTC, ETH) et Finnhub (NVDA).

Streaming en temps réel : Kafka.

Analyse : Flink/Spark calcule les relations entre les cryptomonnaies et Nvidia.

Visualisation : Tableau affiche les données traitées à partir de l’index Elasticsearch.


# 🛠 Prérequis

## Avant de commencer, assurez-vous d'avoir les outils suivants installés :

🐳 Docker Desktop

🐍 Python 3.8+

💻 16 Go de RAM (minimum recommandé)

🖥️ Git

📊 Tableau




# ⚙️ Installation et Démarrage
## 1️⃣ Cloner le Dépôt

git clone git clone [project_data_pipeline](https://github.com/Cherkani/project_data_pipeline.git)

cd project_data_pipeline

## 2️⃣ Lancer l'Infrastructure

Assurez-vous que Docker est en cours d'exécution, puis exécutez :

docker-compose up --build

## 3️⃣ Configurer le Tableau de Bord dans Tableau

Connexion aux Données :

Configurez une connexion dans Tableau avec Elasticsearch en utilisant l'adresse suivante :
arduino

http://localhost:9200

Créer des Graphiques :

Configurez des visualisations interactives en utilisant les index des données (btc, eth, nvda).

## 📄 Structure du Projet

docker-compose.yml : Définit tous les services nécessaires (Kafka, Elasticsearch, NiFi, etc.).

nifi/ : Flux NiFi pour collecter et publier les données.

spark/ : Scripts pour le traitement en temps réel avec Spark.

dashboards/ : Modèles de tableau de bord pour Tableau.

## 🌐 APIs Utilisées

Binance API : Prix des cryptomonnaies en temps réel.

Finnhub API : Données des actions Nvidia.

## 📊 Tableaux de Bord

Les tableaux de bord incluent :

Variations des prix BTC, ETH, NVDA.

Corrélations entre cryptomonnaies et actions.

Visualisation des tendances historiques.

## 🛡 Contributions

Les contributions sont les bienvenues ! Pour contribuer :


