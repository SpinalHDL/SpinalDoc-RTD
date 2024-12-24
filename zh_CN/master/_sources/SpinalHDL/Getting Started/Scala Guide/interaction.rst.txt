
Interaction
===========

Introduction
------------

SpinalHDL is, in fact, not an language: it's a regular Scala library. This could seem strange at first glance, but it is a very powerful combination.

You can use the whole Scala world to help you in the description of your hardware via the SpinalHDL library, but to do that properly, it's important to understand how SpinalHDL interacts with Scala.

How SpinalHDL works behind the API
----------------------------------

When you execute your SpinalHDL hardware description, each time you use SpinalHDL functions, operators, or classes, it will build an in-memory graph that represents the netlist of your design.

Then, when the elaboration is done (instantiation of your top-level ``Component`` classes), SpinalHDL will do some passes on the graph that was constructed, and if everything is fine, it will flush that graph into a VHDL or Verilog file.

Everything is a reference
-------------------------

For example, if you define a Scala function which takes a parameter of type ``Bits``, when you call it, it will be passed as a reference. As consequence of that, if you assign that argument inside the function, it has the same effect on the underlying ``Bits`` object as if you had assigned to it outside the function.

.. _hardware_type:

Hardware types
--------------

Hardware data types in SpinalHDL are the combination of two things:


* An instance of a given Scala type
* The configuration of that instance

For example ``Bits(8 bits)`` is the combination of the Scala type ``Bits`` and its ``8 bits`` configuration (as a construction parameter). 

.. note:

   The ``8 bits`` expression is converted by Scala into ``BitCount(8)`` and the BitCount object passed as the
   constructor argument, this a kind of syntactic sugar provided by Scala that
   enhances type-safety by being more expressive.  This would be an example of
   why SpinalHDL might look like a whole new language.

RGB example
^^^^^^^^^^^

Let's take an Rgb bundle class as example:

.. code-block:: scala

   case class Rgb(rWidth: Int, gWidth: Int, bWidth: Int) extends Bundle {
     val r = UInt(rWidth bits)
     val g = UInt(gWidth bits)
     val b = UInt(bWidth bits)
   }

The hardware data type here is the combination of the Scala ``Rgb`` class and its ``rWidth``, ``gWidth``, and ``bWidth`` parameterization.

Here is an example of usage:

.. code-block:: scala

   // Define an Rgb signal
   val myRgbSignal = Rgb(5, 6, 5)               

   // Define another Rgb signal of the same data type as the preceding one
   val myRgbCloned = cloneOf(myRgbSignal)

You can also use functions to define various kinds of type factories (typedef):

.. code-block:: scala

   // Define a type factory function
   def myRgbTypeDef = Rgb(5, 6, 5)

   // Use that type factory to create an Rgb signal
   val myRgbFromTypeDef = myRgbTypeDef

Names of signals in the generated RTL
-------------------------------------

To name signals in the generated RTL, SpinalHDL uses Java reflections to walk through your entire component hierarchy, collecting all references stored inside the class attributes, and naming them with their attribute name.

This is why the names of every signal defined inside a function are lost:

.. code-block:: scala

   def myFunction(arg: UInt) {
     val temp = arg + 1  // You will not retrieve the `temp` signal in the generated RTL
     return temp
   }

   val value = myFunction(U"000001") + 42

One solution if you want preserve the names of the internal variables in the generated RTL, is to use ``Area``:

.. code-block:: scala

   def myFunction(arg: UInt) new Area {
     val temp = arg + 1  // You will not retrieve the temp signal in the generated RTL
   }

   val myFunctionCall = myFunction(U"000001")  // Will generate `temp` with `myFunctionCall_temp` as the name
   val value = myFunctionCall.temp  + 42

Scala is for elaboration, SpinalHDL for hardware description
------------------------------------------------------------

For example, if you write a Scala for loop to generate some hardware, it will generate the unrolled result in VHDL/Verilog.

Also, if you want a constant, you should not use SpinalHDL hardware literals but the Scala ones. For example:

.. code-block:: scala

   // This is wrong, because you can't use a hardware Bool as construction parameter. (It will cause hierarchy violations.)
   class SubComponent(activeHigh: Bool) extends Component { 
     // ...
   }

   // This is right, you can use all the Scala world to parameterize your hardware.
   class SubComponent(activeHigh: Boolean) extends Component {
     // ...
   }

Scala elaboration capabilities (if, for, functional programming)
----------------------------------------------------------------

All of Scala's syntax can be used to elaborate hardware designs, for instance, a Scala ``if`` statement could be used to enable or disable the generation of hardware:

.. code-block:: scala

   val counter = Reg(UInt(8 bits))
   counter := counter + 1
   if(generateAClearWhenHit42) {  // Elaboration test, like an if generate in vhdl
     when(counter === 42) {       // Hardware test
       counter := 0
     }
   }

The same is true for Scala ``for`` loops:

.. code-block:: scala

   val value = Reg(Bits(8 bits))
   when(something) {
     // Set all bits of value by using a Scala for loop (evaluated during hardware elaboration)
     for(idx <- 0 to 7) {
       value(idx) := True
     }
   }

Also, functional programming techniques can be used with many SpinalHDL types:

.. code-block:: scala

   val values = Vec(Bits(8 bits), 4)

   val valuesAre42    = values.map(_ === 42)
   val valuesAreAll42 = valuesAre42.reduce(_ && _)

   val valuesAreEqualToTheirIndex = values.zipWithIndex.map{ case (value, i) => value === i }
