.. _type_introduction:

==========
Data types
==========

The language provides 5 base types, and 2 composite types that can be used.

* Base types: :ref:`Bool <Bool>` , :ref:`Bits <Bits>` , :ref:`UInt <Int>` for unsigned integers, :ref:`SInt <Int>` for signed integers and :ref:`Enum <Enum>`.
* Composite types: :ref:`Bundle <Bundle>` and :ref:`Vec <Vec>`.

.. image:: /asset/picture/types.svg
   :width: 400px

In addition to the base types, Spinal has support under development for:

* :ref:`Fixed-point <fixed>` numbers (partial support)
* :ref:`Auto-range Fixed-point <afix>` numbers (add,sub,mul support)
* :ref:`Floating-point <Floating>` numbers (experimental support)


Additionally, if you want to assign a don't care value to some hardware, for instance, to provide a default value, you can use the assignDontCare API to do so. 


.. code-block:: scala

   val myBits  = Bits(8 bits)
   myBits.assignDontCare() // Will assign all the bits to 'x'


Finally, a special type is available for checking equality between a BitVector and a bit constant pattern that contains
holes defined like a bitmask (bit positions not to be compared by the equality expression).
 
Here is an example to show how you can achieve this (note the use of 'M' prefix) :

.. code-block:: scala

   val myBits  = Bits(8 bits)
   val itMatch = myBits === M"00--10--" // - for don't care value



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
   AFix
