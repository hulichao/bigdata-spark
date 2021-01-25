package com.hoult.work

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
 * 数据源：1.ip地址的访问日志 2.ip地址映射表
 * 需要把映射表广播，地址转换为long类型进行比较
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


    val brIPInfo = sc.broadcast(ipInfoRDD.map(x => (ip2Long(x.startIp), ip2Long(x.endIp), x.address))collect())

    //关联后的结果rdd
    ipLogsRDD
      .map(x => {
        val index  = binarySearch(brIPInfo.value, ip2Long(x))
        if (index != -1 )
          brIPInfo.value(index)._3
        else
          "NULL"
      }).map(x => (x, 1))
      .reduceByKey(_ + _)
      .map(x => s"城市：${x._1}, 访问量：${x._2}")
      .repartition(1)
      .saveAsTextFile("data/work/output_ips")

  }

  //ip转成long类型
  def ip2Long(ip: String): Long = {
    val fragments = ip.split("[.]")
    var ipNum = 0L
    for (i <- 0 until fragments.length) {
      ipNum = fragments(i).toLong | ipNum << 8L
    }
    ipNum
  }

  //二分法匹配ip规则
  def binarySearch(lines: Array[(Long, Long, String)], ip: Long): Int = {
    var low = 0
    var high = lines.length - 1
    while (low <= high) {
      val middle = (low + high) / 2
      if ((ip >= lines(middle)._1) && (ip <= lines(middle)._2))
        return middle
      if (ip < lines(middle)._1)
        high = middle - 1
      else {
        low = middle + 1
      }
    }
    -1
  }

}

case class Ip(startIp: String, endIp: String, address: String)
