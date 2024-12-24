
Register defined as component input
===================================

Introduction
------------

In SpinalHDL, you are not allowed to define a component that has a register as an input.
The reasoning behind this is to prevent surprises when the user tries to drive the inputs of child components with the registered signal.
If a registered input is desired, you will need to declare the unregistered input in the ``io`` bundle, and register the signal in the body of the component.

Example
-------

The following code :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = in(Reg(UInt(8 bits)))
     }
   }

will throw:

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

If a registered ``a`` is required, it can be done like so:

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = in UInt(8 bits)
     }
     val a = RegNext(io.a)
   }
