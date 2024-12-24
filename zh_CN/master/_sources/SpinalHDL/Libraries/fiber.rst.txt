.. role:: raw-html-m2r(raw)
   :format: html

.. _fiber:

Fiber framework
===============

.. warning::
   This framework is not expected to be used for general RTL generation and targets large system
   design management and code generation. It is currently used as toplevel integration tool in SaxonSoC.

Currently in development.

The Fiber to run the hardware elaboration in a out of order manner, a bit similarly to Makefile, where you can define rules and dependencies which will then be solved when you run a make command. It is very similar to the Scala Future feature.

Using this framework can complicate simple things but provide some strong features for complex cases :

- You can define things before even knowing all their requirements, ex :
  instantiating a interruption controller, before knowing how many interrupt signal lines you need
- Abstract/lazy/partial SoC architecture definition allowing the creation of SoC template for further specializations
- Automatic requirement negotiation between multiple agents in a decentralized way, ex : between masters and slaves of a memory bus

The framework is mainly composed of : 

- ``Handle[T]``, which can be used later to store a value of type ``T``.
- ``handle.load`` which allow to set the value of a handle (will reschedule all tasks waiting on it)
- ``handle.get``, which return the value of the given handle. Will block the task execution if that handle isn't loaded yet
- ``Handle{ /*code*/ }``, which fork a new task which will execute the given code. The result of that code will be loaded into the Handle
- ``soon(handle)``, which allows the current task to announce that it will load ``handle`` with a value (used for scheduling)


Simple dummy example
--------------------

There is a simple example :

.. code-block:: scala

  import spinal.core.fiber._

  // Create two empty Handles
  val a, b = Handle[Int] 

  // Create a Handle which will be loaded asynchronously by the given body result
  val calculator = Handle {  
      a.get + b.get // .get will block until they are loaded
  }

  // Same as above
  val printer = Handle {
      println(s"a + b = ${calculator.get}") // .get is blocking until the calculator body is done
  }

  // Synchronously load a and b, this will unblock a.get and b.get 
  a.load(3)
  b.load(4)

Its runtime will be : 

- create a and b
- fork the calculator task, but is blocked when executing a.get
- fork the printer task, but is blocked when executing calculator.get
- load a and b, which reschedule the calculator task (as it was waiting on a)
- calculator do its a + b sum, and load its Handle with that result, which reschedule the printer task
- printer task print its stuff
- everything done


So, the main point of that example is to show that we kind of overcome the sequential execution of things, as a and b are loaded after the definition of the calculator.


Handle[T]
---------

Handle[T] are a bit like scala's Future[T], they allow to talk about something before it is even existing, and wait on it.

.. code-block:: scala
    
    val x,y = Handle[Int]
    val xPlus2 : Handle[Int] = x.produce(x.get + 2) // x.produce can be used to generate a new Handle when x is loaded
    val xPlus3 : Handle[Int] = x.derivate(_ + 3)    // x.derivate is as x.produce, but also provide the x.get as argument of the lambda function
    x.load(3) // x will now contain the value 3


soon(handle)
^^^^^^^^^^^^

In order to maintain a proper graph of dependencies between tasks and Handle, a task can specify in advance that it will load a given handle. This is very usefull in case of a generation starvation/deadlock for SpinalHDL to report accuratly where is the issue.




