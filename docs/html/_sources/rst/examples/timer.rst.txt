
Timer
=====

.. role:: raw-html-m2r(raw)
   :format: html


Introduction
------------

A timer module is probably one of the most basic pieces of hardware. But even for a timer, there are some interresting things that you can do with SpinalHDL. This example will define a simple timer component which integrates a bus bridging utile.

Timer
-----

So let's start with the ``Timer`` component.

Specification
^^^^^^^^^^^^^

The ``Timer`` component will have a single construction parameter:

.. list-table::
   :header-rows: 1

   * - Parameter Name
     - Type
     - Description
   * - width
     - Int
     - Specify the bit width of the timer counter


And also some inputs/outputs:

.. list-table::
   :header-rows: 1

   * - IO Name
     - Direction
     - Type
     - Description
   * - tick
     - in
     - Bool
     - When ``tick`` is True, the timer count up until ``limit``.
   * - clear
     - in
     - Bool
     - When ``tick`` is True, the timer is set to zero. ``clear`` has priority over ``tick``.
   * - limit
     - in
     - UInt(width bits)
     - When the timer value is equal to ``limit``\ , the ``tick`` input is inhibited.
   * - full
     - out
     - Bool
     - ``full`` is high when the timer value is equal to ``limit`` and ``tick`` is high.
   * - value
     - out
     - UInt(width bits)
     - Wire out the timer counter value.


Implementation
^^^^^^^^^^^^^^

.. code-block:: scala

   case class Timer(width : Int) extends Component{
     val io = new Bundle{
       val tick      = in Bool
       val clear     = in Bool
       val limit     = in UInt(width bits)

       val full  = out Bool
       val value     = out UInt(width bits)
     }

     val counter = Reg(UInt(width bits))
     when(io.tick && !io.full){
       counter := counter + 1
     }
     when(io.clear){
       counter := 0
     }

     io.full := counter === io.limit && io.tick
     io.value := counter
   }

Bridging function
-----------------

Now we can start with the main purpose of this example: defining a bus bridging function. To do that we will use two techniques:


* Using the ``BusSlaveFactory`` tool documented `here </SpinalDoc/spinal/lib/bus_slave_factory/>`_
* Defining a function inside the ``Timer`` component which can be called from the parent component to drive the ``Timer``\ 's IO in an abstract way.

Specification
^^^^^^^^^^^^^

This bridging function will take the following parameters:

.. list-table::
   :header-rows: 1

   * - Parameter Name
     - Type
     - Description
   * - busCtrl
     - BusSlaveFactory
     - The ``BusSlaveFactory`` instance that will be used by the function to create the bridging logic.
   * - baseAddress
     - BigInt
     - The base address where the bridging logic should be mapped.
   * - ticks
     - Seq[Bool]
     - A list of Bool sources that can be used as a tick signal.
   * - clears
     - Seq[Bool]
     - A list of Bool sources that can be used as a clear signal.


The register mapping assumes that the bus system is 32 bits wide:

.. list-table::
   :header-rows: 1

   * - Name
     - Access
     - Width
     - Address offset
     - Bit offset
     - Description
   * - ticksEnable
     - RW
     - len(ticks)
     - 0
     - 0
     - Each ``ticks`` bool can be actived if the corresponding ``ticksEnable`` bit is high.
   * - clearsEnable
     - RW
     - len(clears)
     - 0
     - 16
     - Each ``clears`` bool can be actived if the corresponding ``clearsEnable`` bit is high.
   * - limit
     - RW
     - width
     - 4
     - 0
     - Access the limit value of the timer component.\ :raw-html-m2r:`<br>` When this register is written to, the timer is cleared.
   * - value
     - R
     - width
     - 8
     - 0
     - Access the value of the timer.
   * - clear
     - W
     - -
     - 8
     - -
     - When this register is written to, it clears the timer.


Implementation
^^^^^^^^^^^^^^

Let's add this bridging function inside the ``Timer`` component.

