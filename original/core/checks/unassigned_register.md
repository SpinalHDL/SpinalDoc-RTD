---
layout: page
title: UNASSIGNED REGISTER
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/core/checks/unassigned_register/
---


## Introduction
SpinalHDL will check that all registers which have impacts on the design are assigned by something.

## Example

The following code :

```scala
class TopLevel extends Component {
  val result = out(UInt(8 bits))
  val a = Reg(UInt(8 bits))
  result := a
}
```

will throw :

```
UNASSIGNED REGISTER (toplevel/a :  UInt[8 bits]), defined at
  ***
  Source file location of the toplevel/a definition via the stack trace
  ***
```

A fix could be :

```scala
class TopLevel extends Component {
  val result = out(UInt(8 bits))
  val a = Reg(UInt(8 bits))
  a := 42
  result := a
}
```

## Register with only init

In some case, because of the design parameterization, it could make sense to generate a register which as no assignement but only a init statement.

```scala
class TopLevel extends Component {
  val result = out(UInt(8 bits))
  val a = Reg(UInt(8 bits)) init(42)

  if(something)
    a := somethingElse
  result := a
}
```

will throw :

```
UNASSIGNED REGISTER (toplevel/a :  UInt[8 bits]), defined at
  ***
  Source file location of the toplevel/a definition via the stack trace
  ***
```

To fix it you can ask SpinalHDL to transform the register into a combinatorial one if no assignement is present but it as a init statement :

```scala
class TopLevel extends Component {
  val result = out(UInt(8 bits))
  val a = Reg(UInt(8 bits)).init(42).allowUnsetRegToAvoidLatch
  
  if(something)
    a := somethingElse
  result := a
}
```
