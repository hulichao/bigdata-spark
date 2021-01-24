package com.hoult.sparkcore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 查找两两用户的共同好友
 */
object FindFriends {
  def main(args: Array[String]): Unit = {
    // 创建SparkContext
    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName.init).setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")

    val lines: RDD[String] = sc.textFile("file:///E:\\hu_devlope\\idea_workpace\\bigdata-spark\\data\\fields.dat")

    // 方法一：核心思想利用笛卡尔积求两两的好友，然后去除多余的数据
    val friendsRDD: RDD[(String, Array[String])] = lines.map { line =>
      val fields: Array[String] = line.split(",")
      val userId = fields(0).trim
      val friends: Array[String] = fields(1).trim.split("\\s+")
      (userId, friends)
    }

    friendsRDD.cartesian(friendsRDD)
      .filter { case ((id1, _), (id2, _)) => id1 < id2 }
      .map{case ((id1, friends1), (id2, friends2)) =>
        //((id1, id2), friends1.toSet & friends2.toSet)
        ((id1, id2), friends1.intersect(friends2).sorted.toBuffer)
      }.sortByKey()
       .collect().foreach(println)

    // 方法二：消除笛卡尔积，更高效。
    // 核心思想：将数据变形，找到两两的好友， 再执行数据的合并
    // 数组不做hashcode
    println("*****************************************************************")
    friendsRDD.flatMapValues(x => x.combinations(2))
        .map(x => (x._2.mkString(" & "), Set(x._1)))
      .map{case (k, v) => (v.mkString(" & "), Set(k))}
      .reduceByKey(_ | _)
      .sortByKey()
      .collect().foreach(println)

    // 备注：flatMapValues / combinations / 数据的变形 / reduceByKey / 集合的操作

    // 关闭SparkContext
    sc.stop()
  }
}