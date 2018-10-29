---
layout: page
title: QSysify
description: "This pages describes the lib components of Spinal"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/lib/eda/qsysify/
---

## Introduction

QSysify is a tool which is able to generate a QSys IP (tcl script) from a SpinalHDL component by analysing its IO definition. It currently implement the following interfaces features :

- Master/Slave AvalonMM
- Master/Slave APB3
- Clock domain input
- Reset output
- Interrupt input
- Conduit (Used in last resort)

## Example

In the case of a UART controller :

```scala
case class AvalonMMUartCtrl(...) extends Component{
  val io = new Bundle{
    val bus =  slave(AvalonMM(AvalonMMUartCtrl.getAvalonMMConfig))
    val uart = master(Uart())
  }

  //...
}
```

The following  `main` will generate the Verilog and the QSys TCL script with io.bus as an AvalonMM and io.uart as a conduit :

```scala
object AvalonMMUartCtrl{
  def main(args: Array[String]) {
    //Generate the Verilog
    val toplevel = SpinalVerilog(AvalonMMUartCtrl(UartCtrlMemoryMappedConfig(...))).toplevel

    //Add some tags to the avalon bus to specify it's clock domain (information used by QSysify)
    toplevel.io.bus addTag(ClockDomainTag(toplevel.clockDomain))

    //Generate the QSys IP (tcl script)
    QSysify(toplevel)
  }
}
```

## tags

Because QSys require some information that are not specified in the SpinalHDL hardware specification, some tags should be added to interface:

### AvalonMM / APB3

```scala
io.bus addTag(ClockDomainTag(busClockDomain))
```

### Interrupt input

```scala
io.interrupt addTag(InterruptReceiverTag(relatedMemoryInterfacei, interruptClockDomain))
```

### Reset output

```scala
io.resetOutput addTag(ResetEmitterTag(resetOutputClockDomain))
```

## Adding new interface support

Basicaly, the QSysify tool can be setup with a list of interface `emitter` [(as you can see here)](https://github.com/SpinalHDL/SpinalHDL/blob/764193013f84cfe4f82d7d1f1739c4561ef65860/lib/src/main/scala/spinal/lib/eda/altera/QSys.scala#L12)

You can create your own emitter by creating a new class extending [QSysifyInterfaceEmiter](https://github.com/SpinalHDL/SpinalHDL/blob/764193013f84cfe4f82d7d1f1739c4561ef65860/lib/src/main/scala/spinal/lib/eda/altera/QSys.scala#L24)
