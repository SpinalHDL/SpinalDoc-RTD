
Io bundle
=========

Introduction
------------

SpinalHDL will check that in each io bundle there only in/out/inout signals.

Example
-------

The following code :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = UInt(8 bits)
     }
   }

will throw :

.. code-block:: text

   IO BUNDLE ERROR : A direction less (toplevel/io_a :  UInt[8 bits]) signal was defined into toplevel component's io bundle
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

But if for meta hardware description reasons you realy want io.a to be direction less, you can do :

.. code-block:: scala

   class TopLevel extends Component {
     val io = new Bundle {
       val a = UInt(8 bits)
     }
     a.allowDirectionLessIo
   }
