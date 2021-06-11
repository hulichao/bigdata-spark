package com.hoult.unittest

import com.hoult.spark.common.Father
import org.apache.spark.rdd.RDD

object Demo extends Father{
  def main(args: Array[String]): Unit = {
    val rdd: RDD[Int] = sc.makeRDD(Seq(1, 2, 3))
    val it: Iterator[Int] = rdd.collect().toList.iterator
  }
}
