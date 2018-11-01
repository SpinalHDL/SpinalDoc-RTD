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

   * - Syntax
     - Return
     - Description
   * - widthOf(x : BitVector)
     - Int
     - Return the width of a Bits/UInt/SInt signal
   * - log2Up(x : BigInt)
     - Int
     - Return the number of bit needed to represent x states
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

You can clone a given hardware data type by using the ``cloneOf(x)`` function. It will return you a new instance of the same Scala type and the same parameterization than ``x``.

For example :

.. code-block:: scala

   def plusOne(value : UInt) : UInt = {
     //Will recreate a UInt with the same width than `value`
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

In many chunk of reusable hardware, we need to give a parameterizable data type. For example if you want to define a FIFO or a shift register, you need a data type parameter to specify which kind of payload you want for the component.

To do that there is two very similar ways.

The old way
^^^^^^^^^^^

There is an example of the old way to do that in the case of a ShiftRegister definition :

.. code-block:: scala

   case class ShiftRegister[T <: Data](dataType: T, depth: Int) extends Component {
     val io = new Bundle {
       val input  = in (cloneOf(dataType))
       val output = out(cloneOf(dataType))
     }
     // ...
   }

And there is how you can instantiate that component :

.. code-block:: scala

   val shiftReg = ShiftRegister(Bits(32 bits),depth = 8)

As you can see, the raw hardware type is directly passed as a construction parameter. And they each time you want to create an new instance of that kind of hardware data type, you need to use the cloneOf(...) function. But this way of doing things is not ultra safe, because you can forget the cloneOf function easily,

The safe way
^^^^^^^^^^^^

There is an example of the safe way to do that in the case of a ShiftRegister definition :

.. code-block:: scala

   case class ShiftRegister[T <: Data](dataType: HardType[T], depth: Int) extends Component {
     val io = new Bundle {
       val input  = in (dataType())
       val output = out(dataType())
     }
     // ...
   }

And there is how you can instantiate that component (which is exactly the same than before):

.. code-block:: scala

   val shiftReg = ShiftRegister(Bits(32 bits),depth = 8)

So as you can see, it use an HardType wrapper, which is kind of blueprint definition of an hardware data type. This way of doing things is easier to use than the "old way", because to create a new instance of the hardware data type you just need to call the ``apply`` function of that HardType (which mean, just adding brackets after the HardType instance) .

Also this mechanism is completely transparent from the point of view of the user, an hardware data type could be implicitly converted into an HardType.

Frequency and time
------------------

SpinalHDL HDL has a dedicated syntax to defne frequencies and times value :

.. code-block:: scala

   val frequency = 100 MHz
   val timeoutLimit = 3 ms
   val period = 100 us

   val periodCycles = frequency*period
   val timeoutCycles = frequency*timeoutLimit

For time definition you can use following postfixes to get an ``TimeNumber``\ :\ :raw-html-m2r:`<br>`
fs, ps, ns, us, ms, sec, mn, hr

For time definition you can use following postfixes to get an ``HertzNumber`` :\ :raw-html-m2r:`<br>`
Hz, KHz, MHz, GHz, THz

``TimeNumber`` and ``HertzNumber`` are based on the ``PhysicalNumber`` class which use  scala ``BigDecimal`` to store numbers.
