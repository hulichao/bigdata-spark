package com.hoult.work

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
 * 数据源：1.ip地址的访问日志 2.ip地址映射表
 * 两表关联，然后对结果分析出各城市的总访问量
 */
object FindIp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getCanonicalName)
      .getOrCreate()
    val sc = spark.sparkContext

    import spark.implicits._
    val ipLogsRDD = sc.textFile("data/http.log")
      .map(_.split("\\|")(1))


    val ipInfoRDD = sc.textFile("data/ip.dat").map {
      case line: String => {
        val strSplit: Array[String] = line.split("\\|")
        Ip(strSplit(0), strSplit(1), strSplit(7))
      }
    }

    //关联后的结果rdd
    ipLogsRDD.map{x =>
      val ips: Array[Ip] = ipInfoRDD.collect()
      for (elem <- ips) {
        if (elem.startIp < x && elem.endIp > x) {
          elem.address
        } else "NULL"
      }
    }.take(10).foreach(println)



  }
}

case class Ip(startIp: String, endIp: String, address: String)
