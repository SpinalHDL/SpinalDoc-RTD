.. role:: raw-html-m2r(raw)
   :format: html

.. _state_machine:

State machine
=============

Introduction
------------

In SpinalHDL you can define your state machine like in VHDL/Verilog, by using enumerations and switch/case statements. But in SpinalHDL you can also use a dedicated syntax.

The state machine below is implemented in the following examples:

.. image:: /asset/picture/fsm_simple.svg
   :align: center
   :width: 300

Style A:

.. code-block:: scala

   import spinal.lib.fsm._

   class TopLevel extends Component {
     val io = new Bundle {
       val result = out Bool()
     }

     val fsm = new StateMachine {
       val counter = Reg(UInt(8 bits)) init(0)
       io.result := False

       val stateA : State = new State with EntryPoint {
         whenIsActive(goto(stateB))
       }
       val stateB : State = new State {
         onEntry(counter := 0)
         whenIsActive {
           counter := counter + 1
           when(counter === 4) {
             goto(stateC)
           }
         }
         onExit(io.result := True)
       }
       val stateC : State = new State {
         whenIsActive(goto(stateA))
       }
     }
   }

Style B:

.. code-block:: scala

   import spinal.lib.fsm._

   class TopLevel extends Component {
     val io = new Bundle {
       val result = out Bool()
     }

     val fsm = new StateMachine {
       val stateA = new State with EntryPoint
       val stateB = new State
       val stateC = new State

       val counter = Reg(UInt(8 bits)) init(0)
       io.result := False

       stateA
         .whenIsActive(goto(stateB))

       stateB
         .onEntry(counter := 0)
         .whenIsActive {
           counter := counter + 1
           when(counter === 4) {
             goto(stateC)
           }
         }
         .onExit(io.result := True)

       stateC
         .whenIsActive(goto(stateA))
     }
   }

StateMachine
------------

``StateMachine`` is the base class. It manages the logic of the FSM.

.. code-block:: scala

   val myFsm = new StateMachine {
     // Definition of states
   }

``StateMachine`` also provides some accessors:

.. list-table::
   :header-rows: 1
   :widths: 1 1 5

   * - Name
     - Return
     - Description
   * - ``isActive(state)``
     - ``Bool``
     - Returns ``True`` when the state machine is in the given state
   * - ``isEntering(state)``
     - ``Bool``
     - Returns ``True`` when the state machine is entering the given state

Entry point
^^^^^^^^^^^

A state can be defined as the entry point of the state machine by extending the EntryPoint trait:

.. code-block:: scala

   val stateA = new State with EntryPoint

Or by using ``setEntry(state)``:

.. code-block:: scala

   val stateA = new State
   setEntry(stateA)

Transitions
^^^^^^^^^^^

* Transitions are represented by ``goto(nextState)``, which schedules the state machine to be in ``nextState`` the next cycle.
* ``exit()`` schedules the state machine to be in the boot state the next cycle (or, in ``StateFsm``, to exit the current nested state machine).

These two functions can be used inside state definitions (see below) or using ``always { yourStatements }``,
which always applies ``yourStatements``, with a priority over states.

State encoding
^^^^^^^^^^^^^^

By default the FSM state vector will be encoded using the native encoding of the language/tools the RTL is generated for (Verilog or VHDL).
This default can be overridden by using the ``setEncoding(...)`` method which either takes a ``SpinalEnumEncoding`` or
varargs of type ``(State, BigInt)`` for a custom encoding. 

.. code-block:: scala
   :caption: Using a ``SpinalEnumEncoding``
   
   val fsm = new StateMachine {
     setEncoding(binaryOneHot)

     ...
   }

.. code-block:: scala
   :caption: Using a custom encoding

   val fsm = new StateMachine {
     val stateA = new State with EntryPoint
     val stateB = new State
     ...
     setEncoding((stateA -> 0x23), (stateB -> 0x22))
   }

.. warning:: When using the ``graySequential`` enum encoding, no check is done to verify that the FSM transitions only produce
             single-bit changes in the state vector. The encoding is done according to the order of state definitions and the
             designer must ensure that only valid transitions are done if needed.

States
------

Multiple kinds of states can be used:

* ``State`` (the base one)
* ``StateDelay``
* ``StateFsm``
* ``StateParallelFsm``

Each of them provides the following functions to define the logic associated to them:

.. list-table::
   :header-rows: 1
   :widths: 1 10

   * - Name
     - Description
   * - .. code-block:: scala
     
          state.onEntry {
            yourStatements
          }
     - ``yourStatements`` is applied when the state machine is not in ``state`` and will be in ``state`` the next cycle
   * - .. code-block:: scala
         
          state.onExit {
            yourStatements
          }
     - ``yourStatements`` is applied when the state machine is in ``state`` and will be in another state the next cycle
   * - .. code-block:: scala
     
          state.whenIsActive {
            yourStatements
          }
     - ``yourStatements`` is applied when the state machine is in ``state``
   * - .. code-block:: scala
     
          state.whenIsNext {
            yourStatements
          }
     - ``yourStatements`` is executed when the state machine will be in ``state`` the next cycle (even if it is already in it)

``state.`` is implicit in a ``new State`` block:

.. image:: /asset/picture/fsm_stateb.svg
   :align: center
   :width: 300

.. code-block:: scala

   val stateB : State = new State {
     onEntry(counter := 0)
     whenIsActive {
       counter := counter + 1
       when(counter === 4) {
         goto(stateC)
       }
     }
     onExit(io.result := True)
   }

StateDelay
^^^^^^^^^^

``StateDelay`` allows you to create a state which waits for a fixed number of cycles before executing statements in ``whenCompleted {...}``. The preferred way to use it is:

.. code-block:: scala

   val stateG : State = new StateDelay(cyclesCount=40) {
     whenCompleted {
       goto(stateH)
     }
   }

It can also be written in one line:

.. code-block:: scala

   val stateG : State = new StateDelay(40) { whenCompleted(goto(stateH)) }

StateFsm
^^^^^^^^

``StateFsm`` allows you to describe a state containing a nested state machine. When the nested state machine is done (exited), statements in ``whenCompleted { ... }`` are executed.

There is an example of StateFsm definition :

.. code-block:: scala

   // internalFsm is a function defined below
   val stateC = new StateFsm(fsm=internalFsm()) {
     whenCompleted {
       goto(stateD)
     }
   }

   def internalFsm() = new StateMachine {
     val counter = Reg(UInt(8 bits)) init(0)

     val stateA : State = new State with EntryPoint {
       whenIsActive {
         goto(stateB)
       }
     }

     val stateB : State = new State {
       onEntry (counter := 0)
       whenIsActive {
         when(counter === 4) {
           exit()
         }
         counter := counter + 1
       }
     }
   }

In the example above, ``exit()`` makes the state machine jump to the boot state (a internal hidden state). This notifies ``StateFsm`` about the completion of the inner state machine.

StateParallelFsm
^^^^^^^^^^^^^^^^

``StateParallelFsm`` allows you to handle multiple nested state machines. When all nested state machine are done, statements in ``whenCompleted { ... }`` are executed.

Example:

.. code-block:: scala

   val stateD = new StateParallelFsm (internalFsmA(), internalFsmB()) {
     whenCompleted {
       goto(stateE)
     }
   }

Notes about the entry state
^^^^^^^^^^^^^^^^^^^^^^^^^^^

The way the entry state has been defined above makes it so that between the reset and the first clock sampling, the state machine is in a boot state. It is only after the first clock sampling that the defined entry state becomes active. This allows to properly enter the entry state (applying statements in ``onEntry``), and allows nested state machines.

While it is useful, it is also possible to bypass that feature and directly having a state machine booting into a user state.

To do so, use `makeInstantEntry()` instead of defining a ``new State``. This function returns the boot state, active directly after reset.

.. note::
   The ``onEntry`` of that state will only be called when it transitions from another state to this state and not during boot.

.. note::
   During simulation, the boot state is always named ``BOOT``.

Example:

.. code-block:: scala

    // State sequance: IDLE, STATE_A, STATE_B, ...
    val fsm = new StateMachine {
      // IDLE is named BOOT in simulation
      val IDLE = makeInstantEntry()
      val STATE_A, STATE_B, STATE_C = new State
      
      IDLE.whenIsActive(goto(STATE_A))
      STATE_A.whenIsActive(goto(STATE_B))
      STATE_B.whenIsActive(goto(STATE_C))
      STATE_C.whenIsActive(goto(STATE_B))
    }

.. code-block:: scala

    //  State sequence : BOOT, IDLE, STATE_A, STATE_B, ...
    val fsm = new StateMachine {
      val IDLE, STATE_A, STATE_B, STATE_C = new State
      setEntry(IDLE)
      
      IDLE.whenIsActive(goto(STATE_A))
      STATE_A.whenIsActive(goto(STATE_B))
      STATE_B.whenIsActive(goto(STATE_C))
      STATE_C.whenIsActive(goto(STATE_B))
    }