.. code-block:: scala

   case class Timer(width : Int) extends Component{
     val io = new Bundle{
       val tick      = in Bool
       val clear     = in Bool
       val limit     = in UInt(width bits)

       val full  = out Bool
       val value     = out UInt(width bits)
     }  

     // Logic previously defined
     // ....

     // The function prototype uses Scala currying funcName(arg1,arg2)(arg3,arg3)
     // which allow to call the function with a nice syntax later
     // This function also returns an area, which allows to keep names of inner signals in the generated VHDL/Verilog.
     def driveFrom(busCtrl : BusSlaveFactory,baseAddress : BigInt)(ticks : Seq[Bool],clears : Seq[Bool]) = new Area {
       //Address 0 => clear/tick masks + bus
       val ticksEnable  = busCtrl.createReadWrite(Bits(ticks.length bits),baseAddress + 0,0) init(0)
       val clearsEnable = busCtrl.createReadWrite(Bits(clears.length bits),baseAddress + 0,16) init(0)
       val busClearing  = False

       io.clear := (clearsEnable & clears.asBits).orR | busClearing
       io.tick  := (ticksEnable  & ticks.asBits ).orR

       //Address 4 => read/write limit (+ auto clear)
       busCtrl.driveAndRead(io.limit,baseAddress + 4)
       busClearing setWhen(busCtrl.isWriting(baseAddress + 4))

       //Address 8 => read timer value / write => clear timer value
       busCtrl.read(io.value,baseAddress + 8)
       busClearing setWhen(busCtrl.isWriting(baseAddress + 8))
     }
   }

Usage
^^^^^

Here is some demonstration code which is very close to the one used in the Pinsec SoC timer module. Basically it instantiates following elements:


* One 16 bit prescaler
* One 32 bit timer
* Three 16 bit timers

Then by using an ``Apb3SlaveFactory`` and functions defined inside the ``Timer``\ s, it creates bridging logic between the APB3 bus and all instantiated components.

.. code-block:: scala

   val io = new Bundle{
     val apb = Apb3(ApbConfig(addressWidth = 8, dataWidth = 32))
     val interrupt = in Bool
     val external = new Bundle{
       val tick  = Bool
       val clear = Bool
     }
   }

   //Prescaler is very similar to the timer, it mainly integrates a piece of auto reload logic.
   val prescaler = Prescaler(width = 16)

   val timerA = Timer(width = 32)
   val timerB,timerC,timerD = Timer(width = 16)

   val busCtrl = Apb3SlaveFactory(io.apb)
   val prescalerBridge = prescaler.driveFrom(busCtrl,0x00)

   val timerABridge = timerA.driveFrom(busCtrl,0x40)(
     // The first element is True, which allows you to have a mode where the timer is always counting up.
     ticks  = List(True, prescaler.io.overflow),
     // By looping the timer full to the clears, it allows you to create an autoreload mode.
     clears = List(timerA.io.full)
   )

   val timerBBridge = timerB.driveFrom(busCtrl,0x50)(
     //The external.tick could allow to create an impulsion counter mode
     ticks  = List(True, prescaler.io.overflow, io.external.tick),
     //external.clear could allow to create an timeout mode.
     clears = List(timerB.io.full, io.external.clear)
   )

   val timerCBridge = timerC.driveFrom(busCtrl,0x60)(
     ticks  = List(True, prescaler.io.overflow, io.external.tick),
     clears = List(timerC.io.full, io.external.clear)
   )

   val timerDBridge = timerD.driveFrom(busCtrl,0x70)(
     ticks  = List(True, prescaler.io.overflow, io.external.tick),
     clears = List(timerD.io.full, io.external.clear)
   )

   val interruptCtrl = InterruptCtrl(4)
   val interruptCtrlBridge = interruptCtrl.driveFrom(busCtrl,0x10)
   interruptCtrl.io.inputs(0) := timerA.io.full
   interruptCtrl.io.inputs(1) := timerB.io.full
   interruptCtrl.io.inputs(2) := timerC.io.full
   interruptCtrl.io.inputs(3) := timerD.io.full
   io.interrupt := interruptCtrl.io.pendings.orR
