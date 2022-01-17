# Package stage
FROM adoptopenjdk/openjdk11:latest
COPY /target/kafka-data-pipelines-1.0-jar-with-dependencies.jar /usr/local/app/kafka-data-pipelines.jar

CMD java -cp /usr/local/app/kafka-data-pipelines.jar com.github.mingyima.kafka.producer.Producer kafka:9092
