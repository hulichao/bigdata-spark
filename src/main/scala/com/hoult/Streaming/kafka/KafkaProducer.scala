package com.hoult.Streaming.kafka

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

object KafkaProducer {
  def main(args: Array[String]): Unit = {
    // 定义 kafka 参数
    val brokers = "linux121:9092,linux122:9092,linux123:9092"
    val topic1 = "hoult_topic01"
    val topic2 = "hoult_topic02"
    val prop = new Properties()

    prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])

    // KafkaProducer
    val producer = new KafkaProducer[String, String](prop)

    for (i <- 1 to 1000000){
      val msg1 = new ProducerRecord[String, String](topic1, i.toString, i.toString)
      val msg2 = new ProducerRecord[String, String](topic2, i.toString, i.toString)
      // 发送消息
      producer.send(msg1)
      producer.send(msg2)
      producer.send(msg2)

      println(s"i = $i")
      Thread.sleep(100)
    }

    producer.close()
  }
}