package com.hoult.Streaming.work

import java.util.Properties

import com.hoult.Streaming.kafka.OffsetsWithRedisUtils
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, HasOffsetRanges, KafkaUtils, LocationStrategies, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}


/**
 * 每秒处理Kafka数据，生成结构化数据，输入另外一个Kafka topic
 */
object KafkaStreamingETL {
  val log = Logger.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName).setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(5))

    // 需要消费的topic
    val topics: Array[String] = Array("mytopic1")
    val groupid = "mygroup1"
    // 定义kafka相关参数
    val kafkaParams: Map[String, Object] = getKafkaConsumerParameters(groupid)
    // 从Redis获取offset
    val fromOffsets = OffsetsWithRedisUtils.getOffsetsFromRedis(topics, groupid)

    // 创建DStream
    val dstream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent,
      // 从kafka中读取数据
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams, fromOffsets)
    )

    // 转换后的数据发送到另一个topic
    dstream.foreachRDD{rdd =>
      if (!rdd.isEmpty) {
        val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        rdd.foreachPartition(process)
        // 将offset保存到Redis
        OffsetsWithRedisUtils.saveOffsetsToRedis(offsetRanges, groupid)
      }
    }

    // 启动作业
    ssc.start()
    ssc.awaitTermination()
  }

  def process(iter: Iterator[ConsumerRecord[String, String]]) = {
    iter.map(line => parse(line.value))
      .filter(!_.isEmpty)
//      .foreach(println)
      .foreach(line =>sendMsg2Topic(line, "mytopic2"))
  }

  def parse(text: String): String = {
    try{
      val arr = text.replace("<<<!>>>", "").split(",")
      if (arr.length != 15) return ""
      arr.mkString("|")
    } catch {
      case e: Exception =>
        log.error("解析数据出错！", e)
        ""
    }
  }

  def getKafkaConsumerParameters(groupid: String): Map[String, Object] = {
    Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "linux121:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> groupid,
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean),
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest"
    )
  }

  def getKafkaProducerParameters(): Properties = {
    val prop = new Properties()
    prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "linux121:9092")
    prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    prop
  }

  def sendMsg2Topic(msg: String, topic: String): Unit = {
    val producer = new KafkaProducer[String, String](getKafkaProducerParameters())
    val record = new ProducerRecord[String, String](topic, msg)
    producer.send(record)
  }
}
