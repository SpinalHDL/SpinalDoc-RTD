.. role:: raw-html-m2r(raw)
   :format: html

VHDL and Verilog generation
===========================

Generate VHDL and Verilog from an SpinalHDL Component
-----------------------------------------------------

To generate the VHDL from an SpinalHDL component you just need to call ``SpinalVhdl(new YourComponent)`` in a Scala ``main``.

To generate the Verilog, it's exactly the same, but with ``SpinalVerilog`` in place of ``SpinalVHDL``

.. code-block:: scala

   import spinal.core._

   //A simple component definition
   class MyTopLevel extends Component {
     //Define some input/output. Bundle like a VHDL record or a verilog struct.
     val io = new Bundle {
       val a = in Bool
       val b = in Bool
       val c = out Bool
     }

     //Define some asynchronous logic
     io.c := io.a & io.b
   }

   //This is the main that generate the VHDL and the Verilog corresponding to MyTopLevel
   object MyMain {
     def main(args: Array[String]) {
       SpinalVhdl(new MyTopLevel)
       SpinalVerilog(new MyTopLevel)
     }
   }

.. warning::
   SpinalVhdl and SpinalVerilog could need to create multiple instance of your component class. It's why its first argument is not a Component reference but a function that return a new component.

.. warning::
   SpinalVerilog implementation has start the 5 June 2016. This backend pass successfully the same regression tests than the VHDL one (RISCV CPU, Multicore and pipelined mandelbrot,UART RX/TX, Single clock fifo, Dual clock fifo, Gray counter, ..). But still, if you have any issue with this young backend, please, make a git issue.

Parametrization from Scala
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. list-table::
   :header-rows: 1
   :widths: 1 1 1 5

   * - Argument name
     - Type
     - Default
     - Description
   * - mode
     - SpinalMode
     - null
     - | Set the SpinalHDL mode.
       | Could be set to ``VHDL`` or ``Verilog``
   * - defaultConfigForClockDomains
     - ClockDomainConfig
     - | RisingEdgeClock
       | AsynchronousReset
       | ResetActiveHigh
       | ClockEnableActiveHigh
     - Set the clock configuration that will be use as default for all new ``ClockDomain``.
   * - onlyStdLogicVectorAtTopLevelIo
     - Boolean
     - false
     - Change all unsigned/signed toplevel io into std_logic_vector.
   * - defaultClockDomainFrequency
     - IClockDomainFrequency
     -  UnknownFrequency
     - Default clock frequency
   * - targetDirectory
     - String
     - Current directory
     - Directory where files are generated


And there is the syntax to specify them :

.. code-block:: scala

   SpinalConfig(mode = VHDL, targetDirectory="temp/myDesign").generate(new UartCtrl)

   // Or for Verilog in a more scalable formatting :
   SpinalConfig(
     mode = Verilog,
     targetDirectory="temp/myDesign"
   ).generate(new UartCtrl)

Parametrization from shell
^^^^^^^^^^^^^^^^^^^^^^^^^^

You can also specify generation parameters by using command line arguments.

.. code-block:: scala

   def main(args: Array[String]): Unit = {
     SpinalConfig.shell(args)(new UartCtrl)
   }

Arguments syntax is :

.. code-block:: text

   Usage: SpinalCore [options]

     --vhdl
           Select the VHDL mode
     --verilog
           Select the Verilog mode
     -d | --debug
           Enter in debug mode directly
     -o <value> | --targetDirectory <value>
           Set the target directory

Generated VHDL and Verilog
--------------------------

The way how a SpinalHDL RTL description is translated into VHDL and Verilog is important :


* Names in Scala are preserved in VHDL and Verilog.
* ``Component`` hierarchy in Scala is preserved in VHDL and Verilog.
* ``when`` statements in Scala are emitted as if statements in VHDL and Verilog
* ``switch`` statements in Scala are emitted as case statements in VHDL and Verilog in all standard cases

Organization
^^^^^^^^^^^^

When you use the VHDL generation, stuff are generated into a single file which contain tree section :


#. A package that contain enumeration's definitions
#. A package that contain function used by architectures
#. All components needed by your design

