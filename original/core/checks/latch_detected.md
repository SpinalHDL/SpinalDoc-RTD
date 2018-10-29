---
layout: page
title: LATCH DETECTED
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/core/checks/latch_detected/
---


## Introduction
SpinalHDL will check that no combinatorial signal will infer a latch in synthesis. In other words, that no combinatorial are partialy assigned.

## Example

The following code :

```scala
class TopLevel extends Component {
  val cond = in(Bool)
  val a = UInt(8 bits)

  when(cond){
    a := 42
  }
}
```

will throw :

```
LATCH DETECTED from the combinatorial signal (toplevel/a :  UInt[8 bits]), defined at
  ***
  Source file location of the toplevel/io_a definition via the stack trace
  ***
```

A fix could be :

```scala
class TopLevel extends Component {
  val cond = in(Bool)
  val a = UInt(8 bits)

  a := 0
  when(cond){
    a := 42
  }
}
```
