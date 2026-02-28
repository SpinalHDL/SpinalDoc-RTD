
Spinal can't clone class
========================

Introduction
------------

This error happens when SpinalHDL wants to create a new datatype instance via the ``cloneOf`` function but isn't able to do it.
The reason for this is nearly always because it can't retrieve the construction parameters of a ``Bundle``.

Example 1
---------

The following code:

.. code-block:: scala

    // cloneOf(this) isn't able to retrieve the width value that was used to construct itself
    class RGB(width : Int) extends Bundle {
      val r, g, b = UInt(width bits)
    }

    class TopLevel extends Component {
      val tmp = Stream(new RGB(8)) // Stream requires the capability to cloneOf(new RGB(8))
    }

will throw:

.. code-block:: text

   *** Spinal can't clone class spinal.tester.PlayDevMessages$RGB datatype
   *** You have two way to solve that :
   *** In place to declare a "class Bundle(args){}", create a "case class Bundle(args){}"
   *** Or override by your self the bundle clone function
     ***
     Source file location of the RGB class definition via the stack trace
     ***

A fix could be:

.. code-block:: scala

   case class RGB(width : Int) extends Bundle {
     val r, g, b = UInt(width bits)
   }

   class TopLevel extends Component {
     val tmp = Stream(RGB(8))
   }

Example 2
---------

The following code:

.. code-block:: scala

  case class Xlen(val xlen: Int) {}

  case class MemoryAddress()(implicit xlenConfig: Xlen) extends Bundle {
      val address = UInt(xlenConfig.xlen bits)
  }

  class DebugMemory(implicit config: Xlen) extends Component {
      val io = new Bundle {
          val inputAddress = in(MemoryAddress())
      }   

      val someAddress = RegNext(io.inputAddress) // -> ERROR *****************************
  }

raises an exception:

.. code-block:: text

  [error] *** Spinal can't clone class debug.MemoryAddress datatype

In this case, a solution is to override the clone function to propagate the implicit parameter.

.. code-block:: scala

  case class MemoryAddress()(implicit xlenConfig: Xlen) extends Bundle {
    val address = UInt(xlenConfig.xlen bits)

    override def clone = MemoryAddress()
  }

.. note::

  We need to clone the hardware element, not the eventually assigned value in it.

.. note::

  An alternative is to used :ref:`ScopeProperty <scopeproperty>`.