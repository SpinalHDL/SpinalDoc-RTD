---
layout: page
title: Scala starter
description: "TODO"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /scala/basics/
---

{% include important.html content="Variable and functions should be defined into `object`, `class`, `function`. You can't define them on the root of a Scala file." %}

## Types

In Scala, there is 5 major types

| Type    | Literal       | Description              |
| ------- | ------------- | ------------------------ |
| Boolean | true, false   | -                        |
| Int     | 3, 0x32       | 32 bits integer          |
| Float   | 3.14f         | 32 bits floating point   |
| Double  | 3.14          |  64 bits floating point  |
| String  | "Hello world" | UTF-16 string            |


## Variable
In scala, you can define variable by using the var keyword :

```scala
var number : Int = 0
number = 6
number += 4
println(number) // 10
```

Scala is able to infer the type automatically. You don't need to specify it if the variable is assigned at declaration:

```scala
var number = 0   //The type of 'number' is inferred as a Int during the compilation.
```

But in fact, it's not very common to use `var` in Scala. In place of `var`, the `val` is very often used. `val` allow you to define a constant value :

```scala
val two   = 2
val three = 3
val six   = two * three
```

## Function
For example, if you want to define a function which return true if the sum of its two arguments is bigger than zero, you can do as following :

```scala
def sumBiggerThanZero(a: Float, b: Float): Boolean = {
  return (a + b) > 0
}
```

Then to call this function you can do as following :

```scala
sumBiggerThanZero(2.3f, 5.4f)
```

You can also specify arguements by name, which is useful if you have many arguements :

```scala
sumBiggerThanZero(
  a = 2.3f,
  b = 5.4f
)
```

### Return
The return keyword is not necessary. In absence of it, Scala take the last statement of your function as returned value.

```scala
def sumBiggerThanZero(a: Float, b: Float): Boolean = {
  (a + b) > 0
}
```

### Return type inferation
Scala is able to automatically infer the return type. You don't need to specify it :

```scala
def sumBiggerThanZero(a: Float, b: Float) = {
  (a + b) > 0
}
```

### Curly braces
Scala function doesn't require to have curly braces if your function contain only one statement :

```scala
def sumBiggerThanZero(a: Float, b: Float) = (a + b) > 0
```

### Function that return nothing
If you want a function to return nothing, the return type should be set to `Unit`. It's equivalent to the C/C++ void.

```scala
def printer(): Unit = {
  println("1234")
  println("5678")
}
```

### Arguements default value
You can specify a default value to each arguements of a function :

```scala
def sumBiggerThanZero(a: Float, b: Float = 0.0f) = {
  (a + b) > 0
}
```


### Apply
Functions named apply are special because you can call them without having to type their name :

```scala
class Array(){
  def apply(index: Int): Int = index + 3
}

val array = new Array()
val value = array(4)   //array(4) is interpreted as array.apply(4) and will return 7
```

This concept is also applicable for scala `object` (static)

```scala
object MajorityVote{
  def apply(value: Int): Int = ...
}

val value = MajorityVote(4) // Will call MajorityVote.apply(4)
```

## Object

In scala, there is no `static` keyword. In place of that, there is `object`. Everything defined into an `object` is static.

The following example define a static function named pow2 which take as parameter an floating point value and return a floating point as well.

```scala
object MathUtils{
  def pow2(value: Float): Float = value*value
}
```

Then you can call it by writing :

```scala
MathUtils.pow2(42.0f)
```

## Entry point (main)

The entry point of a Scala program (the main function) should be defined into an object as a function named `main`.

```scala
object MyTopLevelMain{
  def main(args: Array[String]) {
    println("Hello world")
  }
}
```

## Class

The class syntax is very similar to the Java one. Imagine you want to define an Color class which take as construction parameter three Float value (r,g,b) :

```scala
class Color(r: Float, g: Float, b: Float){
  def getGrayLevel(): Float = r * 0.3f + g * 0.4f + b *0.4f
}
```

Then to instantiate a the class from the previous example and use its gray function :

```scala
val blue = new Color(0, 0, 1)
val grayLevelOfBlue = blue.getGrayLevel()
```

Be careful, if you want to access a construction parameter of the class from the outside, this construction parameter should be defined as a val :

```scala
class Color(valr : Float, val g: Float, val b: Float){ ... }
...
val blue = new Color(0, 0, 1)
val redLevelOfBlue = blue.r
```

### Inheritance
As an example, imagine you want to define an class Rectangle and a class Square which extends the class Shape :

```scala
class Shape{
  def getArea(): Float
}

class Square(sideLength: Float) extends Shape {
  override def getArea() = sideLength * sideLength
}

class Rectangle(width: Float, height: Float) extends Shape {
  override def getArea() = width * height
}
```

### Case class

Case class is an alternative way of declaring classes.

```scala
case class Rectangle(width: Float, height: Float) extends Shape {
  override def getArea() = width * height
}
```

Then there is some differences between `case class` and `class` :

- case class doesn't need the `new` keyword to be instantiated
- construction parameters are accessible from the outside, you don't need to define them as `val`.

In SpinalHDL, for some reason explains into the coding conventions, it's in general recommended to use case class instead of class to have less typing and more coherency.

## Templates / Type parameterization

Imagine you want to design a class which is a queue of a given datatype, in that case you need to provide a type parameter to the class :

```scala
class  Queue[T](){
  def push(that: T) : Unit = ...
  def pop(): T = ...
}
```

If you want to restrict the `T` type to be a sub class of a given type (for example Shape), you can use the `<: Shape` syntax :

```scala
class Shape() {   
    def getArea(): Float
}
class Rectangle() extends Shape { ... }

class  Queue[T <: Shape](){
  def push(that: T): Unit = ...
  def pop(): T = ...
}
```

The same is possible for functions :

```scala
def doSomething[T <: Shape](shape: T): Something = { shape.getArea() }
```
