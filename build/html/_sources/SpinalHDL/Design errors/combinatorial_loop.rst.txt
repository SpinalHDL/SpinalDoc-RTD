
Combinatorial loop
==================

Introduction
------------

SpinalHDL will check that there are no combinatorial loops in the design.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits) // PlayDev.scala line 831
     val b = UInt(8 bits) // PlayDev.scala line 832
     val c = UInt(8 bits)
     val d = UInt(8 bits)

     a := b
     b := c | d
     d := a
     c := 0
   }

will throw :

.. code-block:: text

   COMBINATORIAL LOOP :
     Partial chain :
       >>> (toplevel/a :  UInt[8 bits]) at ***(PlayDev.scala:831) >>>
       >>> (toplevel/d :  UInt[8 bits]) at ***(PlayDev.scala:834) >>>
       >>> (toplevel/b :  UInt[8 bits]) at ***(PlayDev.scala:832) >>>
       >>> (toplevel/a :  UInt[8 bits]) at ***(PlayDev.scala:831) >>>

     Full chain :
       (toplevel/a :  UInt[8 bits])
       (toplevel/d :  UInt[8 bits])
       (UInt | UInt)[8 bits]
       (toplevel/b :  UInt[8 bits])
       (toplevel/a :  UInt[8 bits])

A possible fix could be:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits) // PlayDev.scala line 831
     val b = UInt(8 bits) // PlayDev.scala line 832
     val c = UInt(8 bits)
     val d = UInt(8 bits)

     a := b
     b := c | d
     d := 42
     c := 0
   }

False-positives
---------------

It should be said that SpinalHDL's algorithm to detect combinatorial loops can be pessimistic, and it may give false positives.
If it is giving a false positive, you can manually disable loop checking on one signal of the loop like so:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     a := 0
     a(1) := a(0) // False positive because of this line
   }

could be fixed by :

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits).noCombLoopCheck
     a := 0
     a(1) := a(0)
   }

It should also be said that assignments such as ``(a(1) := a(0))`` can make some tools like `Verilator <https://www.veripool.org/wiki/verilator>`_ unhappy.
It may be better to use a ``Vec(Bool(), 8)`` in this case.
