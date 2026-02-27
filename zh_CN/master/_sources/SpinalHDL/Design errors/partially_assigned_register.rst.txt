Partially assigned register
============================

Introduction
------------

SpinalHDL will check that when a register is conditionally assigned, all bits of the register must at least be assigned once. If some bits are left unassigned, it can lead to unintended hardware behavior such as latches or unpredictable values.

This error occurs when:

* A register is assigned within a conditional block (``when``/``switch``)
* Only some bits of the register are assigned
* Other bits are left without assignment in that path

Example
-------

The following code:

.. code-block:: scala

    class TopLevel extends Component{

      val io = new Bundle {
        val condition = in Bool()
        val result = out UInt( 8 bits )
      }

      val myReg = Reg(UInt( 8 bits ))

      when(io.condition){
        myReg(3 downto 0) := 0xF   // Only lower 4 bits assigned
        // Upper 4 bits are not assigned!
      }

      io.result := myReg
    }

will throw:

::

   PARTIALLY ASSIGNED REGISTER (toplevel/myReg :  UInt[8 bits]), unassigned bit mask is 11110000, defined at
   ***
   Source file location of the toplevel/myReg definition via the stack trace
   ***

How to fix it
-------------

There are several ways to fix this error:

**Solution 1: Assign the complete register**

.. code-block:: scala

   class TopLevel extends Component{

     val io = new Bundle {
       val condition = in Bool()
       val result = out UInt(8 bits)
     }

     val myReg = Reg(UInt( 8 bits ))

     when(io.condition){
       myReg(7 downto 0) := 0x0F   // Assign all 8 bits

     }

     io.result := myReg
   }


**Solution 2: Explicitly handle all bits**

.. code-block:: scala

   class TopLevel extends Component{

     val io = new Bundle {
       val condition = in Bool()
       val result = out UInt(8 bits)
     }

     val myReg = Reg(UInt(8 bits))

     when(io.condition){
       myReg(3 downto 0) := 0xF   // Explicitly keep upper bits
       myReg(7 downto 4) := myReg(7 downto 4)

     }

     io.result := myReg
   }

**Solution 3: Provide a default value**

.. code-block:: scala

   class TopLevel extends Component{

     val io = new Bundle {
       val condition = in Bool()
       val result = out UInt(8 bits)
     }

     val myReg = Reg(UInt(8 bits))

     when(io.condition){
       myReg(3 downto 0) := 0xF
       // Upper bits will retain their value from previous cycle
     }.otherwise {
       myReg := myReg // Explicitly maintain value
     }

     io.result := myReg
   }
