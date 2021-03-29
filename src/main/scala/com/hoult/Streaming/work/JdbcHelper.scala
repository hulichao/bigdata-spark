package com.hoult.Streaming.work

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.util.Properties

import com.hoult.structed.bean.BusInfo
import org.apache.spark.sql.ForeachWriter

class JdbcHelper extends ForeachWriter[BusInfo] {
  var conn: Connection = _
  var statement: PreparedStatement = _
  override def open(partitionId: Long, epochId: Long): Boolean = {
    if (conn == null) {
      conn = JdbcHelper.openConnection
    }
    true
  }

  override def process(value: BusInfo): Unit = {
    //把数据写入mysql表中
    val arr: Array[String] = value.lglat.split("_")
    val sql = "insert into car_gps(deployNum,plateNum,timeStr,lng,lat) values(?,?,?,?,?)"
    statement = conn.prepareStatement(sql)
    statement.setString(1, value.deployNum)
    statement.setString(2, value.plateNum)
    statement.setString(3, value.timeStr)
    statement.setString(4, arr(0))
    statement.setString(5, arr(1))
    statement.executeUpdate()
  }

  override def close(errorOrNull: Throwable): Unit = {
    if (null != conn) conn.close()
    if (null != statement) statement.close()
  }
}

object JdbcHelper {
  var conn: Connection = _
  val url = "jdbc:mysql://hadoop1:3306/test?useUnicode=true&characterEncoding=utf8"
  val username = "root"
  val password = "123456"
  def openConnection: Connection = {
    if (null == conn || conn.isClosed) {
      val p = new Properties
      Class.forName("com.mysql.jdbc.Driver")
      conn = DriverManager.getConnection(url, username, password)
    }
    conn
  }
}
