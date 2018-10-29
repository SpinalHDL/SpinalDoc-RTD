---
layout: page
title: Support
description: "Support"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /frequent_errors/
---


## Introduction
This page will talk about errors which could happen when people are using SpinalHDL.

## Exception in thread "main" java.lang.NullPointerException

<b>Console symptoms :</b>

```
Exception in thread "main" java.lang.NullPointerException
```

<b>Code Example :</b>

```scala
val a = b + 1         //b can't be read at that time, because b isn't instanciated yet
val b = UInt(4 bits)
```

<b>Issue explanation : </b><br>
<br>
SpinalHDL is not a language, it is an Scala library, which mean, it obey to the same rules than the Scala general purpose programming language. When you run your SpinalHDL hardware description to generate the corresponding VHDL/Verilog RTL, your SpinalHDL hardware description will be executed as a Scala programm, and b will be a `null` reference until the programm execution come to that line, and it's why you can't use it before.

## Hierarchy violation
The SpinalHDL compiler check that all your assignements are legal from an hierarchy perspective. Multiple cases are elaborated in following chapters

### Signal X can't be assigned by Y

<b>Console symptoms :</b>

```
Hierarchy violation : Signal X can't be assigned by Y
```

<b>Code Example :</b>

```scala
class ComponentX extends Component{
  ...
  val X = Bool
  ...
}

class ComponentY extends Component{
  ...
  val componentX = new ComponentX
  val Y = Bool
  componentX.X := Y //This assignement is not legal
  ...
}
```

```scala
class ComponentX extends Component{
  val io = new Bundle{
    val X = Bool   //Forgot to specify an in/out direction
  }
  ...
}

class ComponentY extends Component{
  ...
  val componentX = new ComponentX
  val Y = Bool
  componentX.io.X := Y //This assignement will be detected as not legal
  ...
}
```

<b>Issue explanation : </b><br>
<br>
You can only assign input signals of subcomponents, else there is an hierarchy violation. If this issue happend, you probably forgot to specify the X signal's direction.

### Input signal X can't be assigned by Y


<b>Console symptoms :</b>

```
Hierarchy violation : Input signal X can't be assigned by Y
```

<b>Code Example :</b>

```scala
class ComponentXY extends Component{
  val io = new Bundle{
    val X = in Bool
  }
  ...
  val Y = Bool
  io.X := Y //This assignement is not legal
  ...
}
```

<b>Issue explanation : </b><br>
<br>
You can only assign an input signals from the parent component, else there is an hierarchy violation. If this issue happend, you probably mixed signals direction declaration.

### Output signal X can't be assigned by Y


<b>Console symptoms :</b>

```
Hierarchy violation : Output signal X can't be assigned by Y
```

<b>Code Example :</b>

```scala
class ComponentX extends Component{
  val io = new Bundle{
    val X = out Bool
  }
  ...
}

class ComponentY extends Component{
  ...
  val componentX = new ComponentX
  val Y = Bool
  componentX.X := Y //This assignement is not legal
  ...
}
```

<b>Issue explanation : </b><br>
<br>
You can only assign output signals of a component from the inside of it, else there is an hierarchy violation. If this issue happend, you probably mixed signals direction declaration.
