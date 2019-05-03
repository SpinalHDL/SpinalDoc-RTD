
Spinal can't clone class
========================

Introduction
------------

This error happens when SpinalHDL wants to create a new datatype via the cloneOf function but isn't able to do it. The reasons for this is nearly always because it can't retreive the construction parameters of a Bundle.

Example
-------

The following code:

.. code-block:: scala

    //cloneOf(this) isn't able to retreive the width value that was used to construct itself
    class RGB(width : Int) extends Bundle{   
      val r,g,b = UInt(width bits)
    }

    class TopLevel extends Component {
      val tmp = Stream(new RGB(8)) //Stream requires the capability to cloneOf(new RGB(8))
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

   case class RGB(width : Int) extends Bundle{   
     val r,g,b = UInt(width bits)
   }

   class TopLevel extends Component {
     val tmp = Stream(RGB(8))
   }
