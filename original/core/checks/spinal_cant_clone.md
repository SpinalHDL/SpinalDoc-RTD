---
layout: page
title: Spinal cant clone class
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/core/checks/spinal_cant_clone/
---


## Introduction
This error happen when SpinalHDL want to create a new datatype via the cloneOf feature but isn't able to do it. The reasons of this is nearly always because it can retreive the construction parameters of Bundle.

## Example

The following code :

```scala
 //cloneOf(this) isn't able to retreive the width value that was used to construct itself
 class RGB(width : Int) extends Bundle{   
   val r,g,b = UInt(width bits)
 }

 class TopLevel extends Component {
   val tmp = Stream(new RGB(8)) //Stream require the capability to cloneOf(new RGB(8))
 }
```

will throw :

```
*** Spinal can't clone class spinal.tester.PlayDevMessages$RGB datatype
*** You have two way to solve that :
*** In place to declare a "class Bundle(args){}", create a "case class Bundle(args){}"
*** Or override by your self the bundle clone function
  ***
  Source file location of the RGB class definition via the stack trace
  ***
```

A fix could be :

```scala
case class RGB(width : Int) extends Bundle{   
  val r,g,b = UInt(width bits)
}

class TopLevel extends Component {
  val tmp = Stream(RGB(8))
}
```
