
Combinational loop
==================

Introduction
------------

SpinalHDL will check that there is no combinatorial loop across all the design.

Example
-------

The following code :

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits) //PlayDev.scala line 831
     val b = UInt(8 bits) //PlayDev.scala line 832
     val c = UInt(8 bits)
     val d = UInt(8 bits)

     a := b
     b := c | d
     d := a
     c := 0
   }

will throw :

.. code-block::

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

A fix could be :

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits) //PlayDev.scala line 831
     val b = UInt(8 bits) //PlayDev.scala line 832
     val c = UInt(8 bits)
     val d = UInt(8 bits)

     a := b
     b := c | d
     d := 42
     c := 0
   }

False-positive
--------------

It should be known that currently SpinalHDL is tracking combinatorial loop in a pessimistic way. If it give you a false positive combinatorial loop, you can manualy disable this checks on one signal of the loop.

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     a := 0
     a(1) := a(0) //False positive because of this line
   }

could be fixed by :

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits).noCombLoopCheck
     a := 0
     a(1) := a(0)
   }

It should also be known that this kind of assignements (a(1) := a(0)) could make some tools unhappy (Verilator). It could be preferable to write your logics using an Vec(Bool, 8).
