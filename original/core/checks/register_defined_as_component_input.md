---
layout: page
title: REGISTER DEFINED AS COMPONENT INPUT
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/core/checks/register_defined_as_component_input/
---


## Introduction
In SpinalHDL, it is not allowed to define an component input as a register. The reason of that is for the user to avoid having surprise when he drive sub components inputs.

## Example

The following code :

```scala
class TopLevel extends Component {
  val io = new Bundle {
    val a = in(Reg(UInt(8 bits)))
  }
}
```

will throw :

```
REGISTER DEFINED AS COMPONENT INPUT : (toplevel/io_a : in UInt[8 bits]) is defined as a registered input of the toplevel component, but isn't allowed.
  ***
  Source file location of the toplevel/io_a definition via the stack trace
  ***
```

A fix could be :

```scala
class TopLevel extends Component {
  val io = new Bundle {
    val a = in UInt(8 bits)
  }
}
```
