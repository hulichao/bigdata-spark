package com.hoult.work

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
 * 读取日志表到rdd
 * 拿到需要的字段：ip, 访问时间：date_time, 视频名video_name (url中的xx.mp4),
 * 分析：
 * 1.计算独立IP数
 *
 * 2.统计每个视频独立IP数（视频的标志：在日志文件的某些可以找到 *.mp4，代表一个视频文件）
 *
 * 3.统计一天中每个小时的流量
 */
object LogAnaylse {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getCanonicalName)
      .getOrCreate()
    val sc = spark.sparkContext

    import spark.implicits._
    val ipLogsRDD = sc.textFile("data/cdn.txt")
      .map(line => {
        val strs: Array[String] = line.split("\\s+")
        strs(0), strs(3).
      })
      .map()
      .take(10).foreach(println)


  }
}

case class Log(ip: String, dateTime: String, videoName: String)

