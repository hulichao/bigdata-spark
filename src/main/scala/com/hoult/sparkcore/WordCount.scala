package com.hoult.sparkcore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    //1.创建SparkContext
//    val conf = new SparkConf().setAppName("WordCount") //打包上传使用这种
    val conf = new SparkConf().setAppName("WordCount").setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("error")

    //2. 读取本地文件
//    val linex: RDD[String] = sc.textFile(args(0)) //打包上传使用这种
    val linex: RDD[String] = sc.textFile("hdfs://linux121:9000/data/wc.txt")
//    val linex: RDD[String] = sc.textFile("data/wc.txt")

    //3. RDD转换
    val words: RDD[String] = linex.flatMap(line => line.split("\\s+"))
    val wordMap: RDD[(String, Int)] = words.map(x => (x, 1))
    val result: RDD[(String, Int)] = wordMap.reduceByKey(_ + _)

    //4.输出
    result.foreach(println)
    //5.关闭

    sc.stop()

    //6.打包 submit运行
  }

}
