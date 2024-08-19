package spinaldoc.examples.intermediate

import spinal.core._
import spinal.lib._

import scala.language.postfixOps

case class UartCtrlGenerics(dataWidthMax: Int = 8,
                            clockDividerWidth: Int = 20, // baudrate = Fclk / rxSamplePerBit / clockDividerWidth
                            preSamplingSize: Int = 1,
                            samplingSize: Int = 5,
                            postSamplingSize: Int = 2) {
  val rxSamplePerBit = preSamplingSize + samplingSize + postSamplingSize
  assert(isPow2(rxSamplePerBit))
  if((samplingSize % 2) == 0)
    SpinalWarning(s"It's not nice to have a odd samplingSize value (because of the majority vote)")
}
// end case class UartCtrlGenerics

case class Uart() extends Bundle with IMasterSlave {
  val txd = Bool()
  val rxd = Bool()

  override def asMaster(): Unit = {
    out(txd)
    in(rxd)
  }
}
// end case class Uart

// begin enums
object UartParityType extends SpinalEnum(binarySequential) {
  val NONE, EVEN, ODD = newElement()
}

object UartStopType extends SpinalEnum(binarySequential) {
  val ONE, TWO = newElement()
  def toBitCount(that: C): UInt = (that === ONE) ? U"0" | U"1"
}
// end enums

// begin internal bundles
case class UartCtrlFrameConfig(g: UartCtrlGenerics) extends Bundle {
  val dataLength = UInt(log2Up(g.dataWidthMax) bits) // Bit count = dataLength + 1
  val stop       = UartStopType()
  val parity     = UartParityType()
}

case class UartCtrlConfig(g: UartCtrlGenerics) extends Bundle {
  val frame        = UartCtrlFrameConfig(g)
  val clockDivider = UInt(g.clockDividerWidth bits) // see UartCtrlGenerics.clockDividerWidth for calculation

  def setClockDivider(baudrate: Double, clkFrequency: HertzNumber = ClockDomain.current.frequency.getValue): Unit = {
    clockDivider := (clkFrequency.toDouble / baudrate / g.rxSamplePerBit).toInt
  }
}
// end internal bundles

object UartCtrlTxState extends SpinalEnum {
  val IDLE, START, DATA, PARITY, STOP = newElement()
}
// end object UartCtrlTxState

class UartCtrlTx(g : UartCtrlGenerics) extends Component {
  import g._

  val io = new Bundle {
    val configFrame  = in(UartCtrlFrameConfig(g))
    val samplingTick = in Bool()
    val write        = slave Stream Bits(dataWidthMax bits)
    val txd          = out Bool()
  }

  // Provide one clockDivider.tick each rxSamplePerBit pulse of io.samplingTick
  // Used by the stateMachine as a baudrate time reference
  val clockDivider = new Area {
    val counter = Reg(UInt(log2Up(rxSamplePerBit) bits)) init 0
    val tick = False
    when(io.samplingTick) {
      counter := counter - 1
      tick := counter === 0
    }
  }

  // Count up each clockDivider.tick, used by the state machine to count up data bits and stop bits
  val tickCounter = new Area {
    val value = Reg(UInt(log2Up(Math.max(dataWidthMax, 2)) bits))
    def reset(): Unit = value := 0

    when(clockDivider.tick) {
      value := value + 1
    }
  }

