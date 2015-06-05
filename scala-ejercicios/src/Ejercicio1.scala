

/**
 * @author pagf
 */
object Ejercicio1 extends App {

  def aplica3D[T](list: List[(T, T, T)], func: (T) => T, coord: String): List[(T, T, T)] = {

    if (list.length == 1) {
      coord match {
        case "x" => List((func(list(0)._1), list(0)._2, list(0)._3))
        case "y" => List((list(0)._1, func(list(0)._2), list(0)._3))
        case "z" => List((list(0)._1, list(0)._2, func(list(0)._3)))
      }
    } else {
      aplica3D(list.take(1), func, coord) ::: aplica3D(list.drop(1), func, coord)
    }
  }

  val lista = List((1, 2, 3), (4, 5, 6), (7, 8, 9), (10, 11, 12))

  def suma(x: Int) = x + 2

  aplica3D(lista, suma, "y")

}