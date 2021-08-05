
.. _type_introduction:

Introduction
============

The language provides 5 base types and 2 composite types that can be used.


* Base types: :ref:`Bool <Bool>` , :ref:`Bits <Bits>` , :ref:`UInt <Int>` for unsigned integers, :ref:`SInt <Int>` for signed integers and :ref:`Enum <Enum>`.
* Composite types: :ref:`Bundle <Bundle>` and :ref:`Vec <Vec>`.

.. image:: /asset/picture/types.svg
   :width: 400px


In addition to the base types Spinal supports Fixed point that is documented :ref:`there <fixed>` and floating point that is actually under development :ref:`there <Floating>`.

Finally, a special type is available for checking equality between a BitVector and a bits constant that contain hole (don't care values). Below, there is an example :

.. code-block:: scala

   val myBits  = Bits(8 bits)
   val itMatch = myBits === M"00--10--" // - don't care value
