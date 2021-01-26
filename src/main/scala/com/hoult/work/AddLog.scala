package com.hoult.work

import org.apache.spark.sql.SparkSession

object AddLog {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getCanonicalName)
      .getOrCreate()
    val sc = spark.sparkContext

    val clickRDD = sc.textFile("data/click.log")
    val impRDD = sc.textFile("data/imp.log")

    val clickRes = clickRDD.map{line => {
      val arr = line.split("\\s+")
      val adid = arr(3).substring(arr(3).lastIndexOf("=") + 1)
      (adid, 1)
    }}.reduceByKey(_ + _)

    val impRes = impRDD.map { line =>
      val arr = line.split("\\s+")
      val adid = arr(3).substring(arr(3).lastIndexOf("=") + 1)
      (adid, 1)
    }.reduceByKey(_ + _)

    //保存到hdfs
    clickRes.fullOuterJoin(impRes)
      .map(x => x._1 + "," + x._2._1.getOrElse(0) + "," + x._2._2.getOrElse(0))
      .repartition(1)
//      .saveAsTextFile("hdfs://linux121:9000/data/")
      .saveAsTextFile("data/add_log")

    sc.stop()
  }
}
