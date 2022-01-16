package com.github.mingyima.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import scala.util.Random

case class Ticker(
 name: String,
 price: java.lang.Integer
)

object DataGenerator {
  def createJsonNode(tickers: Seq[Ticker], enableRandom: Boolean = true)(implicit objectMapper: ObjectMapper): ObjectNode = {
    val updatedTickers = if (enableRandom) Random.shuffle(tickers).take(Random.between(1, tickers.size + 1))
      .map(ticker => Ticker(ticker.name, Random.between(ticker.price * 0.9, ticker.price * 1.1).toInt)) else tickers

    val jsonNode = objectMapper.createObjectNode()

    jsonNode.set("tickers", {
      val arrayNode = objectMapper.createArrayNode()

      updatedTickers.foreach(ticker => {
        val node = objectMapper.createObjectNode()
        node.put("name", ticker.name)
        node.put("price", ticker.price)
        arrayNode.add(node)
      })

      arrayNode
    })

    jsonNode
  }
}