  val stateMachine = new Area {
    import UartCtrlTxState._

    val state = RegInit(IDLE)
    val parity = Reg(Bool())
    val txd = True

    when(clockDivider.tick) {
      parity := parity ^ txd
    }

    io.write.ready := False
    switch(state) {
      is(IDLE) {
        when(io.write.valid && clockDivider.tick) {
          state := START
        }
      }
      is(START) {
        txd := False
        when(clockDivider.tick) {
          state := DATA
          parity := io.configFrame.parity === UartParityType.ODD
          tickCounter.reset()
        }
      }
      is(DATA) {
        txd := io.write.payload(tickCounter.value)
        when(clockDivider.tick) {
          when(tickCounter.value === io.configFrame.dataLength) {
            io.write.ready := True
            tickCounter.reset()
            when(io.configFrame.parity === UartParityType.NONE) {
              state := STOP
            } otherwise {
              state := PARITY
            }
          }
        }
      }
      is(PARITY) {
        txd := parity
        when(clockDivider.tick) {
          state := STOP
          tickCounter.reset()
        }
      }
      is(STOP) {
        when(clockDivider.tick) {
          when(tickCounter.value === UartStopType.toBitCount(io.configFrame.stop)) {
            state := io.write.valid ? START | IDLE
          }
        }
      }
    }
  }

  io.txd := RegNext(stateMachine.txd, True)
}
// end class UartCtrlTx

object UartCtrlRxState extends SpinalEnum {
  val IDLE, START, DATA, PARITY, STOP = newElement()
}
// end object UartCtrlRxState

class UartCtrlRx(g : UartCtrlGenerics) extends Component {
  import g._
  val io = new Bundle {
    val configFrame  = in(UartCtrlFrameConfig(g))
    val samplingTick = in Bool()
    val read         = master Flow Bits(dataWidthMax bits)
    val rxd          = in Bool()
  }

  // Implement the rxd sampling with a majority vote over samplingSize bits
  // Provide a new sampler.value each time sampler.tick is high
  val sampler = new Area {
    val synchronizer = BufferCC(io.rxd)
    val samples     = spinal.lib.History(that=synchronizer, when=io.samplingTick, length=samplingSize)
    val value       = RegNext(MajorityVote(samples))
    val tick        = RegNext(io.samplingTick)
  }

  // Provide a bitTimer.tick each rxSamplePerBit
  // reset() can be called to recenter the counter over a start bit.
  val bitTimer = new Area {
    val counter = Reg(UInt(log2Up(rxSamplePerBit) bits))
    def reset(): Unit = counter := preSamplingSize + (samplingSize - 1) / 2 - 1
    val tick = False
    when(sampler.tick) {
      counter := counter - 1
      tick := counter === 0
    }
  }

  // Provide bitCounter.value that count up each bitTimer.tick, Used by the state machine to count data bits and stop bits
  // reset() can be called to reset it to zero
  val bitCounter = new Area {
    val value = Reg(UInt(log2Up(Math.max(dataWidthMax, 2)) bits))
    def reset(): Unit = value := 0

    when(bitTimer.tick) {
      value := value + 1
    }
  }

  val stateMachine = new Area {
    import UartCtrlRxState._

    val state   = RegInit(IDLE)
    val parity  = Reg(Bool())
    val shifter = Reg(io.read.payload)

    // Parity calculation
    when(bitTimer.tick) {
      parity := parity ^ sampler.value
    }

    io.read.valid := False
    switch(state) {
      is(IDLE) {
        when(!sampler.value) {
          state := START
          bitTimer.reset()
          bitCounter.reset()
        }
      }
      is(START) {
        when(bitTimer.tick) {
          state := DATA
          bitCounter.reset()
          parity := io.configFrame.parity === UartParityType.ODD
          when(sampler.value) {
            state := IDLE
          }
        }
      }
      is(DATA) {
        when(bitTimer.tick) {
          shifter(bitCounter.value) := sampler.value
          when(bitCounter.value === io.configFrame.dataLength) {
            bitCounter.reset()
            when(io.configFrame.parity === UartParityType.NONE) {
              state := STOP
            } otherwise {
              state := PARITY
            }
          }
        }
      }
      is(PARITY) {
        when(bitTimer.tick) {
          state := STOP
          bitCounter.reset()
          when(parity =/= sampler.value) {
            state := IDLE
          }
        }
      }
      is(STOP) {
        when(bitTimer.tick) {
          when(!sampler.value) {
            state := IDLE
          }.elsewhen(bitCounter.value === UartStopType.toBitCount(io.configFrame.stop)) {
            state := IDLE
            io.read.valid := True
          }
        }
      }
    }
  }
  io.read.payload := stateMachine.shifter
}
// end class UartCtrlRx

