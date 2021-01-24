package com.hoult.sparksql

import org.apache.spark.sql.SparkSession

object Demo2 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("test")
      .master("local[*]")
      .getOrCreate()


    val df1 = spark.read.csv("data/people1.csv")
    df1.printSchema()
    df1.show()
    val df2 = spark.read.csv("data/people2.csv")
    df2.printSchema()
    df2.show()
    // 指定参数
    // spark 2.3.0
    val schema = "name string, age int, job string"
    val df3 = spark.read
      .options(Map(("delimiter", ";"), ("header", "true")))
      .schema(schema)
      .csv("data/people2.csv")
    df3.printSchema()
    df3.show
    // 自动类型推断
    val df4 = spark.read
      .option("delimiter", ";")
      .option("header", "true")
      .option("inferschema", "true")
      .csv("data/people2.csv")
    df4.printSchema()
    df4.show
  }
}
