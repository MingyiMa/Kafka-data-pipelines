package com.github.mingyima.kafka

import org.apache.spark.sql.types._
import org.apache.spark.sql._

class ConsumerTest extends SparkTest {
  import spark.implicits._

  test("test producer terminate function & spark-kafka topic reading") {
    val thread = new Thread(Producer)
    thread.start()

    try {
      val df = spark.read
        .format("kafka")
        .option("kafka.bootstrap.servers", "127.0.0.1:9092")
        .option("subscribe", "stock")
        .option("startingOffsets", "earliest")
        .option("endingOffsets", "latest")
        .load()

      df.count > 0 shouldBe true
    } finally {
      Producer.terminate()
      thread.join()
    }
  }

  test("test consumer transform function") {
    val jsonString = """{"tickers":[{"name":"AAPL","price":198},{"name":"AMZN","price":2052},{"name":"MSFT","price":99}]}"""

    val schema = StructType(Seq(
      StructField("value", StringType, nullable = true),
      StructField("timestamp", TimestampType, nullable = true)
    ))

    val data = Seq(Row(jsonString, java.sql.Timestamp.valueOf("2021-01-15 21:00:00")))
    val df = spark.createDataFrame(spark.sparkContext.parallelize(data), schema)

    val result = Consumer.transform(df)

    val expected = Seq(
      ("AAPL", 198, java.sql.Timestamp.valueOf("2021-01-15 21:00:00")),
      ("AMZN", 2052, java.sql.Timestamp.valueOf("2021-01-15 21:00:00")),
      ("MSFT", 99, java.sql.Timestamp.valueOf("2021-01-15 21:00:00"))
    ).toDF("name", "price", "timestamp")

    result.collect should contain theSameElementsAs expected.collect
  }
}
