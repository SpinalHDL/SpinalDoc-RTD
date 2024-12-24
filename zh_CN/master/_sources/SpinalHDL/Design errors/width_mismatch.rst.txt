
Width mismatch
==============

Introduction
------------

SpinalHDL will check that operators and signals on the left and right side of assignments have the same widths.

Assignment example
------------------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     val b = UInt(4 bits)
     b := a
   }

will throw:

.. code-block:: text

   WIDTH MISMATCH on (toplevel/b :  UInt[4 bits]) := (toplevel/a :  UInt[8 bits]) at
     ***
     Source file location of the OR operator via the stack trace
     ***

A fix could be:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     val b = UInt(4 bits)
     b := a.resized
   }

Operator example
----------------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     val b = UInt(4 bits)
     val result = a | b
   }

will throw:

.. code-block:: text

   WIDTH MISMATCH on (UInt | UInt)[8 bits]
   - Left  operand : (toplevel/a :  UInt[8 bits])
   - Right operand : (toplevel/b :  UInt[4 bits])
     at
     ***
     Source file location of the OR operator via the stack trace
     ***

A fix could be:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     val b = UInt(4 bits)
     val result = a | (b.resized)
   }
