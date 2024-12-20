package org.example;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class KafkaElastic {

    public static void main(String[] args) throws Exception {
        // Set up Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Kafka Consumer properties
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.18.0.10:9093");
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-m9awd");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create Flink Kafka Consumer
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
                "my_topic",
                new SimpleStringSchema(),
                consumerProperties
        );

        // Kafka Producer properties
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.18.0.10:9092"); // Producer port

        // Initialize Flink Kafka Producer
        FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<>(
                "topic_2", // Target topic
                new SimpleStringSchema(), // Serialization schema
                producerProperties // Kafka producer properties
        );

        // Stream processing
        DataStream<String> stream = env.addSource(consumer);

        DataStream<String> processedStream = stream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String value, Context ctx, Collector<String> out) {
                if (value == null || value.isEmpty()) {
                    System.err.println("Received null or empty message");
                    return;
                }

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(value);

                    long kafkaTimestamp = ctx.timestamp() != null ? ctx.timestamp() : Instant.now().toEpochMilli();

                    String result = processJson(rootNode, kafkaTimestamp);
                    if (result != null) {
                        out.collect(result); // Collect processed data for the next step
                    }
                } catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage() + " | Message: " + value);
                }
            }

            private String processJson(JsonNode jsonNode, long kafkaTimestamp) {
                try {
                    String currentTimestamp = TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(kafkaTimestamp));

                    if (jsonNode.has("c") && jsonNode.has("h") && jsonNode.has("l") && jsonNode.has("pc")) {
                        double currentPrice = jsonNode.get("c").asDouble();
                        double change = jsonNode.get("d").asDouble();
                        double percentChange = jsonNode.get("dp").asDouble();
                        double high = jsonNode.get("h").asDouble();
                        double low = jsonNode.get("l").asDouble();
                        double open = jsonNode.get("o").asDouble();
                        double prevClose = jsonNode.get("pc").asDouble();

                        // Create NVIDIA stock data JSON
                        return String.format(
                                "{ \"currentPrice\": %.2f, \"change\": %.2f, \"percentChange\": %.4f, \"high\": %.2f, \"low\": %.2f, " +
                                        "\"open\": %.2f, \"previousClose\": %.2f, \"kafkaTimestamp\": %d, \"currentTimestamp\": \"%s\" }",
                                currentPrice, change, percentChange, high, low, open, prevClose, kafkaTimestamp, currentTimestamp
                        );
                    }else if (jsonNode.isArray()) {
                        StringBuilder resultBuilder = new StringBuilder();
                        resultBuilder.append("[");

                        boolean isFirst = true;
                        for (JsonNode node : jsonNode) {
                            if (node.has("symbol") && node.has("price")) {
                                String symbol = node.get("symbol").asText();
                                String price = node.get("price").asText();

                                if (symbol.isEmpty()) {
                                    System.err.println("Invalid symbol: empty value in node " + node.toString());
                                    continue;
                                }

                                double priceValue;
                                try {
                                    priceValue = Double.parseDouble(price);
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid price: " + price + " in node " + node.toString());
                                    continue;
                                }

                                // Append to result
                                if (!isFirst) {
                                    resultBuilder.append(",");
                                } else {
                                    isFirst = false;
                                }
                                resultBuilder.append(String.format(
                                        "{ \"symbol\": \"%s\", \"price\": %.2f, \"kafkaTimestamp\": %d, \"currentTimestamp\": \"%s\" }",
                                        symbol, priceValue, kafkaTimestamp, currentTimestamp
                                ));
                            } else {
                                System.err.println("Missing 'symbol' or 'price' in node: " + node.toString());
                            }
                        }
                        resultBuilder.append("]");
                        return resultBuilder.toString();
                    }
                    else {
                        System.err.println("Missing required fields: " + jsonNode.toString());
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("Error while processing JSON: " + e.getMessage());
                    return null;
                }
            }
        });

        // Add producer as a sink to the processed stream
        processedStream.addSink(producer);

        // Execute the Flink job
        env.execute("Flink Kafka to Kafka Job");
    }

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.of("UTC"));
}