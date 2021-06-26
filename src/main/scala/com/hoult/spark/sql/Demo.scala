package com.hoult.spark.sql

import com.hoult.spark.common.Father

import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
import org.apache.spark.sql.Row

object Demo extends Father {
  def main(args: Array[String]): Unit = {
    mapFilter
  }

  def mapFilter(): Unit = {

    // 创建RDD
    val arrayData = List(Array("12", "23", "44", "2"), Array("22", "33", "44", "55")).map(r => Row(r: _*))

    val arrayRDD = spark.sparkContext.parallelize(arrayData)

    // 创建DataFrame
    import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
    import org.apache.spark.sql.Row

    val arraySchema = new StructType()
      .add("a",StringType)
      .add("b",StringType)
      .add("c",StringType)
      .add("d",StringType)

    val arrayDF = spark.createDataFrame(arrayRDD, arraySchema)

    arrayDF.printSchema
    arrayDF.show()

  }

}
