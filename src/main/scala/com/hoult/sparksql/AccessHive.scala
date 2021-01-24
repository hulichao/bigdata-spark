package com.hoult.sparksql

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object AccessHive {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Demo1")
      .master("local[*]")
      .enableHiveSupport()

      // Spark使用与Hive相同的约定写parquet数据
      .config("spark.sql.parquet.writeLegacyFormat", "true")
      .getOrCreate()
    val sc = spark.sparkContext
    sc.setLogLevel("warn")

//    spark.sql("show databases").show
    spark.sql("select * from ods.ods_log_event").show(false)
//    val df: DataFrame = spark.table("ods.ods_trade_product_info")
//    df.show()
//    df.write.mode(SaveMode.Append).saveAsTable("ods.ods_trade_product_info_backup")
//    spark.table("ods.ods_trade_product_info_backup").show

    spark.close()
  }
}