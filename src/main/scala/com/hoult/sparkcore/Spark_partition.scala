package com.hoult.sparkcore

import org.apache.spark.rdd.RDD
import org.apache.spark.{Partitioner, SparkConf, SparkContext}

object Spark_partition {
  def main(args: Array[String]): Unit = {
    val config: SparkConf = new SparkConf().setMaster("local[*]").setAppName("WordCount")
    val sc: SparkContext = new SparkContext(config)
    val listRDD: RDD[(String, Int)] = sc.makeRDD(List(("a", 1), ("b", 2), ("c", 3)))
    val partRDD: RDD[(String, Int)] = listRDD.partitionBy(new MyPartitionner(3))
    partRDD.saveAsTextFile("output")
  }

}

class MyPartitionner (partitions: Int) extends Partitioner {
  override def numPartitions: Int = {
    partitions
  }

  override def getPartition(key: Any): Int = {
    1
  }
}
