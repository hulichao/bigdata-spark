package com.hoult.sparkcore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SuperWordCount1 {
  private val stopWords = "in on to from by a an the is are were was i we you your he his some any of as can it each".split("\\s+")
  private val punctuation = "[\\)\\.,:;'!\\?]"

  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "E:\\hu_devlope\\hadoop-2.9.2");

    // 创建SparkContext
    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName.init).setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val acc3 = sc.collectionAccumulator[String]("allWords")

    // RDD转换
    // 换为小写，去除标点符号(难)，去除停用词(难)
    val lines: RDD[String] = sc.textFile("file:///E:\\hu_devlope\\idea_workpace\\bigdata-spark\\data\\swc.dat")
    lines.flatMap(_.split("\\s+"))
      .map(_.toLowerCase)
      .map(_.replaceAll(punctuation, ""))
      .filter(word => !stopWords.contains(word) && word.trim.length>0)
      .map((_, 1))
      .reduceByKey(_+_)
      .sortBy(_._2, false)
      .collect.foreach(println)

    // 结果输出

    // 关闭SparkContext
    sc.stop()
  }
}