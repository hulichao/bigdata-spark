package com.hoult.spark.common

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

class Mother {
  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark")
  val spark = SparkSession.builder().config(conf).getOrCreate()
  val sc = spark.sparkContext
  import spark.implicits._

}
