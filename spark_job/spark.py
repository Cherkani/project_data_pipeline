from pyspark.sql import SparkSession
from pyspark.sql.functions import from_json, col, expr
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, ArrayType
import requests

# Kafka and Elasticsearch Configuration
KAFKA_TOPIC = "topic_2"
KAFKA_BROKERS = "172.18.0.10:9093"
BTC_ELASTIC_URL = "http://172.18.0.6:9200/btc/_doc"
ETH_ELASTIC_URL = "http://172.18.0.6:9200/eth/_doc"
NVD_ELASTIC_URL = "http://172.18.0.6:9200/nvidia_stock/_doc"

# Schema Definitions
nvda_schema = StructType([
    StructField("currentPrice", DoubleType(), True),
    StructField("change", DoubleType(), True),
    StructField("percentChange", DoubleType(), True),
    StructField("high", DoubleType(), True),
    StructField("low", DoubleType(), True),
    StructField("open", DoubleType(), True),
    StructField("previousClose", DoubleType(), True),
    StructField("kafkaTimestamp", StringType(), True),
    StructField("currentTimestamp", StringType(), True)
])

crypto_schema = ArrayType(StructType([
    StructField("symbol", StringType(), True),
    StructField("price", DoubleType(), True),
    StructField("kafkaTimestamp", StringType(), True),
    StructField("currentTimestamp", StringType(), True)
]))

# Send data to Elasticsearch
def send_to_elastic(url, payload):
    try:
        response = requests.post(url, json=payload)
        if response.status_code in [200, 201]:
            print(f"Data sent to {url}: {payload}")
        else:
            print(f"Failed to send to {url}: {response.status_code} {response.text}")
    except Exception as e:
        print(f"Error sending data to {url}: {e}")

# Process Each Row
def process_row(row):
    try:
        if row.symbol:  # Crypto Data
            url = BTC_ELASTIC_URL if row.symbol == "BTCUSDT" else ETH_ELASTIC_URL
            send_to_elastic(url, row.asDict())
        elif row.currentPrice:  # NVIDIA Data
            send_to_elastic(NVD_ELASTIC_URL, row.asDict())
    except Exception as e:
        print(f"Error processing row: {e}")

# Main Function
def main():
    spark = SparkSession.builder \
        .appName("Spark Kafka Processor Single Message") \
        .master("local[*]") \
        .getOrCreate()

    kafka_stream = spark.readStream \
        .format("kafka") \
        .option("kafka.bootstrap.servers", KAFKA_BROKERS) \
        .option("subscribe", KAFKA_TOPIC) \
        .load()

    # Parse NVIDIA Data
    nvda_stream = kafka_stream.selectExpr("CAST(value AS STRING) as json_value") \
        .select(from_json(col("json_value"), nvda_schema).alias("data")) \
        .select("data.*")

    # Parse Crypto Data
    crypto_stream = kafka_stream.selectExpr("CAST(value AS STRING) as json_value") \
        .select(from_json(col("json_value"), crypto_schema).alias("data")) \
        .select(expr("inline(data)"))

    # Combine Both Streams
    unified_stream = nvda_stream.unionByName(crypto_stream, allowMissingColumns=True)

    # Process Each Row
    query = unified_stream.writeStream \
        .foreach(process_row) \
        .trigger(processingTime="0.1 seconds") \
        .start()

    query.awaitTermination()

if __name__ == "__main__":
    main()