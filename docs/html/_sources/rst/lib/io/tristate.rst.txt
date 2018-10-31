
TriState
========

Introduction
------------

SpinalHDL doesn't support natively tristates (inout) signals at the moment. The reason of that are :


* They are not really kind of digital things
* And except for IO, they aren't used for digital design
* The tristate concept doesn't fit naturally in the SpinalHDL internal graph.

Of course it's possible to add a native tristate support, but for the moment, the clean solution to manage them is to use an Tristate Bundle bus defined in the spinal.lib :

TriState
--------

The TriState bundle is defined as following :

.. code-block:: scala

   case class TriState[T <: Data](dataType : HardType[T]) extends Bundle with IMasterSlave{
     val read,write : T = dataType()
     val writeEnable = Bool

     override def asMaster(): Unit = {
       out(write,writeEnable)
       in(read)
     }
   }

Then, as a master, you can use the ``read`` signal to read the outside value, you can use the ``writeEnable`` to enable your output, and finally use the ``write`` to set the value that you want to drive on the output.

There is an example of usage :

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
