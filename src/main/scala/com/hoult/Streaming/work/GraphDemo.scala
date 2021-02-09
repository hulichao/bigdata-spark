package com.hoult.Streaming.work

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD

object GraphDemo {
  def main(args: Array[String]): Unit = {
    // 初始化
    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName.init).setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("warn")

    //初始化数据
    val vertexArray: Array[(Long, String)] = Array((1L, "SFO"), (2L, "ORD"), (3L, "DFW"))
    val edgeArray: Array[Edge[Int]] = Array(
      Edge(1L, 2L, 1800),
      Edge(2L, 3L, 800),
      Edge(3L, 1L, 1400)
    )

    //构造vertexRDD和edgeRDD
    val vertexRDD: RDD[(VertexId, String)] = sc.makeRDD(vertexArray)
    val edgeRDD: RDD[Edge[Int]] = sc.makeRDD(edgeArray)

    //构造图
    val graph: Graph[String, Int] = Graph(vertexRDD, edgeRDD)

    //所有的顶点
    graph.vertices.foreach(println)

    //所有的边
    graph.edges.foreach(println)

    //所有的triplets
    graph.triplets.foreach(println)

    //求顶点数
    val vertexCnt = graph.vertices.count()
    println(s"顶点数：$vertexCnt")

    //求边数
    val edgeCnt = graph.edges.count()
    println(s"边数：$edgeCnt")

    //机场距离大于1000的
    graph.edges.filter(_.attr > 1000).foreach(println)

    //按所有机场之间的距离排序（降序）
    graph.edges.sortBy(-_.attr).collect().foreach(println)
  }
}
