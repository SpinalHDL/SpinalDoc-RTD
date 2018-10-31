
Hierarchy violation
===================

Introduction
------------

SpinalHDL will check that you never access a signal which is outside the current component range.

There is the list of signal which can be read within a component :


* All direction less signals defined in the same component
* All in/out/inout signals of the current component
* All in/out/inout signals of children components

There is the list of signals which can be assigned within a component :


* All direction less signals defined in the same component
* All out/inout signals of the current component
* All in/inout signals of children components

If an ``HIERARCHY VIOLATION`` appear, it mean that one of the above rules was violated.

Example
-------

The following code :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = in UInt(8 bits)
     }
     val tmp = U"x42"
     io.a := tmp
   }

will throw :

.. code-block::

   HIERARCHY VIOLATION : (toplevel/io_a : in UInt[8 bits]) is drived by (toplevel/tmp :  UInt[8 bits]), but isn't accessible in the toplevel component.
     ***
     Source file location of the `io.a := tmp` via the stack trace
     ***

A fix could be :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = out UInt(8 bits)
     }

     io.a := 42
   }
