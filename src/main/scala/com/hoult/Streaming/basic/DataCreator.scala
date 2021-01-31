package com.hoult.Streaming.basic

import java.io.FileWriter

import scala.util.Random

object DataCreater {
  //初始化地址值，循环次数，数据
  private val datapath = "data/log/score.txt"
  private val max_records = 20
  private val brand = Array("手机", "笔记本", "小龙虾", "卫生纸", "吸尘器",  "苹果", "洗面奶", "保温杯")

  def Creater(): Unit ={

    val rand = new Random()
    val writer: FileWriter = new FileWriter(datapath,true)

    // create age of data
    for(i <- 1 to max_records){
      //电器名称
      var phonePlus = brand(rand.nextInt(5))
      //电器价格
      var price = rand.nextInt(999)+1000
      //数据拼接
      writer.write( phonePlus + "," + price)
      writer.write(System.getProperty("line.separator"))
    }
    writer.flush()
    writer.close()
  }
  def main(args: Array[String]): Unit = {
    Creater()
    System.exit(1)
  }
}
