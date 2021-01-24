package com.hoult.sparksql

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.{Row, SparkSession}

object UDF {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName(this.getClass.getCanonicalName)
      .master("local[*]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    import spark.implicits._
    import spark.sql
    val data = List(("scala", "author1"), ("spark", "author2"),
      ("hadoop", "author3"), ("hive", "author4"),
      ("strom", "author5"), ("kafka", "author6"))
    val df = data.toDF("title", "author")
    df.createTempView("books")

    // 定义scala函数并注册
    def len1(str: String): Int = str.length
    spark.udf.register("len1", len1 _)

    // 使用udf，select、where子句
//    sql("select title, author, len1(title) as titleLength from books").show
//    sql("select title, author from books where len1(title)>5").show

    // DSL
//    df.filter("len1(title)>5").show

    // 如果要在DSL语法中使用$符号包裹字符串表示一个Column，需要用udf方法来接收函数。这种函数无需注册
    import org.apache.spark.sql.functions._

    val len2: UserDefinedFunction = udf(len1 _)
    df.select($"title", $"author", len2($"title")).show
    df.filter(len2($"title")>5).show

    // 不使用UDF
    df.map{case Row (title: String, author: String) => (title, author, title.length)}.show

    spark.stop()
  }
}