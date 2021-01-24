package com.hoult.sparkcore

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SuperWordCount2 {
  private val stopWords = "in on to from by a an the is are were was i we you your he his some any of as can it each".split("\\s+")
  private val punctuation = "[\\)\\.,:;'!\\?]"
  private val url = "jdbc:mysql://linux123:3306/ebiz?useUnicode=true&characterEncoding=utf-8&useSSL=false"
  private val username = "root"
  private val password = "123456"

  def main(args: Array[String]): Unit = {
    // 创建SparkContext
    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName.init).setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")

    // RDD转换
    // 换为小写，去除标点符号(难)，去除停用词(难)
    val lines: RDD[String] = sc.textFile("file:///E:\\hu_devlope\\idea_workpace\\bigdata-spark\\data\\swc.dat")
    val resultRDD: RDD[(String, Int)] = lines.flatMap(_.split("\\s+"))
      .map(_.toLowerCase)
      .map(_.replaceAll(punctuation, ""))
      .filter(word => !stopWords.contains(word) && word.trim.length > 0)
      .map((_, 1))
      .reduceByKey(_ + _)
      .sortBy(_._2, false)

    // 结果输出
    resultRDD.saveAsTextFile("file:///E:\\hu_devlope\\idea_workpace\\bigdata-spark\\data\\superwc")
    // 使用 foreach，对每条记录创建连接
    resultRDD.foreach{case (k, v) =>
      var conn: Connection = null
      var stmt: PreparedStatement = null
      val sql = "insert into wordcount values (?, ?)"
      try {
        conn = DriverManager.getConnection(url, username, password)
        stmt = conn.prepareStatement(sql)
        stmt.setString(1, k)
        stmt.setInt(2, v)
        stmt.executeUpdate()
      }catch {
        case e: Exception => e.printStackTrace()
      } finally {
        if (stmt != null) stmt.close()
        if (conn != null) conn.close()
      }
    }

    // 关闭SparkContext
    sc.stop()
  }
}