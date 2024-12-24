.. role:: raw-html-m2r(raw)
   :format: html

VHDL equivalences
=================

Entity and architecture
-----------------------

In SpinalHDL, a VHDL entity and architecture are both defined inside a ``Component``.

Here is an example of a component which has 3 inputs (``a``, ``b``, ``c``) and an output (``result``). This component also has an ``offset`` construction parameter (like a VHDL generic).

.. code-block:: scala

   case class MyComponent(offset: Int) extends Component {
     val io = new Bundle {
       val a, b, c = in UInt(8 bits)
       val result  = out UInt(8 bits)
     }
     io.result := a + b + c + offset
   }

Then to instantiate that component, you don't need to bind it:

.. code-block:: scala

   case class TopLevel extends Component {
     ...
     val mySubComponent = MyComponent(offset = 5)

     ...

     mySubComponent.io.a := 1
     mySubComponent.io.b := 2
     mySubComponent.io.c := 3
     ??? := mySubComponent.io.result

     ...
   }

Data types
----------

SpinalHDL data types are similar to the VHDL ones:

.. list-table::
   :header-rows: 1

   * - VHDL
     - SpinalHDL
   * - std_logic
     - Bool
   * - std_logic_vector
     - Bits
   * - unsigned
     - UInt
   * - signed
     - SInt


| In VHDL, to define an 8 bit ``unsigned`` you have to give the range of bits ``unsigned(7 downto 0)``,
| whereas in SpinalHDL you simply supply the number of bits ``UInt(8 bits)``.

.. list-table::
   :header-rows: 1

   * - VHDL
     - SpinalHDL
   * - records
     - Bundle
   * - array
     - Vec
   * - enum
     - SpinalEnum

Here is an example of the SpinalHDL ``Bundle`` definition. ``channelWidth`` is a construction parameter, like VHDL generics, but for data structures:

.. code-block:: scala

   case class RGB(channelWidth: Int) extends Bundle {
     val r, g, b = UInt(channelWidth bits)
   }

Then for example, to instantiate a ``Bundle``, you need to write ``val myColor = RGB(channelWidth=8)``.

Signal
------

Here is an example about signal instantiations:

.. code-block:: scala

   case class MyComponent(offset: Int) extends Component {
     val io = new Bundle {
       val a, b, c = UInt(8 bits)
       val result  = UInt(8 bits)
     }
     val ab = UInt(8 bits)
     ab := a + b

     val abc = ab + c            // You can define a signal directly with its value
     io.result := abc + offset
   }

Assignments
-----------

In SpinalHDL, the ``:=`` assignment operator is equivalent to the VHDL signal assignment (``<=``):

.. code-block:: scala

   val myUInt = UInt(8 bits)
   myUInt := 6

Conditional assignments are done like in VHDL by using ``if``/``case`` statements:

.. code-block:: scala

   val clear   = Bool()
   val counter = Reg(UInt(8 bits))

   when(clear) {
     counter := 0
   }.elsewhen(counter === 76) {
     counter := 79
   }.otherwise {
     counter(7) := ! counter(7)
   }

   switch(counter) {
     is(42) {
       counter := 65
     }
     default {
       counter := counter + 1
     }
   }

Literals
--------

Literals are a little bit different than in VHDL:

.. code-block:: scala

   val myBool = Bool()
   myBool := False
   myBool := True
   myBool := Bool(4 > 7)

   val myUInt = UInt(8 bits)
   myUInt := "0001_1100"
   myUInt := "xEE"
   myUInt := 42
   myUInt := U(54,8 bits)
   myUInt := ((3 downto 0) -> myBool, default -> true)
   when(myUInt === U(myUInt.range -> true)) {
     myUInt(3) := False
   }

Registers
---------

In SpinalHDL, registers are explicitly specified while in VHDL registers are inferred. Here is an example of SpinalHDL registers:

.. code-block:: scala

   val counter = Reg(UInt(8 bits))  init(0)  
   counter := counter + 1   // Count up each cycle

   // init(0) means that the register should be initialized to zero when a reset occurs

Process blocks
--------------

Process blocks are a simulation feature that is unnecessary to design RTL. It's why SpinalHDL doesn't contain any feature analogous to process blocks, and you can assign what you want, where you want.

.. code-block:: scala

   val cond = Bool()
   val myCombinatorial = Bool()
   val myRegister = UInt(8 bits)

   myCombinatorial := False
   when(cond) {
     myCombinatorial := True
     myRegister = myRegister + 1
   }
