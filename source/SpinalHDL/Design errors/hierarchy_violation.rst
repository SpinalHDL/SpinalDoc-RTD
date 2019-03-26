
Hierarchy violation
===================

Introduction
------------

SpinalHDL will check that signals are never accessed outside of the current component's boundaries.

The following signals can be read inside a component:


* All directionless signals defined in the current component
* All in/out/inout signals of the current component
* All in/out/inout signals of children components

In addition, the following signals can be assigned to inside a component:


* All directionless signals defined in the current component
* All out/inout signals of the current component
* All in/inout signals of children components

If a ``HIERARCHY VIOLATION`` error appears, it means that one of the above rules was violated.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = in UInt(8 bits)
     }
     val tmp = U"x42"
     io.a := tmp
   }

will throw:

.. code-block:: text

   HIERARCHY VIOLATION : (toplevel/io_a : in UInt[8 bits]) is drived by (toplevel/tmp :  UInt[8 bits]), but isn't accessible in the toplevel component.
     ***
     Source file location of the `io.a := tmp` via the stack trace
     ***

A fix could be :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = out UInt(8 bits) // changed from in to out
     }
     val tmp = U"x42"
     io.a := tmp
   }
