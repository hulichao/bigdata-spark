package com.hoult.sparksql

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, DoubleType, StringType, StructType}
import org.apache.spark.sql.{Row, SparkSession}

class TypeUnsafeUDAF extends UserDefinedAggregateFunction {
  // 定义输入数据的类型
  override def inputSchema: StructType = new StructType().add("sales", DoubleType).add("saleDate", StringType)

  // 定义数据缓存的类型
  override def bufferSchema: StructType = new StructType().add("year2019", DoubleType).add("year2020", DoubleType)

  // 定义最终返回结果的类型
  override def dataType: DataType = DoubleType

  // 对于相同的结果是否有相同的输出
  override def deterministic: Boolean = true

  // 数据缓存的初始化
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer.update(0, 0.0)
    buffer.update(1, 0.0)
  }

  // 分区内数据合并
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    // 销售量、销售日期(year)
    val sales = input.getAs[Double](0)
    val saleYear = input.getAs[String](1).take(4)

    saleYear match{
      case "2019" => buffer(0) = buffer.getAs[Double](0) + sales
      case "2020" => buffer(1) = buffer.getAs[Double](1) + sales
      case _ => println("Error!")
    }
  }

  // 分区间数据合并
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    buffer1(0) = buffer1.getAs[Double](0) + buffer2.getAs[Double](0)
    buffer1(1) = buffer1.getAs[Double](1) + buffer2.getAs[Double](1)
  }

  // 计算最终的结果
  override def evaluate(buffer: Row): Double = {
    // (2020年的合计值 – 2019年的合计值) / 2019年的合计值
    if (math.abs(buffer.getAs[Double](0)) < 0.000000001) 0.0
    else (buffer.getAs[Double](1) - buffer.getAs[Double](0)) / buffer.getAs[Double](0)
  }
}

object TypeUnsafeUDAFTest{
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
    val userFunc = new TypeUnsafeUDAF
    spark.udf.register("userFunc", userFunc)
    spark.sql("select userFunc(sales, saleDate) as rate from sales").show()

    spark.stop()
  }
}