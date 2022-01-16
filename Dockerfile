#
# Build stage
#
FROM maven:3.8-adoptopenjdk-11 AS build
COPY src /build/src
COPY pom.xml /build/
RUN mvn -f /build/pom.xml clean package -DskipTests

#
# Package stage
#
FROM adoptopenjdk/openjdk11:alpine
COPY --from=build /build/target/kafka-data-pipelines-1.0.jar /usr/local/app/kafka-data-pipelines.jar
ENTRYPOINT ["java","-jar","/usr/local/app/kafka-data-pipelines.jar"]
