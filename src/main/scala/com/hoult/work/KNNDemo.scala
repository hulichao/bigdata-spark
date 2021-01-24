package com.hoult.work

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object KNNDemo {
  def main(args: Array[String]): Unit = {

    //1.初始化
    val conf=new SparkConf().setAppName("SimpleKnn").setMaster("local[*]")
    val sc=new SparkContext(conf)
    val K=15

    //2.读取数据,封装数据
    val data: RDD[LabelPoint] = sc.textFile("data/lris.csv")
      .map(line => {
        val arr = line.split(",")
        if (arr.length == 6) {
          LabelPoint(arr.last, arr.init.map(_.toDouble))
        } else {
          println(arr.toBuffer)
          LabelPoint(" ", arr.map(_.toDouble))
        }
      })


    //3.过滤出样本数据和测试数据
    val sampleData=data.filter(_.label!=" ")
    val testData=data.filter(_.label==" ").map(_.point).collect()

    //4.求每一条测试数据与样本数据的距离
    testData.foreach(elem=>{
      val distance=sampleData.map(x=>(getDistance(elem,x.point),x.label))
      //获取距离最近的k个样本
      val minDistance=distance.sortBy(_._1).take(K)
      //取出这k个样本的label并且获取出现最多的label即为测试数据的label
      val labels=minDistance.map(_._2)
        .groupBy(x=>x)
        .mapValues(_.length)
        .toList
        .sortBy(_._2).reverse
        .take(1)
        .map(_._1)
      printf(s"${elem.toBuffer.mkString(",")},${labels.toBuffer.mkString(",")}")
      println()
    })
    sc.stop()

  }

  case class LabelPoint(label:String,point:Array[Double])

  import scala.math._

  def getDistance(x:Array[Double],y:Array[Double]):Double={
    sqrt(x.zip(y).map(z=>pow(z._1-z._2,2)).sum)
  }
}
