package com.hoult.sparkcore

import java.util.regex.{Matcher, Pattern}

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object LogAnalysis {
  val ipVedioPattern =  Pattern.compile("""(\S*) .*/(\S*\.mp4) .*""")
  val timeFlowPattern =  Pattern.compile(""".* \[(.*)\] .* (200|206|304) (\d*) .*""")

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName).setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val N = 10

    // 读解析数据
    val logRDD = sc.textFile("data/cdn.txt")

    // 1 计算独立IP数
    logRDD.map(line => (line.split("\\s+")(0), 1))
      .reduceByKey(_+_)
      .sortBy(_._2, false)
      .take(N)
      .foreach(println)

    // 2、统计每个视频独立IP数
    // 方法一：
    println("统计每个视频独立IP数. 方法一：")
    val ipvideoRDD = logRDD.map(line => {
      val matcher: Matcher = ipVedioPattern.matcher(line)
      if (matcher.matches()) {
        val ip = matcher.group(1)
        val video =  matcher.group(2)
        ((video, ip), 1)
      } else (("", ""), 1)
    })

    ipvideoRDD.reduceByKey(_+_)
      .map{case ((video, ip), count) => (video, 1)}
      .reduceByKey(_+_)
      .sortBy(_._2, false)
      .take(N)
      .foreach(println)

//    // 方法二：
//    println("统计每个视频独立IP数. 方法二：")
//    val ipvideoRDD2 = logRDD.map(line => {
//      val matcher: Matcher = ipVedioPattern.matcher(line)
//      if (matcher.matches()) {
//        val ip = matcher.group(1)
//        val video =  matcher.group(2)
//        (video, ip)
//      } else ("", "")
//    })
//    ipvideoRDD2.groupByKey()
//      .mapValues(buffer => buffer.toList.distinct.length)
//      .sortBy(_._2, false)
//      .take(N)
//      .foreach(println)

    // 3、统计一天中每个小时的流量
    val timeFlowRDD: RDD[(String, Long)] = logRDD.map(line => {
      val matcher: Matcher = timeFlowPattern.matcher(line)
      if (matcher.matches()) {
        val time = matcher.group(1)
        val hour: String = time.split(":")(1)
        val flow: Long = matcher.group(3).toLong
        (hour, flow)
      } else ("", 0L)
    })

    println("统计一天中每个小时的流量:")
    timeFlowRDD.reduceByKey(_ + _)
      .sortBy(_._2, false)
      .take(N)
      .map(elem => (elem._1, elem._2 / 1024 / 1024 / 1024))
      .foreach(println)

    sc.stop()
  }
}