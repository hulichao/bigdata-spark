package com.hoult.spark.stream

import com.hoult.spark.common.Mother
import org.apache.spark.streaming.dstream.DStream

object KafkaSource extends Mother {
  def main(args: Array[String]): Unit = {
    //1.初始化配置
    //2.初始化streaming
    //3.1通过监控端口创建Dstream,读到的数据为一行行
    //    val lineStreams = ssc.socketTextStream("localhost", 9999)
    //3.2 通过文件来创建Dstream
    val lineStreams: DStream[String] = ssc.textFileStream("input")

    val wordStreams: DStream[String] = lineStreams.flatMap(_.split("\\s+"))
    val wordAndOne: DStream[(String, Int)] = wordStreams.map((_, 1))
    val wordAndCount: DStream[(String, Int)] = wordAndOne.reduceByKey(_ + _)
    //4.打印
    wordAndCount.print()
    //5.启动，并等待输入
    ssc.start()
    ssc.awaitTermination()

  }
}
