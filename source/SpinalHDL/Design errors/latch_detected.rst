
Latch detected
==============

Introduction
------------

SpinalHDL will check that no combinatorial signal will infer a latch during synthesis. In other words, that no combinatorial signals are partialy assigned.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val cond = in(Bool)
     val a = UInt(8 bits)

     when(cond){
       a := 42
     }
   }

will throw:

.. code-block:: text

   LATCH DETECTED from the combinatorial signal (toplevel/a :  UInt[8 bits]), defined at
     ***
     Source file location of the toplevel/io_a definition via the stack trace
     ***

A fix could be:

.. code-block:: scala

   class TopLevel extends Component {
     val cond = in(Bool)
     val a = UInt(8 bits)

     a := 0
     when(cond){
       a := 42
     }
   }
