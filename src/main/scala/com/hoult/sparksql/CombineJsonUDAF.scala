package com.hoult.sparksql

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{Encoder, Encoders, SparkSession, TypedColumn}
import org.json4s._
import org.json4s.jackson.JsonMethods._

class CombineJsonUDAF extends Aggregator[String, String, String]{
  // 定义初值
  override def zero: String = ""

  // 分区内的数据合并
  override def reduce(buffer: String, input: String): String = {
    combineJsonStr(buffer, input)
  }

  // 分区间的数据合并
  override def merge(b1: String, b2: String): String = {
    combineJsonStr(b1, b2)

  }

  // 计算最终结果
  override def finish(reduction: String): String = {
    reduction
  }

  private def combineJsonStr(json1: String, json2: String): String = {

    def combineJson(src: JSONObject, add: JSONObject) = {
      val it: java.util.Iterator[String] = add.keySet().iterator()
      while (it.hasNext) {
        val key = it.next()
        val value = add.get(key)
        src.put(key, value)
      }

      src.toJSONString
    }

    val j1 = JSON.parseObject(json1)
    val j2 = JSON.parseObject(json2)
    if (j1 == null)
      return json2
    if (j2 == null)
      return json1
    combineJson(j1, j2)
  }

  // 定义编码器
  override def bufferEncoder: Encoder[String] = Encoders.STRING
  override def outputEncoder: Encoder[String] = Encoders.STRING
}

object TypeSafeUDAFTest{
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)
    val spark = SparkSession.builder()
      .appName(s"${this.getClass.getCanonicalName}")
      .master("local[*]")
      .getOrCreate()

    val jsons = Seq(
      """
        |{
        |               "name": "Mary",
        |               "age": 5
        |             }
        |""".stripMargin,
      """
        |{
        |     "name": "Mary2",
        |     "age": 10,
        |     "add": "beijing"
        | }
        |""".stripMargin
    )

    import spark.implicits._
    val ds = spark.createDataset(jsons)
    ds.show

    val rate: TypedColumn[String, String] = new CombineJsonUDAF().toColumn.name("c")
    ds.select(rate).show(false)

    spark.stop()
  }
}