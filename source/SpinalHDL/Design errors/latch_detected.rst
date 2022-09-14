
Latch detected
==============

Introduction
------------

SpinalHDL will check that no combinational signals will infer a latch during synthesis.
In other words, this is a check that no combinational signals are partially assigned.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val cond = in(Bool)
     val a = UInt(8 bits)

     when(cond) {
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
     when(cond) {
       a := 42
     }
   }

Due to mux
----------

Another reason for a latch being detected is often a non-exhaustive ``mux``/``muxList`` statement
with a missing default:

.. code-block:: scala

  val u1 = UInt(1 bit)
  u1.mux(
    0 -> False,
    // case for 1 is missing
  )

which can be fixed by adding the missing case (or a default case):

.. code-block:: scala

  val u1 = UInt(1 bit)
  u1.mux(
    0 -> False,
    default -> True
  )

In e.g. width generic code it is often a better solution to use ``muxListDc`` as this will not
generate an error for those cases were a default is not needed:

.. code-block:: scala

  val u1 = UInt(1 bit)
  // automatically adds default if needed
  u1.muxListDc(Seq(0 -> True))