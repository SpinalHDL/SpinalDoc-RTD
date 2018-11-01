.. role:: raw-html-m2r(raw)
   :format: html

.. _clock_domain:

Clock domains
=============

Introduction
------------

In *SpinalHDL*\ , clock and reset signals can be combined to create a **clock domain**. Clock domains could be applied to some area of the design and then all synchronous elements instantiated into this area will then **implicitly** use this clock domain.

Clock domain application work like a stack, which mean, if you are in a given clock domain, you can still apply another clock domain locally.

.. _clock_domain_instantiation:

Instantiation
-------------

The syntax to define a clock domain is as follows (using EBNF syntax):

.. code-block:: scala

   ClockDomain(
     clock: Bool 
     [,reset: Bool]
     [,softReset: Bool]
     [,clockEnable: Bool]
     [,frequency: IClockDomainFrequency]
     [,config: ClockDomainConfig]
   )

This definition takes five parameters:

.. list-table::
   :header-rows: 1

   * - Argument
     - Description
     - Default
   * - ``clock``
     - Clock signal that defines the domain
     - -
   * - ``reset``
     - Reset signal. If a register which need a reset and his clock domain didn't provide one, an error message happen
     - null
   * - ``softReset``
     - Reset which infer an additional synchronous reset
     - null
   * - ``clockEnable``
     - The goal of this signal is to disable the clock on the whole clock domain without having to manually implement that on each synchronous element
     - null
   * - ``frequency``
     - Allow to specify the frequency of the given clock domain and later get it in your desing
     - UnknownFrequency
   * - ``config``
     - Specify polarity of signals and the nature of the reset
     - Current config


An applied example to define a specific clock domain within the design is as follows:

.. code-block:: scala

   val coreClock = Bool
   val coreReset = Bool

   // Define a new clock domain
   val coreClockDomain = ClockDomain(coreClock,coreReset)

   // Use this domain in an area of the design
   val coreArea = new ClockingArea(coreClockDomain){
     val coreClockedRegister = Reg(UInt(4 bit))
   }

Configuration
^^^^^^^^^^^^^

In addition to the constructor parameters given :ref:`here <clock_domain_instantiation>`\ , the following elements of each clock domain are configurable via a ``ClockDomainConfig``\ class :

.. list-table::
   :header-rows: 1

   * - Property
     - Valid values
   * - ``clockEdge``
     - ``RISING``\ , ``FALLING``
   * - ``ResetKind``
     - ``ASYNC``\ , ``SYNC``\ , ``BOOT`` which is supported by some FPGA (FF values loaded by the bitstream)
   * - ``resetActiveLevel``
     - ``HIGH``\ , ``LOW``
   * - ``softResetActiveLevel``
     - ``HIGH``\ , ``LOW``
   * - ``clockEnableActiveLevel``
     - ``HIGH``\ , ``LOW``


.. code-block:: scala

   class CustomClockExample extends Component {
     val io = new Bundle {
       val clk    = in Bool
       val resetn = in Bool
       val result = out UInt (4 bits)
     }

     // configure the clock domain 
     val myClockDomain = ClockDomain(
       clock  = io.clk,
       reset  = io.resetn,
       config = ClockDomainConfig(
         clockEdge        = RISING,
         resetKind        = ASYNC,
         resetActiveLevel = LOW
       )
     )

     // Define an Area which use myClockDomain
     val myArea = new ClockingArea(myClockDomain) {
       val myReg = Reg(UInt(4 bits)) init(7)

       myReg := myReg + 1

       io.result := myReg
     }
   }

By default, a ClockDomain is applied to the whole design. The configuration of this one is :


* Clock : rising edge
* Reset : asynchronous, active high
* No clock enable

Internal clock
^^^^^^^^^^^^^^

An alternative syntax to create a clock domain is the following : 

.. code-block:: scala

   ClockDomain.internal(
     name: String,
     [config: ClockDomainConfig,] 
     [withReset: Boolean,] 
     [withSoftReset: Boolean,]
     [withClockEnable: Boolean,]
     [frequency: IClockDomainFrequency]
   )

This definition takes six parameters:

.. list-table::
   :header-rows: 1

   * - Argument
     - Description
     - Default
   * - ``name``
     - Name of clk and reset signal
     - -
   * - ``config``
     - Specify polarity of signals and the nature of the reset
     - Current config
   * - ``withReset``
     - Add a reset signal
     - true
   * - ``withSoftReset``
     - Add a soft reset signal
     - false
   * - ``withClockEnable``
     - Add a clock enable
     - false
   * - ``frequency``
     - Frequency of the clock domain
     - UnknownFrequency


