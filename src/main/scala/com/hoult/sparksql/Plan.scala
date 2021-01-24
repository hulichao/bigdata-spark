package com.hoult.sparksql

import org.apache.spark.sql.{DataFrame, SparkSession}

object Plan {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Demo1")
      .master("local[*]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("warn")

    import spark.implicits._

    Seq((0, "zhansan", 10),
      (1, "lisi", 11),
      (2, "wangwu", 12)).toDF("id", "name", "age").createOrReplaceTempView("stu")

    Seq((0, "chinese", 80), (0, "math", 100), (0, "english", 98),
      (1, "chinese", 86), (1, "math", 97), (1, "english", 90),
      (2, "chinese", 90), (2, "math", 94), (2, "english", 88)
    ).toDF("id", "subject", "score").createOrReplaceTempView("score")

    val df: DataFrame = spark.sql(
      """
        |select sum(v), name
        |  from (select stu.id, 100 + 10 + score.score as v, name
        |          from stu join score
        |          where stu.id = score.id and stu.age >= 11) tmp
        |group by name
        |""".stripMargin)
    import org.apache.spark.sql.functions._
    df.select(rand()).show
//    println(df.queryExecution.hiveResultString())
//    println("+++++++++++++++++")
//    println(df.queryExecution)
//    df.show()

    val df1: DataFrame = spark.sql(
      """
        |select sum(v), name
        |  from (select stu.id, 100 + 10 + score.score as v, name
        |          from stu join score on stu.id = score.id where stu.age >= 11) tmp
        |group by name
        |""".stripMargin)
    println(df1.queryExecution)

    val df2: DataFrame = spark.sql(
      """
        |select sum(v), name
        |  from (select stu.id, 100 + 10 + score.score as v, name
        |          from stu join score on stu.id = score.id where stu.age >= 11) tmp
        |group by name
        |""".stripMargin)
    println(df2.queryExecution)
//    df1.show()

    // 打印执行计划
//    println(df.queryExecution)

    spark.close()
  }
}