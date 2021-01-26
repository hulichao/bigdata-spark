
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

object JodTimeTest {

  def main(args: Array[String]): Unit = {

    import java.text.SimpleDateFormat
    import java.util.Locale
    val formatter = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z", Locale.ENGLISH)
    println(formatter.parse("27/Feb/2008:10:12:44 +0800").getHours)
//    println(res)
//    import org.joda.time.DateTime
//    val dateTime = new DateTime(2012, 12, 13, 18, 23, 55)
//    val str4 = dateTime.toString("EEEE dd MMMM, yyyy HH:mm:ssa");
//    println(str4)


    val  timePattern=".*(2017):([0-9]{2}):[0-9]{2}:[0-9]{2}.*".r


    val al timePattern(year, hour) = "15/Feb/2017:00:54:08 +0800"
    println(hour)
  }

}
