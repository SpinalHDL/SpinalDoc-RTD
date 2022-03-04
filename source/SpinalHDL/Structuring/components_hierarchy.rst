.. role:: raw-html-m2r(raw)
   :format: html

Component and hierarchy
=======================

Introduction
------------

Like in VHDL and Verilog, you can define components that can be used to build a design hierarchy. However, in SpinalHDL, you don't need to bind their ports at instantiation:

.. code-block:: scala

   class AdderCell() extends Component {
     // Declaring external ports in a Bundle called `io` is recommended
     val io = new Bundle {
       val a, b, cin = in Bool()
       val sum, cout = out Bool()
     }
     // Do some logic
     io.sum := io.a ^ io.b ^ io.cin
     io.cout := (io.a & io.b) | (io.a & io.cin) | (io.b & io.cin)
   }

   class Adder(width: Int) extends Component {
     ...
     // Create 2 AdderCell instances
     val cell0 = new AdderCell()
     val cell1 = new AdderCell()
     cell1.io.cin := cell0.io.cout   // Connect cout of cell0 to cin of cell1

     // Another example which creates an array of ArrayCell instances
     val cellArray = Array.fill(width)(new AdderCell())
     cellArray(1).io.cin := cellArray(0).io.cout   // Connect cout of cell(0) to cin of cell(1)
     ...
   }

.. tip::
   | ``val io = new Bundle { ... }``
   | Declaring external ports in a ``Bundle`` called ``io`` is recommended. If you name your bundle ``io``, SpinalHDL will check that all of its elements are defined as inputs or outputs.

Input / output definition
-------------------------

The syntax to define inputs and outputs is as follows:

.. list-table::
   :header-rows: 1
   :widths: 2 3 1

   * - Syntax
     - Description
     - Return
   * - in Bool()/out Bool()
     - Create an input Bool/output Bool
     - Bool
   * - in/out Bits/UInt/SInt[(x bit)]
     - Create an input/output of the corresponding type
     - Bits/UInt/SInt
   * - in/out(T)
     - For all other data types, you may have to add some brackets around it. Sorry, this is a Scala limitation.
     - T
   * - master/slave(T)
     - This syntax is provided by the ``spinal.lib`` library (If you annotate your object with the ``slave`` syntax, then import ``spinal.lib.slave`` instead).
       T should extend ``IMasterSlave`` – Some documentation is available :ref:`here <interface_example_apb>`. You may not actually need the brackets, so ``master T`` is fine as well.
     - T


There are some rules to follow with component interconnection:


* Components can only **read** output and input signals of child components.
* Components can read their own output port values (unlike in VHDL).

.. tip::
   If for some reason you need to read signals from far away in the hierarchy (such as for debugging or temporal patches), you can do it by using the value returned by ``some.where.else.theSignal.pull()``

Pruned signals
--------------

SpinalHDL only generates things which are directly or indirectly required to drive the outputs of your top-level entity.

All other signals (the useless ones) are removed from the RTL generation and are inserted into a list of pruned signals. You can get this list via the ``printPruned`` and the ``printPrunedIo`` functions on the generated ``SpinalReport`` object:

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a,b = in UInt(8 bits)
       val result = out UInt(8 bits)
     }

     io.result := io.a + io.b

     val unusedSignal = UInt(8 bits)
     val unusedSignal2 = UInt(8 bits)

     unusedSignal2 := unusedSignal
   }

   object Main {
     def main(args: Array[String]) {
       SpinalVhdl(new TopLevel).printPruned()
       //This will report :
       //  [Warning] Unused wire detected : toplevel/unusedSignal : UInt[8 bits]
       //  [Warning] Unused wire detected : toplevel/unusedSignal2 : UInt[8 bits]
     }
   }

If you want to keep a pruned signal in the generated RTL for debugging reasons, you can use the ``keep`` function of that signal:

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a, b = in UInt(8 bits)
       val result = out UInt(8 bits)
     }

     io.result := io.a + io.b

     val unusedSignal = UInt(8 bits)
     val unusedSignal2 = UInt(8 bits).keep()

     unusedSignal  := 0
     unusedSignal2 := unusedSignal
   }

   object Main {
     def main(args: Array[String]) {
       SpinalVhdl(new TopLevel).printPruned()
       // This will report nothing
     }
   }

Parametrized Hardware ("Generic" in VHDL, "Parameter" in Verilog)
-----------------------------------------------------------------

If you want to parameterize your component, you can give parameters to the constructor of the component as follows:

.. code-block:: scala

   class MyAdder(width: BitCount) extends Component {
     val io = new Bundle {
       val a, b   = in UInt(width)
       val result = out UInt(width)
     }
     io.result := io.a + io.b
   }

   object Main {
     def main(args: Array[String]) {
       SpinalVhdl(new MyAdder(32 bits))
     }
   }

If you have several parameters, it is a good practice to give a specific configuration class as follows:

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

Within a module, each component has a name, called a "partial name".
The "full" name is built by joining every component's parent name with "_", for example: ``io_clockDomain_reset``.
You can use ``setName`` to replace this convention with a custom name.
This is especially useful when interfacing with external components.
The other methods are called ``getName``, ``setPartialName``, and ``getPartialName`` respectively.

When synthesized, each module gets the name of the Scala class defining it. You can override this as well with ``setDefinitionName``.

.. raw:: html

   <!--
   TODO
   ### Input or Output is a basic type

   ### Input or Output is a bundle type

   ## Master/Slave interface

   -->
