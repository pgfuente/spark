

/**
 * @author pagf
 */
object Ejercicio3 extends App {

  def generaTests(args: List[String]): List[(Int) => Boolean] = {
    var results: List[(Int) => Boolean] = List()
    for (i <- 0 to args.length / 2 - 1) {
      val items = args.slice(i * 2, 2 + i * 2)
      items(0) match {
        case ">" => results = results :+ ((x: Int) => x > items(1).toInt)
        case "<" => results = results :+ ((x: Int) => x < items(1).toInt)
        case "=" => results = results :+ ((x: Int) => x == items(1).toInt)
      }
    }
    results
  }

  val listaTests = generaTests(List(">", "3", "<", "5", ">", "8", "=", "10"))

   Ejercicio2.numTests(listaTests, 12)
   
   Ejercicio2.numTests(listaTests,10)
}