When you use the Verilog generation, stuff are generated into a single file which contain two section :


#. All enumeration defines
#. All modules needed by your design

Combinatorial logic
^^^^^^^^^^^^^^^^^^^

Scala :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val cond           = in  Bool
       val value          = in  UInt (4 bits)
       val withoutProcess = out UInt(4 bits)
       val withProcess    = out UInt(4 bits)
     }
     io.withoutProcess := io.value
     io.withProcess := 0
     when(io.cond){
       switch(io.value){
         is(U"0000"){
           io.withProcess := 8
         }
         is(U"0001"){
           io.withProcess := 9
         }
         default{
           io.withProcess := io.value+1
         }
       }
     }
   }

VHDL :

.. code-block:: vhdl

   entity TopLevel is
     port(
       io_cond : in std_logic;
       io_value : in unsigned(3 downto 0);
       io_withoutProcess : out unsigned(3 downto 0);
       io_withProcess : out unsigned(3 downto 0)
     );
   end TopLevel;

   architecture arch of TopLevel is
   begin
     io_withoutProcess <= io_value;
     process(io_cond,io_value)
     begin
       io_withProcess <= pkg_unsigned("0000");
       if io_cond = '1' then
         case io_value is
           when pkg_unsigned("0000") =>
             io_withProcess <= pkg_unsigned("1000");
           when pkg_unsigned("0001") =>
             io_withProcess <= pkg_unsigned("1001");
           when others =>
             io_withProcess <= (io_value + pkg_unsigned("0001"));
         end case;
       end if;
     end process;
   end arch;

Flipflop
^^^^^^^^

Scala :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val cond   = in Bool
       val value  = in UInt (4 bit)
       val resultA = out UInt(4 bit)
       val resultB = out UInt(4 bit)
     }

     val regWithReset = Reg(UInt(4 bits)) init(0)
     val regWithoutReset = Reg(UInt(4 bits))

     regWithReset := io.value
     regWithoutReset := 0
     when(io.cond){
       regWithoutReset := io.value
     }

     io.resultA := regWithReset
     io.resultB := regWithoutReset
   }

VHDL :

.. code-block:: vhdl

   entity TopLevel is
     port(
       io_cond : in std_logic;
       io_value : in unsigned(3 downto 0);
       io_resultA : out unsigned(3 downto 0);
       io_resultB : out unsigned(3 downto 0);
       clk : in std_logic;
       reset : in std_logic
     );
   end TopLevel;

   architecture arch of TopLevel is

     signal regWithReset : unsigned(3 downto 0);
     signal regWithoutReset : unsigned(3 downto 0);
   begin
     io_resultA <= regWithReset;
     io_resultB <= regWithoutReset;
     process(clk,reset)
     begin
       if reset = '1' then
         regWithReset <= pkg_unsigned("0000");
       elsif rising_edge(clk) then
         regWithReset <= io_value;
       end if;
     end process;

     process(clk)
     begin
       if rising_edge(clk) then
         regWithoutReset <= pkg_unsigned("0000");
         if io_cond = '1' then
           regWithoutReset <= io_value;
         end if;
       end if;
     end process;
   end arch;

VHDL and Verilog attributes
---------------------------

| In some situation, it's useful to give some attributes to some signals of a given design to obtain a specific synthesis result.
| To do that, on any signals or memory of your design you can call the following functions :

.. list-table::
   :header-rows: 1
   :widths: 1 2

   * - Syntax
     - Description
   * - addAttribute(name)
     - Add a boolean attribute with the given ``name`` set to true
   * - addAttribute(name,value)
     - Add a string attribute with the given ``name`` set to ``value``


Example :

.. code-block:: scala

   val pcPlus4 = pc + 4
   pcPlus4.addAttribute("keep")

Produced declaration in VHDL :

.. code-block:: vhdl

   attribute keep : boolean;
   signal pcPlus4 : unsigned(31 downto 0);
   attribute keep of pcPlus4: signal is true;

Produced declaration in Verilog :

.. code-block:: verilog

   (* keep *) wire [31:0] pcPlus4;
