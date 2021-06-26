package com.hoult.spark.stream

import com.hoult.spark.common.Mother

object FileStream extends Mother{

  def main(args: Array[String]): Unit = {

    //1.初始化Spark配置信息
    //2.初始化SparkStreamingContext

    //3.创建自定义receiver的Streaming
//    val lineStream = ssc.receiverStream(new CustomerReceiver("localhost", 9999))
    val lineStream = ssc.textFileStream("input")

    //4.将每一行数据做切分，形成一个个单词
    val wordStreams = lineStream.flatMap(_.split("\t"))

    //5.将单词映射成元组（word,1）
    val wordAndOneStreams = wordStreams.map((_, 1))

    //6.将相同的单词次数做统计
    val wordAndCountStreams = wordAndOneStreams.reduceByKey(_ + _)

    //7.打印
    wordAndCountStreams.print()

    //8.启动SparkStreamingContext
    ssc.start()
    ssc.awaitTermination()
  }
}

