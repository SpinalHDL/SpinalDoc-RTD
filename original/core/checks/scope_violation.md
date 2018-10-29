---
layout: page
title: SCOPE VIOLATION
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/core/checks/scope_violation/
---


## Introduction
SpinalHDL will check that there no signals assigned outside it's declaration scope. This error isn't easy to trigger as it require some specific meta hardware description tricks.

## Example

The following code :

```scala
class TopLevel extends Component {
  val cond = Bool()

  var tmp : UInt = null
  when(cond){
    tmp = UInt(8 bits)
  }
  tmp := U"x42"
}
```

will throw :

```
SCOPE VIOLATION : (toplevel/tmp :  UInt[8 bits]) is assigned outside its declaration scope at
  ***
  Source file location of the tmp := U"x42" via the stack trace
  ***
```

A fix could be :

```scala
class TopLevel extends Component {
  val cond = Bool()

  var tmp : UInt = UInt(8 bits)
  when(cond){

  }
  tmp := U"x42"
}
```
