package com.github.mingyima.kafka

import com.github.mingyima.kafka.producer.{DataGenerator, Ticker}

class DataGeneratorTest extends SparkTest {
  val tickers = Seq(Ticker("AMZN", 1902), Ticker("MSFT", 107), Ticker("AAPL", 215))

  test("createJsonNode") {
    val output = DataGenerator.createJsonNode(tickers, enableRandom = false)

    output.toString shouldEqual
      s"""
         |{"tickers":[{"name":"AMZN","price":1902},{"name":"MSFT","price":107},{"name":"AAPL","price":215}]}
         |""".stripMargin.replaceAll("[\n|\\s+]", "")
  }
}
