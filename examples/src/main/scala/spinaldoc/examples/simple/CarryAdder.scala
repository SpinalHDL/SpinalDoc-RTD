package spinaldoc.examples.simple

import spinal.core._

import scala.language.postfixOps

case class CarryAdder(size : Int) extends Component {
  val io = new Bundle {
    val a = in UInt(size bits)
    val b = in UInt(size bits)
    val result = out UInt(size bits)  // result = a + b
  }

  var c = False   // Carry, like a VHDL variable
  for (i <- 0 until size) {
    // Create some intermediate value in the loop scope.
    val a = io.a(i)
    val b = io.b(i)

    // The carry adder's asynchronous logic
    io.result(i) := a ^ b ^ c
    c \= (a & b) | (a & c) | (b & c); // variable assignment
  }
}

object CarryAdderProject extends App {
  SpinalVhdl(CarryAdder(4))
}