.. _utils:

Utils
=====

General
-------

Many tools and utilities are present in :ref:`spinal.lib <lib_introduction>` but some are already present in the SpinalHDL Core.

.. list-table::
   :header-rows: 1
   :widths: 3 1 4

   * - Syntax
     - Return
     - Description
   * - ``widthOf(x : BitVector)``
     - Int
     - Return the width of a Bits/UInt/SInt signal
   * - ``log2Up(x : BigInt)``
     - Int
     - Return the number of bits needed to represent ``x`` states
   * - ``isPow2(x : BigInt)``
     - Boolean
     - Return true if ``x`` is a power of two
   * - ``roundUp(that : BigInt, by : BigInt)``
     - BigInt
     - Return the first ``by`` multiply from ``that`` (included)
   * - ``Cat(x: Data*)``
     - Bits
     - Concatenate all arguments, from MSB to LSB, see `Cat`_
   * - ``Cat(x: Iterable[Data])``
     - Bits
     - Conactenate arguments, from LSB to MSB, see `Cat`_

.. _Cat:

Cat
^^^

As listed above, there are two version of ``Cat``. Both versions concatenate the signals they contain, with a subtle difference:

- ``Cat(x: Data*)`` takes an arbitrary number of hardware signals as parameters.
  It mimics other HDLs and the leftmost parameter becomes the MSB of the resulting ``Bits``, the rightmost the LSB side. Said differently:
  the input is concatenated in the order as written.
- ``Cat(x: Iterable[Data])`` which takes a single Scala iterable collection (Seq / Set / List / ...) containing hardware signals.
  This version places the first element of the list into the LSB, and the last into the MSB.

This seeming difference comes mostly from the convention that ``Bits`` are written from the hightest index to the lowest index, while
Lists are written down starting from index 0 to the highest index. ``Cat`` places index 0 of both conventions at the LSB.

.. code-block:: scala

   val bit0, bit1, bit2 = Bool()

   val first = Cat(bit2, bit1, bit0)

   // is equivalent to
   
   val signals = List(bit0, bit1, bit2)
   val second = Cat(signals)

Cloning hardware datatypes
--------------------------

You can clone a given hardware data type by using the ``cloneOf(x)`` function.
It will return a new instance of the same Scala type and parameters.

For example:

.. code-block:: scala

   def plusOne(value : UInt) : UInt = {
     // Will provide new instance of a UInt with the same width as ``value``
     val temp = cloneOf(value)
     temp := value + 1
     return temp
   }

   // treePlusOne will become a 8 bits value
   val treePlusOne = plusOne(U(3, 8 bits))

You can get more information about how hardware data types are managed on the :ref:`Hardware types page <hardware_type>`.

.. note::
   If you use the ``cloneOf`` function on a ``Bundle``, this ``Bundle`` should be a ``case class`` or should override the clone function internally.

.. code-block:: scala

   // An example of a regular 'class' with 'override def clone()' function
   class MyBundle(ppp : Int) extends Bundle {
      val a = UInt(ppp bits)
      override def clone = new MyBundle(ppp)
    }
    val x = new MyBundle(3)
    val typeDef = HardType(new MyBundle(3))
    val y = typeDef()

    cloneOf(x) // Need clone method, else it errors
    cloneOf(y) // Is ok


Passing a datatype as construction parameter
--------------------------------------------

Many pieces of reusable hardware need to be parameterized by some data type.
For example if you want to define a FIFO or a shift register, you need a parameter to specify which kind of payload you want for the component.

There are two similar ways to do this.

The old way
^^^^^^^^^^^

A good example of the old way to do this is in this definition of a ``ShiftRegister`` component:

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

   val shiftReg = ShiftRegister(Bits(32 bits), depth = 8)

As you can see, the raw hardware type is directly passed as a construction parameter.
Then each time you want to create an new instance of that kind of hardware data type, you need to use the ``cloneOf(...)`` function.
Doing things this way is not super safe as it's easy to forget to use ``cloneOf``.

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

   val shiftReg = ShiftRegister(Bits(32 bits), depth = 8)

Notice how the example above uses a ``HardType`` wrapper around the raw data type ``T``, which is a "blueprint" definition of a hardware data type.
This way of doing things is easier to use than the "old way", because to create a new instance of the hardware data type you only need to call the ``apply`` function of that ``HardType`` (or in other words, just add parentheses after the parameter).

Additionally, this mechanism is completely transparent from the point of view of the user, as a hardware data type can be implicitly converted into a ``HardType``.

Frequency and time
------------------

SpinalHDL has a dedicated syntax to define frequency and time values:

.. code-block:: scala

   val frequency = 100 MHz	// infers type TimeNumber
   val timeoutLimit = 3 ms	// infers type HertzNumber
   val period = 100 us		// infers type TimeNumber

   val periodCycles = frequency * period             // infers type BigDecimal
   val timeoutCycles = frequency * timeoutLimit      // infers type BigDecimal

| For time definitions you can use following postfixes to get a ``TimeNumber``:
| ``fs``, ``ps``, ``ns``, ``us``, ``ms``, ``sec``, ``mn``, ``hr``

| For time definitions you can use following postfixes to get a ``HertzNumber``:
| ``Hz``, ``KHz``, ``MHz``, ``GHz``, ``THz``

``TimeNumber`` and ``HertzNumber`` are based on the ``PhysicalNumber`` class which use  scala ``BigDecimal`` to store numbers.

Binary prefix
-------------

SpinalHDL allows the definition of integer numbers using binary prefix notation according to IEC.

.. code-block:: scala

   val memSize = 512 MiB      // infers type BigInt
   val dpRamSize = 4 KiB      // infers type BigInt

The following binary prefix notations are available:

.. list-table::
   :header-rows: 1
   :widths: 1 2

   * - Binary Prefix
     - Value
   * - Byte, Bytes
     - 1
   * - KiB
     - 1024 == 1 << 10
   * - MiB
     - 1024\ :sup:`2` == 1 << 20
   * - GiB
     - 1024\ :sup:`3` == 1 << 30
   * - TiB
     - 1024\ :sup:`4` == 1 << 40
   * - PiB
     - 1024\ :sup:`5` == 1 << 50
   * - EiB
     - 1024\ :sup:`6` == 1 << 60
   * - ZiB
     - 1024\ :sup:`7` == 1 << 70
   * - YiB
     - 1024\ :sup:`8` == 1 << 80


Of course, BigInt can also be printed as a string in bytes unit. ``BigInt(1024).byteUnit``.

.. code-block:: scala

   val memSize = 512 MiB
    
   println(memSize)
   >> 536870912 

   println(memSize.byteUnit)
   >> 512MiB

   val dpRamSize = BigInt("123456789", 16)

   println(dpRamSize.byteUnit())
   >> 4GiB+564MiB+345KiB+905Byte

   println((32.MiB + 12.KiB + 223).byteUnit())
   >> 32MiB+12KiB+223Byte

   println((32.MiB + 12.KiB + 223).byteUnit(ceil = true))
   >> 33~MiB



