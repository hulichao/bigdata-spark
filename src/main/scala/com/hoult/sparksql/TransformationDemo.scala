package com.hoult.sparksql

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object TransformationDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Demo1")
      .master("local[*]")
      .getOrCreate()
    val sc = spark.sparkContext
    sc.setLogLevel("warn")

    import spark.implicits._
    val df1: DataFrame = spark.read
      .option("header", "true")
      .option("inferschema", "true")
      .csv("data/emp.dat")

//    df1.printSchema()

//    df1.map(row=>row.getAs[Int](0)).show

//    // randomSplit(与RDD类似，将DF、DS按给定参数分成多份)
//    val Array(dfx, dfy, dfz) = df1.randomSplit(Array(0.5, 0.6, 0.7))
//    dfx.count
//    dfy.count
//    dfz.count
//
//    // 取10行数据生成新的DataSet
//    val df2 = df1.limit(10)
//
//    // distinct，去重
//    val df3 = df1.union(df1)
//    df3.distinct.count
//
//    // dropDuplicates，按列值去重
//    df2.dropDuplicates.show
//    df2.dropDuplicates("mgr", "deptno").show
//    df2.dropDuplicates("mgr").show
//    df2.dropDuplicates("deptno").show
//
//    // 返回全部列的统计（count、mean、stddev、min、max）
//    df1.describe().show
//
//    // 返回指定列的统计
//    df1.describe("sal").show
//    df1.describe("sal", "comm").show
//
//    df1.createOrReplaceTempView("t1")
//    spark.sql("select * from t1").show
//    spark.catalog.cacheTable("t1")
//    spark.catalog.uncacheTable("t1")

    import org.apache.spark.sql.functions._
    df1.groupBy("Job").agg(min("sal").as("minsal"), max("sal").as("maxsal")).where($"minsal" > 2000).show

    val lst = List(StudentAge(1,"Alice", 18), StudentAge(2,"Andy", 19), StudentAge(3,"Bob", 17), StudentAge(4,"Justin", 21), StudentAge(5,"Cindy", 20))
    val ds1 = spark.createDataset(lst)
    ds1.show()

    // 定义第二个数据集
    val rdd = sc.makeRDD(List(StudentHeight("Alice", 160), StudentHeight("Andy", 159), StudentHeight("Bob", 170), StudentHeight("Cindy", 165), StudentHeight("Rose", 160)))
    val ds2 = rdd.toDS


    spark.close()
  }
}

case class StudentAge(sno: Int, sname: String, age: Int)
case class StudentHeight(sname: String, height: Int)