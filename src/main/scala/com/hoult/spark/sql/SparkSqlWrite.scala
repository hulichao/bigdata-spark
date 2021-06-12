package com.hoult.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSqlWrite {
  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-SQl")
  val spark = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate()
  val sc = spark.sparkContext

  def main(args: Array[String]): Unit = {
    val filename = "data/students.json"
    val df: DataFrame = spark.read.format("json").load(filename)
    df.select("name").write.format("csv").mode("overwrite").save("data/output")
  }
}
