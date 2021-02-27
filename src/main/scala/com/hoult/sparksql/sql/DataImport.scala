package com.hoult.spark.sql

import scala.util.parsing.json.JSONObject

object DataImport {
  def main(args: Array[String]): Unit = {
    List()
    val list1: List[Any] = List("ALLEN", 300.0, 30, 7499, "1981-02-20 00:00:00", "SALESMAN", 7698, 1600.0)
    val list2: List[Any] = List("JONES", 300.0, 30, 7499, "1981-02-20 00:00:00", "SALESMAN", 7698, 1600.0)
    println(list1.zip(list2).toMap)

    val jsonString = JSONObject.formatted(list1.zip(list2).toMap.toString())
    println(jsonString)
  }

}
