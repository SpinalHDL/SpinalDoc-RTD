.. role:: raw-html-m2r(raw)
   :format: html

VHDL comparison
===============

Introduction
------------

This page will show the main differences between VHDL and SpinalHDL. Things will not be explained in depth.

Process
-------

Processes have no senses when you define RTL, and worst than that, they are very annoying and force you to split your code and duplicate things.

To produce the following RTL:

<img src="{{ "/images/process_rtl.svg" |  prepend: site.baseurl }}" alt="Company logo"/>

You will have to write the following VHDL:

.. code-block:: ada

     signal mySignal : std_logic;
     signal myRegister : std_logic_vector(3 downto 0);
     signal myRegisterWithReset : std_logic_vector(3 downto 0);
   begin
     process(cond)
     begin
       mySignal <= '0';
       if cond = '1' then
         mySignal <= '1';
       end if;
     end process;

     process(clk)
     begin
       if rising_edge(clk) then
         if cond = '1' then
           myRegister <= myRegister + 1;
         end if;
       end if;
     end process;

     process(clk,reset)
     begin
       if reset = '1' then
         myRegisterWithReset <= (others => '0');
       elsif rising_edge(clk) then
         if cond = '1' then
           myRegisterWithReset <= myRegisterWithReset + 1;
         end if;
       end if;
     end process;

While in SpinalHDL, it's:

.. code-block:: scala

   val mySignal = Bool
   val myRegister = Reg(UInt(4 bits))
   val myRegisterWithReset = Reg(UInt(4 bits)) init(0)

   mySignal := False
   when(cond) {
     mySignal := True
     myRegister := myRegister + 1
     myRegisterWithReset := myRegisterWithReset + 1
   }

Implicit vs explicit definitions
--------------------------------

In VHDL, when you declare a signal, you don't specify if this is a combinatorial signal or a register. Where and how you assign to it decides whether it is combinatorial or registered.

In SpinalHDL these kinds of things are explicit. Registers are defined as registers directly in their declaration.

Clock domains
-------------

In VHDL, every time you want to define a bunch of registers, you need the carry the clock and the reset wire to them. In addition, you have to hardcode everywhere how those clock and reset signals should be used (clock edge, reset polarity, reset nature (async, sync)).

In SpinalHDL you can define a ClockDomain, and then define the area of your hardware that uses it.

For example:

.. code-block:: scala

   val coreClockDomain = ClockDomain(
     clock = io.coreClk,
     reset = io.coreReset,
     config = ClockDomainConfig(
       clockEdge = RISING,
       resetKind = ASYNC,
       resetActiveLevel = HIGH
     )
   )
   val coreArea = new ClockingArea(coreClockDomain) {
     val myCoreClockedRegister = Reg(UInt(4 bit))
     // ...
     // coreClockDomain will also be applied to all sub components instantiated in the Area
     // ... 
   }

Component's internal organization
---------------------------------

In VHDL, you have the ``block`` features that allow you to define sub areas of logic inside your component. But in fact, nobody uses them, because most people don't know about them, and also because all signals defined inside them are not readable from the outside.

In SpinalHDL you have an ``Area`` feature that does it correctly:

.. code-block:: scala

   val timeout = new Area {
     val counter = Reg(UInt(8 bits)) init(0)
     val overflow = False
     when(counter =/= 100){
       counter := counter + 1
     } otherwise {
       overflow := True
     }
   }

   val core = new Area {
     when(timeout.overflow){
       timeout.counter := 0
     }
   }

Safety
------

In VHDL as in SpinalHDL, it's easy to write combinatorial loops, or to infer a latch by forgetting to drive a signal in paths of a process.

Then, to detect those issues, you can use some ``lint`` tools that will analyse your VHDL, but those tools aren't free. In SpinalHDL the ``lint`` process in integrated inside the compiler, and it won't generate the RTL code until everything is fine. It also checks clock domain crossing.

Functions and procedures
------------------------

Function and procedures are not used very often in VHDL, probably because they are very limited:


