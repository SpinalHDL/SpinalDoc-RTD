.. _example_apb3:

APB3 definition
===============

Introduction
------------

This example will show the syntax to define an APB3 ``Bundle``.

Specification
-------------

The specification from ARM could be interpreted as follows:

.. list-table::
   :header-rows: 1
   :widths: 1 2 1 3

   * - Signal Name
     - Type
     - Driver side
     - Comment
   * - PADDR
     - UInt(addressWidth bits)
     - Master
     - Address in byte
   * - PSEL
     - Bits(selWidth)
     - Master
     - One bit per slave
   * - PENABLE
     - Bool
     - Master
     - 
   * - PWRITE
     - Bool
     - Master
     - 
   * - PWDATA
     - Bits(dataWidth bits)
     - Master
     - 
   * - PREADY
     - Bool
     - Slave
     - 
   * - PRDATA
     - Bits(dataWidth bits)
     - Slave
     - 
   * - PSLVERROR
     - Bool
     - Slave
     - Optional


Implementation
--------------

This specification shows that the APB3 bus has multiple possible configurations. To represent that, we can define a configuration class in Scala:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/Apb3.scala
   :language: scala
   :start-at: case class Apb3Config(
   :end-before: // end case class Apb3Config

Then we can define the APB3 ``Bundle`` which will be used to represent the bus in hardware:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/Apb3.scala
   :language: scala
   :start-at: case class Apb3(
   :end-before: // end case class Apb3

Usage
-----

Here is a usage example of this definition:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/Apb3.scala
   :language: scala
   :start-after: // start usage example
   :end-before: // end usage example
