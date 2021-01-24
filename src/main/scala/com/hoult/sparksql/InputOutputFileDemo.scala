package com.hoult.sparksql

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object InputOutputFileDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Demo1")
      .master("local[*]")
      .getOrCreate()
    val sc = spark.sparkContext
    sc.setLogLevel("warn")

    // parquet
    import spark._
    val df1: DataFrame = spark.read.load("data/users.parquet")
    df1.createOrReplaceTempView("t1")
//    df1.show
//
//    sql(
//      """
//        |create or replace temporary view users
//        | using parquet
//        | options (path "data/users.parquet")
//        |""".stripMargin)
//
//    sql(
//      """
//        |select * from users
//        |""".stripMargin).show

//    df1.write
//        .mode("overwrite")
//        .save("data/parquet")

    // json
//    val df3: DataFrame = spark.read.format("json").load("data/emp.json")
//    df3.show()
//
//    sql(
//      """
//        |create or replace temporary view emp
//        | using json
//        |options (path "data/emp.json")
//        |""".stripMargin)
//    sql(
//      """
//        |select * from emp
//        |""".stripMargin).write
//        .format("json")
//        .mode("overwrite")
//        .save("data/json")

    // csv
//    val df2 = spark.read.format("csv")
//      .option("header", "true")
//      .option("inferschema", "true")
//      .load("data/people1.csv")
//    df2.show()
//
    sql(
      """
        |create or replace temporary view people
        | using csv
        |options (path "data/people1.csv",
        |         header "true",
        |         inferschema "true")
        |""".stripMargin)
//
//    sql("select * from people").write
//        .format("csv")
//        .mode("overwrite")
//        .save("data/csv")

    // jdbc
    val jdbcDF: DataFrame = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://hadoop-mysql:3306/ebiz?useSSL=false")
      .option("user", "root")
      .option("password", "123456")
      .option("driver", "com.mysql.jdbc.Driver")
      .option("dbtable", "hoult_product_info")
      .load()
    jdbcDF.show()
//
//    jdbcDF.write
//        .format("jdbc")
//      .option("url", "jdbc:mysql://linux123:8806/ebiz?useSSL=false&characterEncoding=utf8")
//      .option("user", "hive")
//      .option("password", "12345678")
//      .option("driver", "com.mysql.jdbc.Driver")
//      .option("dbtable", "hoult_product_info_back")
//        .mode(SaveMode.Append)
//        .save()

    spark.close()
  }
}