Analyse des Corrélations Crypto-Stock avec Architecture Modulaire
🚀 Description
Ce projet implémente une infrastructure modulaire pour analyser et visualiser les corrélations entre les cryptomonnaies (Bitcoin, Ethereum) et les actions Nvidia. Il combine des flux de données en temps réel et des données historiques pour fournir une analyse approfondie et une visualisation interactive via des tableaux de bord.

📊 Fonctionnalités Principales
Acquisition de données en temps réel depuis les APIs Binance et Finnhub.
Stockage des données en temps réel (Kafka) et historiques (Elasticsearch).
Traitement des données avec Apache Flink/Spark pour calculer des corrélations.
Visualisation interactive avec Kibana.
🏗 Architecture
L'architecture repose sur une approche modulaire avec les composants suivants :

NiFi : Récupération des données des APIs.
Kafka : Gestion des flux de données en temps réel.
Elasticsearch : Stockage et interrogation des données.
Kibana : Visualisation interactive.
Apache Flink/Spark : Traitement des données en streaming.
📈 Pipeline de Traitement
Collecte des données : API Binance (BTC, ETH) et Finnhub (NVDA).
Streaming en temps réel : Kafka.
Analyse : Flink/Spark calcule les relations entre les cryptomonnaies et Nvidia.
Visualisation : Tableaux de bord dans Kibana.
🛠 Prérequis
Avant de commencer, assurez-vous d'avoir les outils suivants installés :

🐳 Docker Desktop
🐍 Python 3.8+
💻 16 Go de RAM (minimum recommandé)
🖥️ Git
📊 Tableau ou Kibana  



# 📦 Setup du Projet

## 1️⃣ Cloner le Dépôt
git clone [project_data_pipeline](https://github.com/Cherkani/project_data_pipeline.git)
cd project_data_pipeline

## 2️⃣ Démarrer l'Infrastructure Docker
docker-compose up --build
