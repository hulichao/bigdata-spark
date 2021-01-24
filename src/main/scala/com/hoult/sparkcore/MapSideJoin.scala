package com.hoult.sparkcore

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object MapSideJoin {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName(this.getClass.getCanonicalName.init)
    val sc = new SparkContext(conf)
    // 设置本地文件切分大小
    sc.hadoopConfiguration.setLong("fs.local.block.size", 128*1024*1024)

    // map task：数据准备
    val productMap: collection.Map[String, String] = sc.textFile("data/hoult_product_info.txt")
      .map { line =>
        val fields = line.split(";")
        (fields(0), line)
      }.collectAsMap()
    val productBC: Broadcast[collection.Map[String, String]] = sc.broadcast(productMap)

    val orderRDD: RDD[(String, String)] = sc.textFile("data/orderinfo.txt",8 )
      .map { line =>
        val fields = line.split(";")
        (fields(2), line)
      }

    // 完成map side join操作。
    // RDD[(String, (String, String))]：(pid, (商品信息，订单信息))
    val resultRDD: RDD[(String, (String, String))] = orderRDD.map { case (pid, orderInfo) =>
      val productInfoMap: collection.Map[String, String] = productBC.value
      val produceInfoString: String = productInfoMap.getOrElse(pid, null)
      (pid, (produceInfoString, orderInfo))
    }
    println(resultRDD.count())

    Thread.sleep(1000000)

    sc.stop()
  }
}