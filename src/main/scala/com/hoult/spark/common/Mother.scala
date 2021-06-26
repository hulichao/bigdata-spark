package com.hoult.spark.common

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

class Mother {
  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark")
//  val spark = SparkSession.builder().config(conf).getOrCreate()
//  val sc = spark.sparkContext
//  import spark.implicits._
  val ssc = new StreamingContext(conf, Seconds(5))
  ssc.sparkContext.setLogLevel("ERROR")

}
