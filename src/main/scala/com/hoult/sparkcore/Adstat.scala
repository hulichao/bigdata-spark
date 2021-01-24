package com.hoult.sparkcore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

// 数据格式：时间点			省份		城市	用户		广告
object Adstat {
  def main(args: Array[String]): Unit = {
    // 1、创建SparkContext
//    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName.init).setMaster("local[*]")
    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName.init).setMaster("spark://linux121:7077")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val N = 3

    // 2、生成RDD
    val lines: RDD[String] = sc.textFile("file:///E:\\hu_devlope\\idea_workpace\\bigdata-spark\\data\\advert.log")

    // 3、RDD转换
    // 1、统计每一个省份点击TOP3的广告ID
    val stat1RDD: RDD[(String, String)] = lines.map { line =>
      val fields: Array[String] = line.split("\\s+")
      (fields(1), fields(4))
    }

    // 按省份、广告汇总
    val reduce1RDD: RDD[((String, String), Int)] = stat1RDD.map { case (provice, adid) => ((provice, adid), 1) }
      .reduceByKey(_ + _)

    // 对以上汇总信息求Top3
    reduce1RDD.map{case ((provice, adid), count) => (provice, (adid, count))}
      .groupByKey()
      .mapValues(buf => buf.toList.sortWith(_._2 > _._2).take(N).map(_._1).mkString(":"))
      .foreach(println)

    // 2、统计每一个省份每一个小时的TOP3广告ID
    lines.map { line =>
      val fields: Array[String] = line.split("\\s+")
      ((getHour(fields(0)), fields(1), fields(4)), 1)
    }.reduceByKey(_+_)
      .map{case ((hour, provice, adid), count) => ((provice, hour), (adid, count))}
        .groupByKey()
      .mapValues(buf => buf.toList.sortWith(_._2 > _._2).take(N).map(_._1).mkString(":"))
      .collect.foreach(println)

    // 5、关闭SparkContext
    sc.stop()
  }

  def getHour(str: String): Int = {
    import org.joda.time.DateTime

    val dt = new DateTime(str.toLong)
    dt.getHourOfDay
  }
}