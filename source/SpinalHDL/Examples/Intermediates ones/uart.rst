.. role:: raw-html-m2r(raw)
   :format: html

.. _example_uart:

UART
====

Specification
-------------

This UART controller tutorial is based on `this <https://github.com/SpinalHDL/SpinalHDL/tree/master/lib/src/main/scala/spinal/lib/com/uart>`_ implementation.

This implementation is characterized by:


* ClockDivider/Parity/StopBit/DataLength configs are set by the component inputs.
* RXD input is filtered by using a sampling window of N samples and a majority vote.


Interfaces of this UartCtrl are:

.. list-table::
   :header-rows: 1
   :widths: 1 1 10

   * - Name
     - Type
     - Description
   * - config
     - UartCtrlConfig
     - Give all configurations to the controller
   * - write
     - Stream[Bits]
     - Port used by the system to give transmission order to the controller
   * - read
     - Flow[Bits]
     - Port used by the controller to notify the system about a successfully received frame
   * - uart
     - Uart
     - Uart interface with rxd / txd


Data structures
---------------

Before implementing the controller itself we need to define some data structures.

Controller construction parameters
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. list-table::
   :header-rows: 1
   :widths: 1 1 20

   * - Name
     - Type
     - Description
   * - dataWidthMax
     - Int
     - Maximum number of data bits that could be sent using a single UART frame
   * - clockDividerWidth
     - Int
     - Number of bits that the clock divider has
   * - preSamplingSize
     - Int
     - Number of samples to drop at the beginning of the sampling window
   * - samplingSize
     - Int
     - Number of samples use at the middle of the window to get the filtered RXD value
   * - postSamplingSize
     - Int
     - Number of samples to drop at the end of the sampling window


To make the implementation easier let's assume that ``preSamplingSize + samplingSize + postSamplingSize`` is always a power of two.

Instead of adding each construction parameters (generics) to ``UartCtrl`` one by one, we can group them inside a class that will be used as single parameter of ``UartCtrl``.

.. code-block:: scala

   case class UartCtrlGenerics( dataWidthMax: Int = 8,
                                clockDividerWidth: Int = 20, // baudrate = Fclk / rxSamplePerBit / clockDividerWidth
                                preSamplingSize: Int = 1,
                                samplingSize: Int = 5,
                                postSamplingSize: Int = 2) {
     val rxSamplePerBit = preSamplingSize + samplingSize + postSamplingSize
     assert(isPow2(rxSamplePerBit))
     if ((samplingSize % 2) == 0)
       SpinalWarning(s"It's not nice to have a odd samplingSize value (because of the majority vote)")
   }

UART bus
^^^^^^^^

Let's define a UART bus without flow control.

.. code-block:: scala

   case class Uart() extends Bundle with IMasterSlave {
     val txd = Bool
     val rxd = Bool

     override def asMaster(): Unit = {
       out(txd)
       in(rxd)
     }
   }

UART configuration enums
^^^^^^^^^^^^^^^^^^^^^^^^

Let's define parity and stop bit enumerations.

.. code-block:: scala

   object UartParityType extends SpinalEnum(sequancial) {
     val NONE, EVEN, ODD = newElement()
   }

   object UartStopType extends SpinalEnum(sequancial) {
     val ONE, TWO = newElement()
     def toBitCount(that : T) : UInt = (that === ONE) ? U"0" | U"1"
   }

UartCtrl configuration Bundles
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Let's define ``Bundle``\ s that will be used as IO elements to setup ``UartCtrl``.

.. code-block:: scala

   case class UartCtrlFrameConfig(g: UartCtrlGenerics) extends Bundle {
     val dataLength = UInt(log2Up(g.dataWidthMax) bit) //Bit count = dataLength + 1
     val stop       = UartStopType()
     val parity     = UartParityType()
   }

   case class UartCtrlConfig(g: UartCtrlGenerics) extends Bundle {
     val frame        = UartCtrlFrameConfig(g)
     val clockDivider = UInt (g.clockDividerWidth bit) //see UartCtrlGenerics.clockDividerWidth for calculation

     def setClockDivider(baudrate : Double,clkFrequency : Double = ClockDomain.current.frequency.getValue) : Unit = {
       clockDivider := (clkFrequency / baudrate / g.rxSamplePerBit).toInt
     }
   }

