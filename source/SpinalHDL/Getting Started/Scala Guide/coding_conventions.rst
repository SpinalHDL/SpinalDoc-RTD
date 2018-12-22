
Coding conventions
==================

Introduction
------------

The coding conventions used in SpinalHDL is the same than the one documented in the `scala doc <https://docs.scala-lang.org/style/>`_.

Then some additional practice and practical cases are explained in next chapters.

class vs case class
-------------------

When you need to define a Bundle or a Component, have a preference to declare them as case class.

Reasons are :


* It avoid the use of the new keywords. Never having to use it is better than sometime in some condition.
* The case class provide an clone function. This is useful when SpinalHDL need to clone one Bundle. For example when you define a new Reg or a new Stream of something.
* Construction parameters are directly visible from outside.

[case] class
^^^^^^^^^^^^

All class's names should start with a upper case letter

.. code-block:: scala

   class Fifo extends Component {

   }

   class Counter extends Area {

   }

   case class Color extends Bundle {

   }

companion object
^^^^^^^^^^^^^^^^

Companion object should start with a upper case letter.

.. code-block:: scala

   object Fifo {
     def apply(that: Stream[Bits]): Stream[Bits] = {...}
   }

   object MajorityVote {
     def apply(that: Bits): UInt = {...}
   }

A exception to this rule is be when the compagnion object is used as a function (only ``apply`` inside), and these ``apply`` functions doesn't generate hardware :

.. code-block:: scala

   object log2{
     def apply(value: Int): Int = {...}
   }

function
^^^^^^^^

Function should always start with a lower case letter :

.. code-block:: scala

   def sinTable = (0 until sampleCount).map(sampleIndex => {
     val sinValue = Math.sin(2 * Math.PI * sampleIndex / sampleCount)
     S((sinValue * ((1 << resolutionWidth) / 2 - 1)).toInt, resolutionWidth bits)
   })

   val rom =  Mem(SInt(resolutionWidth bit),initialContent = sinTable)

instances
^^^^^^^^^

Instances of classes should always start with lower case letter :

.. code-block:: scala

   val fifo   = new Fifo()
   val buffer = Reg(Bits(8 bits))

if / when
^^^^^^^^^

Scala ``if`` and SpinalHDL ``when`` should normally be written by the following way :

.. code-block:: scala

   if(cond){
     ...
   } else if(cond){

   } else {

   }

   when(cond){

   }.elseWhen(cond){

   }.otherwise{

   }

Exceptions could be :


* It's fine to omit the dot before otherwise.
* It's fine to compress a whole ``if``\ /\ ``when`` statements on a single line if it make the code more readable

switch
^^^^^^

SpinalHDL switch should normally be written by the following way :

.. code-block:: scala

   switch(value){
     is(key){

     }
     is(key){

     }
     default{

     }
   }

It's fine to compress a ``is``\ /\ ``default`` statements on a single line if it make the code more readable

Parameters
^^^^^^^^^^

Grouping parameters of a component/bundle inside a case class is in general welcome :


* Easier to carry/manipulate to configure the design
* Better maintainability

.. code-block:: scala

   case class RgbConfig(rWidth: Int, gWidth: Int, bWidth: Int){
     def getWidth = rWidth + gWidth + bWidth
   }

   case class Rgb(c: RgbConfig) extends Bundle {
     val r = UInt(c.rWidth bit)
     val g = UInt(c.gWidth bit)
     val b = UInt(c.bWidth bit)
   }

But this should not be applied in all cases, for example for a Fifo, it doesn't make sense to group the dataType parameter with the depth of the fifo because in general the dataType is something related to the design while the depth is something related to the configuration of the design.

.. code-block:: scala

   class Fifo[T <: Data](dataType: T, depth: Int) extends Component {

   }
