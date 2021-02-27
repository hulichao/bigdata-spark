package com.hoult.sparkcore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark_Oper {
  def main(args: Array[String]): Unit = {
    val config: SparkConf = new SparkConf().setMaster("local[*]").setAppName("WordCount")
    val sc: SparkContext = new SparkContext(config)

    //算子mapPartions
//    val listRDD: RDD[Int] = sc.makeRDD(1 to 10,5)
    //    val result:  RDD[(Int, Int)] = listRDD.mapPartitionsWithIndex {
//      case (num, datas) => {
//        datas.map((_, num))
//      }
//    }
    //算子flatMap
//    val listRDD: RDD[Array[Int]] = sc.makeRDD(Array(Array(1, 2),Array(2, 3)))
//    val result: RDD[Int] = listRDD.flatMap(x => x)
//    result.collect().foreach(println)

    //glom 算子 将一个分区的数据放到一个数组中
//    val listRDD: RDD[Int] = sc.parallelize(1 to 12, 4)
//    val result: RDD[Array[Int]] = listRDD.glom()
//    for (elem <- result.collect()) {
//      println(elem.toList)
//    }

    //groupBy 算子，按照返回值来分,filter 算子
//    val listRDD: RDD[Int] = sc.parallelize(1 to 12, 4)
//    val result: RDD[(Int, Iterable[Int])] = listRDD.groupBy(i => i%2)
//    val result2: RDD[Int] = listRDD.filter(x => x%2 == 0)
//
//    result.collect().foreach(println)
//    result2.collect().foreach(println)
    //sample算子
    val listRDD: RDD[Int] = sc.parallelize(1 to 12, 4)
    val result: RDD[Int] = listRDD.sample(false, 0.4, 1)
    result.collect().foreach(println)

    //distinct算子
    listRDD.distinct(10)
    //coalesce 算子,缩小分区数,按原来分区来缩减
    listRDD.coalesce(10)


//    val list01 = List((1, "student01"), (2, "student02"), (3, "student03"))
//    val list02 = List((1, "teacher01"), (2, "teacher02"), (3, "teacher03"))
//    sc.parallelize(list01).join(sc.parallelize(list02)).foreach(println)

    val list01 = List((1, "a"),(1, "a"), (2, "b"), (3, "e"))
    val list02 = List((1, "A"), (2, "B"), (3, "E"))
    val list03 = List((1, "[ab]"), (2, "[bB]"), (3, "eE"),(3, "eE"))
    sc.parallelize(list01).cogroup(sc.parallelize(list02),sc.parallelize(list03)).foreach(println)
  }
}