Implementation
--------------

In ``UartCtrl``\ , 3 things will be instantiated:


* One clock divider that generates a tick pulse at the UART RX sampling rate.
* One ``UartCtrlTx`` ``Component``
* One ``UartCtrlRx`` ``Component``

UartCtrlTx
^^^^^^^^^^

The interfaces of this ``Component`` are the following :

.. list-table::
   :header-rows: 1
   :widths: 1 1 10

   * - Name
     - Type
     - Description
   * - configFrame
     - UartCtrlFrameConfig
     - Containts data bit width count and party/stop bits configurations
   * - samplingTick
     - Bool
     - Time reference that pulses ``rxSamplePerBit`` times per UART baud
   * - write
     - Stream[Bits]
     - Port used by the system to give transmission orders to the controller
   * - txd
     - Bool
     - UART txd pin


Let's define the enumeration that will be used to store the state of ``UartCtrlTx``\ :

.. code-block:: scala

   object UartCtrlTxState extends SpinalEnum {
     val IDLE, START, DATA, PARITY, STOP = newElement()
   }

Let's define the skeleton of ``UartCtrlTx``\ :

.. code-block:: scala

   class UartCtrlTx(g : UartCtrlGenerics) extends Component {
     import g._

     val io = new Bundle {
       val configFrame  = in(UartCtrlFrameConfig(g))
       val samplingTick = in Bool
       val write        = slave Stream (Bits(dataWidthMax bit))
       val txd          = out Bool
     }

     // Provide one clockDivider.tick each rxSamplePerBit pulses of io.samplingTick
     // Used by the stateMachine as a baud rate time reference
     val clockDivider = new Area {
       val counter = Reg(UInt(log2Up(rxSamplePerBit) bits)) init(0)
       val tick = False
       ..
     }

     // Count up each clockDivider.tick, used by the state machine to count up data bits and stop bits
     val tickCounter = new Area {
       val value = Reg(UInt(Math.max(dataWidthMax, 2) bit))
       def reset() = value := 0
       ..
     }

     val stateMachine = new Area {
       import UartCtrlTxState._

       val state = RegInit(IDLE)
       val parity = Reg(Bool)
       val txd = True
       ..
       switch(state) {
         ..
       }
     }

     io.txd := RegNext(stateMachine.txd) init(True)
   }

And here is the complete implementation:

.. code-block:: scala

   class UartCtrlTx(g : UartCtrlGenerics) extends Component {
     import g._

     val io = new Bundle {
       val configFrame  = in(UartCtrlFrameConfig(g))
       val samplingTick = in Bool
       val write        = slave Stream (Bits(dataWidthMax bit))
       val txd          = out Bool
     }

     // Provide one clockDivider.tick each rxSamplePerBit pulse of io.samplingTick
     // Used by the stateMachine as a baud rate time reference
     val clockDivider = new Area {
       val counter = Reg(UInt(log2Up(rxSamplePerBit) bits)) init(0)
       val tick = False
       when(io.samplingTick) {
         counter := counter - 1
         tick := counter === 0
       }
     }

     // Count up each clockDivider.tick, used by the state machine to count up data bits and stop bits
     val tickCounter = new Area {
       val value = Reg(UInt(Math.max(dataWidthMax, 2) bit))
       def reset() = value := 0

       when(clockDivider.tick) {
         value := value + 1
       }
     }

     val stateMachine = new Area {
       import UartCtrlTxState._

       val state = RegInit(IDLE)
       val parity = Reg(Bool)
       val txd = True

       when(clockDivider.tick) {
         parity := parity ^ txd
       }

       io.write.ready := False
       switch(state) {
         is(IDLE){
           when(io.write.valid && clockDivider.tick){
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
             when(tickCounter.value === toBitCount(io.configFrame.stop)) {
               state := io.write.valid ? START | IDLE
             }
           }
         }
       }
     }

     io.txd := RegNext(stateMachine.txd, True)
   }

UartCtrlRx
^^^^^^^^^^

The interfaces of this ``Component`` are the following:

