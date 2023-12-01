package spinaldoc.libraries.flow

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.sim.{FlowDriver, FlowMonitor, ScoreboardInOrder}

import scala.language.postfixOps

case class SomeDUT() extends Component {
  val io = new Bundle {
    val input = slave(Flow(UInt(8 bit)))
    val output = master(Flow(UInt(8 bit)))
  }
  io.output <-< io.input
}

object Example extends App {
  val dut = SimConfig.withWave.compile(SomeDUT())

  dut.doSim("simple test") { dut =>
    SimTimeout(10000)

    val scoreboard = ScoreboardInOrder[Int]()

    // drive random data at random intervals, and add inputted data to scoreboard
    FlowDriver(dut.io.input, dut.clockDomain) { payload =>
      payload.randomize()
      true
    }
    FlowMonitor(dut.io.input, dut.clockDomain) { payload =>
      scoreboard.pushRef(payload.toInt)
    }

    // add all data coming out of DUT to scoreboard
    FlowMonitor(dut.io.output, dut.clockDomain) { payload =>
      scoreboard.pushDut(payload.toInt)
    }

    dut.clockDomain.forkStimulus(10)
    dut.clockDomain.waitActiveEdgeWhere(scoreboard.matches == 100)
  }
}