
Clock crossing violation
========================

Introduction
------------

SpinalHDL will check that every register of your design only depends (through combinational logic paths) on registers which use the same or a synchronous clock domain.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val clkA = ClockDomain.external("clkA")
     val clkB = ClockDomain.external("clkB")

     val regA = clkA(Reg(UInt(8 bits)))   // PlayDev.scala:834
     val regB = clkB(Reg(UInt(8 bits)))   // PlayDev.scala:835

     val tmp = regA + regA                // PlayDev.scala:838
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

There are multiple possible fixes, listed below:

 - :ref:`crossClockDomain tags <crossclockdomain-tag>`
 - :ref:`setSynchronousWith method <setsynchronouswith>`
 - :ref:`BufferCC type <buffercc>`

.. _crossclockdomain-tag:

crossClockDomain tag
^^^^^^^^^^^^^^^^^^^^

The ``crossClockDomain`` tag can be used to communicate "It's alright, don't panic about this specific clock crossing" to the SpinalHDL compiler.

.. code-block:: scala

   class TopLevel extends Component {
     val clkA = ClockDomain.external("clkA")
     val clkB = ClockDomain.external("clkB")

     val regA = clkA(Reg(UInt(8 bits)))
     val regB = clkB(Reg(UInt(8 bits))).addTag(crossClockDomain)


     val tmp = regA + regA
     regB := tmp
   }

.. _setsynchronouswith:

setSynchronousWith
^^^^^^^^^^^^^^^^^^

You can also specify that two clock domains are synchronous together by using the ``setSynchronousWith`` method of one of the ``ClockDomain`` objects.

.. code-block:: scala

   class TopLevel extends Component {
     val clkA = ClockDomain.external("clkA")
     val clkB = ClockDomain.external("clkB")
     clkB.setSynchronousWith(clkA)

     val regA = clkA(Reg(UInt(8 bits)))
     val regB = clkB(Reg(UInt(8 bits)))


     val tmp = regA + regA
     regB := tmp
   }

.. _buffercc:

BufferCC
^^^^^^^^

When exchanging single-bit signals (such as ``Bool`` types), or Gray-coded values, you can use ``BufferCC`` to safely cross different ``ClockDomain`` regions.

.. warning::
   Do not use ``BufferCC`` with multi-bit signals, as there is a risk of corrupted reads on the receiving side if the clocks are asynchronous.
   See the :ref:`Clock Domains <clock_domain>` page for more details.

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
        val pushPtrGray = BufferCC(pushToPopGray, B(0, ptrWidth bits))
        val empty       = isEmpty(popPtrGray, pushPtrGray)   
        ...
      }
   }
