.. role:: raw-html-m2r(raw)
   :format: html

Component and hierarchy
=======================

Introduction
------------

Like in VHDL and Verilog, you can define components that can be used to build a design hierarchy.  However, in SpinalHDL, you don't need to bind their ports at instantiation

.. code-block:: scala

   class AdderCell extends Component {
     //Declaring external ports in a Bundle called `io` is recommended
     val io = new Bundle {
       val a, b, cin = in Bool
       val sum, cout = out Bool
     }
     //Do some logic
     io.sum := io.a ^ io.b ^ io.cin
     io.cout := (io.a & io.b) | (io.a & io.cin) | (io.b & io.cin)
   }

   class Adder(width: Int) extends Component {
     ...
     //Create 2 AdderCell
     val cell0 = new AdderCell
     val cell1 = new AdderCell
     cell1.io.cin := cell0.io.cout   //Connect cout of cell0 to cin of cell1

     // Another example which create an array of ArrayCell
     val cellArray = Array.fill(width)(new AdderCell)
     cellArray(1).io.cin := cellArray(0).io.cout   //Connect cout of cell(0) to cin of cell(1)
     ...
   }

.. tip::
   | val io = new Bundle{ ... } :
   | Declaring external ports in a Bundle called `io` is recommended. If you call your bundle `io`, Spinal will check that all elements are defined as input or output.

Input / output definition
-------------------------

The syntax to define inputs and outputs is the following:

.. list-table::
   :header-rows: 1
   :widths: 2 3 1

   * - Syntax
     - Description
     - Return
   * - in Bool/out Bool
     - Create an input Bool/output Bool
     - Bool
   * - in/out Bits/UInt/SInt[(x bit)]
     - Create an input/output of the corresponding type
     - Bits/UInt/SInt
   * - in/out(T)
     - | For all other data types, you may have to add some brackets
       | around it. Sorry this is a Scala limitation.
     - T
   * - master/slave(T)
     - | This syntax is provided by the `spinal.lib` (If you call your object `slave`,
       | then call `spinal.lib.slave` instead). T should extend IMasterSlave –
       | Some documentation is available :ref:`here <interface_eaxample_apb>`.
       | You may not actually need the brackets, so `master T` is fine as well.
     - T


There are some rules to follow with component interconnection:


* Components can only read output and input signals of child components
* Components can read their own output port values (unlike VHDL)

.. tip::
   If for some reason, you need to read signals from far away in the hierarchy (debug, temporal patch) you can do it by using the value returned by `some.where.else.theSignal.pull()`.

Pruned signals
--------------

SpinalHDL only generates things which are required to drive the outputs of your top level entity (directly or indirectly).

All other signals (the useless ones) are removed from the RTL generation and are inserted into a list of pruned signals. You can get this list via the ``printPruned`` and the ``printPrunedIo`` function on the generated ``SpinalReport``.

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle{
       val a,b = in UInt(8 bits)
       val result = out UInt(8 bits)
     }

     io.result := io.a + io.b

     val unusedSignal = UInt(8 bits)
     val unusedSignal2 = UInt(8 bits)

     unusedSignal2 := unusedSignal
   }

   object Main{
     def main(args: Array[String]) {
       SpinalVhdl(new TopLevel).printPruned()
       //This will report :
       //  [Warning] Unused wire detected : toplevel/unusedSignal : UInt[8 bits]
       //  [Warning] Unused wire detected : toplevel/unusedSignal2 : UInt[8 bits]
     }
   }

If you want to keep a pruned signal into the generated RTL for debug reasons, you can use the ``keep`` function of that signal:

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle{
       val a,b = in UInt(8 bits)
       val result = out UInt(8 bits)
     }

     io.result := io.a + io.b

     val unusedSignal = UInt(8 bits)
     val unusedSignal2 = UInt(8 bits).keep()

     unusedSignal  := 0
     unusedSignal2 := unusedSignal
   }

   object Main{
     def main(args: Array[String]) {
       SpinalVhdl(new TopLevel).printPruned()
       //This will report nothing
     }
   }

Parametrized Hardware ("Generic" in VHDL, "Parameter" in Verilog)
-----------------------------------------------------------------

If you want to parameterize your component, you can give parameters to the constructor of the component as follows: 

.. code-block:: scala

   class MyAdder(width: BitCount) extends Component {
     val io = new Bundle{
       val a,b    = in UInt(width)
       val result = out UInt(width)
     }
     io.result := io.a + io.b
   }

   object Main{
     def main(args: Array[String]) {
       SpinalVhdl(new MyAdder(32 bits))
     }
   }

I you have several parameters, it is a good practice to give a specific configuration class as follows:

.. code-block:: scala

   case class MySocConfig(axiFrequency  : HertzNumber,
                          onChipRamSize : BigInt, 
                          cpu           : RiscCoreConfig,
                          iCache        : InstructionCacheConfig)

   class MySoc(config: MySocConfig) extends Component {
       ...
   }

Synthesized component names
---------------------------

Within a module, each component has a name, called "partial name". The "full" name is built by joining every component's parent's name on "_": "io_clockDomain_reset". You can use `setName` to replace this convention with a custom name. This is especially useful when interfacing with external components. The other methods are called `getName`, `setPartialName`, `getPartialName` respectively.

When synthesized, each module gets the name of the Scala class defining it. You can override this as well with `setDefinitionName`.

.. raw:: html

   <!--
   TODO
   ### Input or Output is a basic type

   ### Input or Output is a bundle type

   ## Master/Slave interface

   -->

