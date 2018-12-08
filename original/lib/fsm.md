---
layout: page
title: State machine
description: "This pages describes the lib components of Spinal"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/lib/fsm/
---

## Introduction
In SpinalHDL you can define your state machine like in VHDL/Verilog, by using enumerations and switch cases statements. But in SpinalHDL you can also use a dedicated syntax.

The following state machine is implemented in following examples :

<img src="https://cdn.jsdelivr.net/gh/SpinalHDL/SpinalDoc@9c3a3cd928361f2cc93ec90c8727b3903592f970/asset/picture/fsm_simple.svg"  align="middle" width="100">


Style A :

```scala
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
```

Style B :

```scala
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
```


## StateMachine
StateMachine is the base class that will manage the logic of your FSM.

```scala
val myFsm = new StateMachine{
  // Here will come states definition
}
```

The StateMachine class also provide some utils :

| Name | Return | Description |
| ------- | ---- | ---- |
| isActive(state)     | Bool | Return True when the state machine is in the given state |
| isEntering(state)   | Bool | Return True when the state machine is entering the given state |


## States
There is multiple kinds of states that you can use.

- State (the base one)
- StateDelay
- StateFsm
- StateParallelFsm

In each of them you have access the following utilities :

| Name | Description |
| ------- | ---- |
| onEntry{<br> &nbsp;&nbsp;yourStatements<br>}      | yourStatements is executed the cycle before entering the state |
| onExit{<br> &nbsp;&nbsp;yourStatements<br>}       | yourStatements is executed when the state machine will be in another state the next cycle |
| whenIsActive{<br> &nbsp;&nbsp;yourStatements<br>} | yourStatements is executed when the state machine is in the state |
| whenIsNext{<br> &nbsp;&nbsp;yourStatements<br>}   | yourStatements is executed when the state machine will be in the state the next cycle |
| goto(nextState)                                   | Set the state of the state machine by nextState |
| exit()                                            | Set the state of the state machine to the boot one |

For example, the following state could be defined in SpinalHDL by using the following syntax :

<img src="https://cdn.jsdelivr.net/gh/SpinalHDL/SpinalDoc@078d8598cd84600cf83dab86a45a7c5c986706e1/asset/picture/fsm_stateb.svg"  align="middle" width="100">

```scala
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
```

You can also define your state as the entry point of the state machine by extends the EntryPoint trait.

```scala
val stateA: State = new State with EntryPoint {
  whenIsActive {
    goto(stateB)
  }
}
```

### StateDelay
StateDelay allow you to create a state which wait a fixed number of cycles before executing statments in your `whenCompleted{...}`. The standard way to write it is :

```scala
val stateG : State = new StateDelay(cyclesCount=40){
  whenCompleted{
    goto(stateH)
  }
}
```

But you can also write it like that :

```scala
val stateG : State = new StateDelay(40){whenCompleted(goto(stateH))}
```

### StateFsm
StateFsm Allow you to describe a state which contains a nested state machine. When the nested state machine is done, your statments in `whenCompleted{...}` are executed.

There is an example of StateFsm definition :

```scala
val stateC = new StateFsm(fsm=internalFsm()){
  whenCompleted{
    goto(stateD)
  }
}
```

As you can see in the precedent code, it use a `internalFsm` function to create the inner state machine. There is an example of definition bellow :

```scala
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
```

In the precedent example, the `exit()` call will make the state machine jump to the boot state (a internal hidden state). This notify the StateFsm about the completion of the inner state machine.

### StateParallelFsm
This state is able to handle multiple nested state machines. When all nested state machine are done, your statments in `whenCompleted{...}` are executed.

There is an example of declaration :

```scala
val stateD = new StateParallelFsm (internalFsmA(), internalFsmB()){
  whenCompleted{
    goto(stateE)
  }
}
```
