---
layout: page
title: CLOCK CROSSING VIOLATION
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/core/checks/clock_crossing_violation/
---


## Introduction
SpinalHDL will check that each registers of your design only depend (through some combinatorial logic) on registers which use same clock domain or a syncronus one.

## Example

The following code :

```scala
class TopLevel extends Component {
  val clkA = ClockDomain.external("clkA")
  val clkB = ClockDomain.external("clkB")

  val regA = clkA(Reg(UInt(8 bits)))   //PlayDev.scala:834
  val regB = clkB(Reg(UInt(8 bits)))   //PlayDev.scala:835

  val tmp = regA + regA                //PlayDev.scala:838
  regB := tmp
}
```

will throw :

```
CLOCK CROSSING VIOLATION from (toplevel/regA :  UInt[8 bits]) to (toplevel/regB :  UInt[8 bits]).
- Register declaration at
  ***
  Source file location of the toplevel/regA definition via the stack trace
  ***
- through
      >>> (toplevel/regA :  UInt[8 bits]) at ***(PlayDev.scala:834) >>>
      >>> (toplevel/tmp :  UInt[8 bits]) at ***(PlayDev.scala:838) >>>
      >>> (toplevel/regB :  UInt[8 bits]) at ***(PlayDev.scala:835) >>>

```

There is multiple fixes possible :

### crossClockDomain tag
The crossClockDomain can be used to say "It's allright, don't panic" to SpinalHDL

```scala
class TopLevel extends Component {
  val clkA = ClockDomain.external("clkA")
  val clkB = ClockDomain.external("clkB")

  val regA = clkA(Reg(UInt(8 bits)))
  val regB = clkB(Reg(UInt(8 bits))).addTag(crossClockDomain)


  val tmp = regA + regA
  regB := tmp
}
```

### setSyncronousWith
You can specify that two clock domains are syncronous together.

```scala
class TopLevel extends Component {
  val clkA = ClockDomain.external("clkA")
  val clkB = ClockDomain.external("clkB")
  clkB.setSyncronousWith(clkA)

  val regA = clkA(Reg(UInt(8 bits)))
  val regB = clkB(Reg(UInt(8 bits)))


  val tmp = regA + regA
  regB := tmp
}
```
