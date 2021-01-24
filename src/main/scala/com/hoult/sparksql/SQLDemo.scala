package com.hoult.sparksql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SparkSession}

case class Info(id: String, tags: String)

object SQLDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName(this.getClass.getCanonicalName)
      .master("local[*]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("warn")
    import spark.implicits._

    // 准备数据
    val arr = Array("1 1,2,3", "2 2,3", "3 1,2")
    val rdd: RDD[Info] = spark.sparkContext.makeRDD(arr)
      .map { line =>
        val fields: Array[String] = line.split("\\s+")
        Info(fields(0), fields(1))
      }
    val ds: Dataset[Info] = spark.createDataset(rdd)
    ds.createOrReplaceTempView("t1")
    ds.show

    // 用SQL处理 - HQL
    spark.sql(
      """
        |select id, tag
        |  from t1
        |       lateral view explode(split(tags, ",")) t2 as tag
        |""".stripMargin
    ).show

    // SparkSQL
    spark.sql(
      """
        |select id, explode(split(tags, ",")) tag
        |  from t1
        |""".stripMargin
    ).show



    spark.close()
  }
}