* You can only define a chunk of combinatorial hardware, or only a chunk of registers (if you call the function/procedure inside a clocked process).
* You can't define a process inside them.
* You can't instantiate a component inside them.
* The scope of what you can read/write inside them are limited.

In spinalHDL, all those limitation are removed.

An example that mixes combinatorial logic and a register in a single function:

.. code-block:: scala

   def simpleAluPipeline(op: Bits, a: UInt, b: UInt): UInt = {
     val result = UInt(8 bits)

     switch(op){
       is(0){result := a + b}
       is(1){result := a - b}
       is(2){result := a * b}
     }

     return RegNext(result)
   }

An example with the queue function inside the Stream Bundle (handshake). This function instantiates a FIFO component:

.. code-block:: scala

   class Stream[T <: Data](dataType:  T) extends Bundle with IMasterSlave with DataCarrier[T] {
     val valid = Bool
     val ready = Bool
     val payload = cloneOf(dataType)

     def queue(size: Int): Stream[T] = {
       val fifo = new StreamFifo(dataType, size)
       fifo.io.push <> this
       fifo.io.pop
     }
   }

An example where a function assigns a signal defined outside itself:

.. code-block:: scala

   val counter = Reg(UInt(8 bits)) init(0)
   counter := counter + 1

   def clear() : Unit = {
     counter := 0
   }

   when(counter > 42){
     clear()
   }

Buses and Interfaces
--------------------

VHDL is very boring when it comes to buses and interfaces. You have two options:

1) Define buses and interfaces wire by wire, each time and everywhere:

.. code-block:: ada

   PADDR   : in unsigned(addressWidth-1 downto 0);
   PSEL    : in std_logic
   PENABLE : in std_logic;
   PREADY  : out std_logic;
   PWRITE  : in std_logic;
   PWDATA  : in std_logic_vector(dataWidth-1 downto 0);
   PRDATA  : out std_logic_vector(dataWidth-1 downto 0);

2) Use records but lose parameterization (statically fixed in package), and you have to define one for each directions:

.. code-block:: ada

   P_m : in APB_M;
   P_s : out APB_S;

SpinalHDL has very strong support for bus and interface declarations with limitless parameterizations:

.. code-block:: scala

   val P = slave(Apb3(addressWidth, dataWidth))

You can also use object oriented programming to define configuration objects:

.. code-block:: scala

   val coreConfig = CoreConfig(
     pcWidth = 32,
     addrWidth = 32,
     startAddress = 0x00000000,
     regFileReadyKind = sync,
     branchPrediction = dynamic,
     bypassExecute0 = true,
     bypassExecute1 = true,
     bypassWriteBack = true,
     bypassWriteBackBuffer = true,
     collapseBubble = false,
     fastFetchCmdPcCalculation = true,
     dynamicBranchPredictorCacheSizeLog2 = 7
   )

   //The CPU has a system of plugins which allows adding new features into the core.
   //Those extensions are not directly implemented into the core, but are kind of an additive logic patch defined in a separated area.
   coreConfig.add(new MulExtension)
   coreConfig.add(new DivExtension)
   coreConfig.add(new BarrelShifterFullExtension)

   val iCacheConfig = InstructionCacheConfig(
     cacheSize =4096,
     bytePerLine =32,
     wayCount = 1,  //Can only be one for the moment
     wrappedMemAccess = true,
     addressWidth = 32,
     cpuDataWidth = 32,
     memDataWidth = 32
   )

   new RiscvCoreAxi4(
     coreConfig = coreConfig,
     iCacheConfig = iCacheConfig,
     dCacheConfig = null,
     debug = debug,
     interruptCount = interruptCount
   )

Signal declaration
------------------

VHDL forces you to define all signals on the top of your architecture, which is annoying.

.. code-block:: VHDL

     ..
     .. (many signal declarations)
     ..
     signal a : std_logic;
     ..
     .. (many signal declarations)
     ..
   begin
     ..
     .. (many logic definitions)
     ..
     a <= x & y
     ..
     .. (many logic definitions)
     ..

SpinalHDL is flexible when it comes to signal declarations.

