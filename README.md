# Kafka-data-pipelines
### Step 1 - Download the git repo
```bash
git clone https://github.com/MingyiMa/Kafka-data-pipelines.git
```
### Step 2 - Run Docker-compose
```bash
docker-compose -f docker-compose.yml up &
```
### Step 3 - Create Kafka topic
```bash
docker exec -d kafka-data-pipelines-kafka-1 kafka-topics.sh --bootstrap-server kafka:9092 --topic stock --create --partitions 3 --replication-factor 1
```
### Step 4 - Start the producer 
```bash
docker exec -it kafka-data-pipelines-producer-1 java -cp /usr/local/app/kafka-data-pipelines.jar com.github.mingyima.kafka.producer.Producer kafka:9092
```
### Step 5 - Start the consumer
```bash
docker exec -it kafka-data-pipelines-consumer-1 bash /usr/local/app/spark/bin/spark-submit \
  --class com.github.mingyima.kafka.consumer.Consumer \
  --master local[2] \
  /usr/local/app/kafka-data-pipelines.jar \
  kafka:9092
```
### Step 6 - Clean up
```bash
docker stop kafka-data-pipelines-zookeeper-1 kafka-data-pipelines-kafka-1 kafka-data-pipelines-producer-1 kafka-data-pipelines-consumer-1
docker rm -v kafka-data-pipelines-zookeeper-1 kafka-data-pipelines-kafka-1 kafka-data-pipelines-producer-1 kafka-data-pipelines-consumer-1
docker image rm bitnami/kafka:latest bitnami/zookeeper:latest kafka-data-pipelines_consumer:latest kafka-data-pipelines_producer:latest
```