package com.github.mingyima.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.util.concurrent.RateLimiter
import org.apache.kafka.clients.producer.ProducerConfig.{BOOTSTRAP_SERVERS_CONFIG, KEY_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER_CLASS_CONFIG}
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.{Logger, LoggerFactory}

import java.util.{Date, Properties}

object Producer extends Runnable {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  implicit val objectMapper: ObjectMapper = new ObjectMapper()

  // thread stopper config
  @volatile var running: Boolean = true

  // producer config
  val properties: Properties = new Properties()
  val topic = "stock"
  val bootstrapServer = "127.0.0.1:9092"
  val keySerializer: String = classOf[StringSerializer].getName
  val valueSerializer = "org.springframework.kafka.support.serializer.JsonSerializer"

  def main(args: Array[String]): Unit = {
    properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
    properties.setProperty(KEY_SERIALIZER_CLASS_CONFIG, keySerializer)
    properties.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer)
    Producer.run()
  }

  override def run(): Unit = {
    val producer = new KafkaProducer[String, ObjectNode](properties)

    try {
      logger.info("Starting Kafka stork ticker data generator: ")

      val tickers = Seq(Ticker("AMZN", 1902), Ticker("MSFT", 107), Ticker("AAPL", 215))
      var counter = 1
      val rateLimiter = RateLimiter.create(10)

      Thread.sleep(1000)

      while (running) {
        rateLimiter.acquire(1)
        val record = new ProducerRecord[String, ObjectNode](topic, DataGenerator.createJsonNode(tickers))

        producer.send(record, new Callback() {
          override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
            if (exception == null) {
              logger.info("Received new metadata. \n" +
                "Topic: " + metadata.topic() + "\n" +
                "Partition: " + metadata.partition() + "\n" +
                "Offset: " + metadata.offset() + "\n" +
                "Timestamp: " + new Date(metadata.timestamp()) + "\n" +
                "Counter: " + (counter % 10) + "\n"
              )
            } else {
              logger.error("Error while producing", exception)
            }
          }
        })
        counter += 1
        producer.flush()
      }
    } catch {
      case e: InterruptedException => logger.error("Thread Interrupted", e)
      case e: Throwable => e.printStackTrace()
    } finally {
      producer.close()
    }
  }

  def terminate(): Unit = {
    running = false
  }
}
