package spinaldoc.libraries.flow

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

import scala.language.postfixOps

case class FlowExample() extends Component {
  val io = new Bundle {
    val request = slave(Flow(Bits(8 bit)))
    val answer = master(Flow(Bits(8 bit)))
  }
  val storage = Reg(Bits(8 bit))

  val fsm = new StateMachine {
    io.answer.setIdle()

    val idle: State = new State with EntryPoint {
      whenIsActive {
        when(io.request.valid) {
          storage := io.request.payload
          goto(sendEcho)
        }
      }
    }

    val sendEcho: State = new State {
      whenIsActive {
        io.answer.push(storage)
        goto(idle)
      }
    }
  }

  // This StateMachine behaves equivalently to
  // io.answer <-< io.request
}
// end FlowExample

object FlowExample extends App {
  SpinalVerilog(FlowExample())
}
