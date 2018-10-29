---
layout: page
title: Counter with a clear input
description: "This pages gives some examples Spinal"
tags: [getting started, examples]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/examples/simple/counter_with_clear/
---


This example defines a component with a `clear` input and a `value` output.
Each clock cycle, the `value` output is incrementing, but when `clear` is high, `value` is cleared.

```scala
class Counter(width : Int) extends Component{
  val io = new Bundle{
    val clear = in Bool
    val value = out UInt(width bits)
  }
  val register = Reg(UInt(width bits)) init(0)
  register := register + 1
  when(io.clear){
    register := 0
  }
  io.value := register
}
```
