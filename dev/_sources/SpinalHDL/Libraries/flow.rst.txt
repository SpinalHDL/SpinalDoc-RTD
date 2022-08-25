
Flow
====

Specification
-------------

| The Flow interface is a simple valid/payload protocol which mean the slave can't halt the bus.
| It could be used, for example, to represent data coming from an UART controller, requests to write an on-chip memory, etc.

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
     - | Return a Flow drived by x
       | through a register stage that cut valid/payload paths
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

.. code-block:: scala

    val request = Flow(Bits(8 bits))
    val answer  = Flow(Bits(8 bits))
    val storage = Reg(Bits(8 bits)) init 0

    val fsm = new StateMachine {
      answer.setIdle()

      val idle: State = new State with EntryPoint {
        whenIsActive {
          when(request.valid) {
            storage := request.payload
            goto(sendEcho)
          }
        }
      }

      val sendEcho: State = new State {
        whenIsActive {
            answer.push(storage)
            goto(idle)
        }
      }
    }

    // equivalently

    answer <-< request

