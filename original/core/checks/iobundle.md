---
layout: page
title: IO BUNDLE ERROR
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/core/checks/iobundle/
---


## Introduction
SpinalHDL will check that in each io bundle there only in/out/inout signals.

## Example

The following code :

```scala
class TopLevel extends Component {
  val io = new Bundle {
    val a = UInt(8 bits)
  }
}
```

will throw :

```
IO BUNDLE ERROR : A direction less (toplevel/io_a :  UInt[8 bits]) signal was defined into toplevel component's io bundle
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

But if for meta hardware description reasons you realy want io.a to be direction less, you can do :

```scala
class TopLevel extends Component {
  val io = new Bundle {
    val a = UInt(8 bits)
  }
  a.allowDirectionLessIo
}
```
