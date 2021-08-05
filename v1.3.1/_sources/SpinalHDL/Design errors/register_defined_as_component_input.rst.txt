
Register defined as component input
===================================

Introduction
------------

In SpinalHDL, it is not allowed to define an component input as a register. The reason of that is for the user to avoid having surprise when he drive sub components inputs.

Example
-------

The following code :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = in(Reg(UInt(8 bits)))
     }
   }

will throw :

.. code-block:: text

   REGISTER DEFINED AS COMPONENT INPUT : (toplevel/io_a : in UInt[8 bits]) is defined as a registered input of the toplevel component, but isn't allowed.
     ***
     Source file location of the toplevel/io_a definition via the stack trace
     ***

A fix could be :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = in UInt(8 bits)
     }
   }
