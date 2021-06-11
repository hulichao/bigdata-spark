package com.hoult.sparksql.udf

import org.apache.spark.SparkConf
import org.apache.spark.sql._
import org.apache.spark.sql.expressions.{Aggregator, MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, DoubleType, LongType, StructType}


object SparkUDAfDemo {
  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-SQl")
  val spark = SparkSession.builder().config(conf).getOrCreate()
  val sc = spark.sparkContext
  import spark.implicits._
  def main(args: Array[String]): Unit = {
    val stuDF: DataFrame = spark.read.json("data/students.json")
    //弱类型聚合函数
    spark.udf.register("avgAvg", new SparkUDAfFunction)
    stuDF.createTempView("student")
    spark.sql("select avgAvg(age) from student").show()

    //强类型聚合函数,转为查询列,注意这里必须是ds
    val udaf = new MyAgeAvgUDAFFunction
    val avgColumn: TypedColumn[UserData, Double] = udaf.toColumn.name("avgAge")
    stuDF.as[UserData].select(avgColumn).show()
    spark.stop()
  }
}

//声明用户自定义函数【强类型】
case class UserData (id: BigInt, name: String, age: BigInt)
case class AvgBuffer (var sum: BigInt, var count: Int)

class MyAgeAvgUDAFFunction extends Aggregator[UserData, AvgBuffer, Double] {//In buffer result
  //初始化
  override def zero: AvgBuffer = {
    AvgBuffer(0, 0)
  }

  /**
   * 聚合数据
   * @param b
   * @param a
   * @return
   */
  override def reduce(b: AvgBuffer, a: UserData): AvgBuffer = {
    b.count = b.count + 1
    b.sum = b.sum + a.age
    b
  }

  //合并merge
  override def merge(b1: AvgBuffer, b2: AvgBuffer): AvgBuffer = {
    b1.sum = b1.sum + b2.sum
    b1.count = b1.count + b2.count
    b1
  }

  //完成计算
  override def finish(reduction: AvgBuffer): Double = {
    reduction.sum.toDouble /reduction.count
  }

  override def bufferEncoder: Encoder[AvgBuffer] = Encoders.product //自定义转码

  override def outputEncoder: Encoder[Double] = Encoders.scalaDouble //原生转码

}
//声明用户自定义函数【弱类型】
//1.继承UserDefinedAggregateFunction
//2.实现方法
class SparkUDAfFunction extends UserDefinedAggregateFunction {

  //函数输入的数据结构
  override def inputSchema: StructType = {
    new StructType().add("age", LongType)
  }

  //计算时候的数据结构
  override def bufferSchema: StructType = {
    new StructType()
      .add("sum", LongType)
      .add("count", LongType)
  }

  //函数返回的数据类型
  override def dataType: DataType = DoubleType

  //函数是否稳定
  override def deterministic: Boolean = true

  //函数计算缓存区初始化
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = 0L //代表sum
    buffer(1) = 0L //代表count
  }

  //更新数据，根据输入的每一条数据更新缓存区
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    buffer(0) = buffer.getLong(0) + input.getLong(0)
    buffer(1) = buffer.getLong(1) + 1
  }

  //将多个结果的缓存区合在一起
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    buffer1(0) = buffer1.getLong(0) + buffer2.getLong(0)
    buffer1(1) = buffer1.getLong(1) + buffer2.getLong(1)
  }

  //最终的结果返回
  override def evaluate(buffer: Row): Any = {
    buffer(0).toString.toDouble/buffer(1).toString.toDouble
  }
}
