
Interaction
===========

Introduction
------------

In fact, SpinalHDL is not an language, it's regular Scala library. It could seem strange at the first look, but this is a very powerful combination.

You can use the whole Scala world to help you in the description of your hardware via the SpinalHDL library, but to do that properly, it's great to understand how SpinalHDL interact with Scala.

How SpinalHDL work behind the API
---------------------------------

Basically, when you execute your SpinalHDL hardware description, each time you use an SpinalHDL function, operator, classes, it will build into your PC memory an graph that represent the netlist of your design.

Then, when the elaboration is done (Instantiation of your toplevel Component classes), SpinalHDL will do some passes on the graph that was constructed and if everything is fine, it will flush it into a VHDL or in a Verilog file.

Everything is reference
-----------------------

For example, if you define a Scala function which take as parameter a Bits, when you call it, the Bits argument will be passed as a reference. As consequence of that, if you assign that argument inside the function, it will assign it as if you have done it outside the function.

.. _hardware_type:

Hardware types
--------------

Hardware data types in SpinalHDL are the combination of two things :


* An instance of a given Scala type
* The configuration of that instance

For example ``Bits(8 bits)`` is the combination of the Scala type Bits and its 8 bits configuration (as construction parameter).

RGB example
^^^^^^^^^^^

Let's take an Rgb bundle class as example :

.. code-block:: scala

   case class Rgb(rWidth: Int, gWidth: Int, bWidth: Int) extends Bundle {
     val r = UInt(rWidth bits)
     val g = UInt(gWidth bits)
     val b = UInt(bWidth bits)
   }

It appear that the hardware data type is the combination of the Scala Rgb classes and its rWidth, gWidth, bWidth parameterization.

There is some example of usage :

.. code-block:: scala

   //Define a Rgb signal
   val myRgbSignal = Rgb(5, 6, 5)               

   //Define another Rgb signal of the same data type as the precedent one
   val myRgbCloned = cloneOf(myRgbSignal)

You can also use functions to define kind of type factory (typedef) :

.. code-block:: scala

   //Define a type factory function
   def myRgbTypeDef = Rgb(5, 6, 5)

   //Use that type factory to create a Rgb signal
   val myRgbFromTypeDef = myRgbTypeDef

Names of signals in the generated RTL
-------------------------------------

To name signals in the generated RTL, SpinalHDL use Java reflections to walk through all your component hierarchy in order to collect all references stored into class attributes and name them with their attribute name.

It's why, if you define a function, all signal's names inside the function are lost :

.. code-block:: scala

   def myFunction(arg: UInt){
     val temp = arg + 1  //You will not retrieve the `temp` signal in the generated RTL
     return temp
   }

   val value = myFunction(U"000001")  + 42

One solution if you want to keep function's internal variable's names in the generated RTL, is to use Area :

.. code-block:: scala

   def myFunction(arg: UInt) new Area {
     val temp = arg + 1  //You will not retrieve the temp signal in the generated RTL
   }

   val myFunctionCall = myFunction(U"000001")  //Will generate `temp` with `myFunctionCall_temp` as name
   val value = myFunctionCall.temp  + 42

Scala is for elaboration, SpinalHDL for hardware description
------------------------------------------------------------

For example, if you write a scala for loop to generate some hardware, it will generate the unrooled result in VHDL/Verilog.

Also, you want a constant, you should not use SpinalHDL Hardware literals but the scala ones. For example :

.. code-block:: scala

   //This is wrong, because you can't use an hardware Bool as construction parameter, which will do hierarchy violations.
   class SubComponent(activeHigh: Bool) extends Component { 
     //...
   }

   //This is right, you can use all the scala world to parameterize your hardware.
   class SubComponent(activeHigh: Boolean) extends Component {
     //...
   }

Scala elaboration capabilities (if, for, functional programming)
----------------------------------------------------------------

All the scala syntax could be used to elaborate the hardware, as instance a scala if statement could be used to enable or disable the generation of a given hardware :

.. code-block:: scala

   val counter = Reg(UInt(8 bits))
   counter := counter + 1
   if(generateAClearWhenHit42){  //Elaboration test, like an if generate in vhdl
     when(counter === 42){       //Hardware test
       counter := 0
     }
   }

The same is true for scala for loops :

.. code-block:: scala

   val value = Reg(Bits(8 bits))
   when(something){
     //Set all bits of value by using a scala for loop (evaluated during the hardware elaboration)
     for(idx <- 0 to 7){
       value(idx) := True
     }
   }

Also, all the functionnal programming stuff of SpinalHDL can be used :

.. code-block:: scala

   val values = Vec(Bits(8 bits),4)

   val valuesAre42    = vecOfBits.map(_ === 42)
   val valuesAreAll42 = valuesAre42.reduce(_ && _)

   val valuesAreEqualsToTheirIndex = vecOfBits.zipWithIndex.map{case (value, i) => value === i}
