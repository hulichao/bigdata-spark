package com.hoult.spark.sql

import java.util

import com.hoult.spark.common.Father
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ListBuffer

object Demo extends Father {
  def main(args: Array[String]): Unit = {
    mapFilter
  }

  def mapFilter(): Unit = {
    val data: RDD[String] = sc.makeRDD(List("12", "23", "44", "2", "5"))
    val mr: RDD[String] = data.mapPartitions(line => {
      var lt = ListBuffer[String]()
      println("-------line--------: " + line)
      for (l <- line if l != "12") {
        lt.append(l)
        println("-------l--------: " + l)
        println("-------lt--------: " + lt)
      }
      lt.iterator
    })
    mr.collect().foreach(println)

  }

}
