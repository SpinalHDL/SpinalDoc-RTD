.. role:: raw-html-m2r(raw)
   :format: html

Parametrization
==================

Introduction
------------

There is multiple aspect to parametrization : 

- Providing elaboration time parameters to the design
- Optionaly generate some hardware

Elaboration time parameters
------------------------------------------

You can use the whole scala syntax to provide elaboration time parameters. 

Here is an example of class parameters

.. code-block:: scala

  csae class MyBus(width : Int) extends Bundle{
    val mySignal = UInt(width bits)
  }  
  
.. code-block:: scala

  case class MyComponent(width : Int) extends Component{
    val bus = MyBus(width)
  }
  
You can also use global variable defined in scala object's, but note that recently was added the ScopeProperty feature which improve on that solution.

Optional hardware
------------------------------------------

So here there is more possibilities. 

For optional signal :

.. code-block:: scala

  case class MyComponent(flag : Boolean) extends Component{
    val mySignal = flag generate (Bool())  //Equivalent to "if (flag) in Bool() else null"
  }

You can do the same in Bundle.
    
Note that you can also use scala Option.

If you want to disable the generation of a chunk of hardware : 

.. code-block:: scala

  case class MyComponent(flag : Boolean) extends Component{
    val myHardware = flag generate new Area{
      //optional hardware here
    }
  }

You can also use scala for loops :

.. code-block:: scala

  case class MyComponent(amount : Int) extends Component{
    val myHardware = for(i <- 0 until amount) yield new Area{
      // hardware here
    }
  }
  
So, you can extends those scala usages at elaboration time as much as you want, including using the whole scala collections (List, Set, Map, ...) 
to build some data model and then converting them into hardware in a procedural way (ex iterating over those list elements).




