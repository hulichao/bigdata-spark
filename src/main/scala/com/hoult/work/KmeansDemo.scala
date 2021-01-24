package com.hoult.work

import org.apache.commons.lang3.math.NumberUtils
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer
import scala.math.{pow, sqrt}
import scala.util.Random

object KmeansDemo {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getCanonicalName)
      .getOrCreate()

    val sc = spark.sparkContext
    val dataset = spark.read.textFile("data/lris.csv")
      .rdd.map(_.split(",").filter(NumberUtils.isNumber _).map(_.toDouble))
      .filter(!_.isEmpty).map(_.toSeq)


    val res: RDD[(Seq[Double], Int)] = train(dataset, 3)

    res.sample(false, 0.1, 1234L)
      .map(tp => (tp._1.mkString(","), tp._2))
      .foreach(println)
  }

  // 定义一个方法 传入的参数是 数据集、K、最大迭代次数、代价函数变化阈值
  // 其中 最大迭代次数和代价函数变化阈值是设定了默认值，可以根据需要做相应更改
  def train(data: RDD[Seq[Double]], k: Int, maxIter: Int = 40, tol: Double = 1e-4) = {

    val sc: SparkContext = data.sparkContext

    var i = 0 // 迭代次数
    var cost = 0D //初始的代价函数
    var convergence = false   //判断收敛，即代价函数变化小于阈值tol

    // step1 :随机选取 k个初始聚类中心
    var initk: Array[(Seq[Double], Int)] = data.takeSample(false, k, Random.nextLong()).zip(Range(0, k))

    var res: RDD[(Seq[Double], Int)] = null

    while (i < maxIter && !convergence) {

      val bcCenters = sc.broadcast(initk)

      val centers: Array[(Seq[Double], Int)] = bcCenters.value

      val clustered: RDD[(Int, (Double, Seq[Double], Int))] = data.mapPartitions(points => {

        val listBuffer = new ListBuffer[(Int, (Double, Seq[Double], Int))]()

        // 计算每个样本点到各个聚类中心的距离
        points.foreach { point =>

          // 计算聚类id以及最小距离平方和、样本点、1
          val cost: (Int, (Double, Seq[Double], Int)) = centers.map(ct => {

            ct._2 -> (getDistance(ct._1.toArray, point.toArray), point, 1)

          }).minBy(_._2._1)  // 将该样本归属到最近的聚类中心
          listBuffer.append(cost)
        }

        listBuffer.toIterator
      })
      //
      val mpartition: Array[(Int, (Double, Seq[Double]))] = clustered
        .reduceByKey((a, b) => {
          val cost = a._1 + b._1   //代价函数
          val count = a._3 + b._3   // 每个类的样本数累加
          val newCenters = a._2.zip(b._2).map(tp => tp._1 + tp._2)    // 新的聚类中心点集
          (cost, newCenters, count)
        })
        .map {
          case (clusterId, (costs, point, count)) =>
            clusterId -> (costs, point.map(_ / count))   // 新的聚类中心
        }
        .collect()
      val newCost = mpartition.map(_._2._1).sum   // 代价函数
      convergence =  math.abs(newCost - cost) <= tol    // 判断收敛，即代价函数变化是否小于小于阈值tol
      // 变换新的代价函数
      cost = newCost
      // 变换初始聚类中心
      initk = mpartition.map(tp => (tp._2._2, tp._1))
      // 聚类结果 返回样本点以及所属类的id
      res = clustered.map(tp=>(tp._2._2,tp._1))
      i += 1
    }
    // 返回聚类结果
    res
  }

  def getDistance(x:Array[Double],y:Array[Double]):Double={
    sqrt(x.zip(y).map(z=>pow(z._1-z._2,2)).sum)
  }


}
