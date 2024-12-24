.. _clock_domain:

Clock domains
=============

Introduction
------------

In SpinalHDL, clock and reset signals can be combined to create a **clock domain**. Clock domains can be applied to some areas of the design and then all synchronous elements instantiated into those areas will then **implicitly** use this clock domain.

Clock domain application works like a stack, which means that if you are in a given clock domain you can still apply another clock domain locally.

Please note that a register captures its clock domain when the register is created, not when it is assigned. So please make sure to create them inside the desired ``ClockingArea``.

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
   :widths: 1 10 1

   * - Argument
     - Description
     - Default
   * - ``clock``
     - Clock signal that defines the domain
     - 
   * - ``reset``
     - Reset signal. If a register exists which needs a reset and the clock domain doesn't provide one, an error message will be displayed
     - null
   * - ``softReset``
     - Reset which infers an additional synchronous reset
     - null
   * - ``clockEnable``
     - The goal of this signal is to disable the clock on the whole clock domain without having to manually implement that on each synchronous element
     - null
   * - ``frequency``
     - Allows you to specify the frequency of the given clock domain and later read it in your design.
       This parameter does not generate and PLL or other hardware to control the frequency
     - UnknownFrequency
   * - ``config``
     - Specify the polarity of signals and the nature of the reset
     - Current config


An applied example to define a specific clock domain within the design is as follows:

.. code-block:: scala

   val coreClock = Bool()
   val coreReset = Bool()

   // Define a new clock domain
   val coreClockDomain = ClockDomain(coreClock, coreReset)

   // Use this domain in an area of the design
   val coreArea = new ClockingArea(coreClockDomain) {
     val coreClockedRegister = Reg(UInt(4 bits))
   }

When an `Area` is not needed, it is also possible to apply the clock domain directly. Two syntaxes exist:

.. code-block:: scala

   class Counters extends Component {
     val io = new Bundle {
       val enable = in Bool ()
       val freeCount, gatedCount, gatedCount2 = out UInt (4 bits)
     }
     val freeCounter = CounterFreeRun(16)
     io.freeCount := freeCounter.value
   
     // In a real design consider using a glitch free single purpose CLKGATE primitive instead
     val gatedClk = ClockDomain.current.readClockWire && io.enable
     val gated = ClockDomain(gatedClk, ClockDomain.current.readResetWire)
   
     // Here the "gated" clock domain is applied on "gatedCounter" and "gatedCounter2"
     val gatedCounter = gated(CounterFreeRun(16))
     io.gatedCount := gatedCounter.value
     val gatedCounter2 = gated on CounterFreeRun(16)
     io.gatedCount2 := gatedCounter2.value
   
     assert(gatedCounter.value === gatedCounter2.value, "gated count mismatch")
   }


Configuration
^^^^^^^^^^^^^

In addition to :ref:`constructor parameters <clock_domain_instantiation>`\ , the following elements of each clock domain are configurable via a ``ClockDomainConfig``\ class:

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Property
     - Valid values
   * - ``clockEdge``
     - ``RISING``\ , ``FALLING``
   * - ``resetKind``
     - ``ASYNC``\ , ``SYNC``\ , and ``BOOT`` which is supported by some FPGAs (where FF values are loaded by the bitstream)
   * - ``resetActiveLevel``
     - ``HIGH``\ , ``LOW``
   * - ``softResetActiveLevel``
     - ``HIGH``\ , ``LOW``
   * - ``clockEnableActiveLevel``
     - ``HIGH``\ , ``LOW``


