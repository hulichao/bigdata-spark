package com.hoult.sparksql.udf

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, DoubleType, StringType, StructType}
import org.apache.spark.sql.{Row, SparkSession}

import scala.util.Try

class JSONCombineUDAF extends UserDefinedAggregateFunction {
  // 定义输入数据的类型
  override def inputSchema: StructType = new StructType().add("jsonStr", StringType)
  // 定义数据缓存的类型
  override def bufferSchema: StructType = new StructType().add("jsonStrBuf", StringType)
  // 定义最终返回结果的类型
  override def dataType: DataType = StringType

  // 对于相同的结果是否有相同的输出
  override def deterministic: Boolean = true

  // 数据缓存的初始化
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer.update(0, "")
  }

  // 分区内数据合并
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    val jsonStr = input.get(0)
    jsonStr match{
      case x: String => buffer(0) = Try(combineJsonStr(buffer.getString(0), input.getString(0))).getOrElse(null)
      case _ => println("json combine Error!")
    }
  }

  // 分区间数据合并
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    buffer1(0) = combineJsonStr(buffer1.getString(0), buffer2.getString(0))
  }

  // 计算最终的结果
  override def evaluate(buffer: Row): String = {
    buffer.getString(0)
  }

  private def combineJsonStr(json1: String, json2: String): String = {

    val j1 = JSON.parseObject(json1)
    val j2 = JSON.parseObject(json2)
    if (j1 == null)
      return json2
    if (j2 == null)
      return json1
    combineJson(j1, j2)
  }

  private def combineJson(src: JSONObject, add: JSONObject) = {
    val it: java.util.Iterator[String] = add.keySet().iterator()
    while (it.hasNext) {
      val key = it.next()
      val value = add.get(key)
      src.put(key, value)
    }

    src.toJSONString
  }
}

object TypeUnsafeUDAFTest3{
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)
    val spark = SparkSession.builder()
      .appName(s"${this.getClass.getCanonicalName}")
      .master("local[*]")
      .getOrCreate()

    val sales = Seq(
      (1, "Widget Co",        1000.00, 0.00,    "AZ", "2019-01-02"),
      (2, "Acme Widgets",     1000.00, 500.00,  "CA", "2019-02-01"),
      (3, "Widgetry",         1000.00, 200.00,  "CA", "2020-01-11"),
      (4, "Widgets R Us",     2000.00, 0.0,     "CA", "2020-02-19"),
      (5, "Ye Olde Widgete",  3000.00, 0.0,     "MA", "2020-02-28"))
    val salesDF = spark.createDataFrame(sales).toDF("id", "name", "sales", "discount", "state", "saleDate")
    salesDF.createTempView("sales")
    val userFunc = new JSONCombineUDAF
    spark.udf.register("userFunc", userFunc)
    spark.sql("select userFunc(to_json(struct(*))) as rate from sales group by state").show(false)

    spark.stop()
  }
}