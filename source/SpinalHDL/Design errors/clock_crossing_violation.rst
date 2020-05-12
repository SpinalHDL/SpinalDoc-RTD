
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

Signal Bits or Gray-coded Bits can use BufferCC to cross different clockDomain 

.. code-block:: scala

   class AsyncFifo extends Component {
      val popToPushGray = Bits(ptrWidth bits)
      val pushToPopGray = Bits(ptrWidth bits)
     
      val pushCC = new ClockingArea(pushClock) {
        val pushPtr     = Counter(depth << 1)
        val pushPtrGray = RegNext(toGray(pushPtr.valueNext)) init(0)
        val popPtrGray  = BufferCC(popToPushGray, B(0, ptrWidth bits))
        val full        = isFull(pushPtrGray, popPtrGray)
        ...
      }
     
      val popCC = new ClockingArea(popClock) {
        val popPtr      = Counter(depth << 1)
        val popPtrGray  = RegNext(toGray(popPtr.valueNext)) init(0)
        val pushPtrGray = BufferCC(pushToPopGray, B(0, ptrWidth bit))
        val empty       = isEmpty(popPtrGray, pushPtrGray)   
        ...
      }
   }

.. warning::
   Do not use BufferCC for general multi-Bits cross-domain process as mentioned under :ref:`Clock Domains <clock_domain>`  