.. code-block:: scala

   class CustomClockExample extends Component {
     val io = new Bundle {
       val clk    = in Bool()
       val resetn = in Bool()
       val result = out UInt (4 bits)
     }

     // Configure the clock domain
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

By default, a ``ClockDomain`` is applied to the whole design. The configuration of this default domain is:


* Clock : rising edge
* Reset : asynchronous, active high
* No clock enable

This corresponds to the following ``ClockDomainConfig``:

.. code-block:: scala

   val defaultCC = ClockDomainConfig(
     clockEdge        = RISING,
     resetKind        = ASYNC,
     resetActiveLevel = HIGH
   )

Internal clock
^^^^^^^^^^^^^^

An alternative syntax to create a clock domain is the following: 

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
   :widths: 1 5 1

   * - Argument
     - Description
     - Default
   * - ``name``
     - Name of `clk` and `reset` signal
     - 
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


The advantage of this approach is to create clock and reset signals with a known/specified name instead of an inherited one.

Once created, you have to assign the ``ClockDomain``'s signals, as shown in the example below:

.. code-block:: scala

   class InternalClockWithPllExample extends Component {
     val io = new Bundle {
       val clk100M = in Bool()
       val aReset  = in Bool()
       val result  = out UInt (4 bits)
     }
     // myClockDomain.clock will be named myClockName_clk
     // myClockDomain.reset will be named myClockName_reset
     val myClockDomain = ClockDomain.internal("myClockName")

     // Instantiate a PLL (probably a BlackBox)
     val pll = new Pll()
     pll.io.clkIn := io.clk100M

     // Assign myClockDomain signals with something
     myClockDomain.clock := pll.io.clockOut
     myClockDomain.reset := io.aReset || !pll.io.

     // Do whatever you want with myClockDomain
     val myArea = new ClockingArea(myClockDomain) {
       val myReg = Reg(UInt(4 bits)) init(7)
       myReg := myReg + 1

       io.result := myReg
     }
   }

.. warning::
   In other components then the one you created the ClockDomain in, you must not use ``.clock`` and ``.reset``,
   but ``.readClockWire`` and ``.readResetWire`` as listed below. For the global ClockDomain you must always
   use those ``.readXXX`` functions.


External clock
^^^^^^^^^^^^^^

You can define a clock domain which is driven by the outside anywhere in your source. It will then automatically add clock and reset wires from the top level inputs to all synchronous elements.

.. code-block:: scala

   ClockDomain.external(
     name: String,
     [config: ClockDomainConfig,] 
     [withReset: Boolean,] 
     [withSoftReset: Boolean,]
     [withClockEnable: Boolean,]
     [frequency: IClockDomainFrequency]
   )

The arguments to the ``ClockDomain.external`` function are exactly the same as in the ``ClockDomain.internal`` function. Below is an example of a design using ``ClockDomain.external``:

.. code-block:: scala

   class ExternalClockExample extends Component {
     val io = new Bundle {
       val result = out UInt (4 bits)
     }

     // On the top level you have two signals  :
     //     myClockName_clk and myClockName_reset
     val myClockDomain = ClockDomain.external("myClockName")

     val myArea = new ClockingArea(myClockDomain) {
       val myReg = Reg(UInt(4 bits)) init(7)
       myReg := myReg + 1

       io.result := myReg
     }
   }

Signal priorities in HDL generation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In the current version, reset and clock enable signals have different priorities. Their order is : ``asyncReset``, ``clockEnable``, ``syncReset`` and ``softReset``.

Please be careful that clockEnable has a higher priority than syncReset. If you do a sync reset when the clockEnable is disabled (especially at the beginning of a simulation), the gated registers will not be reset.

Here is an example:

.. code-block:: scala

  val clockedArea = new ClockEnableArea(clockEnable) {
    val reg = RegNext(io.input) init(False)
  }

It will generate VerilogHDL codes like:

.. code-block:: verilog

  always @(posedge clk) begin
    if(clockedArea_newClockEnable) begin
      if(!resetn) begin
        clockedArea_reg <= 1'b0;
      end else begin
        clockedArea_reg <= io_input;
      end
    end
  end

If that behavior is problematic, one workaround is to use a when statement as a clock enable instead of using the ClockDomain.enable feature. This is open for future improvements.

Context
^^^^^^^

You can retrieve in which clock domain you are by calling ``ClockDomain.current`` anywhere.

The returned ``ClockDomain`` instance has the following functions that can be called:

.. list-table::
   :header-rows: 1
   :widths: 1 5 1

   * - name
     - Description
     - Return
   * - frequency.getValue
     - | Return the frequency of the clock domain.
       | This being the arbitrary value you configured the domain with.
     - Double
   * - hasReset
     - Return if the clock domain has a reset signal
     - Boolean
   * - hasSoftReset
     - Return if the clock domain has a soft reset signal
     - Boolean
   * - hasClockEnable
     - Return if the clock domain has a clock enable signal
     - Boolean
   * - readClockWire
     - Return a signal derived from the clock signal
     - Bool
   * - readResetWire
     - Return a signal derived from the reset signal
     - Bool
   * - readSoftResetWire
     - Return a signal derived from the soft reset signal
     - Bool
   * - readClockEnableWire
     - Return a signal derived from the clock enable signal
     - Bool
   * - isResetActive
     - Return True when the reset is active
     - Bool
   * - isSoftResetActive
     - Return True when the soft reset is active
     - Bool
   * - isClockEnableActive
     - Return True when the clock enable is active
     - Bool

An example is included below where a UART controller uses the frequency specification to set its clock divider:

.. code-block:: scala

   val coreClockDomain = ClockDomain(coreClock, coreReset, frequency=FixedFrequency(100e6))

   val coreArea = new ClockingArea(coreClockDomain) {
     val ctrl = new UartCtrl()
     ctrl.io.config.clockDivider := (coreClk.frequency.getValue / 57.6e3 / 8).toInt
   }

Clock domain crossing
---------------------

SpinalHDL checks at compile time that there are no unwanted/unspecified cross clock domain signal reads. If you want to read a signal that is emitted by another ``ClockDomain`` area, you should add the ``crossClockDomain`` tag to the destination signal as depicted in the following example:

.. code-block:: scala

   //             _____                        _____             _____
   //            |     |  (crossClockDomain)  |     |           |     |
   //  dataIn -->|     |--------------------->|     |---------->|     |--> dataOut
   //            | FF  |                      | FF  |           | FF  |
   //  clkA   -->|     |              clkB -->|     |   clkB -->|     |
   //  rstA   -->|_____|              rstB -->|_____|   rstB -->|_____|



   // Implementation where clock and reset pins are given by components' IO
   class CrossingExample extends Component {
     val io = new Bundle {
       val clkA = in Bool()
       val rstA = in Bool()

       val clkB = in Bool()
       val rstB = in Bool()

       val dataIn  = in Bool()
       val dataOut = out Bool()
     }

     // sample dataIn with clkA
     val area_clkA = new ClockingArea(ClockDomain(io.clkA,io.rstA)) {
       val reg = RegNext(io.dataIn) init(False)
     }

     // 2 register stages to avoid metastability issues
     val area_clkB = new ClockingArea(ClockDomain(io.clkB,io.rstB)) {
       val buf0   = RegNext(area_clkA.reg) init(False) addTag(crossClockDomain)
       val buf1   = RegNext(buf0)          init(False)
     }

     io.dataOut := area_clkB.buf1
   }


   // Alternative implementation where clock domains are given as parameters
   class CrossingExample(clkA : ClockDomain,clkB : ClockDomain) extends Component {
     val io = new Bundle {
       val dataIn  = in Bool()
       val dataOut = out Bool()
     }

     // sample dataIn with clkA
     val area_clkA = new ClockingArea(clkA) {
       val reg = RegNext(io.dataIn) init(False)
     }

     // 2 register stages to avoid metastability issues
     val area_clkB = new ClockingArea(clkB) {
       val buf0   = RegNext(area_clkA.reg) init(False) addTag(crossClockDomain)
       val buf1   = RegNext(buf0)          init(False)
     }

     io.dataOut := area_clkB.buf1
   }

In general, you can use 2 or more flip-flop driven by the destination clock domain to prevent metastability. The ``BufferCC(input: T, init: T = null, bufferDepth: Int = 2)`` function provided in ``spinal.lib._`` will instantiate the necessary flip-flops (the number of flip-flops will depends on the ``bufferDepth`` parameter) to mitigate the phenomena.

.. code-block:: scala

   class CrossingExample(clkA : ClockDomain,clkB : ClockDomain) extends Component {
     val io = new Bundle {
       val dataIn  = in Bool()
       val dataOut = out Bool()
     }

     // sample dataIn with clkA
     val area_clkA = new ClockingArea(clkA) {
       val reg = RegNext(io.dataIn) init(False)
     }

     // BufferCC to avoid metastability issues
     val area_clkB = new ClockingArea(clkB) {
       val buf1   = BufferCC(area_clkA.reg, False)
     }

     io.dataOut := area_clkB.buf1
   }

.. warning::
   The ``BufferCC`` function is only for signals of type ``Bit``, or ``Bits`` operating as Gray-coded counters (only 1 bit-flip per clock cycle), and can not used for multi-bit cross-domain processes. For multi-bit cases, it is recommended to use ``StreamFifoCC`` for high bandwidth requirements, or use ``StreamCCByToggle`` to reduce resource usage in cases where bandwidth is not critical.

Special clocking Areas
----------------------

Slow Area
^^^^^^^^^

A ``SlowArea`` is used to create a new clock domain area which is slower than the current one:

.. code-block:: scala

   class TopLevel extends Component {

     // Use the current clock domain : 100MHz
     val areaStd = new Area {    
       val counter = out(CounterFreeRun(16).value)
     }

     // Slow the current clockDomain by 4 : 25 MHz
     val areaDiv4 = new SlowArea(4) {
       val counter = out(CounterFreeRun(16).value)
     }

     // Slow the current clockDomain to 50MHz
     val area50Mhz = new SlowArea(50 MHz) {
       val counter = out(CounterFreeRun(16).value)
     }
   }

   def main(args: Array[String]) {
     new SpinalConfig(
       defaultClockDomainFrequency = FixedFrequency(100 MHz)
     ).generateVhdl(new TopLevel)
   }

.. warning::
   The clock signal used in a SlowArea is the same as the parent one. The SlowArea add instead a clock-enable signal that will slow down 
   the sampling rate inside it. In other words, ``ClockDomain.current.readClockWire`` will return the fast (parent
   domain) clock. To obtain the clock enable, use ``ClockDomain.current.readClockEnableWire``

BootReset
^^^^^^^^^

`clockDomain.withBootReset()` could specify register's resetKind as BOOT.
`clockDomain.withSyncReset()` could specify register's resetKind as SYNC (sync-reset).

.. code-block:: scala 

    class  Top extends Component {
        val io = new Bundle {
          val data = in Bits(8 bit)
          val a, b, c, d = out Bits(8 bit)
        }
        io.a  :=  RegNext(io.data) init 0
        io.b  :=  clockDomain.withBootReset()  on RegNext(io.data) init 0
        io.c  :=  clockDomain.withSyncReset()  on RegNext(io.data) init 0
        io.d  :=  clockDomain.withAsyncReset() on RegNext(io.data) init 0
    }
    SpinalVerilog(new Top)

ResetArea
^^^^^^^^^

A ``ResetArea`` is used to create a new clock domain area where a special reset signal is combined with the current clock domain reset:

.. code-block:: scala

   class TopLevel extends Component {

     val specialReset = Bool()

     // The reset of this area is done with the specialReset signal 
     val areaRst_1 = new ResetArea(specialReset, false) {
       val counter = out(CounterFreeRun(16).value)
     }

     // The reset of this area is a combination between the current reset and the specialReset
     val areaRst_2 = new ResetArea(specialReset, true) {
       val counter = out(CounterFreeRun(16).value)
     }
   }

ClockEnableArea
^^^^^^^^^^^^^^^

A ``ClockEnableArea`` is used to add an additional clock enable in the current clock domain:

.. code-block:: scala

   class TopLevel extends Component {

     val clockEnable = Bool()

     // Add a clock enable for this area 
     val area_1 = new ClockEnableArea(clockEnable) {
       val counter = out(CounterFreeRun(16).value)
     }
   }
