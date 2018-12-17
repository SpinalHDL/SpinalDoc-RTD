.. role:: raw-html-m2r(raw)
   :format: html

.. _state_machine:

State machine
=============

Introduction
------------

In SpinalHDL you can define your state machine like in VHDL/Verilog, by using enumerations and switch cases statements. But in SpinalHDL you can also use a dedicated syntax.

The following state machine is implemented in following examples :

.. image:: /asset/picture/fsm_simple.svg
   :align: center
   :width: 300

Style A :

.. code-block:: scala

   import spinal.lib.fsm._

   class TopLevel extends Component {
     val io = new Bundle{
       val result = out Bool
     }

     val fsm = new StateMachine{
       val counter = Reg(UInt(8 bits)) init (0)
       io.result := False

       val stateA : State = new State with EntryPoint{
         whenIsActive (goto(stateB))
       }
       val stateB : State = new State{
         onEntry(counter := 0)
         whenIsActive {
           counter := counter + 1
           when(counter === 4){
             goto(stateC)
           }
         }
         onExit(io.result := True)
       }
       val stateC : State = new State{
         whenIsActive (goto(stateA))
       }
     }
   }

Style B :

.. code-block:: scala

   import spinal.lib.fsm._

   class TopLevel extends Component {
     val io = new Bundle{
       val result = out Bool
     }

     val fsm = new StateMachine{
       val stateA = new State with EntryPoint
       val stateB = new State
       val stateC = new State

       val counter = Reg(UInt(8 bits)) init (0)
       io.result := False

       stateA
         .whenIsActive (goto(stateB))

       stateB
         .onEntry(counter := 0)
         .whenIsActive {
           counter := counter + 1
           when(counter === 4){
             goto(stateC)
           }
         }
         .onExit(io.result := True)

       stateC
         .whenIsActive (goto(stateA))
     }
   }

StateMachine
------------

StateMachine is the base class that will manage the logic of your FSM.

.. code-block:: scala

   val myFsm = new StateMachine{
     // Here will come states definition
   }

The StateMachine class also provide some utils :

.. list-table::
   :header-rows: 1
   :widths: 1 1 5

   * - Name
     - Return
     - Description
   * - isActive(state)
     - Bool
     - Return True when the state machine is in the given state
   * - isEntering(state)
     - Bool
     - Return True when the state machine is entering the given state


States
------

There is multiple kinds of states that you can use.


* State (the base one)
* StateDelay
* StateFsm
* StateParallelFsm

In each of them you have access the following utilities :

.. list-table::
   :header-rows: 1
   :widths: 1 10

   * - Name
     - Description
   * - | onEntry{
       |  yourStatements
       | }
     - yourStatements is executed the cycle before entering the state
   * - | onExit{
       |  yourStatements
       | }
     - yourStatements is executed when the state machine will be in another state the next cycle
   * - | whenIsActive{
       |  yourStatements
       | }
     - yourStatements is executed when the state machine is in the state
   * - | whenIsNext{
       |  yourStatements
       | }
     - yourStatements is executed when the state machine will be in the state the next cycle
   * - goto(nextState)
     - Set the state of the state machine by nextState
   * - exit()
     - Set the state of the state machine to the boot one


For example, the following state could be defined in SpinalHDL by using the following syntax :

.. image:: /asset/picture/fsm_stateb.svg
   :align: center
   :width: 300

.. code-block:: scala

   val stateB : State = new State{
     onEntry(counter := 0)
     whenIsActive {
       counter := counter + 1
       when(counter === 4){
         goto(stateC)
       }
     }
     onExit(io.result := True)
   }

You can also define your state as the entry point of the state machine by extends the EntryPoint trait.

.. code-block:: scala

   val stateA: State = new State with EntryPoint {
     whenIsActive {
       goto(stateB)
     }
   }

StateDelay
^^^^^^^^^^

StateDelay allow you to create a state which wait a fixed number of cycles before executing statments in your ``whenCompleted{...}``. The standard way to write it is :

.. code-block:: scala

   val stateG : State = new StateDelay(cyclesCount=40){
     whenCompleted{
       goto(stateH)
     }
   }

But you can also write it like that :

.. code-block:: scala

   val stateG : State = new StateDelay(40){whenCompleted(goto(stateH))}

StateFsm
^^^^^^^^

StateFsm Allow you to describe a state which contains a nested state machine. When the nested state machine is done, your statments in ``whenCompleted{...}`` are executed.

There is an example of StateFsm definition :

.. code-block:: scala

   val stateC = new StateFsm(fsm=internalFsm()){
     whenCompleted{
       goto(stateD)
     }
   }

As you can see in the precedent code, it use a ``internalFsm`` function to create the inner state machine. There is an example of definition bellow :

.. code-block:: scala

   def internalFsm() = new StateMachine {
     val counter = Reg(UInt(8 bits)) init (0)

     val stateA: State = new State with EntryPoint {
       whenIsActive {
         goto(stateB)
       }
     }

     val stateB: State = new State {
       onEntry (counter := 0)
       whenIsActive {
         when(counter === 4) {
           exit()
         }
         counter := counter + 1
       }
     }
   }

In the precedent example, the ``exit()`` call will make the state machine jump to the boot state (a internal hidden state). This notify the StateFsm about the completion of the inner state machine.

StateParallelFsm
^^^^^^^^^^^^^^^^

This state is able to handle multiple nested state machines. When all nested state machine are done, your statments in ``whenCompleted{...}`` are executed.

There is an example of declaration :

.. code-block:: scala

   val stateD = new StateParallelFsm (internalFsmA(), internalFsmB()){
     whenCompleted{
       goto(stateE)
     }
   }
