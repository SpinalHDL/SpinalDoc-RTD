
Clock domains 
==========================================

Stimulus API
----------------------------------

There is a list of ClockDomain stimulation functionalities :

.. list-table::
   :header-rows: 1
   :widths: 2 5

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

Wait API
----------------------------------

There is a list of ClockDomain utilities that you can use to wait a given event from it :

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - ClockDomain wait functions
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

All the functionalities of the wait API can only be called from the inside of a thread, and not from a callback.

.. _sim_clock_threadless:

Callback API
----------------------------------


There is a list of ClockDomain utilities that you can use to wait a given event from it :

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - ClockDomain callback functions
     - Description
   * - onNextSampling { callback }
     - Execute the callback code only once on next the ClockDomain sample (active edge + reset off + clock enable on)
   * - onSamplings { callback }
     - Execute the callback code each time the ClockDomain sample (active edge + reset off + clock enable on)
   * - onActiveEdges { callback }
     - Execute the callback code each time the ClockDomain clock do its configured edge
   * - onEdges { callback }
     - Execute the callback code each time the ClockDomain clock do a rising or falling edge
   * - onRisingEdges { callback }
     - Execute the callback code each time the ClockDomain clock do a rising edge
   * - onFallingEdges { callback }
     - Execute the callback code each time the ClockDomain clock do a falling edge


Default ClockDomain
----------------------------------

You can access the default ClockDomain of your toplevel by the following way :

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


New ClockDomain
--------------------------------

If you toplevel define some clock and reset inputs which aren't directly integrated into their clockdomain, you can define their corresponding clockdomain directly in the testbench :

.. code-block:: scala

   //In the testbench
   ClockDomain(dut.io.coreClk, dut.io.coreReset).forkStimulus(10)
