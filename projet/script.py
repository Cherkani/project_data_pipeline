from pyflink.datastream import StreamExecutionEnvironment
from pyflink.datastream.connectors.kafka import FlinkKafkaConsumer
from pyflink.common.serialization import SimpleStringSchema
import json

# Initialize the Stream Execution Environment
env = StreamExecutionEnvironment.get_execution_environment()

# Kafka Consumer Properties
kafka_props = {
    'bootstrap.servers': 'localhost:9092',  # Kafka Broker
    'group.id': 'flink-group'               # Consumer Group ID
}

# Create Kafka Consumer
kafka_consumer = FlinkKafkaConsumer(
    topics='flink-input-topic',            # Topic from NiFi
    deserialization_schema=SimpleStringSchema(),
    properties=kafka_props
)

# Define the Flink job to process the stream
stream = env.add_source(kafka_consumer)
stream.map(lambda x: f"Received from Kafka: {x}") \
      .print()

# Execute the Flink job
env.execute("Flink Kafka Consumer Job")
