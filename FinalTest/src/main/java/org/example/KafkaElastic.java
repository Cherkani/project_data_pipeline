package org.example;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class KafkaElastic {

    public static void main(String[] args) throws Exception {
        // Set up Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Kafka Consumer properties
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.18.0.10:9093");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-m9awd");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create Flink Kafka Consumer
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
                "my_topic",
                new SimpleStringSchema(),
                properties
        );

        // Stream processing
        DataStream<String> stream = env.addSource(consumer);

        stream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String value, Context ctx, Collector<String> out) {
                if (value == null || value.isEmpty()) {
                    System.err.println("Received null or empty message");
                    return;
                }

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(value);

                    long kafkaTimestamp = ctx.timestamp(); // Get Kafka timestamp

                    if (rootNode.isArray()) {
                        // Process JSON array
                        for (JsonNode jsonNode : rootNode) {
                            processAndSendToElastic(jsonNode, kafkaTimestamp);
                        }
                    } else if (rootNode.isObject()) {
                        // Process single JSON object
                        processAndSendToElastic(rootNode, kafkaTimestamp);
                    } else {
                        System.err.println("Invalid JSON format: " + value);
                    }

                } catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage() + " | Message: " + value);
                }
            }
        });

        // Execute the Flink job
        env.execute("Flink Kafka to Elasticsearch Job");
    }
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.of("UTC"));
    private static void processAndSendToElastic(JsonNode jsonNode, long kafkaTimestamp) {
        try {
            String currentTimestamp = TIMESTAMP_FORMATTER.format(Instant.now());

            if (jsonNode.has("c") && jsonNode.has("h") && jsonNode.has("l") && jsonNode.has("pc")) {
                double currentPrice = jsonNode.get("c").asDouble();
                double change = jsonNode.get("d").asDouble();
                double percentChange = jsonNode.get("dp").asDouble();
                double high = jsonNode.get("h").asDouble();
                double low = jsonNode.get("l").asDouble();
                double open = jsonNode.get("o").asDouble();
                double prevClose = jsonNode.get("pc").asDouble();

                // Send NVIDIA stock data with Kafka timestamp
                sendToElasticNvidiaStock(currentPrice, change, percentChange, high, low, open, prevClose, kafkaTimestamp,currentTimestamp);
            } else {
                // Default "symbol" and "price" logic
                String symbol = jsonNode.has("symbol") ? jsonNode.get("symbol").asText() : null;
                String price = jsonNode.has("price") ? jsonNode.get("price").asText() : null;
                if (symbol == null || price == null) {
                    System.err.println("Missing required fields: " + jsonNode.toString());
                    return;
                }
                sendToElastic(symbol, price, kafkaTimestamp,currentTimestamp);
            }
        } catch (Exception e) {
            System.err.println("Error while processing data: " + e.getMessage() + " | JSON: " + jsonNode.toString());
        }
    }
    private static void sendToElasticNvidiaStock(double currentPrice, double change, double percentChange,
                                                 double high, double low, double open, double prevClose, long timestamp, String currentTimestamp) {
        String index = "nvidia_stock";
        String elasticUrl = "http://172.18.0.6:9200/" + index + "/_doc";

        String jsonInputString = String.format(
                "{ \"currentPrice\": %.2f, \"change\": %.2f, \"percentChange\": %.4f, \"high\": %.2f, \"low\": %.2f, " +
                        "\"open\": %.2f, \"previousClose\": %.2f, \"kafkaTimestamp\": %d, \"currentTimestamp\": \"%s\" }",
                currentPrice, change, percentChange, high, low, open, prevClose, timestamp, currentTimestamp
        );
        sendToElasticsearch(elasticUrl, jsonInputString);
    }
    private static void sendToElastic(String symbol, String price, long timestamp, String currentTimestamp) {
        String index = symbol.contains("BTC") ? "btc" : symbol.contains("ETH") ? "eth" : "default_index";
        String elasticUrl = "http://172.18.0.6:9200/" + index + "/_doc";
        String jsonInputString = String.format(
                "{ \"symbol\": \"%s\", \"price\": \"%s\", \"kafkaTimestamp\": %d, \"currentTimestamp\": \"%s\" }",
                symbol, price, timestamp, currentTimestamp
        );

        sendToElasticsearch(elasticUrl, jsonInputString);
    }

    private static void sendToElasticsearch(String elasticUrl, String jsonInputString) {
        try {
            URL url = new URL(elasticUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonInputString.getBytes("utf-8"));
            }

            int responseCode = connection.getResponseCode();
            System.out.println("POST response to Elasticsearch: " + responseCode);
        } catch (Exception e) {
            System.err.println("Error sending data to Elasticsearch: " + e.getMessage());
        }
    }
}