class UartCtrl(g: UartCtrlGenerics=UartCtrlGenerics()) extends Component {
  val io = new Bundle {
    val config = in(UartCtrlConfig(g))
    val write  = slave(Stream(Bits(g.dataWidthMax bits)))
    val read   = master(Flow(Bits(g.dataWidthMax bits)))
    val uart   = master(Uart())
  }

  val tx = new UartCtrlTx(g)
  val rx = new UartCtrlRx(g)

  // Clock divider used by RX and TX
  val clockDivider = new Area {
    val counter = Reg(UInt(g.clockDividerWidth bits)) init 0
    val tick = counter === 0

    counter := counter - 1
    when(tick) {
      counter := io.config.clockDivider
    }
  }

  tx.io.samplingTick := clockDivider.tick
  rx.io.samplingTick := clockDivider.tick

  tx.io.configFrame := io.config.frame
  rx.io.configFrame := io.config.frame

  tx.io.write << io.write
  rx.io.read >> io.read

  io.uart.txd <> tx.io.txd
  io.uart.rxd <> rx.io.rxd
}
// end class UartCtrl

case class UartCtrlInitConfig(baudrate: Int = 0,
                              dataLength: Int = 1,
                              parity: UartParityType.E = null,
                              stop: UartStopType.E = null
                             ) {
  require(dataLength >= 1)
  def initReg(reg : UartCtrlConfig): Unit = {
    require(reg.isReg)
    if(baudrate != 0) reg.clockDivider init((ClockDomain.current.frequency.getValue / baudrate / reg.g.rxSamplePerBit).toInt-1)
    if(dataLength != 1) reg.frame.dataLength init (dataLength - 1)
    if(parity != null) reg.frame.parity init parity
    if(stop != null) reg.frame.stop init stop
  }
}

object UartCtrl {
  def apply(config: UartCtrlInitConfig, readonly: Boolean = false): UartCtrl = {
    val uartCtrl = new UartCtrl()
    uartCtrl.io.config.setClockDivider(config.baudrate)
    uartCtrl.io.config.frame.dataLength := config.dataLength - 1
    uartCtrl.io.config.frame.parity := config.parity
    uartCtrl.io.config.frame.stop := config.stop
    if (readonly) {
      uartCtrl.io.write.valid := False
      uartCtrl.io.write.payload := B(0)
    }
    uartCtrl
  }
}
// end object UartCtrl

case class UartRxTx() extends Component {
  val io = new Bundle {
    val uart = master(Uart())
    val write  = slave(Stream(Bits(8 bits)))
    val read   = master(Flow(Bits(8 bits)))
  }
  // start rxtx snippet
  val uartCtrl = UartCtrl(
    config=UartCtrlInitConfig(
      baudrate = 115200,
      dataLength = 8,
      parity = UartParityType.NONE,
      stop = UartStopType.ONE
    )
  )
  // end rxtx snippet
  io.uart <> uartCtrl.io.uart
  io.write <> uartCtrl.io.write
  io.read <> uartCtrl.io.read
}

case class UartTx() extends Component {
  val io = new Bundle {
    val tx = out Bool()
    val write  = slave(Stream(Bits(8 bits)))
  }

  val uartCtrl = UartCtrl(
    config=UartCtrlInitConfig(
      baudrate = 115200,
      dataLength = 8,
      parity = UartParityType.NONE,
      stop = UartStopType.ONE
    )
  )
  // start tx snippet
  uartCtrl.io.uart.rxd := True
  io.tx := uartCtrl.io.uart.txd
  // end tx snippet
  io.write <> uartCtrl.io.write
}

