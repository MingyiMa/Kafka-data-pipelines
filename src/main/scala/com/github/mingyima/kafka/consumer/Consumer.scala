package com.github.mingyima.kafka.consumer

import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.{Logger, LoggerFactory}

object Consumer {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  // consumer config
  val topic = "stock"

  def main(args: Array[String]): Unit = {
    val bootstrapServer = if (args.length != 0) args(0) else "127.0.0.1:9092"

    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getSimpleName)
      .master("local[2]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    logger.info("Starting Kafka Steam Reader: ")

    val raw = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", bootstrapServer)
      .option("subscribe", topic)
      .load()

    val query = transform(raw)
      .withWatermark("timestamp", "5 seconds")
      .groupBy(window(col("timestamp"), "30 seconds"), col("name"))
      .agg(round(avg(col("price")), 2) as "average price")
      .withColumn("timestamp", col("window.end"))
      .drop("window")
      .writeStream
      .trigger(Trigger.ProcessingTime(30000))
      .outputMode("update")
      .format("console")
      .option("truncate", "false")
      .start()

    query.awaitTermination()
  }

  def transform(df: DataFrame): DataFrame = {
    val schema = ArrayType(new StructType().add("name", StringType).add("price", IntegerType))

    df.selectExpr("CAST(value AS STRING)", "timestamp")
      .withColumn("tickers", flatten(map_values(from_json(col("value"), MapType(StringType, schema)))))
      .withColumn("ticker", explode(col("tickers")))
      .select(col("ticker.name") as "name", col("ticker.price") as "price", col("timestamp"))
  }
}
