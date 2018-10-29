---
layout: page
title: Color summing example
description: "This pages gives some examples Spinal"
tags: [getting started, examples]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/examples/simple/color_summing/
---



First let's define a Color `Bundle` with an addition operator.

```scala
  case class Color(channelWidth: Int) extends Bundle {
    val r = UInt(channelWidth bits)
    val g = UInt(channelWidth bits)
    val b = UInt(channelWidth bits)

    def +(that: Color): Color = {
      val result = Color(channelWidth)
      result.r := this.r + that.r
      result.g := this.g + that.g
      result.b := this.b + that.b
      return result
    }

    def clear(): Color ={
      this.r := 0
      this.g := 0
      this.b := 0
      this
    }
  }
```

Then let's define a component with a `sources` input which is a vector of colors, and a `result` output which is the sum of the `sources` input.

```scala
  class ColorSumming(sourceCount: Int, channelWidth: Int) extends Component {
    val io = new Bundle {
      val sources = in Vec(Color(channelWidth), sourceCount)
      val result = out(Color(channelWidth))
    }

    var sum = Color(channelWidth)
    sum.clear()
    for (i <- 0 to sourceCount - 1) {
      sum \= sum + io.sources(i)
    }
    io.result := sum
  }
```
