
Flow
====

Specification
-------------

The Flow interface is a simple valid/payload protocol which means the slave can't halt the bus.
It could be used to represent data coming from an UART controller, requests to write an on-chip memory, etc.

.. list-table::
   :header-rows: 1
   :widths: 1 1 1 10 1

   * - Signal
     - Type
     - Driver
     - Description
     - Don't care when
   * - valid
     - Bool
     - Master
     - When high => payload present on the interface
     - 
   * - payload
     - T
     - Master
     - Content of the transaction
     - valid is low


Functions
---------

.. list-table::
   :header-rows: 1
   :widths: 1 10 1 1

   * - Syntax
     - Description
     - Return
     - Latency
   * - Flow(type : Data)
     - Create a Flow of a given type
     - Flow[T]
     - 
   * - master/slave Flow(type : Data)
     - | Create a Flow of a given type
       | Initialized with corresponding in/out setup
     - Flow[T]
     - 
   * - x.m2sPipe()
     - | Return a Flow driven by x
       | through a register stage that cut valid/payload paths
     - Flow[T]
     - 1
   * - x.stage()
     - Equivalent to x.m2sPipe() 
     - Flow[T]
     - 1
   * - | x << y
       | y >> x
     - Connect y to x
     - 
     - 0
   * - | x <-< y
       | y >-> x
     - Connect y to x through a m2sPipe
     - 
     - 1
   * - x.throwWhen(cond : Bool)
     - | Return a Flow connected to x 
       | When cond is high, transaction are dropped
     - Flow[T]
     - 0
   * - x.toReg()
     - Return a register which is loaded with ``payload`` when valid is high
     - T
     - 
   * - x.setIdle()
     - Set the Flow in an Idle state: ``valid`` is ``False`` and don't care about ``payload``.
     -
     -
   * - x.push(newPayload: T)
     - Assign a new valid payload to the Flow. ``valid`` is set to ``True``.
     -
     -

Code example
------------

.. literalinclude:: /../examples/src/main/scala/spinaldoc/libraries/flow/FlowExample.scala
   :language: scala
   :start-at: case class FlowExample()
   :end-before: // end FlowExample

Simulation Support
------------------

.. list-table::
  :header-rows: 1
  :widths: 1 5
  
  * - Class
    - Usage
  * - FlowMonitor
    - Used for both master and slave sides, calls function with payload if Flow transmits data.
  * - FlowDriver
    - Testbench master side, drives values by calling function to apply value (if available). Function must return if value was available. Supports random delays.
  * - ScoreboardInOrder
    - Often used to compare reference/dut data

.. literalinclude:: /../examples/src/main/scala/spinaldoc/libraries/flow/SimSupport.scala
   :language: scala
