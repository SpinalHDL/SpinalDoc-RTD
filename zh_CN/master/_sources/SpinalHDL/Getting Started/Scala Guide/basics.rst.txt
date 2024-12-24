
.. important::
   Variables and functions should be defined into ``object``\ , ``class``\ , ``function``. You can't define them on the root of a Scala file.

Basics
======

Types
-----

In Scala, there are 5 major types:

.. list-table::
   :header-rows: 1
   :widths: 1 1 2

   * - Type
     - Literal
     - Description
   * - Boolean
     - true, false
     - 
   * - Int
     - 3, 0x32
     - 32 bits integer
   * - Float
     - 3.14f
     - 32 bits floating point
   * - Double
     - 3.14
     - 64 bits floating point
   * - String
     - "Hello world"
     - UTF-16 string


Variables
---------

In Scala, you can define a variable by using the ``var`` keyword:

.. code-block:: scala

   var number : Int = 0
   number = 6
   number += 4
   println(number) // 10

Scala is able to infer the type automatically. You don't need to specify it if the variable is assigned at declaration:

.. code-block:: scala

   var number = 0   // The type of 'number' is inferred as an Int during compilation.

However, it's not very common to use ``var`` in Scala. Instead, constant values defined by ``val`` are often used:

.. code-block:: scala

   val two   = 2
   val three = 3
   val six   = two * three

Functions
---------

For example, if you want to define a function which returns ``true`` if the sum of its two arguments is bigger than zero, you can do as follows:

.. code-block:: scala

   def sumBiggerThanZero(a: Float, b: Float): Boolean = {
     return (a + b) > 0
   }

Then, to call this function, you can write:

.. code-block:: scala

   sumBiggerThanZero(2.3f, 5.4f)

You can also specify arguments by name, which is useful if you have many arguments:

.. code-block:: scala

   sumBiggerThanZero(
     a = 2.3f,
     b = 5.4f
   )

Return
^^^^^^

The ``return`` keyword is not necessary. In absence of it, Scala takes the last statement of your function as the returned value.

.. code-block:: scala

   def sumBiggerThanZero(a: Float, b: Float): Boolean = {
     (a + b) > 0
   }

Return type inferation
^^^^^^^^^^^^^^^^^^^^^^

Scala is able to automatically infer the return type. You don't need to specify it:

.. code-block:: scala

   def sumBiggerThanZero(a: Float, b: Float) = {
     (a + b) > 0
   }

Curly braces
^^^^^^^^^^^^

Scala functions don't require curly braces if your function contains only one statement:

.. code-block:: scala

   def sumBiggerThanZero(a: Float, b: Float) = (a + b) > 0

Function that returns nothing
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

If you want a function to return nothing, the return type should be set to ``Unit``. It's equivalent to the C/C++ ``void`` type.

.. code-block:: scala

   def printer(): Unit = {
     println("1234")
     println("5678")
   }

Argument default values
^^^^^^^^^^^^^^^^^^^^^^^

You can specify a default value for each argument of a function:

.. code-block:: scala

   def sumBiggerThanZero(a: Float, b: Float = 0.0f) = {
     (a + b) > 0
   }

Apply
^^^^^

Functions named ``apply`` are special because you can call them without having to type their name:

.. code-block:: scala

   class Array() {
     def apply(index: Int): Int = index + 3
   }

   val array = new Array()
   val value = array(4)   // array(4) is interpreted as array.apply(4) and will return 7

This concept is also applicable for Scala ``object`` (static)

.. code-block:: scala

   object MajorityVote {
     def apply(value: Int): Int = ...
   }

   val value = MajorityVote(4) // Will call MajorityVote.apply(4)

Object
------

In Scala, there is no ``static`` keyword. In place of that, there is ``object``. Everything defined inside an ``object`` definition is static.

The following example defines a static function named ``pow2`` which takes a floating point value as parameter and returns a floating point value as well.

.. code-block:: scala

   object MathUtils {
     def pow2(value: Float): Float = value * value
   }

Then you can call it by writing:

.. code-block:: scala

   MathUtils.pow2(42.0f)

Entry point (main)
------------------

The entry point of a Scala program (the main function) should be defined inside an object as a function named ``main``.

.. code-block:: scala

   object MyTopLevelMain {
     def main(args: Array[String]) {
       println("Hello world")
     }
   }

Class
-----

The class syntax is very similar to Java. Imagine that you want to define a ``Color`` class which takes as construction parameters three Float values (r,g,b) :

.. code-block:: scala

   class Color(r: Float, g: Float, b: Float) {
     def getGrayLevel(): Float = r * 0.3f + g * 0.4f + b * 0.4f
   }

Then, to instantiate the class from the previous example and use its ``getGrayLevel`` function:

.. code-block:: scala

   val blue = new Color(0, 0, 1)
   val grayLevelOfBlue = blue.getGrayLevel()

Be careful, if you want to access a construction parameter of the class from the outside, this construction parameter should be defined as a ``val``:

.. code-block:: scala

   class Color(val r: Float, val g: Float, val b: Float) { ... }
   ...
   val blue = new Color(0, 0, 1)
   val redLevelOfBlue = blue.r

Inheritance
^^^^^^^^^^^

As an example, suppose that you want to define two classes, ``Rectangle`` and ``Square``, which extend the class ``Shape``:

.. code-block:: scala

   class Shape {
     def getArea(): Float
   }

   class Square(sideLength: Float) extends Shape {
     override def getArea() = sideLength * sideLength
   }

   class Rectangle(width: Float, height: Float) extends Shape {
     override def getArea() = width * height
   }

Case class
^^^^^^^^^^

Case class is an alternative way of declaring classes.

.. code-block:: scala

   case class Rectangle(width: Float, height: Float) extends Shape {
     override def getArea() = width * height
   }

Then there are some differences between ``case class`` and ``class`` :

* case classes don't need the ``new`` keyword to be instantiated.
* construction parameters are accessible from outside; you don't need to define them as ``val``.

In SpinalHDL, this explains the reasoning behind the coding conventions: it's in general recommended to use ``case class`` instead of ``class`` in order to have less typing and more coherency.

Templates / Type parameterization
---------------------------------

Imagine you want to design a class which is a queue of a given datatype, in that case you need to provide a type parameter to the class:

.. code-block:: scala

   class  Queue[T]() {
     def push(that: T) : Unit = ...
     def pop(): T = ...
   }

If you want to restrict the ``T`` type to be a sub class of a given type (for example ``Shape``), you can use the ``<: Shape`` syntax :

.. code-block:: scala

   class Shape() {   
       def getArea(): Float
   }
   class Rectangle() extends Shape { ... }

   class  Queue[T <: Shape]() {
     def push(that: T): Unit = ...
     def pop(): T = ...
   }

The same is possible for functions:

.. code-block:: scala

   def doSomething[T <: Shape](shape: T): Something = { shape.getArea() }
