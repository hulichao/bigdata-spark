package com.hoult.sparkcore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object JoinDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName(this.getClass.getCanonicalName.init)
    val sc = new SparkContext(conf)
    // 设置本地文件切分大小
    sc.hadoopConfiguration.setLong("fs.local.block.size", 128*1024*1024)

    // map task：数据准备
    val productRDD: RDD[(String, String)] = sc.textFile("data/hoult_product_info.txt")
      .map { line =>
        val fields = line.split(";")
        (fields(0), line)
      }

    val orderRDD: RDD[(String, String)] = sc.textFile("data/orderinfo.txt",8 )
      .map { line =>
        val fields = line.split(";")
        (fields(2), line)
      }

    // join有shuffle操作
    val resultRDD: RDD[(String, (String, String))] = productRDD.join(orderRDD)

    println(resultRDD.count())

    Thread.sleep(1000000)

    sc.stop()
  }
}