
Introduction
------------

The simulation api provide the capaibilities to read and write the DUT signals, fork and join simulation processes, sleep and wait until a given condition is filled.

Don't forget the following imports :

.. code-block:: scala

   import spinal.sim._
   import spinal.core.sim._

Read and write signals
----------------------

Each interface signals of the toplevel can be read into scala and write from scala :

.. list-table::
   :header-rows: 1

   * - Syntax
     - Description
   * - Bool.toBoolean
     - Read an hardware Bool as a Scala Boolean value
   * - Bits/UInt/SInt.toInt
     - Read an hardware BitVector as a Scala Int value
   * - Bits/UInt/SInt.toLong
     - Read an hardware BitVector as a Scala Long value
   * - Bits/UInt/SInt.toBigInt
     - Read an hardware BitVector as a Scala BigInt value
   * - Bool #= Boolean
     - Assign a hardware Bool from an Scala Boolean
   * - Bits/UInt/SInt #= Int
     - Assign a hardware BitVector from an Scala Int
   * - Bits/UInt/SInt #= Long
     - Assign a hardware BitVector from an Scala Long
   * - Bits/UInt/SInt #= BigInt
     - Assign a hardware BitVector from an Scala BigInt


.. code-block:: scala

   dut.io.a #= 42
   dut.io.a #= 42l
   dut.io.a #= BigInt("101010", 2)
   dut.io.a #= BigInt("0123456789ABCDEF", 16)
   println(dut.io.b.toInt)

Fork and join simulation threads
--------------------------------

.. code-block:: scala

   //Create a new thread
   val myNewThread = fork{
     //New simulation thread body
   }

   //Wait until `myNewThread` is execution is done.
   myNewThread.join()

Sleep and waitUntil
-------------------

.. code-block:: scala

   //Sleep 1000 units of time
   sleep(1000)

   //waitUntil the dut.io.a value is bigger than 42 before continuing
   waitUntil(dut.io.a > 42)

External clock domains
----------------------

In the case you are using an external clock domain to feed your design with clock edges (this include the by default clock domain), you can access them in the simulation by the following way :

.. code-block:: scala

   //Example of thread forking to generate an reset and then toggeling the clock each 5 units of times.
   //dut.clockDomain refer to the implicit clock domain during the component instanciation.
   fork{
     dut.clockDomain.assertReset()
     dut.clockDomain.fallingEdge()
     sleep(10)
     while(true){
       dut.clockDomain.clockToggle()
       sleep(5)
     }
   }

But you can also directly fork a standard reset/clock process :

.. code-block:: scala

   dut.clockDomain.forkStimulus(period = 10)

And there is an example of how to wait for a rising edge on the clock :

.. code-block:: scala

   dut.clockDomain.waitRisingEdge()

There is a list of ClockDomain simulation functionalities :

.. list-table::
   :header-rows: 1

   * - ClockDomain stimulus functions
     - Description
   * - forkStimulus(period)
     - Fork a simulation process to generate the clockdomain simulus (clock, reset, softReset, clockEnable signals)
   * - forkSimSpeedPrinter(printPeriod)
     - Fork a simulation process which will periodicaly print the simulation speed in kcycles per real time second. ``printPeriod`` is in realtime second
   * - clockToggle()
     - Toggle the clock signal
   * - fallingEdge()
     - Clear the clock signal
   * - risingEdge()
     - Set the clock signal
   * - assertReset()
     - Set the reset signal to its active level
   * - disassertReset()
     - Set the reset signal to its inactive level
   * - assertClockEnable()
     - Set the clockEnable signal to its active level
   * - disassertClockEnable()
     - Set the clockEnable signal to its active level
   * - assertSoftReset()
     - Set the softReset signal to its active level
   * - disassertSoftReset()
     - Set the softReset signal to its active level


.. list-table::
   :header-rows: 1

   * - ClockDomain monitoring functions
     - Description
   * - waitSampling([cyclesCount])
     - Wait until the ClockDomain made a sampling, (Active clock edge && disassertReset && assertClockEnable)
   * - waitRisingEdge([cyclesCount])
     - Wait cyclesCount rising edges on the clock, if not cycleCount isn't specified => 1 cycle, cyclesCount = 0 is legal, not sensitive to reset/softReset/clockEnable
   * - waitFallingEdge([cyclesCount])
     - Same as waitRisingEdge but for the falling edge
   * - waitActiveEdge([cyclesCount])
     - Same as waitRisingEdge but for the edge level specified by the ClockDomainConfig
   * - waitRisingEdgeWhere(condition)
     - As waitRisingEdge, but to exit, the boolean ``condition`` must be true when the rising edge occure
   * - waitFallingEdgeWhere(condition)
     - Same as waitRisingEdgeWhere but for the falling edge
   * - waitActiveEdgeWhere(condition)
     - Same as waitRisingEdgeWhere but for the edge level specified by the ClockDomainConfig


Note that if you toplevel define some clock and reset inputs which aren't directly integrated into their clockdomain, you can define their corresponding clockdomain directly in the testbench :

.. code-block:: scala

   //In the testbench
   ClockDomain(dut.io.coreClk, dut.io.coreReset).forkStimulus(10)
