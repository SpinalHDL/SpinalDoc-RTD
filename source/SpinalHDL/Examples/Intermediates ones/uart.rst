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
If so we can skip resetting counters in a few places.

Instead of adding each construction parameters (generics) to ``UartCtrl`` one by one, we can group them inside a class that will be used as single parameter of ``UartCtrl``.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-at: case class UartCtrlGenerics(
   :end-before: // end case class UartCtrlGenerics

UART interface
^^^^^^^^^^^^^^

Let's define a UART interface bundle without flow control.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-at: case class Uart(
   :end-before: // end case class Uart


UART configuration enums
^^^^^^^^^^^^^^^^^^^^^^^^

Let's define parity and stop bit enumerations.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-after: // begin enums
   :end-before: // end enums

UartCtrl configuration Bundles
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Let's define bundles that will be used as IO elements to setup ``UartCtrl``.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-after: // begin internal bundles
   :end-before: // end internal bundles

Implementation
--------------

In ``UartCtrl``\ , 3 things will be instantiated:


* One clock divider that generates a tick pulse at the UART RX sampling rate.
* One ``UartCtrlTx`` component
* One ``UartCtrlRx`` component

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
     - Contains data bit width count and party/stop bits configurations
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

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-at: object UartCtrlTxState
   :end-before: // end object UartCtrlTxState


Let's define the skeleton of ``UartCtrlTx``\ :

.. code-block:: scala

   class UartCtrlTx(g : UartCtrlGenerics) extends Component {
     import g._

     val io = new Bundle {
       val configFrame  = in(UartCtrlFrameConfig(g))
       val samplingTick = in Bool()
       val write        = slave Stream (Bits(dataWidthMax bits))
       val txd          = out Bool()
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
       val value = Reg(UInt(Math.max(dataWidthMax, 2) bits))
       def reset() = value := 0
       ..
     }

     val stateMachine = new Area {
       import UartCtrlTxState._

       val state = RegInit(IDLE)
       val parity = Reg(Bool())
       val txd = True
       ..
       switch(state) {
         ..
       }
     }

     io.txd := RegNext(stateMachine.txd) init(True)
   }

And here is the complete implementation:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-at: class UartCtrlTx(
   :end-before: // end class UartCtrlTx

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

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-at: object UartCtrlRxState
   :end-before: // end object UartCtrlRxState

Let's define the skeleton of the UartCtrlRx :

.. code-block:: scala

   class UartCtrlRx(g : UartCtrlGenerics) extends Component {
     import g._
     val io = new Bundle {
       val configFrame  = in(UartCtrlFrameConfig(g))
       val samplingTick = in Bool()
       val read         = master Flow (Bits(dataWidthMax bits))
       val rxd          = in Bool()
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
       val counter = Reg(UInt(log2Up(rxSamplePerBit) bits))
       def reset() = counter := preSamplingSize + (samplingSize - 1) / 2 - 1)
       val tick = False
       ...
     }

     // Provide bitCounter.value that count up each bitTimer.tick, Used by the state machine to count data bits and stop bits
     // reset() can be called to reset it to zero
     val bitCounter = new Area {
       val value = Reg(UInt(Math.max(dataWidthMax, 2) bits))
       def reset() = value := 0
       ...
     }

     val stateMachine = new Area {
       import UartCtrlRxState._

       val state   = RegInit(IDLE)
       val parity  = Reg(Bool())
       val shifter = Reg(io.read.payload)
       ...
       switch(state) {
         ...
       }
     }
   }

And here is the complete implementation:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-at: class UartCtrlRx(
   :end-before: // end class UartCtrlRx

UartCtrl
^^^^^^^^

Let's write ``UartCtrl`` that instantiates the ``UartCtrlRx`` and ``UartCtrlTx`` parts, generate the clock divider logic, and connect them to each other.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-at: class UartCtrl(
   :end-before: // end class UartCtrl

To make it easier to use the UART with fixed settings, we introduce an companion object for ``UartCtrl``. It allows us to provide
additional ways of instantiating a UartCtrl component with different sets of parameters. Here we define a ``UartCtrlInitConfig``
holding the settings for a component that is not runtime configurable. Note that it is still possible to instantiate the UartCtrl
manually like all other components, which one would do if a runtime-configurable UART is needed (via ``val uart = new UartCtrl()``).

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-at: case class UartCtrlInitConfig(
   :end-before: // end object UartCtrl


Simple usage 
-----------------------

To synthesize a ``UartCtrl`` as ``115200-N-8-1``:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-after: // start rxtx snippet
   :end-before: // end rxtx snippet

If you are using ``txd`` pin only, add:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-after: // start tx snippet
   :end-before: // end tx snippet

On the contrary, if you are using ``rxd`` pin only:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-after: // start rx snippet
   :end-before: // end rx snippet

Example with test bench
-----------------------

Here is a top level example that does the followings things:


* Instantiate ``UartCtrl`` and set its configuration to 921600 baud/s, no parity, 1 stop bit.
* Each time a byte is received from the UART, it writes it on the leds output.
* Every 2000 cycles, it sends the switches input value to the UART.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-at: case class UartCtrlUsageExample(
   :end-before: // end UartCtrlUsageExample

`Here <https://github.com/SpinalHDL/SpinalHDL/blob/master/tester/src/test/resources/UartCtrlUsageExample_tb.vhd>`_ you can get a simple VHDL testbench for this small ``UartCtrlUsageExample``.

Bonus: Having fun with Stream
-----------------------------

If you want to queue data received from the UART:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-after: // start rx queue
   :end-before: // end rx queue

If you want to add a queue on the write interface and do some flow control:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-after: // start tx queue
   :end-before: // end tx queue

If you want to send a 0x55 header before sending the value of switches, you can replace the write generator of the preceding example by:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Uart.scala
   :language: scala
   :start-after: // start with header
   :end-before: // end with header
