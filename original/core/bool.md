---
layout      : page
title       : Boolean (Bool)
description : "Describes Bool data type"
tags        : [datatype]
categories  : [documentation, types, Bool]
permalink   : /spinal/core/types/Bool
sidebar     : spinal_sidebar
---


### Description

The `Bool` type corresponds to a boolean value (True or False).
    

### Declaration

The syntax to declare a boolean value is as follows: (everything between [] are optional)

| Syntax                | Description                                              | Return |
| --------------------- | -------------------------------------------------------- | ------ |
| Bool[()]              | Create a              Bool                               | Bool   |
| True                  | Create a Bool assigned with `true`                       | Bool   |
| False                 | Create a Bool assigned with `false`                      | Bool   |
| Bool(value: Boolean)  | Create a Bool assigned with a Scala Boolean(true, false) | Bool   |


```scala
val myBool_1 = Bool          // Create a Bool 
myBool_1 := False            // := is the assignment operator

val myBool_2 = False         // Equivalent to the code above 

val myBool_3 = Bool(5 > 12)  // Use a Scala Boolean to create a Bool 
```


### Operators

The following operators are available for the `Bool` type


#### Logic

| Operator              | Description                | Return type |
| --------------------- | -------------------------- | ----------- |
| !x                    |  Logical NOT               | Bool        |
| x && y <br> x & y     |  Logical AND               | Bool        |
| x \|\| y <br> x \| y  |  Logical OR                | Bool        |
| x ^ y                 | Logical XOR                | Bool        |
| x.set[()]             |  Set x to True             | -           |
| x.clear[()]           |  Set x to False            | -           |
| x.setWhen(cond)       | Set x when cond is True    | Bool        |
| x.clearWhen(cond)     |  Clear x when cond is True | Bool        |


```scala
val a, b, c = Bool
val res = (!a & b) ^ c   // ((NOT a) AND b) XOR c

val d = False
when(cond){
  d.set()    // equivalent to d := True
}

val e = False
e.setWhen(cond) // equivalent to when(cond){ d := True }
```



#### Edge detection

| Operator              | Description                                                  | Return type |
| --------------------- | -----------------------------------------------------------  | ----------- |
| x.edge[()]            | Return True when x changes state                             | Bool        |
| x.edge(initAt: Bool)  | Same as x.edge but with a reset value                        | Bool        |
| x.rise[()]            | Return True when x was low at the last cycle and is now high | Bool        |
| x.rise(initAt: Bool)  | Same as x.rise but with a reset value                        | Bool        |
| x.fall[()]            | Return True when x was high at the last cycle and is now low | Bool        |
| x.fall(initAt: Bool)  | Same as x.fall but with a reset value                        | Bool        |
| x.edges[()]           | Return a bundle (rise, fall, toggle)                         | BoolEdges   |
| x.edges(initAt: Bool) | Same as x.edges but with a reste value                       | BoolEdges   |


```scala
when(myBool_1.rise(False)){
	// do something when a rising edge is detected 
} 


val edgeBundle = myBool_2.edges(False)
when(edgeBundle.rise){
	// do something when a rising edge is detected
}
when(edgeBundle.fall){
	// do something when a falling edge is detected
}
when(edgeBundle.toggle){
	// do something at each edge
}
```


#### Comparison

| Operator | Description | Return type |
| -------- | ----------- | ----------- |
| x === y  |  Equality   | Bool        |
| x =/= y  |  Inequality | Bool        |


```scala
when(myBool){ // Equivalent to when(myBool === True)
	// do something when myBool is True
}

when(!myBool){ // Equivalent to when(myBool === False)
	// do something when myBool is False
}
```


#### Type cast		
  		  
| Operator           | Description                  | Return              |		 
| ------------------ | ---------------------------- | ------------------- |		 
| x.asBits           | Binary cast in Bits          | Bits(w(x) bits)     |		 
| x.asUInt           | Binary cast in UInt          | UInt(w(x) bits)     |		 
| x.asSInt           | Binary cast in SInt          | SInt(w(x) bits)     |		 
| x.asUInt(bitCount) | Binary cast in UInt + resize | UInt(bitCount bits) |		 
| x.asBits(bitCount) | Binary cast in Bits + resize | Bits(bitCount bits) |


```scala
// Add the carry to an SInt value
val carry = Bool 
val res = mySInt + carry.asSInt 
```



#### Misc

| Operator                            | Description                                               | Return                         |
| ----------------------------------- | --------------------------------------------------------- | ------------------------------ |
| x ## y                              |  Concatenate, x->high, y->low                             | Bits(w(x) + w(y) bits)         |


```scala
val a, b, c = Bool

// Concatenation of three Bool into a Bits
val myBits = a ## b ## c 
```