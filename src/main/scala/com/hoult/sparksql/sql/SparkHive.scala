package com.hoult.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkHive {
  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-SQl")
  val spark = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate()
  val sc = spark.sparkContext
  import spark.implicits._

  def main(args: Array[String]): Unit = {
    spark.sql("show databases")
  }
}
