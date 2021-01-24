package com.hoult.sparkcore

import java.sql.{Connection, DriverManager, PreparedStatement}

object JDBCDemo {
  def main(args: Array[String]): Unit = {
    // 定义结果集Array[(String, Int)]
    val str = "hadoop spark java scala hbase hive sqoop hue tez atlas datax grinffin zk kafka"
    val result: Array[(String, Int)] = str.split("\\s+").zipWithIndex

    // 定义参数
    val url = "jdbc:mysql://linux123:3306/ebiz?useUnicode=true&characterEncoding=utf-8&useSSL=false"
    val username = "hive"
    val password = "12345678"

    // jdbc 保存数据
    var conn: Connection = null
    var stmt: PreparedStatement = null
    val sql = "insert into wordcount values (?, ?)"
    try {
      conn = DriverManager.getConnection(url, username, password)
      stmt = conn.prepareStatement(sql)
      result.foreach{case (k, v) =>
        stmt.setString(1, k)
        stmt.setInt(2, v)
        stmt.executeUpdate()
      }
    }catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (stmt != null) stmt.close()
      if (conn != null) conn.close()
    }
  }
}