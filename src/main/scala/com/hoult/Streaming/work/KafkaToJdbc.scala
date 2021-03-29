package com.hoult.Streaming.work
import com.hoult.structed.bean.BusInfo
import org.apache.spark.sql.{Column, DataFrame, Dataset, SparkSession}

object KafkaToJdbc {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "root")
    //1 获取sparksession
    val spark: SparkSession = SparkSession.builder()
      .master("local[*]")
      .appName(KafkaToJdbc.getClass.getName)
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._
    //2 定义读取kafka数据源
    val kafkaDf: DataFrame = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "linux121:9092")
      .option("subscribe", "test_bus_info")
      .load()
    //3 处理数据
    val kafkaValDf: DataFrame = kafkaDf.selectExpr("CAST(value AS STRING)")
    //转为ds
    val kafkaDs: Dataset[String] = kafkaValDf.as[String]
    //解析出经纬度数据，写入redis
    //封装为一个case class方便后续获取指定字段的数据
    val busInfoDs: Dataset[BusInfo] = kafkaDs.map(BusInfo(_)).filter(_ != null)

    //将数据写入MySQL表
    busInfoDs.writeStream
      .foreach(new JdbcHelper)
      .outputMode("append")
      .start()
      .awaitTermination()
  }
}
