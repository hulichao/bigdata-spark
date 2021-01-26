package com.hoult.work

import org.apache.spark.sql.{DataFrame, SparkSession}

object DataExchange {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("DateSort")
      .master("local[*]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("warn")

    // 原数据
    val tab = List((1, "2019-03-04", "2020-02-03"),(2, "2020-04-05", "2020-08-04"),(3, "2019-10-09", "2020-06-11"))
    val df: DataFrame = spark.createDataFrame(tab).toDF("ID", "startdate", "enddate")

    val dateset: DataFrame = df.select("startdate").union(df.select("enddate"))
    dateset.createOrReplaceTempView("t")

    val result: DataFrame = spark.sql(
      """
        |select tmp.startdate, nvl(lead(tmp.startdate) over(partition by col order by tmp.startdate), startdate) enddate from
        |(select "1" col, startdate from t) tmp
        |""".stripMargin)

    result.show()
  }

}
