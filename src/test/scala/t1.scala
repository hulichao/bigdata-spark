import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object t1 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("FileDStream").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val rdd1: RDD[Int] = sc.makeRDD(1 to 10)
    val rdd2: RDD[(Int, Int)] = sc.makeRDD((1 to 10).toList.zipWithIndex)

    val result1 = rdd1.countByValue() //可以
    val result2 = rdd1.countByKey() //语法错误

    val result3 = rdd2.countByValue() //可以
    val result4 = rdd2.countByKey() //可以
  }
}
