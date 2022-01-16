package com.github.mingyima.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

trait SparkTest extends AnyFunSuite with Matchers {
  implicit val objectMapper: ObjectMapper = new ObjectMapper()

  implicit val spark: SparkSession = SparkSession.builder()
    .appName(this.getClass.getSimpleName)
    .master("local[2]")
    .config("spark.sql.shuffle.partitions", 2)
    .config("spark.default.parallelism", 2)
    .appName("test")
    .getOrCreate()

  spark.sparkContext.setLogLevel("WARN")
}
