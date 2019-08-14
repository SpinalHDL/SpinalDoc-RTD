
Clock crossing violation
========================

Introduction
------------

SpinalHDL will check that every register of your design only depends (through some combinatorial logic) on registers which use the same or a synchronous clock domain.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val clkA = ClockDomain.external("clkA")
     val clkB = ClockDomain.external("clkB")

     val regA = clkA(Reg(UInt(8 bits)))   //PlayDev.scala:834
     val regB = clkB(Reg(UInt(8 bits)))   //PlayDev.scala:835

     val tmp = regA + regA                //PlayDev.scala:838
     regB := tmp
   }

will throw:

.. code-block:: text

   CLOCK CROSSING VIOLATION from (toplevel/regA :  UInt[8 bits]) to (toplevel/regB :  UInt[8 bits]).
   - Register declaration at
     ***
     Source file location of the toplevel/regA definition via the stack trace
     ***
   - through
         >>> (toplevel/regA :  UInt[8 bits]) at ***(PlayDev.scala:834) >>>
         >>> (toplevel/tmp :  UInt[8 bits]) at ***(PlayDev.scala:838) >>>
         >>> (toplevel/regB :  UInt[8 bits]) at ***(PlayDev.scala:835) >>>

There are multiple possible fixes:

crossClockDomain tag
^^^^^^^^^^^^^^^^^^^^

The crossClockDomain tag can be used to say "It's alright, don't panic" to SpinalHDL

.. code-block:: scala

   class TopLevel extends Component {
     val clkA = ClockDomain.external("clkA")
     val clkB = ClockDomain.external("clkB")

     val regA = clkA(Reg(UInt(8 bits)))
     val regB = clkB(Reg(UInt(8 bits))).addTag(crossClockDomain)


     val tmp = regA + regA
     regB := tmp
   }

setSyncronousWith
^^^^^^^^^^^^^^^^^

You can also specify that two clock domains are syncronous together.

.. code-block:: scala

   class TopLevel extends Component {
     val clkA = ClockDomain.external("clkA")
     val clkB = ClockDomain.external("clkB")
     clkB.setSyncronousWith(clkA)

     val regA = clkA(Reg(UInt(8 bits)))
     val regB = clkB(Reg(UInt(8 bits)))


     val tmp = regA + regA
     regB := tmp
   }

BufferCC
^^^^^^^^
Signal Bits or Gray-coded Bits can BufferCC to cross different clockDomain, 
But BufferCC can't be used for general multi-Bits cross-domain process !