case class UartRx() extends Component {
  val io = new Bundle {
    val rx = in Bool()
    val read   = master(Flow(Bits(8 bits)))
  }
  // start rx snippet
  val uartCtrl = UartCtrl(
    config = UartCtrlInitConfig(
      baudrate = 115200,
      dataLength = 8,
      parity = UartParityType.NONE,
      stop = UartStopType.ONE
    ),
    readonly = true
  )
  // end rx snippet
  uartCtrl.io.uart.rxd := io.rx
  io.read <> uartCtrl.io.read
}

case class UartCtrlUsageExample() extends Component {
  val io = new Bundle {
    val uart = master(Uart())
    val switches = in Bits(8 bits)
    val leds = out Bits(8 bits)
  }

  val uartCtrl = new UartCtrl()
  // set config manually to show that this is still OK
  uartCtrl.io.config.setClockDivider(921600)
  uartCtrl.io.config.frame.dataLength := 7  // 8 bits
  uartCtrl.io.config.frame.parity := UartParityType.NONE
  uartCtrl.io.config.frame.stop := UartStopType.ONE
  uartCtrl.io.uart <> io.uart

  // Assign io.led with a register loaded each time a byte is received
  io.leds := uartCtrl.io.read.toReg()

  // Write the value of switch on the uart each 2000 cycles
  val write = Stream(Bits(8 bits))
  write.valid := CounterFreeRun(2000).willOverflow
  write.payload := io.switches
  write >-> uartCtrl.io.write
}

object UartCtrlUsageExample extends App {
  SpinalConfig(
    defaultClockDomainFrequency = FixedFrequency(100 MHz)
  ).generateVhdl(UartCtrlUsageExample())
}
// end UartCtrlUsageExample

case class UartQueued() extends Component {
  val g = UartCtrlGenerics()
  val io = new Bundle {
    val uart = master(Uart())
    val uartConfig = in(UartCtrlConfig(g))
    val rx = master(Stream(Bits(8 bit)))
    val stopIt = in Bool()
    val tx = slave(Stream(Bits(8 bit)))
  }

  val uartCtrl = new UartCtrl(g)
  uartCtrl.io.uart <> io.uart
  uartCtrl.io.config := io.uartConfig

  // start rx queue
  val queuedReads = uartCtrl.io.read.toStream.queue(16)
  // end rx queue
  queuedReads >> io.rx

  // start tx queue
  val writeCmd = Stream(Bits(8 bits))
  val stopIt = Bool()
  writeCmd.queue(16).haltWhen(stopIt) >> uartCtrl.io.write
  // end tx queue
  stopIt := io.stopIt
  io.tx >> writeCmd
}

case class UartWithHeader() extends Component {
  val io = new Bundle {
    val uart = master(Uart())
    val switches = in Bits(8 bits)
    val leds = out Bits(8 bits)
  }

  val uartCtrl = UartCtrl(
    config=UartCtrlInitConfig(
      baudrate = 115200,
      dataLength = 8,
      parity = UartParityType.NONE,
      stop = UartStopType.ONE
    )
  )
  io.uart <> uartCtrl.io.uart

  // Assign io.led with a register loaded each time a byte is received
  io.leds := uartCtrl.io.read.toReg()

  // start with header
  val write = Stream(Fragment(Bits(8 bits)))
  write.valid := CounterFreeRun(4000).willOverflow
  write.fragment := io.switches
  write.last := True
  write.stage().insertHeader(0x55).toStreamOfFragment >> uartCtrl.io.write
  // end with header
}

object Uart extends App {
  // Since we do internal calculation for dividers, etc. we need to
  // specify the frequency the design will be running at
  val config = SpinalConfig(
    defaultClockDomainFrequency = FixedFrequency(100 MHz)
  )
  config.generateVerilog(UartRxTx())
  config.generateVerilog(UartRx())
  config.generateVerilog(UartTx())
  config.generateVerilog(UartQueued())
  config.generateVerilog(UartWithHeader())
}
