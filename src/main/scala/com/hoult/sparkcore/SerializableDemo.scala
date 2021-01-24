package com.hoult.sparkcore

import org.apache.spark.{SparkConf, SparkContext}

class MyClass1(x: Int){
  val num: Int = x
}

case class MyClass2(num: Int)

class MyClass3(x: Int) extends Serializable {
  val num: Int = x
}

object SerializableDemo {
  def main(args: Array[String]): Unit = {
    // 初始化
    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName.init).setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")

    val o1 = new MyClass1(8)
//    println(s"o1.num = ${o1.num}")

    val rdd1 = sc.makeRDD(1 to 20)
    // 方法
    def add1(x: Int) = x + 100
    // 函数
    val add2 = add1 _

    // 函数、方法都具备序列化和反序列化的能力
//    rdd1.map(add1(_)).foreach(println)
//    println("****************************************************")
//    rdd1.map(add2(_)).foreach(println)

    val object1 = new MyClass1(20)
    val i = 20
     rdd1.map(x => object1.num + x).foreach(println)
//
//    // 解决方案一：使用case class
//    val object2 = MyClass2(20)
//    // rdd1.map(x => object2.num + x).foreach(println)
//
//    // 解决方案二：MyClass1 实现 Serializable 接口
//    val object3 = new MyClass3(20)
//    rdd1.map(x => object3.num + x).foreach(println)

    sc.stop()
  }
}