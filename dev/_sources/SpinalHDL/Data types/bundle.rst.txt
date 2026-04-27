
.. _Bundle:

Bundle
======

Description
^^^^^^^^^^^

The ``Bundle`` is a composite type that defines a group of named signals (of any SpinalHDL basic type) under a single name.

A ``Bundle`` can be used to model data structures, buses, and interfaces.

Declaration
^^^^^^^^^^^

The syntax to declare a bundle is as follows:

.. code-block:: scala

   case class myBundle extends Bundle {
     val bundleItem0 = AnyType
     val bundleItem1 = AnyType
     val bundleItemN = AnyType
   }

For example, a bundle holding a color could be defined as:

.. code-block:: scala

   case class Color(channelWidth: Int) extends Bundle {
     val r, g, b = UInt(channelWidth bits)
   }

You can find an :ref:`APB3 definition <example_apb3>` among the :ref:`Spinal HDL examples <example_list>`.

Conditional signals
~~~~~~~~~~~~~~~~~~~
The signals in the ``Bundle`` can be defined conditionally. 
Unless ``dataWidth`` is greater than 0, there will be no ``data`` signal in elaborated ``myBundle``, as demonstrated in the example below.

.. code-block:: scala

   case class myBundle(dataWidth: Int) extends Bundle {
     val data = (dataWidth > 0) generate (UInt(dataWidth bits))
   }

.. note::

   See also :ref:`generate <generate>` for information about this SpinalHDL method.

Operators
^^^^^^^^^

The following operators are available for the ``Bundle`` type:

Comparison
~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return type
   * - x === y
     - Equality
     - Bool
   * - x =/= y
     - Inequality
     - Bool


.. code-block:: scala

   val color1 = Color(8)
   color1.r := 0 
   color1.g := 0 
   color1.b := 0

   val color2 = Color(8)
   color2.r := 0
   color2.g := 0 
   color2.b := 0

   myBool := color1 === color2  // Compare all elements of the bundle
   // is equivalent to:
   // myBool := color1.r === color2.r && color1.g === color2.g && color1.b === color2.b

Type cast
~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x.asBits
     - Binary cast to Bits
     - Bits(w(x) bits)

.. code-block:: scala

   val color1 = Color(8)
   val myBits := color1.asBits

The elements of the bundle will be mapped into place in the order in which they are defined, LSB first.
Thus, ``r`` in ``color1`` will occupy bits 0 to 8 of ``myBits`` (LSB), followed by ``g`` and ``b`` in that order,
with ``b.msb`` also being the MSB of the resulting Bits type.

Convert Bits back to Bundle
~~~~~~~~~~~~~~~~~~~~~~~~~~~
The ``.assignFromBits`` operator can be viewed as the reverse of ``.asBits``.


.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x.assignFromBits(y)
     - Convert Bits (y) to Bundle(x)
     - Unit   
   * - x.assignFromBits(y, hi, lo)
     - Convert Bits (y) to Bundle(x) with high/low boundary
     - Unit     

The following example saves a Bundle called CommonDataBus into a circular buffer (3rd party memory), reads the Bits out later and converts them back to CommonDataBus format.

.. image:: /asset/image/bundle/CommonDataBus.png

.. code-block:: scala

   case class TestBundle () extends Component {
     val io = new Bundle {
       val we      = in     Bool()
       val addrWr  = in     UInt (7 bits)
       val dataIn  = slave  (CommonDataBus())

       val addrRd  = in     UInt (7 bits)
       val dataOut = master (CommonDataBus())
     }

     val mm = Ram3rdParty_1w_1rs (G_DATA_WIDTH = io.dataIn.getBitsWidth, 
                                  G_ADDR_WIDTH = io.addrWr.getBitsWidth, 
                                  G_VENDOR     = "Intel_Arria10_M20K")

     mm.io.clk_in    := clockDomain.readClockWire
     mm.io.clk_out   := clockDomain.readClockWire

     mm.io.we        := io.we
     mm.io.addr_wr   := io.addrWr.asBits
     mm.io.d         := io.dataIn.asBits

     mm.io.addr_rd   := io.addrRd.asBits
     io.dataOut.assignFromBits(mm.io.q)
   }

IO Element direction
^^^^^^^^^^^^^^^^^^^^

When you define a ``Bundle`` inside the IO definition of your component, you need to specify its direction.

in/out
~~~~~~

If all elements of your bundle go in the same direction you can use ``in(MyBundle())`` or ``out(MyBundle())``.

For example:

.. code-block:: scala

   val io = new Bundle {
     val input  = in (Color(8))
     val output = out(Color(8))
   }

master/slave
~~~~~~~~~~~~

If your interface obeys to a master/slave topology, you can use the ``IMasterSlave`` trait. Then you have to implement the function ``def asMaster(): Unit`` to set the direction of each element from the master's perspective. Then you can use the ``master(MyBundle())`` and ``slave(MyBundle())`` syntax in the IO definition.

There are functions defined as toXXX, such as the ``toStream`` method of the ``Flow`` class. 
These functions can usually be called by the master side. 
In addition, the fromXXX functions are designed for the slave side. 
It is common that there are more functions available for the master side than for the slave side.

For example:

.. code-block:: scala

   case class HandShake(payloadWidth: Int) extends Bundle with IMasterSlave {
     val valid   = Bool()
     val ready   = Bool()
     val payload = Bits(payloadWidth bits)

     // You have to implement this asMaster function.
     // This function should set the direction of each signals from an master point of view
     override def asMaster(): Unit = {
       out(valid, payload)
       in(ready)
     }
   }

   val io = new Bundle {
     val input  = slave(HandShake(8))
     val output = master(HandShake(8))
   }
