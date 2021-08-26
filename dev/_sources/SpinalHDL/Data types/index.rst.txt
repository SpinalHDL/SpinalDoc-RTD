==========
Data types
==========
.. toctree::
   :hidden:

   bool
   bits
   Int
   enum
   bundle
   Vec
   Fix
   Floating


.. _type_introduction:

Introduction
============

The language provides 5 base types, and 2 composite types that can be used.


* Base types: :ref:`Bool <Bool>` , :ref:`Bits <Bits>` , :ref:`UInt <Int>` for unsigned integers, :ref:`SInt <Int>` for signed integers and :ref:`Enum <Enum>`.
* Composite types: :ref:`Bundle <Bundle>` and :ref:`Vec <Vec>`.

.. image:: /asset/picture/types.svg
   :width: 400px


In addition to the base types, Spinal has support under development for:

* :ref:`Fixed-point <fixed>` numbers (partial support)
* :ref:`Floating-point <Floating>` numbers (experimental support)

Finally, a special type is available for checking equality between a BitVector and a bits constant that contains holes (don't care values). An example is shown below:

.. code-block:: scala

   val myBits  = Bits(8 bits)
   val itMatch = myBits === M"00--10--" // - don't care value
