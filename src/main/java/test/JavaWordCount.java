package test;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class JavaWordCount {
    public static void main(String[] args) {
        // 1 创建 JavaSparkContext
        SparkConf conf = new SparkConf().setAppName("test.JavaWordCount").setMaster("local[*]");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        jsc.setLogLevel("warn");

        // 2 生成RDD
//        JavaRDD<String> lines = jsc.textFile("file:///E:\\hu_devlope\\idea_workpace\\bigdata-spark\\data\\wc.txt");
        JavaRDD<String> lines = jsc.textFile("data\\wc.txt");

        // 3 RDD转换
        JavaRDD<String> words = lines.flatMap(line -> Arrays.stream(line.split("\\s+")).iterator());
        JavaPairRDD<String, Integer> wordsMap = words.mapToPair(word -> new Tuple2<>(word, 1));
        JavaPairRDD<String, Integer> results = wordsMap.reduceByKey((x, y) -> x + y);

        // 4 结果输出
//        results.foreach(System.out::println); //输出没有序列化
        results.foreach(x -> System.out.println(x));

        // 5 关闭SparkContext
        jsc.stop();
    }
}