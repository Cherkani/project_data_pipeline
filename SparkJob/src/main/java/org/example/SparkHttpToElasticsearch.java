package org.example;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import scala.Tuple2;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SparkHttpToElasticsearch {

    // Kafka and Elasticsearch Configuration
    private static final String KAFKA_TOPIC = "my_topic";
    private static final String KAFKA_BROKERS = "172.18.0.10:9093";
    private static final String ELASTICSEARCH_URL = "http://172.18.0.6:9200/relationship/_doc";

    public static void main(String[] args) throws Exception {
        // Spark Configuration
        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkHttpToElasticsearch")
                .setMaster("local[*]")
                .set("spark.streaming.kafka.allowNonConsecutiveOffsets", "true");

        // Spark Streaming Context
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(5000));

        // Kafka Consumer Configuration
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", KAFKA_BROKERS);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "crypto-nvda-group");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Collections.singletonList(KAFKA_TOPIC);

        // Kafka Direct Stream
        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaParams)
        );
        stream.flatMapToPair(record -> {
            List<Tuple2<String, Double>> results = new ArrayList<>();
            try {
                String value = record.value();
                if (value.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(value);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        results.add(new Tuple2<>(json.getString("symbol"), json.getDouble("price")));
                    }
                } else {
                    JSONObject json = new JSONObject(value);
                    results.add(new Tuple2<>("Eth", json.getDouble("pc")));
                }
            } catch (Exception e) {
                System.err.println("Erreur de parsing JSON : " + e.getMessage());
            }
            return results.iterator();
        }).foreachRDD(rdd -> {
            try {
                if (!rdd.isEmpty()) {
                    // Cast RDD to JavaPairRDD
                    JavaPairRDD<String, Double> pairRdd = rdd.mapToPair(record -> (Tuple2<String, Double>) record);                    // Collect as map
                    Map<String, Double> latestPrices = pairRdd.collectAsMap();

                    double prevBtc = State.prevBtc;
                    double prevEth = State.prevEth;
                    double prevNvda = State.prevNvda;

                    double btc = latestPrices.getOrDefault("BTCUSDT", prevBtc);
                    double eth = latestPrices.getOrDefault("ETHUSDT", prevEth);
                    double nvda = latestPrices.getOrDefault("NVDA", prevNvda);

                    // Vérifier les relations
                    checkRelationship("BTC", btc, prevBtc, "NVDA", nvda, prevNvda);
                    checkRelationship("ETH", eth, prevEth, "NVDA", nvda, prevNvda);

                    // Mettre à jour l'état
                    State.prevBtc = btc;
                    State.prevEth = eth;
                    State.prevNvda = nvda;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement RDD : " + e.getMessage());
            }
        });


        // Démarrer le contexte Spark Streaming
        jssc.start();
        jssc.awaitTermination();
    }

    // Function to check the relationship
    private static void checkRelationship(String cryptoSymbol, double currentCrypto, double prevCrypto, String stockSymbol, double currentStock, double prevStock) {
        double relationshipScore = 0.0; // Initialize the score

        if (currentCrypto > prevCrypto) {
            if (currentStock > prevStock) {
                relationshipScore = 1.0; // Positive Relationship
            } else {
                relationshipScore = 0.5; // Weak Relationship
            }
        } else if (currentCrypto < prevCrypto) {
            if (currentStock < prevStock) {
                relationshipScore = -1.0; // Negative Relationship
            } else {
                relationshipScore = 0.0; // No Clear Relationship
            }
        } else {
            relationshipScore = 0.0; // No Change
        }

        sendToElastic(cryptoSymbol, stockSymbol, relationshipScore);

    }
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.of("UTC"));

    private static void sendToElastic(String cryptoSymbol, String stockSymbol, double score) {
        try {
            String currentTimestamp = TIMESTAMP_FORMATTER.format(Instant.now());

            URL elasticUrl = new URL(ELASTICSEARCH_URL);
            HttpURLConnection conn = (HttpURLConnection) elasticUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonData = String.format(
                    "{\"crypto\": \"%s\", \"stock\": \"%s\", \"relationship_score\": %.2f, \"timestamp\": \"%s\"}",
                    cryptoSymbol, stockSymbol, score, currentTimestamp
            );

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonData.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                System.out.println("Sent to Elasticsearch: " + jsonData);
            } else {
                System.err.println("Failed to send to Elasticsearch, Response code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error sending to Elasticsearch: " + e.getMessage());
        }
    }
    // Classe pour conserver l'état des prix précédents
    static class State {
        static double prevBtc = 0.0;
        static double prevEth = 0.0;
        static double prevNvda = 0.0;
    }
}