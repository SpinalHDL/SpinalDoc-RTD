Clock domains 
=============

Stimulus API
------------

Below is a list of ``ClockDomain`` stimulation functions:

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - ClockDomain stimulus functions
     - Description
   * - ``forkStimulus(period)``
     - Fork a simulation process to generate the ClockDomain stimulus (clock, reset, softReset, clockEnable signals)
   * - ``forkSimSpeedPrinter(printPeriod)``
     - Fork a simulation process which will periodically print the simulation speed in kilo-cycles per real time second. ``printPeriod`` is in realtime seconds
   * - ``clockToggle()``
     - Toggle the clock signal
   * - ``fallingEdge()``
     - Clear the clock signal
   * - ``risingEdge()``
     - Set the clock signal
   * - ``assertReset()``
     - Set the reset signal to its active level
   * - ``deassertReset()``
     - Set the reset signal to its inactive level
   * - ``assertClockEnable()``
     - Set the clockEnable signal to its active level
   * - ``deassertClockEnable()``
     - Set the clockEnable signal to its active level
   * - ``assertSoftReset()``
     - Set the softReset signal to its active level
   * - ``deassertSoftReset()``
     - Set the softReset signal to its active level

Wait API
--------

Below is a list of ``ClockDomain`` utilities that you can use to wait for a given event from the domain:

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - ClockDomain wait functions
     - Description
   * - ``waitSampling([cyclesCount])``
     - Wait until the ``ClockDomain`` makes a sampling, (active clock edge && deassertReset && assertClockEnable)
   * - ``waitRisingEdge([cyclesCount])``
     - Wait cyclesCount rising edges on the clock; cycleCount defaults to 1 cycle if not otherwise specified. Note, cyclesCount = 0 is legal, and the function is not sensitive to reset/softReset/clockEnable
   * - ``waitFallingEdge([cyclesCount])``
     - Same as ``waitRisingEdge`` but for the falling edge
   * - ``waitActiveEdge([cyclesCount])``
     - Same as ``waitRisingEdge`` but for the edge level specified by the ``ClockDomainConfig``
   * - ``waitRisingEdgeWhere(condition)``
     - Same as ``waitRisingEdge``, but to exit, the boolean ``condition`` must be true when the rising edge occurs
   * - ``waitFallingEdgeWhere(condition)``
     - Same as ``waitRisingEdgeWhere``, but for the falling edge
   * - ``waitActiveEdgeWhere(condition)``
     - Same as ``waitRisingEdgeWhere``, but for the edge level specified by the ``ClockDomainConfig``
   * - ``waitSamplingWhere(condition) : Boolean``
     - Wait until a clockdomain sampled and the given condition is true        
   * - ``waitSamplingWhere(timeout)(condition) : Boolean``
     - Same as waitSamplingWhere defined above, but will never block more than timeout cycles. Return true if the exit condition came from the timeout
     

.. warning::
   All the functionality of the wait API can only be called directly from inside a thread, and not from a callback executed via the Callback API.

.. _sim_clock_threadless:

Callback API
------------

Below is a list of ``ClockDomain`` utilities that you can use to wait for a given event from the domain:

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - ClockDomain callback functions
     - Description
   * - ``onNextSampling { callback }``
     - Execute the callback code only once on the next ``ClockDomain`` sample (active edge + reset off + clock enable on)
   * - ``onSamplings { callback }``
     - Execute the callback code each time the ``ClockDomain`` sample (active edge + reset off + clock enable on)
   * - ``onActiveEdges { callback }``
     - Execute the callback code each time the ``ClockDomain`` clock generates its configured edge
   * - ``onEdges { callback }``
     - Execute the callback code each time the ``ClockDomain`` clock generates a rising or falling edge
   * - ``onRisingEdges { callback }``
     - Execute the callback code each time the ``ClockDomain`` clock generates a rising edge
   * - ``onFallingEdges { callback }``
     - Execute the callback code each time the ``ClockDomain`` clock generates a falling edge
   * - ``onSamplingWhile { callback : Boolean }``
     - Same as onSampling, but you can stop it (forever) by letting the callback returning false


Default ClockDomain
-------------------

You can access the default ``ClockDomain`` of your toplevel as shown below:

.. code-block:: scala

   // Example of thread forking to generate a reset, and then toggling the clock each 5 time units.
   // dut.clockDomain refers to the implicit clock domain created during component instantiation.
   fork {
     dut.clockDomain.assertReset()
     dut.clockDomain.fallingEdge()
     sleep(10)
     while(true) {
       dut.clockDomain.clockToggle()
       sleep(5)
     }
   }

Note that you can also directly fork a standard reset/clock process:

.. code-block:: scala

   dut.clockDomain.forkStimulus(period = 10)

An example of how to wait for a rising edge on the clock:

.. code-block:: scala

   dut.clockDomain.waitRisingEdge()


New ClockDomain
---------------

If your toplevel defines some clock and reset inputs which aren't directly integrated into their ``ClockDomain``, you can define their corresponding ``ClockDomain`` directly in the testbench:

.. code-block:: scala

   // In the testbench
   ClockDomain(dut.io.coreClk, dut.io.coreReset).forkStimulus(10)
