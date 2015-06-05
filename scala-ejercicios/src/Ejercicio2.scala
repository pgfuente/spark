

/**
 * @author pagf
 */
object Ejercicio2 extends App {

  def numTests(tests: List[(Int) => Boolean], value: Int): Int = {
    var result = 0
    tests.foreach(test => {
      if (test(value)) {
        result += 1
      }
    })
    result
  }

  def mayorQue8(x: Int): Boolean = {
    return x > 8
  }

  def par(x: Int): Boolean = {
    return x % 2 == 0
  }

  def impar(x: Int): Boolean = {
    return !par(x)
  }

  val listaTests = List(mayorQue8 _, par _ , impar _)

  numTests(listaTests, 12)

  numTests(listaTests, 3)

}