Parametrization
===============

There are multiple aspects to parametrization :

- Providing and the management of, elaboration time parameters provided
  to SpinalHDL during elaboration of the design
- Using the parameter data to allow the designer to perform any kind
  of hardware construction, configuration and interconnection task
  needed in the design.  Such as optional component generation within
  the hardware design.

Parallels exist with the aims of HDL features such as Verilog module
parameters and VHDL generics.  SpinalHDL brings a far richer and more
powerful set of capabilities into this area with the additional
protection of Scala type safety and SpinalHDL built in HDL design rule
checking.

The SpinalHDL mechanisms for parameterization of components is not built
on top of any native HDL mechanism and so is not impeded by HDL language
level/version support or restrictions about what can be achieved in hand
written HDL.

For readers looking to interoperate with parameterized Verilog or
genericized VHDL using SpinalHDL, please see the section on :ref:`BlackBox <BlackBox>`
IP for those scenarios your project requires.



Elaboration time parameters
---------------------------

You can use the whole Scala syntax to provide elaboration time parameters.

The whole syntax means you have the entire power and feature set of the
Scala language at your disposal to solve parameterization requirements for
your project at the level of complexity you choose.

SpinalHDL does not place any opinionated restrictions on how to achieve
your parameterization goals.  As such there are many Scala design patterns
and a few SpinalHDL helpers that can be used to manage parameters that
are suited to different parameter management scenarios.

Here are some examples and ideas of the possibilities:

 * Hardwired code and constants (not strictly parameter management at all
   but serves to highlight the most basic mechanism, a code change, not a
   parameter data change)
 * Constant values provided from a companion object that are static
   constants in Scala.
 * Values provided to Scala class constructor, often a ``case class`` that
   causes Scala to capture those constructor argument values as constants.
 * Regular Scala flow-control syntax, not limited to but including
   conditionals, looping, lambdas/monads, everything.
 * Config class pattern (examples exist in library items such as
   UartCtrlConfig_, SpiMasterCtrlConfig)
 * Project defined 'Plugin' pattern (examples exist in the VexRiscV_ project
   to configure the feature set the resulting CPU IP core is built with)
 * Values and information loaded from a file or network based source, using
   standard Scala/JVM libraries and APIs.
 * `any mechanism you can create`

All of the mechanisms result in a change in resulting elaborated HDL output.

This could vary from a single constant value change all the way through to
describing the entire bus and interconnection architecture of an entire SoC
all without leaving the Scala programming paradigm.


Here is an example of class parameters

.. code-block:: scala

  case class MyBus(width : Int) extends Bundle {
    val mySignal = UInt(width bits)
  }  
  
.. code-block:: scala

  case class MyComponent(width : Int) extends Component {
    val bus = MyBus(width)
  }
  
You can also use global variable defined in Scala objects (companion object
pattern).

A :ref:`ScopeProperty <scopeproperty>` can also be used for configuration.

Optional hardware
-----------------

So here there is more possibilities. 

.. _generate:

For optional signal :

.. code-block:: scala

  case class MyComponent(flag : Boolean) extends Component {
    val mySignal = flag generate (Bool())
    // equivalent to "val mySignal = if (flag) Bool() else null"
  }


The ``generate`` method is a mechanism to evaluate the expression
that follows for an optional value.  If the predicate is true,
generate will evaluate the given expression and return the
result, otherwise it returns null.

This may be used in cases to help parameterize the SpinalHDL hardware
description using an elaboration-time conditional expression.  Causing HDL
constructs to be emitted or not-emitted in the resulting HDL. The generate
method can be seen as SpinalHDL syntactic sugar reducing language clutter.

Project SpinalHDL code referencing ``mySignal`` would need to ensure it
handles the possibility of null gracefully.  This is usually not a problem
as those parts of the design can also be omitted dependant on the ``flag``
value.  Thus the feature of parameterizing this component is demonstrated.


You can do the same in Bundle.
    
Note that you can also use scala Option.

If you want to disable the generation of a chunk of hardware : 

.. code-block:: scala

  case class MyComponent(flag : Boolean) extends Component {
    val myHardware = flag generate new Area {
      // optional hardware here
    }
  }

You can also use scala for loops :

.. code-block:: scala

  case class MyComponent(amount : Int) extends Component {
    val myHardware = for(i <- 0 until amount) yield new Area {
      // hardware here
    }
  }
  
So, you can extends those scala usages at elaboration time as much as you want, including using the whole scala collections (List, Set, Map, ...) 
to build some data model and then converting them into hardware in a procedural way (ex iterating over those list elements).


.. _UartCtrlConfig: https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Examples/Intermediates%20ones/uart.html#controller-construction-parameters
.. _VexRiscV: https://github.com/SpinalHDL/VexRiscv#plugins