.. code-block:: scala

   val a = Bool
   a := x & y

It also allows you to define and assign signals in a single line.

.. code-block:: scala

   val a = x & y

Component instantiation
-----------------------

VHDL is very verbose about this as you have to redefine all signals of your sub component entity, and then bind them one by one when you instantiate your component.

.. code-block:: VHDL

   divider_cmd_valid : in std_logic;
   divider_cmd_ready : out std_logic;
   divider_cmd_numerator : in unsigned(31 downto 0);
   divider_cmd_denominator : in unsigned(31 downto 0);
   divider_rsp_valid : out std_logic;
   divider_rsp_ready : in std_logic;
   divider_rsp_quotient : out unsigned(31 downto 0);
   divider_rsp_remainder : out unsigned(31 downto 0);

   divider : entity work.UnsignedDivider
     port map (
       clk             => clk,
       reset           => reset,
       cmd_valid       => divider_cmd_valid,
       cmd_ready       => divider_cmd_ready,
       cmd_numerator   => divider_cmd_numerator,
       cmd_denominator => divider_cmd_denominator,
       rsp_valid       => divider_rsp_valid,
       rsp_ready       => divider_rsp_ready,
       rsp_quotient    => divider_rsp_quotient,
       rsp_remainder   => divider_rsp_remainder
     );

SpinalHDL removes that, and allows you to access the IO of sub components in an object oriented way.

.. code-block:: scala

   val divider = new UnsignedDivider()

   //And then if you want to access IO signals of that divider:
   divider.io.cmd.valid := True
   divider.io.cmd.numerator := 42

Casting
-------

There are two annoying casting methods in VHDL:


* boolean <> std_logic (ex: To assign a signal using a condition such as ``mySignal <= myValue < 10`` is not legal)
* unsigned <> integer  (ex: To access an array)

SpinalHDL removes these casts by unifying things.

boolean/std_logic:

.. code-block:: scala

   val value = UInt(8 bits)
   val valueBiggerThanTwo = Bool
   valueBiggerThanTwo := value > 2  //value > 2 return a Bool

unsigned/integer:

.. code-block:: scala

   val array = Vec(UInt(4 bits),8)
   val sel = UInt(3 bits)
   val arraySel = array(sel) //Arrays are indexed directly by using UInt

Resizing
--------

The fact that VHDL is strict about bit size is probably a good thing.

.. code-block:: ada

   my8BitsSignal <= resize(my4BitsSignal,8);

In SpinalHDL you have two ways to do the same:

.. code-block:: scala

   //The traditional way
   my8BitsSignal := my4BitsSignal.resize(8)

   //The smart way
   my8BitsSignal := my4BitsSignal.resized

Parameterization
----------------

VHDL prior to the 2008 revision has many issues with generics. For example, you can't parameterize records, you can't parameterize arrays in the entity, and you can't have types parameters. :raw-html-m2r:`<br>`
Then VHDL 2008 came and fixed those issues. But RTL tool support for VHDL 2008 is really weak depending the vendor.

SpinalHDL has full support of generics integrated natively in its compiler, and it doesn't rely on the VHDL one.

Here is an example of parameterized data structures:

.. code-block:: scala

   val colorStream = Stream(Color(5, 6, 5)))
   val colorFifo   = StreamFifo(Color(5, 6, 5),depth = 128)
   colorFifo.io.push <> colorStream

Here is an example of a parameterized component:

.. code-block:: scala

   class Arbiter[T <: Data](payloadType: T, portCount: Int) extends Component {
     val io = new Bundle {
       val sources = Vec(slave(Stream(payloadType)), portCount)
       val sink = master(Stream(payloadType))
     }
     //...
   }

Meta hardware description
-------------------------

VHDL has kind of a closed syntax. You can't add abstraction layers on top of it.

SpinalHDL, because it's built on top of Scala, is very flexible, and allows you to define new abstraction layers very easily.

Some examples of that are the :ref:`FSM <state_machine>` tool, the :ref:`BusSlaveFactory <bus_slave_factory>` tool, and also the :ref:`JTAG <jtag>` tool.
