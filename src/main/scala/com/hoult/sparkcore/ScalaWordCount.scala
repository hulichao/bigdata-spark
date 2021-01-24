package com.hoult.sparkcore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object ScalaWordCount {
  def main(args: Array[String]): Unit = {
    // 1、创建SparkContext
    val conf = new SparkConf().setAppName("WordCount").setMaster("local")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")

    // 2、读本地文件(集群运行：输入参数)
//    val lines: RDD[String] = sc.textFile(args(0))
    val lines: RDD[String] = sc.textFile("/wcinput/wc.txt")

    // 3、RDD转换
    val words: RDD[String] = lines.flatMap(line => line.split("\\s+"))
    val wordsMap: RDD[(String, Int)] = words.map(x => (x, 1))
    val result: RDD[(String, Int)] = wordsMap.reduceByKey(_ + _)

    // 4、输出
    result.foreach(println)

    // 5、关闭SparkContext
    sc.stop()

    // 6、打包，使用spark-submit提交集群运行
//    spark-submit --master local[*] --class com.hoult.sparkcore.WordCount \
//    original-bigdata-spark-1.0-SNAPSHOT.jar /wcinput/*

//    spark-submit --master yarn --class com.hoult.sparkcore.WordCount \
//    original-bigdata-spark-1.0-SNAPSHOT.jar /wcinput/*
  }
}