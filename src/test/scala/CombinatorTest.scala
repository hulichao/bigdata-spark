object CombinatorTest {
  def main(args: Array[String]): Unit = {
    val arr = (1 to 5).toArray

    arr.combinations(2).foreach(x => println(x.toBuffer))
  }

}