.. list-table::
   :header-rows: 1
   :widths: 1 1 10

   * - Name
     - Type
     - Description
   * - configFrame
     - UartCtrlFrameConfig
     - Contains data bit width and party/stop bits configurations
   * - samplingTick
     - Bool
     - Time reference that pulses ``rxSamplePerBit`` times per UART baud
   * - read
     - Flow[Bits]
     - Port used by the controller to notify the system about a successfully received frame
   * - rxd
     - Bool
     - UART rxd pin, not synchronized with the current clock domain


Let's define the enumeration that will be used to store the state of ``UartCtrlTx``\ :

.. code-block:: scala

   object UartCtrlRxState extends SpinalEnum {
     val IDLE, START, DATA, PARITY, STOP = newElement()
   }

Let's define the skeleton of the UartCtrlRx :

.. code-block:: scala

   class UartCtrlRx(g : UartCtrlGenerics) extends Component {
     import g._
     val io = new Bundle {
       val configFrame  = in(UartCtrlFrameConfig(g))
       val samplingTick = in Bool
       val read         = master Flow (Bits(dataWidthMax bit))
       val rxd          = in Bool
     }

     // Implement the rxd sampling with a majority vote over samplingSize bits
     // Provide a new sampler.value each time sampler.tick is high
     val sampler = new Area {
       val syncroniser = BufferCC(io.rxd)
       val samples     = History(that=syncroniser,when=io.samplingTick,length=samplingSize)
       val value       = RegNext(MajorityVote(samples))
       val tick        = RegNext(io.samplingTick)
     }

     // Provide a bitTimer.tick each rxSamplePerBit
     // reset() can be called to recenter the counter over a start bit.
     val bitTimer = new Area {
       val counter = Reg(UInt(log2Up(rxSamplePerBit) bit))
       def reset() = counter := preSamplingSize + (samplingSize - 1) / 2 - 1)
       val tick = False
       ...
     }

     // Provide bitCounter.value that count up each bitTimer.tick, Used by the state machine to count data bits and stop bits
     // reset() can be called to reset it to zero
     val bitCounter = new Area {
       val value = Reg(UInt(Math.max(dataWidthMax, 2) bit))
       def reset() = value := 0
       ...
     }

     val stateMachine = new Area {
       import UartCtrlRxState._

       val state   = RegInit(IDLE)
       val parity  = Reg(Bool)
       val shifter = Reg(io.read.payload)
       ...
       switch(state) {
         ...
       }
     }
   }

And here is the complete implementation:

.. code-block:: scala

   class UartCtrlRx(g : UartCtrlGenerics) extends Component {
     import g._
     val io = new Bundle {
       val configFrame  = in(UartCtrlFrameConfig(g))
       val samplingTick = in Bool
       val read         = master Flow (Bits(dataWidthMax bit))
       val rxd          = in Bool
     }

     // Implement the rxd sampling with a majority vote over samplingSize bits
     // Provide a new sampler.value each time sampler.tick is high
     val sampler = new Area {
       val syncroniser = BufferCC(io.rxd)
       val samples     = History(that=syncroniser,when=io.samplingTick,length=samplingSize)
       val value       = RegNext(MajorityVote(samples))
       val tick        = RegNext(io.samplingTick)
     }

     // Provide a bitTimer.tick each rxSamplePerBit
     // reset() can be called to recenter the counter over a start bit.
     val bitTimer = new Area {
       val counter = Reg(UInt(log2Up(rxSamplePerBit) bit))
       def reset() = counter := preSamplingSize + (samplingSize - 1) / 2 - 1
       val tick = False
       when(sampler.tick) {
         counter := counter - 1
         when(counter === 0) {
           tick := True
         }
       }
     }

     // Provide bitCounter.value that count up each bitTimer.tick, Used by the state machine to count data bits and stop bits
     // reset() can be called to reset it to zero
     val bitCounter = new Area {
       val value = Reg(UInt(Math.max(dataWidthMax, 2) bit))
       def reset() = value := 0

       when(bitTimer.tick) {
         value := value + 1
       }
     }

     val stateMachine = new Area {
       import UartCtrlRxState._

       val state   = RegInit(IDLE)
       val parity  = Reg(Bool)
       val shifter = Reg(io.read.payload)

       //Parity calculation
       when(bitTimer.tick) {
         parity := parity ^ sampler.value
       }

       io.read.valid := False
       switch(state) {
         is(IDLE) {
           when(sampler.value === False) {
             state := START
             bitTimer.reset()
           }
         }
         is(START) {
           when(bitTimer.tick) {
             state := DATA
             bitCounter.reset()
             parity := io.configFrame.parity === UartParityType.ODD
             when(sampler.value === True) {
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
             }.elsewhen(bitCounter.value === toBitCount(io.configFrame.stop)) {
               state := IDLE
               io.read.valid := True
             }
           }
         }
       }
     }
     io.read.payload := stateMachine.shifter
   }

