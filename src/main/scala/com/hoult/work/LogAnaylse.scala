package com.hoult.work

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
 * 读取日志表到rdd
 * 拿到需要的字段：ip, 访问时间：小时即可, 视频名video_name (url中的xx.mp4),
 * 分析：
 * 1.计算独立IP数
 * 2.统计每个视频独立IP数（视频的标志：在日志文件的某些可以找到 *.mp4，代表一个视频文件）
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


    val cdnRDD = sc.textFile("data/cdn.txt")

    //计算独立ips
//    aloneIPs(cdnRDD.repartition(1))

    //每个视频独立ip数
//    videoIPs(cdnRDD.repartition(1))

    //每小时流量
    hourPoor(cdnRDD.repartition(1))
  }



  /**
   * 独立ip数
   */
  def aloneIPs(cdnRDD: RDD[String]) = {
    //匹配ip地址
    val IPPattern = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))".r

    val ipnums = cdnRDD
      .flatMap(x => (IPPattern findFirstIn x))
      .map(y => (y,1))
      .reduceByKey(_+_)
      .sortBy(_._2,false)

    ipnums.saveAsTextFile("data/cdn/aloneIPs")
  }

  /**
   * 视频独立ip数
   */
  def videoIPs(cdnRDD: RDD[String]) = {
    //匹配 http 响应码和请求数据大小
    val httpSizePattern = ".*\\s(200|206|304)\\s([0-9]+)\\s.*".r


    //[15/Feb/2017:11:17:13 +0800]  匹配 2017:11 按每小时播放量统计
    val timePattern = ".*(2017):([0-9]{2}):[0-9]{2}:[0-9]{2}.*".r

    import scala.util.matching.Regex

    // Entering paste mode (ctrl-D to finish)

    def isMatch(pattern: Regex, str: String) = {
      str match {
        case pattern(_*) => true
        case _ => false
      }
    }

    def getTimeAndSize(line: String) = {
      var res = ("", 0L)
      try {
        val httpSizePattern(code, size) = line
        val timePattern(year, hour) = line
        res = (hour, size.toLong)
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
      res
    }

    val IPPattern = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))".r

    val videoPattern = "([0-9]+).mp4".r

    val res = cdnRDD
      .filter(x => x.matches(".*([0-9]+)\\.mp4.*"))
      .map(x => (videoPattern findFirstIn x toString,IPPattern findFirstIn x toString))
      .aggregateByKey(List[String]())(
        (lst, str) => (lst :+ str),
        (lst1, lst2) => (lst1 ++ lst2)
      )
      .mapValues(_.distinct)
      .sortBy(_._2.size,false)

      res.saveAsTextFile("data/cdn/videoIPs")
  }

  /**
   * 一天中每个小时的流量
   *
   */
  def hourPoor(cdnRDD: RDD[String]) = {
    val httpSizePattern = ".*\\s(200|206|304)\\s([0-9]+)\\s.*".r
    val timePattern = ".*(2017):([0-9]{2}):[0-9]{2}:[0-9]{2}.*".r
    import scala.util.matching.Regex

    def isMatch(pattern: Regex, str: String) = {
      str match {
        case pattern(_*) => true
        case _ => false
      }
    }

    def getTimeAndSize(line: String) = {
      var res = ("", 0L)
      try {
        val httpSizePattern(code, size) = line
        val timePattern(year, hour) = line
        res = (hour, size.toLong)
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
      res
    }

    cdnRDD
      .filter(x=>isMatch(httpSizePattern,x))
      .filter(x=>isMatch(timePattern,x))
      .map(x=>getTimeAndSize(x))
      .reduceByKey(_ + _)
      .sortByKey()
      .map(x=>x._1+"时 CDN流量="+x._2/(102424*1024)+"G")
      .saveAsTextFile("data/cdn/hourPoor")
  }
}


