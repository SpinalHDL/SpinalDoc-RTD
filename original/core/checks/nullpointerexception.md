---
layout: page
title: NullPointerException
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/core/checks/nullpointerexception/
---


## Introduction
NullPointerException is an Scala runtime reported error which happen when a variable was accessed but wasn't initialised.

## Example

The following code :

```scala
class TopLevel extends Component {
  a := 42
  val a = UInt(8 bits)
}
```

will throw :

```
Exception in thread "main" java.lang.NullPointerException
  ***
  Source file location of the a := 42 assignement via the stack trace
  ***
```

A fix could be :

```scala
class TopLevel extends Component {
  val a = UInt(8 bits)
  a := 42
}
```

<b>Issue explanation : </b><br>
<br>
SpinalHDL is not a language, it is an Scala library, which mean, it obey to the same rules than the Scala general purpose programming language. When you run your SpinalHDL hardware description to generate the corresponding VHDL/Verilog RTL, your SpinalHDL hardware description will be executed as a Scala programm, and `a` will be a null reference until the programm execution come to that line, and it’s why you can’t use it before.
