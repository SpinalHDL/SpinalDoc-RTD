package spinaldoc.examples.simple

import spinal.core._
import spinal.lib.CounterFreeRun

import scala.language.postfixOps

case class RgbToGray() extends Component {
  val io = new Bundle{
    val clear = in Bool()
    val r,g,b = in UInt(8 bits)

    val wr = out Bool()
    val address = out UInt(16 bits)
    val data = out UInt(8 bits)
  }

  def scaled(value : UInt,by : Float): UInt = value * U((255*by).toInt,8 bits) >> 8

  val gray = RegNext(
    scaled(io.r,0.3f) +
      scaled(io.g,0.4f) +
      scaled(io.b,0.3f)
  )

  val address = CounterFreeRun(stateCount = 1 << 16)
  io.address := address
  io.wr := True
  io.data := gray

  when(io.clear){
    gray := 0
    address.clear()
    io.wr := False
  }
}
// end case class RgbToGray

object RgbToGray extends App {
  SpinalVerilog(RgbToGray())
}