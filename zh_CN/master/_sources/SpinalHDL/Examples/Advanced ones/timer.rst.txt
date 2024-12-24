.. _Timer:

Timer
=====

Introduction
------------

A timer module is probably one of the most basic pieces of hardware. But even for a timer, there are some interesting things that you can do with SpinalHDL. This example will define a simple timer component which integrates a bus bridging utile.

Timer
-----

So let's start with the ``Timer`` component.

Specification
^^^^^^^^^^^^^

The ``Timer`` component will have a single construction parameter:

.. list-table::
   :header-rows: 1
   :widths: 1 1 2

   * - Parameter Name
     - Type
     - Description
   * - width
     - Int
     - Specify the bit width of the timer counter


And also some inputs/outputs:

.. list-table::
   :header-rows: 1
   :widths: 1 1 2 4

   * - IO Name
     - Direction
     - Type
     - Description
   * - tick
     - in
     - Bool
     - When ``tick`` is True, the timer count up until ``limit``.
   * - clear
     - in
     - Bool
     - When ``tick`` is True, the timer is set to zero. ``clear`` has priority over ``tick``.
   * - limit
     - in
     - UInt(width bits)
     - When the timer value is equal to ``limit``\ , the ``tick`` input is inhibited.
   * - full
     - out
     - Bool
     - ``full`` is high when the timer value is equal to ``limit`` and ``tick`` is high.
   * - value
     - out
     - UInt(width bits)
     - Wire out the timer counter value.


Implementation
^^^^^^^^^^^^^^

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/Timer.scala
   :language: scala
   :start-at: case class Timer
   :end-before: // Timer bus interface
   :append: }

Bridging function
-----------------

Now we can start with the main purpose of this example: defining a bus bridging function. To do that we will use two techniques:


* Using the ``BusSlaveFactory`` tool documented :ref:`here <bus_slave_factory>`
* Defining a function inside the ``Timer`` component which can be called from the parent component to drive the ``Timer``\ 's IO in an abstract way.

Specification
^^^^^^^^^^^^^

This bridging function will take the following parameters:

.. list-table::
   :header-rows: 1
   :widths: 1 1 10

   * - Parameter Name
     - Type
     - Description
   * - busCtrl
     - BusSlaveFactory
     - The ``BusSlaveFactory`` instance that will be used by the function to create the bridging logic.
   * - baseAddress
     - BigInt
     - The base address where the bridging logic should be mapped.
   * - ticks
     - Seq[Bool]
     - A list of Bool sources that can be used as a tick signal.
   * - clears
     - Seq[Bool]
     - A list of Bool sources that can be used as a clear signal.


The register mapping assumes that the bus system is 32 bits wide:

.. list-table::
   :header-rows: 1
   :widths: 1 1 1 1 1 10

   * - Name
     - Access
     - Width
     - Address offset
     - Bit offset
     - Description
   * - ticksEnable
     - RW
     - len(ticks)
     - 0
     - 0
     - Each ``ticks`` bool can be activated if the corresponding ``ticksEnable`` bit is high.
   * - clearsEnable
     - RW
     - len(clears)
     - 0
     - 16
     - Each ``clears`` bool can be activated if the corresponding ``clearsEnable`` bit is high.
   * - limit
     - RW
     - width
     - 4
     - 0
     - | Access the limit value of the timer component. 
       | When this register is written to, the timer is cleared.
   * - value
     - R
     - width
     - 8
     - 0
     - Access the value of the timer.
   * - clear
     - W
     - 
     - 8
     - 
     - When this register is written to, it clears the timer.


Implementation
^^^^^^^^^^^^^^

Let's add this bridging function inside the ``Timer`` component.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/Timer.scala
   :language: scala
   :start-after: // Timer bus interface
   :end-before: // end case class Timer
   :prepend: case class Timer(width : Int) extends Component {
             ...

Usage
^^^^^

Here is some demonstration code which is very close to the one used in the Pinsec SoC timer module. Basically it instantiates following elements:

* One 16 bit prescaler
* One 32 bit timer
* Three 16 bit timers

Then by using an ``Apb3SlaveFactory`` and functions defined inside the ``Timer``\ s, it creates bridging logic between the APB3 bus and all instantiated components.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/Timer.scala
   :language: scala
   :start-after: case class ApbTimer
   :end-before: // end case class ApbTimer
