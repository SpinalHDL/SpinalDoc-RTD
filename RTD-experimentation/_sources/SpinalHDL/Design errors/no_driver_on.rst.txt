
No driver on
============

Introduction
------------

SpinalHDL will check that all combinatorial signals which have impacts on the design are assigned by something.

Example
-------

The following code :

.. code-block:: scala

   class TopLevel extends Component {
     val result = out(UInt(8 bits))
     val a = UInt(8 bits)
     result := a
   }

will throw :

.. code-block:: text

   NO DRIVER ON (toplevel/a :  UInt[8 bits]), defined at
     ***
     Source file location of the toplevel/a definition via the stack trace
     ***

A fix could be :

.. code-block:: scala

   class TopLevel extends Component {
     val result = out(UInt(8 bits))
     val a = UInt(8 bits)
     a := 42
     result := a
   }
