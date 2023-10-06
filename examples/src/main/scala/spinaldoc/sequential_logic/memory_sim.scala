package spinaldoc.libraries.sequential_logic

import spinal.core._
import spinal.lib._
import spinal.core.sim._

import scala.language.postfixOps

case class MemoryExample() extends Component {
  val wordCount = 64
  val io = new Bundle {
    val address = in port UInt(log2Up(wordCount) bit)
    val i = in port Bits(8 bit)
    val o = out port Bits(8 bit)
    val we = in port Bool()
  }

  val mem = Mem(Bits(8 bit), wordCount=wordCount)
  io.o := mem(io.address)
  when(io.we) {
    mem(io.address) := io.i
  }
}
// end case class MemoryExample

object MemorySim extends App {
  SimConfig.withVcdWave.compile {
    val d = MemoryExample()
    // make memory accessible during simulation
    d.mem.simPublic()
    d
  }.doSim("example") { dut =>
    dut.io.we #= false
    dut.clockDomain.forkStimulus(10)
    dut.clockDomain.waitSampling(2)

    // do a write
    dut.io.we #= true
    dut.io.address #= 10
    dut.io.i #= 0xaf
    dut.clockDomain.waitSampling(2)
    // check written data is there
    assert(dut.mem.getBigInt(10) == 0xaf)

    dut.io.we #= false
    dut.clockDomain.waitSampling(1)

    // set some data in memory
    dut.mem.setBigInt(15, 0xfe)
    // do a read to check if it's there
    dut.io.address #= 15
    dut.clockDomain.waitSampling(1)
    assert(dut.io.o.toBigInt == 0xfe)
  }
}

