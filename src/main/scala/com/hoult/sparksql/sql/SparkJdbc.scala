package com.hoult.spark.sql

import java.util.Properties

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkJdbc {
  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-SQl")
//  val spark = SparkSession.builder().config(conf).getOrCreate()
val spark = SparkSession
  .builder()
  .master("local[*]")
  .appName("Spark Hive ")
  .config("hive.metastore.uris", "thrift://dev:8423")
        .config("spark.sql.warehouse.dir", "./")
        .enableHiveSupport()
  .getOrCreate()
  spark sql "show databases" show false
  import spark.implicits._

  def main(args: Array[String]): Unit = {
    val properties: Properties = new Properties()
    properties.put("user", "root")
    properties.put("password", "123456")
    val df: DataFrame = spark.read.json("data/students.json")
//    df.write.mode("append").jdbc("jdbc:mysql://localhost:3306/Test", "student", properties)
//    spark.read.jdbc("jdbc:mysql://dev:8806/Test", "student", properties).show()
  }
}
