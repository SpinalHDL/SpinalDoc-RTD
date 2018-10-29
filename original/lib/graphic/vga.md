---
layout: page
title: VGA
description: "This pages describes the lib components of Spinal"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/lib/graphic/vga/
---


## VGA bus
An VGA bus definition is available via the Vga bundle.

```scala
case class Vga (rgbConfig: RgbConfig) extends Bundle with IMasterSlave{
  val vSync = Bool
  val hSync = Bool

  val colorEn = Bool  //High when the frame is inside the color area
  val color = Rgb(rgbConfig)

  override def asMaster() = this.asOutput()
}
```

## VGA timings
VGA timings could be modeled in hardware by using an VgaTimings bundle :

```scala
case class VgaTimingsHV(timingsWidth: Int) extends Bundle {
  val colorStart = UInt(timingsWidth bit)
  val colorEnd = UInt(timingsWidth bit)
  val syncStart = UInt(timingsWidth bit)
  val syncEnd = UInt(timingsWidth bit)
}

case class VgaTimings(timingsWidth: Int) extends Bundle {
  val h = VgaTimingsHV(timingsWidth)
  val v = VgaTimingsHV(timingsWidth)

   def setAs_h640_v480_r60 = ...
   def driveFrom(busCtrl : BusSlaveFactory,baseAddress : Int) = ...
}
```

## VGA controller
An VGA controller is available. It's definition is the following :

```scala
case class VgaCtrl(rgbConfig: RgbConfig, timingsWidth: Int = 12) extends Component {
  val io = new Bundle {
    val softReset = in Bool
    val timings   = in(VgaTimings(timingsWidth))

    val frameStart = out Bool
    val pixels     = slave Stream (Rgb(rgbConfig))
    val vga        = master(Vga(rgbConfig))

    val error      = out Bool
  }
  // ...
}
```

`frameStart` is a signals that pulse one cycle at the beginning of each new frame.<br>
`pixels` is a stream of color used to feed the VGA interface when needed.<br>
`error` is high when a transaction on the pixels is needed, but nothing is present.
