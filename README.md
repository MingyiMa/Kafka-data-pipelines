# Kafka-data-pipelines
### Step 1 - Download the git repo
```bash
git clone https://github.com/MingyiMa/Kafka-data-pipelines.git
cd Kafka-data-pipelines
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
* Producer sends 10 msgs per sec
  * msg data in JSON format like `{"tickers":[{"name":"AMZN","price":1902},{"name":"MSFT","price":107},{"name":"AAPL","price":215}]}`
  * each msg contains 1-3 stocks with +/-10% values
```bash
docker exec -it kafka-data-pipelines-producer-1 java -cp /usr/local/app/kafka-data-pipelines.jar com.github.mingyima.kafka.producer.Producer kafka:9092
```
### Step 5 - Start the consumer with a new terminal
* Consumer reads data from producer by spark streaming
  * display aggregated result every 30 sec
  * print the avg price per stock along with a timestamp
```bash
docker exec -it kafka-data-pipelines-consumer-1 bash /usr/local/app/spark/bin/spark-submit \
  --class "com.github.mingyima.kafka.consumer.Consumer" \
  --master "local[2]" \
  /usr/local/app/kafka-data-pipelines.jar \
  kafka:9092
```
### Step 6 - Clean up
```bash
docker stop kafka-data-pipelines-zookeeper-1 kafka-data-pipelines-kafka-1 kafka-data-pipelines-producer-1 kafka-data-pipelines-consumer-1
docker rm -v kafka-data-pipelines-zookeeper-1 kafka-data-pipelines-kafka-1 kafka-data-pipelines-producer-1 kafka-data-pipelines-consumer-1
docker volume rm kafka-data-pipelines_kafka_data kafka-data-pipelines_zookeeper_data
docker image rm bitnami/kafka:latest bitnami/zookeeper:latest kafka-data-pipelines_consumer:latest kafka-data-pipelines_producer:latest
```