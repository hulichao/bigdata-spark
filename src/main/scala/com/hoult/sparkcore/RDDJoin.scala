package com.hoult.sparkcore

import org.apache.spark.{SparkConf, SparkContext}

object RDDJoin {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("RDD-Join")
    val sc = SparkContext.getOrCreate(conf)

    // ==================具体代码======================
    // 模拟数据产生
    val rdd1 = sc.parallelize(Array(
      (1, "张三1"),
      (1, "张三2"),
      (2, "李四"),
      (3, "王五"),
      (4, "Tom"),
      (5, "Gerry"),
      (6, "莉莉")
    ), 1)

    val rdd2 = sc.parallelize(Array(
      (1, "上海"),
      (2, "北京1"),
      (2, "北京2"),
      (3, "南京"),
      (4, "纽约"),
      (6, "深圳"),
      (7, "香港")
    ), 1)

    // 调用RDD API实现内连接
    val joinResultRDD = rdd1.join(rdd2).map {
      case (id, (name, address)) => {
        (id, name, address)
      }
    }
    println("----------------")
    joinResultRDD.foreachPartition(iter => {
      iter.foreach(println)
    })

    // 调用RDD API实现左外连接
    val leftJoinResultRDd = rdd1.leftOuterJoin(rdd2).map {
      case (id, (name, addressOption)) => {
        (id, name, addressOption.getOrElse("NULL"))
      }
    }
    println("----------------")
    leftJoinResultRDd.foreachPartition(iter => {
      iter.foreach(println)
    })
    // 左外连接稍微变化一下：需要左表出现，右表不出现的数据(not in)
    println("----------------")
    rdd1.leftOuterJoin(rdd2).filter(_._2._2.isEmpty).map {
      case (id, (name, _)) => (id, name)
    }.foreachPartition(iter => {
      iter.foreach(println)
    })

    // 右外连接
    println("----------------")
    rdd1
      .rightOuterJoin(rdd2)
      .map {
        case (id, (nameOption, address)) => {
          (id, nameOption.getOrElse("NULL"), address)
        }
      }
      .foreachPartition(iter => iter.foreach(println))

    // 全外连接
    println("----------------")
    rdd1
      .fullOuterJoin(rdd2)
      .map {
        case (id, (nameOption, addressOption)) => {
          (id, nameOption.getOrElse("NULL"), addressOption.getOrElse("NULL"))
        }
      }
      .foreachPartition(iter => iter.foreach(println))

    // 休眠为了看4040页面
    Thread.sleep(1000000)
  }
}
