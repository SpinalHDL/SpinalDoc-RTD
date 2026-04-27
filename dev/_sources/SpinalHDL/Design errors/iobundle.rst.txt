
IO bundle
=========

Introduction
------------

SpinalHDL will check that each ``io`` bundle contains only in/out/inout signals.
Other kinds of signals are called directionless signals.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = UInt(8 bits)
     }
   }

will throw:

.. code-block:: text

   IO BUNDLE ERROR : A direction less (toplevel/io_a :  UInt[8 bits]) signal was defined into toplevel component's io bundle
     ***
     Source file location of the toplevel/io_a definition via the stack trace
     ***

A fix could be:

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = in UInt(8 bits)  // provide 'in' direction declaration
     }
   }

But if for meta hardware description reasons you really want ``io.a`` to be directionless, you can do:

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = UInt(8 bits)
     }
     a.allowDirectionLessIo
   }