It's advantage is to create clock and reset signals with a specified name inplace of an inherited one. Then you have to assign those ClockDomain's signals as for instance in the example bellow :

.. code-block:: scala

   class InternalClockWithPllExample extends Component {
     val io = new Bundle {
       val clk100M = in Bool
       val aReset  = in Bool
       val result  = out UInt (4 bits)
     }
     // myClockDomain.clock will be named myClockName_clk
     // myClockDomain.reset will be named myClockName_reset
     val myClockDomain = ClockDomain.internal("myClockName")

     // Instanciate a PLL (probably a BlackBox)
     val pll = new Pll()
     pll.io.clkIn := io.clk100M

     // Assign myClockDomain signals with something
     myClockDomain.clock := pll.io.clockOut
     myClockDomain.reset := io.aReset || !pll.io.

     // Do whatever you want with myClockDomain
     val myArea = new ClockingArea(myClockDomain){
       val myReg = Reg(UInt(4 bits)) init(7)
       myReg := myReg + 1

       io.result := myReg
     }
   }

External clock
^^^^^^^^^^^^^^

You can define everywhere a clock domain which is driven by the outside. It will then automatically add clock and reset wire from the top level inputs to all synchronous elements.

.. code-block:: scala

   ClockDomain.external(
     name: String,
     [config: ClockDomainConfig,] 
     [withReset: Boolean,] 
     [withSoftReset: Boolean,]
     [withClockEnable: Boolean,]
     [frequency: IClockDomainFrequency]
   )

Arguments of the ``ClockDomain.external`` function are exactly the sames than for the ``ClockDomain.internal`` one. Below an example of a desing using ``ClockDomain.external``.

.. code-block:: scala

   class ExternalClockExample extends Component {
     val io = new Bundle {
       val result = out UInt (4 bits)
     }

     // On top level you have two signals  :
     //     myClockName_clk and myClockName_reset
     val myClockDomain = ClockDomain.external("myClockName")

     val myArea = new ClockingArea(myClockDomain){
       val myReg = Reg(UInt(4 bits)) init(7)
       myReg := myReg + 1

       io.result := myReg
     }
   }

Context
^^^^^^^

At any moment you can retrieve in which clock domain you are by calling ``ClockDomain.current``.

Then the returned instance (which is a ClockDomain one) as following functions that you can call :

.. list-table::
   :header-rows: 1

   * - name
     - Description
     - Return
   * - frequency.getValue
     - Return the frequency of the clock domain
     - Double
   * - hasReset
     - Return if the clock domain has a reset signal
     - Boolean
   * - hasSoftReset
     - Return if the clock domain has a reset signal
     - Boolean
   * - hasClockEnable
     - Return if the clock domain has a clock enable signal
     - Boolean
   * - readClockWire
     - Return a signal derived by the clock signal
     - Bool
   * - readResetWire
     - Return a signal derived by the reset signal
     - Bool
   * - readSoftResetWire
     - Return a signal derived by the reset signal
     - Bool
   * - readClockEnableWire
     - Return a signal derived by the clock enable signal
     - Bool
   * - isResetActive
     - Return True when the reset has effect
     - Bool
   * - isSoftResetActive
     - Return True when the softReset has effect
     - Bool
   * - isClockEnableActive
     - Return True when the clock enable has effect
     - Bool


There is an example with an UART controller that use the frequency specification to set its clock divider :

.. code-block:: scala

   val coreClockDomain = ClockDomain(coreClock, coreReset, frequency=FixedFrequency(100e6))

   val coreArea = new ClockingArea(coreClockDomain){
     val ctrl = new UartCtrl()
     ctrl.io.config.clockDivider := (coreClk.frequency.getValue / 57.6e3 / 8).toInt
   }

Clock domain crossing
---------------------

SpinalHDL checks at compile time that there is no unwanted/unspecified cross clock domain signal reads. If you want to read a signal that is emitted by another ``ClockDomain`` area, you should add the ``crossClockDomain`` tag to the destination signal as depicted in the following example:

