package spinaldoc.examples.advanced

import spinal.core._
import spinal.lib._
import spinal.lib.misc.{InterruptCtrl, Prescaler}
import spinal.lib.bus.misc.BusSlaveFactory
import spinal.lib.bus.amba3.apb.{Apb3, Apb3Config, Apb3SlaveFactory}

import scala.language.postfixOps

case class Timer(width : Int) extends Component {
  val io = new Bundle {
    val tick  = in Bool()
    val clear = in Bool()
    val limit = in UInt(width bits)

    val full  = out Bool()
    val value = out UInt(width bits)
  }

  val counter = Reg(UInt(width bits))
  when(io.tick && !io.full) {
    counter := counter + 1
  }
  when(io.clear) {
    counter := 0
  }

  io.full := counter === io.limit && io.tick
  io.value := counter

  // Timer bus interface
  // The function prototype uses Scala currying funcName(arg1,arg2)(arg3,arg3)
  // which allow to call the function with a nice syntax later
  // This function also returns an area, which allows to keep names of inner signals in the generated VHDL/Verilog.
  def driveFrom(busCtrl: BusSlaveFactory, baseAddress: BigInt)(ticks: Seq[Bool], clears: Seq[Bool]) = new Area {
    // Offset 0 => clear/tick masks + bus
    val ticksEnable = busCtrl.createReadAndWrite(Bits(ticks.length bits), baseAddress + 0,0) init(0)
    val clearsEnable = busCtrl.createReadAndWrite(Bits(clears.length bits), baseAddress + 0,16) init(0)
    val busClearing = False

    io.clear := (clearsEnable & clears.asBits).orR | busClearing
    io.tick := (ticksEnable  & ticks.asBits ).orR

    // Offset 4 => read/write limit (+ auto clear)
    busCtrl.driveAndRead(io.limit, baseAddress + 4)
    busClearing.setWhen(busCtrl.isWriting(baseAddress + 4))

    // Offset 8 => read timer value / write => clear timer value
    busCtrl.read(io.value, baseAddress + 8)
    busClearing.setWhen(busCtrl.isWriting(baseAddress + 8))
  }
}
// end case class Timer

case class ApbTimer() extends Component {
  val io = new Bundle {
    val apb = slave(Apb3(Apb3Config(addressWidth=8, dataWidth=32)))
    val interrupt = out Bool()
    val external = new Bundle {
      val tick = in Bool()
      val clear = in Bool()
    }
  }

  //Prescaler is very similar to the timer, it mainly integrates a piece of auto reload logic.
  val prescaler = Prescaler(width = 16)

  val timerA = Timer(width = 32)
  val timerB,timerC,timerD = Timer(width = 16)

  val busCtrl = Apb3SlaveFactory(io.apb)
  
  prescaler.driveFrom(busCtrl, 0x00)

  timerA.driveFrom(busCtrl, 0x40)(
    ticks=List(True, prescaler.io.overflow),
    clears=List(timerA.io.full)
  )
  timerB.driveFrom(busCtrl, 0x50)(
    ticks=List(True, prescaler.io.overflow, io.external.tick),
    clears=List(timerB.io.full, io.external.clear)
  )
  timerC.driveFrom(busCtrl, 0x60)(
    ticks=List(True, prescaler.io.overflow, io.external.tick),
    clears=List(timerC.io.full, io.external.clear)
  )
  timerD.driveFrom(busCtrl, 0x70)(
    ticks=List(True, prescaler.io.overflow, io.external.tick),
    clears=List(timerD.io.full, io.external.clear)
  )

  val interruptCtrl = InterruptCtrl(4)
  interruptCtrl.driveFrom(busCtrl, 0x10)
  interruptCtrl.io.inputs(0) := timerA.io.full
  interruptCtrl.io.inputs(1) := timerB.io.full
  interruptCtrl.io.inputs(2) := timerC.io.full
  interruptCtrl.io.inputs(3) := timerD.io.full
  io.interrupt := interruptCtrl.io.pendings.orR
}
// end case class ApbTimer

object ApbTimer extends App {
  SpinalVerilog(ApbTimer())
}