UartCtrl
^^^^^^^^

Let's write ``UartCtrl`` that instantiates the ``UartCtrlRx`` and ``UartCtrlTx`` parts, generate the clock divider logic, and connect them to each other.

.. code-block:: scala

   class UartCtrl(g : UartCtrlGenerics = UartCtrlGenerics()) extends Component {
     val io = new Bundle {
       val config = in(UartCtrlConfig(g))
       val write  = slave(Stream(Bits(g.dataWidthMax bit)))
       val read   = master(Flow(Bits(g.dataWidthMax bit)))
       val uart   = master(Uart())
     }

     val tx = new UartCtrlTx(g)
     val rx = new UartCtrlRx(g)

     //Clock divider used by RX and TX
     val clockDivider = new Area {
       val counter = Reg(UInt(g.clockDividerWidth bits)) init(0)
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

Example with test bench
-----------------------

Here is a top level example that does the followings things:


* Instantiate ``UartCtrl`` and set its configuration to 921600 baud/s, no parity, 1 stop bit.
* Each time a byte is received from the UART, it writes it on the leds output.
* Every 2000 cycles, it sends the switches input value to the UART.

.. code-block:: scala

   class UartCtrlUsageExample extends Component{
     val io = new Bundle{
       val uart = master(Uart())
       val switchs = in Bits(8 bits)
       val leds = out Bits(8 bits)
     }

     val uartCtrl = new UartCtrl()
     uartCtrl.io.config.setClockDivider(921600)
     uartCtrl.io.config.frame.dataLength := 7  //8 bits
     uartCtrl.io.config.frame.parity := UartParityType.NONE
     uartCtrl.io.config.frame.stop := UartStopType.ONE
     uartCtrl.io.uart <> io.uart

     //Assign io.led with a register loaded each time a byte is received
     io.leds := uartCtrl.io.read.toReg()

     //Write the value of switch on the uart each 2000 cycles
     val write = Stream(Bits(8 bits))
     write.valid := CounterFreeRun(2000).willOverflow
     write.payload := io.switchs
     write >-> uartCtrl.io.write
   }


   object UartCtrlUsageExample{
     def main(args: Array[String]) {
       SpinalVhdl(new UartCtrlUsageExample,defaultClockDomainFrequency=FixedFrequency(50e6))
     }
   }

The following example is just a "mad one" but if you want to send a 0x55 header before sending the value of switches, you can replace the write generator of the preceding example by:

.. code-block:: scala

     val write = Stream(Fragment(Bits(8 bits)))
     write.valid := CounterFreeRun(4000).willOverflow
     write.fragment := io.switchs
     write.last := True
     write.stage().insertHeader(0x55).toStreamOfFragment >> uartCtrl.io.write

`Here <https://github.com/SpinalHDL/SpinalHDL/blob/master/tester/src/test/resources/UartCtrlUsageExample_tb.vhd>`_ you can get a simple VHDL testbench for this small ``UartCtrlUsageExample``.

Bonus: Having fun with Stream
-----------------------------

If you want to queue data received from the UART:

.. code-block:: scala

   val uartCtrl = new UartCtrl()
   val queuedReads = uartCtrl.io.read.toStream.queue(16)

If you want to add a queue on the write interface and do some flow control:

.. code-block:: scala

   val uartCtrl = new UartCtrl()
   val writeCmd = Stream(Bits(8 bits))
   val stopIt = Bool
   writeCmd.queue(16).haltWhen(stopIt) >> uartCtrl.io.write
