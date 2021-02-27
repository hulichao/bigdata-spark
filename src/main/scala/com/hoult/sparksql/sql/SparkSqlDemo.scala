package com.hoult.spark.sql
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._

object DataFrameDemo {
  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-SQl")
  val spark = SparkSession.builder().config(conf).getOrCreate()
  val sc = spark.sparkContext
  import spark.implicits._
  def main(args: Array[String]): Unit = {
    //~~~~~~~~~~~~~sql风格~~~~~~~~~~~~~
    val stuDF: DataFrame = spark.read.json("file:///Users/dxm/IdeaProjects/bigdata_spark/data/students.json")
//    stuDF.show()
//    stuDF.createGlobalTempView("student")
    stuDF.createTempView("student")

//    spark.sql("select * from student").show()
//    spark.sql("select age from student").show()
//    spark.sql("select avg(age) from global_temp.student").show()
//    spark.sql("use global_temp")//会有权限错误
//    spark.newSession().sql("select * from global_temp.student").show()


    //~~~~~~~~~~~~~~~算子风格~~~~~~~~~~~~~~
    stuDF.printSchema()
    stuDF.select(col("age") + 1).show
    stuDF.filter(col("age") > 31).show


    //~~~~~~~~~~rdd 转df 手动~~~~~~~~~~~~~~~~~~~
    val rdd = sc.makeRDD(List((1, "zhangsan", 20),(2, "lisi", 40),(1, "wangwu", 50)))
    val df: DataFrame = rdd.toDF("id","name2","age")
    df.printSchema()
    df.select(col("name2"),col("age")).show()

    //~~~~~~~~~~rdd 转df 样例类~~~~~~~~~~~~~~~~~~~
    val rdd2 = sc.makeRDD(List(("zhangsan", 20),("lisi", 30),("wangwu", 40)))
    val df2: DataFrame = rdd2.map(t => People(t._1, t._2)).toDF()
    df2.show()

    //~~~~~~~~~~df 转rdd 会失去People 变为row~~~~~~~~~~~~~~~~~~~
    val rdd3: RDD[Row] = df2.rdd
    rdd3.foreach(row => {
      println(row.getInt(1))
    })

    //~~~~~~~~~~ds~~~~~~~~~~~~~~~~~~~
    val ds: Dataset[People] = Seq(People("Andy", 32),People("Andd", 33)).toDS()
    ds.select("name").show

    //~~~~~~~~~~rdd 转为ds~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    val ds2: Dataset[People] = rdd2.map(t => People(t._1, t._2)).toDS()
    ds2.select(col("name")).show()

    //~~~~~~~~~~ds 转为rdd~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    val rdd4: RDD[People] = ds2.rdd

    //~~~~~~~~~~df 转为ds~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    df2.as[People]
    //~~~~~~~~~~ds 转为df~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    ds2.toDF()



    //释放
    spark.stop()
  }
}

case class People(name: String, age: Int)

