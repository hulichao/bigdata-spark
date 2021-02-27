package com.hoult.sparkcore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark_RDD {
  def main(args: Array[String]): Unit = {
    val config: SparkConf = new SparkConf().setMaster("local[*]").setAppName("WordCount")
    val sc: SparkContext = new SparkContext(config)
    //list创建
//    val listRDD: RDD[Int] = sc.makeRDD(List(1,2,3,4), 10)
//    listRDD.collect().foreach(println)
    //内存创建
//    val arrayRDD: RDD[Int] = sc.parallelize(Array(1,2,6,4))

//    println(arrayRDD.partitions.size) // 默认四核
//    arrayRDD.collect().foreach(println)

    //默认地址是当前项目，也可以是其他地址hdfs:
    //默认从文件中读取的都是字符串类型
    // 读取文件，传递的分区参数是最小分区数，但是不一定是这个分区数，取决于hadoop分片规则
    val fileRDD: RDD[String] = sc.textFile("in", 4) //默认两核
    println("核：" + fileRDD.partitions.size)
    fileRDD.collect().foreach(println)

    //将RDD数据保存到文件中
    fileRDD.saveAsTextFile("output")
  }
}
