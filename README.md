# Analyse des Corrélations Crypto-Stock avec Architecture Modulaire

## 🚀 Description


Ce projet implémente une infrastructure modulaire pour analyser et visualiser les corrélations entre les cryptomonnaies (Bitcoin, Ethereum) et les actions Nvidia. Il combine des flux de données en temps réel et des données historiques pour fournir une analyse approfondie et une visualisation interactive via des tableaux de bord.


# 📊 Fonctionnalités Principales

## Acquisition de données en temps réel depuis les APIs Binance et Finnhub.

Stockage des données en temps réel avec Kafka et des données historiques avec Elasticsearch.
Traitement des données avec Apache Flink/Spark pour calculer des corrélations.
Visualisation avancée avec Tableau, permettant une analyse approfondie des tendances historiques et des relations en temps réel.


# 🏗 Architecture

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




⚙️ Installation et Démarrage
1️⃣ Cloner le Dépôt
bash
Copy code
git clone git clone [project_data_pipeline](https://github.com/Cherkani/project_data_pipeline.git)
cd project_data_pipeline
2️⃣ Lancer l'Infrastructure
Assurez-vous que Docker est en cours d'exécution, puis exécutez :

bash
Copy code
docker-compose up --build
3️⃣ Configurer le Tableau de Bord dans Tableau
Connexion aux Données :
Configurez une connexion dans Tableau avec Elasticsearch en utilisant l'adresse suivante :
arduino
Copy code
http://localhost:9200
Créer des Graphiques :
Configurez des visualisations interactives en utilisant les index des données (btc, eth, nvda).
📄 Structure du Projet
docker-compose.yml : Définit tous les services nécessaires (Kafka, Elasticsearch, NiFi, etc.).
nifi/ : Flux NiFi pour collecter et publier les données.
spark/ : Scripts pour le traitement en temps réel avec Spark.
dashboards/ : Modèles de tableau de bord pour Tableau.
🌐 APIs Utilisées
Binance API : Prix des cryptomonnaies en temps réel.
Finnhub API : Données des actions Nvidia.
📊 Tableaux de Bord
Les tableaux de bord incluent :

Variations des prix BTC, ETH, NVDA.
Corrélations entre cryptomonnaies et actions.
Visualisation des tendances historiques.
🛡 Contributions
Les contributions sont les bienvenues ! Pour contribuer :