.. code-block:: scala

   //             _____                        _____             _____
   //            |     |  (crossClockDomain)  |     |           |     |
   //  dataIn -->|     |--------------------->|     |---------->|     |--> dataOut
   //            | FF  |                      | FF  |           | FF  |
   //  clkA   -->|     |              clkB -->|     |   clkB -->|     |
   //  rstA   -->|_____|              rstB -->|_____|   rstB -->|_____|



   // Implementation where clock and reset pins are given by components IO
   class CrossingExample extends Component {
     val io = new Bundle {
       val clkA = in Bool
       val rstA = in Bool

       val clkB = in Bool
       val rstB = in Bool

       val dataIn  = in Bool
       val dataOut = out Bool
     }

     // sample dataIn with clkA
     val area_clkA = new ClockingArea(ClockDomain(io.clkA,io.rstA)){  
       val reg = RegNext(io.dataIn) init(False)
     }

     // 2 register stages to avoid metastability issues
     val area_clkB = new ClockingArea(ClockDomain(io.clkB,io.rstB)){  
       val buf0   = RegNext(area_clkA.reg) init(False) addTag(crossClockDomain)
       val buf1   = RegNext(buf0)          init(False)
     }

     io.dataOut := area_clkB.buf1
   }


   //Alternative implementation where clock domains are given as parameters
   class CrossingExample(clkA : ClockDomain,clkB : ClockDomain) extends Component {
     val io = new Bundle {
       val dataIn  = in Bool
       val dataOut = out Bool
     }

     // sample dataIn with clkA
     val area_clkA = new ClockingArea(clkA){  
       val reg = RegNext(io.dataIn) init(False)
     }

     // 2 register stages to avoid metastability issues
     val area_clkB = new ClockingArea(clkB){  
       val buf0   = RegNext(area_clkA.reg) init(False) addTag(crossClockDomain)
       val buf1   = RegNext(buf0)          init(False)
     }

     io.dataOut := area_clkB.buf1
   }

Even shorter by importing the lib ``import spinal.lib._`` SpinalHDL offers a cross clock domain buffer ``BufferCC(input: T, init: T = null, bufferDepth: Int = 2)`` to avoid metastability issues.

.. code-block:: scala

   class CrossingExample(clkA : ClockDomain,clkB : ClockDomain) extends Component {
     val io = new Bundle {
       val dataIn  = in Bool
       val dataOut = out Bool
     }

     // sample dataIn with clkA
     val area_clkA = new ClockingArea(clkA){  
       val reg = RegNext(io.dataIn) init(False)
     }

     // BufferCC to avoid metastability issues
     val area_clkB = new ClockingArea(clkB){  
       val buf1   = BufferCC(area_clkA.reg, False)
     }

     io.dataOut := area_clkB.buf1
   }

Special clocking Area
---------------------

Slow Area
^^^^^^^^^

``SlowArea`` is used to create a new clock domain area which is slower than the current one. 

.. code-block:: scala

   class TopLevel extends Component {

     // Use the current clock domain : 100MHz 
     val areaStd = new Area {    
       val counter = out(CounterFreeRun(16).value)
     }

     // Slow the current clockDomain by 4 : 25 MHz 
     val areaDiv4 = new SlowArea(4){
       val counter = out(CounterFreeRun(16).value)
     }

     // Slow the current clockDomainn to 50MHz 
     val area50Mhz = new SlowArea(50 MHz){
       val counter = out(CounterFreeRun(16).value)
     }
   }

   def main(args: Array[String]) {
     new SpinalConfig(
       defaultClockDomainFrequency = FixedFrequency(100 MHz)
     ).generateVhdl(new TopLevel)
   }

ResetArea
^^^^^^^^^

``ResetArea`` is used to create a new clock domain area where a special reset is combined or not with the current clock domain reset.

.. code-block:: scala

   class TopLevel extends Component {

     val specialReset = Bool 

     // The reset of this area is done with the specialReset signal 
     val areaRst_1 = new ResetArea(specialReset, false){
       val counter = out(CounterFreeRun(16).value)
     }

     // The reset of this area is a combination between the current reset and the specialReset
     val areaRst_2 = new ResetArea(specialReset, true){
       val counter = out(CounterFreeRun(16).value)
     }
   }

ClockEnableArea
^^^^^^^^^^^^^^^

``ClockEnableArea`` is used to add one more clock enable in the current clock domain.

.. code-block:: scala

   class TopLevel extends Component {

     val clockEnable = Bool 

     // Add a clock enable for this area 
     val area_1 = new ClockEnableArea(clockEnable){
       val counter = out(CounterFreeRun(16).value)
     }
   }
