package com.hoult.sparksql.udf

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkUdfDemo {
  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-SQl")
  val spark = SparkSession.builder().config(conf).getOrCreate()
  val sc = spark.sparkContext

  def main(args: Array[String]): Unit = {
    val stuDF: DataFrame = spark.read.json("data/students.json")
    spark.udf.register("addStr", (x: String) => "Name:" + x)
    stuDF.createTempView("student")
    spark.sql("select addStr(name) from student").show()

    spark.stop()
  }

}
