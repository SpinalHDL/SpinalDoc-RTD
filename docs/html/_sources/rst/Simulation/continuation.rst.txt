
Scala continuation (cps)
========================

Introduction
------------

To provide an high speed multi threading environnement, SpinalSim use the scala-continuation feature. This feature while very powerfull come with some drawbacks.

You realy have to read this page if you want to do advanced things.

@suspendable annotation
-----------------------

Each function that can block the execution via an sleep/join/waitUntil statement should have its return type annotated with the @suspendable annotation.

For example :

.. code-block:: scala

   def doStuff(value : Int) : Unit@suspendable = {
     dut.io.a #= value
     sleep(10)
     dut.io.a #= value + 1
   }

It's the reason why you can'block the execution inside scala for loops and foreach/map funcitonal things.

Alternative foreach, map statements
-----------------------------------

As described in with the @suspendable annotation chapter, you can't use native foreach/map function on lists. Instead you will have to do the following :

.. code-block:: scala

   val myList = List(1,2,3)
   myList.suspendable.foreach{i =>
     sleep(i)
   }

Weird type missmatch
--------------------

.. code-block:: scala

   //Compilation error
   if(something){
     sleep(1)
   }

   //Compilation OK
   val dummy = if(something){
     sleep(1)
   }

See https://github.com/scala/scala-continuations/issues/36 for more info. (Unit@cpsParam[Unit,Unit] is equivalent to @suspendable)
