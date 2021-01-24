package com.hoult.sparksql

import org.apache.spark.sql.{DataFrame, SparkSession}

object ActionDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Demo1")
      .master("local[*]")
      .getOrCreate()
    val sc = spark.sparkContext
    sc.setLogLevel("warn")
    val df: DataFrame = spark.read
      .option("header", "true")
      .option("inferschema", "true")
      .csv("data/emp.dat")
    df.printSchema()

    df.toJSON.show(false)
    val h1 = df.head()
    println(h1)

    spark.close()
  }
}