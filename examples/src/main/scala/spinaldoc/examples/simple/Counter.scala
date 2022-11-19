package spinaldoc.examples.simple

import spinal.core._

import scala.language.postfixOps

case class Counter(width: Int) extends Component {
  val io = new Bundle {
    val clear = in Bool()
    val value = out UInt(width bits)
  }

  val register = Reg(UInt(width bits)) init 0
  register := register + 1
  when(io.clear) {
    register := 0
  }
  io.value := register
}
// end case class Counter

object Counter extends App {
  SpinalVerilog(Counter(8))
}
