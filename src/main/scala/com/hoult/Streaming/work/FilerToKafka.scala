package com.hoult.Streaming.work

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object FilerToKafka {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val conf = new SparkConf().setAppName(this.getClass.getCanonicalName.init).setMaster("local[*]")
    val sc = new SparkContext(conf)

    // 定义 kafka producer参数
    val lines: RDD[String] = sc.textFile("data/sample.log")

    // 定义 kafka producer参数
    val prop = new Properties()
    prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "linux121:9092")
    prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])

    // 将读取到的数据发送到mytopic1
    lines.foreachPartition{iter =>
      // KafkaProducer
      val producer = new KafkaProducer[String, String](prop)
      iter.foreach{line =>
        val record = new ProducerRecord[String, String]("mytopic1", line)
        producer.send(record)
      }
      producer.close()
    }
  }
}
