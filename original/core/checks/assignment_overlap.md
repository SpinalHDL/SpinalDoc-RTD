---
layout: page
title: ASSIGNMENT OVERLAP
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/core/checks/assignment_overlap/
---


## Introduction
SpinalHDL will check that no signal assignement completly erase a previous one.

## Example

The following code :

```scala
class TopLevel extends Component {
  val a = UInt(8 bits)
  a := 42
  a := 66 //Erease the a := 42 :(
}
```

will throw :

```
ASSIGNMENT OVERLAP completely the previous one of (toplevel/a :  UInt[8 bits])
  ***
  Source file location of the a := 66 assignement via the stack trace
  ***
```

A fix could be :

```scala
class TopLevel extends Component {
  val a = UInt(8 bits)
  a := 42
  when(something){
    a := 66
  }
}
```

But in the case you realy want to override the previous assignements (Yes, it could make sense in some cases), you can do as following :

```scala
class TopLevel extends Component {
  val a = UInt(8 bits)
  a := 42
  a.allowOverride
  a := 66
}
```
