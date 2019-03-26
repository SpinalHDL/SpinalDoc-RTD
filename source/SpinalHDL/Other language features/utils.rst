.. role:: raw-html-m2r(raw)
   :format: html

.. _utils:

Utils
=====

General
-------

Many tools and utilities are present in :ref:`spinal.lib <lib_introduction>` but some are already present in SpinalHDL Core.

.. list-table::
   :header-rows: 1
   :widths: 3 1 4

   * - Syntax
     - Return
     - Description
   * - widthOf(x : BitVector)
     - Int
     - Return the width of a Bits/UInt/SInt signal
   * - log2Up(x : BigInt)
     - Int
     - Return the number of bits needed to represent x states
   * - isPow2(x : BigInt)
     - Boolean
     - Return true if x is a power of two
   * - roundUp(that : BigInt, by : BigInt)
     - BigInt
     - Return the first ``by`` multiply from ``that`` (included)
   * - Cat(x : Data*)
     - Bits
     - Concatenate all arguments, the first in MSB, the last in LSB


Cloning hardware datatypes
--------------------------

You can clone a given hardware data type by using the ``cloneOf(x)`` function. It will return a new instance of the same Scala type and the same parameterization as its argument.

For example :

.. code-block:: scala

   def plusOne(value : UInt) : UInt = {
     //Will recreate a UInt with the same width than ``value``
     val temp = cloneOf(value)
     temp := value + 1
     return temp
   }

   //treePlusOne will become a 8 bits value
   val treePlusOne = plusOne(U(3,8 bits))

You can get more information about how hardware data types are managed :ref:`here <hardware_type>`

.. note::
   If you use the cloneOf function on a Bundle, this Bundle should be a case class or should override the clone function internally.


Passing a datatype as construction parameter
--------------------------------------------

Many pieces of reusable hardware need to be parameterized by some data type. For example if you want to define a FIFO or a shift register, you need a parameter to specify which kind of payload you want for the component.

There are two similar ways to do this.

The old way
^^^^^^^^^^^

A good example of the old way to do that is in this definition of a ShiftRegister:

.. code-block:: scala

   case class ShiftRegister[T <: Data](dataType: T, depth: Int) extends Component {
     val io = new Bundle {
       val input  = in (cloneOf(dataType))
       val output = out(cloneOf(dataType))
     }
     // ...
   }

And here is how you can instantiate the component:

.. code-block:: scala

   val shiftReg = ShiftRegister(Bits(32 bits),depth = 8)

As you can see, the raw hardware type is directly passed as a construction parameter. Then each time you want to create an new instance of that kind of hardware data type, you need to use the cloneOf(...) function. Doing things this way is not super safe as it's easy to forget to use cloneOf().

The safe way
^^^^^^^^^^^^

An example of the safe way to pass a data type parameter is as follows:

.. code-block:: scala

   case class ShiftRegister[T <: Data](dataType: HardType[T], depth: Int) extends Component {
     val io = new Bundle {
       val input  = in (dataType())
       val output = out(dataType())
     }
     // ...
   }

And here is how you instantiate the component (exactly the same as before):

.. code-block:: scala

   val shiftReg = ShiftRegister(Bits(32 bits),depth = 8)

Notice how the example above uses a HardType wrapper around the raw data type ``T``, which is kind of a blueprint definition of an hardware data type. This way of doing things is easier to use than the "old way", because to create a new instance of the hardware data type you just need to call the ``apply`` function of that HardType (or in other words, just add parentheses after the parameter).

Additionally, this mechanism is completely transparent from the point of view of the user, as a hardware data type can be implicitly converted into an HardType.

Frequency and time
------------------

SpinalHDL HDL has a dedicated syntax to defne frequency and time values:

.. code-block:: scala

   val frequency = 100 MHz
   val timeoutLimit = 3 ms
   val period = 100 us

   val periodCycles = frequency*period
   val timeoutCycles = frequency*timeoutLimit

| For time definitions you can use following postfixes to get a ``TimeNumber`` :
| fs, ps, ns, us, ms, sec, mn, hr

| For time definitions you can use following postfixes to get a ``HertzNumber`` :
| Hz, KHz, MHz, GHz, THz

``TimeNumber`` and ``HertzNumber`` are based on the ``PhysicalNumber`` class which uses scala ``BigDecimal`` to store numbers.
