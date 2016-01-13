package com.gmv.mcd.util

import org.apache.spark.mllib.linalg.{ Vectors, Vector }
import java.util.Date
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat
import org.joda.time.Days

/**
 * @author pagf
 */
object ParseUtils extends Serializable {

  val timepattern = "\\d{2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2},\\d{3}".r
  val timepattern2 = "\\w{3} \\w{3} \\d{2} \\d{2}:\\d{2}:\\d{2} CEST \\d{4}".r
  val transactionpattern = "\\[[\\d\\w-]{36}\\]".r
  val msisdnpattern = "34[6|7][\\d]{8}".r
  val nodepattern = "[a-z]+\\d+".r
  val numberpattern = "[\\d]+\\.*[\\d]+[\\s]*".r
  val literalpattern = "'[^\\[\\],]+?'".r
  val xmlvaluepattern = ">[^<> ]+<".r
  val operationpattern = "Executing operation (\\w)+".r
  val dateformatter = DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss,SSS")

  def vectorize(lines: Iterable[Iterable[String]], types: List[String]): Vector = {
    //FIXME val today = new Date()
    val today = dateformatter.parseDateTime("31 jul 2015 01:00:00,000")
    val vector = collection.mutable.Map[Int, Double]()
    for (t <- lines) {
      val date_str = timepattern.findFirstIn(t.mkString)
      //println("DATE_STR:" + date_str.get)
      val date = dateformatter.parseDateTime(date_str.get)

      for (s <- t) {
        val line = removeVariableData(s)
        val pos = types.indexOf(line) // ir acumulando las ocurrencias
        if (pos > 0) {
          //val decayFactor = Days.daysBetween(date, today).getDays + 1.0
          val decayFactor = 1
          vector(pos) = vector.getOrElse(pos, 0.0) + (1.0 / decayFactor) //decay factor
        }
      }
    }
    val result = Vectors.sparse(types.length, vector.keys.toArray, vector.values.toArray)
    result
  }

  def removeVariableData(s: String): String = {

    var line = timepattern.replaceAllIn(s, "XX:XX:XX")
    line = timepattern2.replaceAllIn(line, "XX:XX:XX")
    line = transactionpattern.replaceAllIn(line, "AA")
    line = xmlvaluepattern.replaceAllIn(line, ">TT<")
    line = numberpattern.replaceAllIn(line, "NN")
    line = literalpattern.replaceAllIn(line, "'LL'")
    if (line.length() > 80) {
      line = line.substring(0, 80)
    }
    line
  }
}