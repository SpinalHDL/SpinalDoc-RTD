package spinaldoc.libraries.sim

import spinal.core._
import spinal.core.sim._
import spinal.lib.misc.test.DualSimTracer

class Toplevel extends Component{
  val counter = out(Reg(UInt(16 bits))) init(0)
  counter := counter + 1
}

object Example extends App {
  val compiled = SimConfig.withFstWave.compile(new Toplevel())

  DualSimTracer(compiled, window = 10000, seed = 42){dut=>
    dut.clockDomain.forkStimulus(10)
    dut.clockDomain.onSamplings{
      val value = dut.counter.toInt

      if(value % 0x1000 == 0){
        println(f"Value=0x$value%x at ${simTime()}")
      }

      // Throw a simulation failure after 64K cycles
      if(value == 0xFFFF){
        simFailure()
      }
    }
  }
}
