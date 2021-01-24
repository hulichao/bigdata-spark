package com.hoult.sparksql

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{Row, SparkSession}

object Demo1 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Demo1")
      .master("local[*]")
      .getOrCreate()
    val sc = spark.sparkContext
    sc.setLogLevel("warn")

    import org.apache.spark.sql.functions._
    import spark.implicits._
    val df = List("1 2019-03-04 2020-02-03", "2 2020-04-05 2020-08-04", "3 2019-10-09 2020-06-11").toDF()
    val w1 = Window.orderBy($"value" asc).rowsBetween(0, 1)
    df.as[String]
      .map(str => str.split(" ")(1) + " " + str.split(" ")(2))
      .flatMap(str => str.split("\\s+"))
      .distinct()
      .sort($"value" asc)
      .withColumn("new", max("value") over (w1))
      .show()

    df.flatMap{ case Row(line: String) =>
      line.split("\\s+").tail
    }.toDF("date")
      .createOrReplaceTempView("t1")

    spark.sql("select date from t1").show
    spark.sql(
      """
        |select date, max(date) over (order by date rows between current row and 1 following) as date1
        |  from t1
        |""".stripMargin).show

    spark.close()
  }
}