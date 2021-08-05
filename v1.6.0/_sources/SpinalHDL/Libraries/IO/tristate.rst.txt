.. _section-tristate:

TriState
========

Introduction
------------

Tri-state signals are weird to handle in many cases:

* They are not really kind of digital things
* And except for IO, they aren't used for digital design
* The tristate concept doesn't fit naturally in the SpinalHDL internal graph.

SpinalHDL provides two different abstractions for tristate signals. The ``TriState`` bundle and :ref:`section-analog_and_inout` signals.
Both serve different purposes:

* TriState should be used for most purposes, especially within a design. The bundle contains an additional signal to carry the current direction.
* ``Analog`` and ``inout`` should be used for drivers on the device boundary and in some other special cases. See the referenced documentation page for more details.

As stated above, the recommended approach is to use ``TriState`` within a design. On the top-level the ``TriState`` bundle is then assigned to an analog inout to get the
synthesis tools to infer the correct I/O driver. This can be done automatically done via the :ref:`InOutWrapper <section-analog_and_inout>` or manually if needed.

TriState
--------

The TriState bundle is defined as following :

.. code-block:: scala

   case class TriState[T <: Data](dataType : HardType[T]) extends Bundle with IMasterSlave{
     val read,write : T = dataType()
     val writeEnable = Bool()

     override def asMaster(): Unit = {
       out(write,writeEnable)
       in(read)
     }
   }

A master can use the ``read`` signal to read the outside value, the ``writeEnable`` to enable the output,
and finally use ``write`` to set the value that is driven on the output.

There is an example of usage:

.. code-block:: scala

   val io = new Bundle{
     val dataBus = master(TriState(Bits(32 bits)))
   }

   io.dataBus.writeEnable := True
   io.dataBus.write := 0x12345678
   when(io.dataBus.read === 42){

   }

TriStateArray
-------------

In some case, you need to have the control over the output enable of each individual pin (Like for GPIO). In this range of cases, you can use the TriStateArray bundle.

It is defined as following :

.. code-block:: scala

   case class TriStateArray(width : BitCount) extends Bundle with IMasterSlave{
     val read,write,writeEnable = Bits(width)

     override def asMaster(): Unit = {
       out(write,writeEnable)
       in(read)
     }
   }

It is the same than the TriState bundle, except that the ``writeEnable`` is an Bits to control each output buffer.

There is an example of usage :

.. code-block:: scala

   val io = new Bundle{
     val dataBus = master(TriStateArray(32 bits)
   }

   io.dataBus.writeEnable := 0x87654321
   io.dataBus.write := 0x12345678
   when(io.dataBus.read === 42){

   }
