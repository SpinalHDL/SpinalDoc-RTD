
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

.. code-block:: scala

   case class Apb3Config(
     addressWidth  : Int,
     dataWidth     : Int,
     selWidth      : Int     = 1,
     useSlaveError : Boolean = true
   )

Then we can define the APB3 ``Bundle`` which will be used to represent the bus in hardware:

.. code-block:: scala

   case class Apb3(config: Apb3Config) extends Bundle with IMasterSlave {
     val PADDR      = UInt(config.addressWidth bit)
     val PSEL       = Bits(config.selWidth bits)
     val PENABLE    = Bool
     val PREADY     = Bool
     val PWRITE     = Bool
     val PWDATA     = Bits(config.dataWidth bit)
     val PRDATA     = Bits(config.dataWidth bit)
     val PSLVERROR  = if(config.useSlaveError) Bool else null

     override def asMaster(): Unit = {
       out(PADDR,PSEL,PENABLE,PWRITE,PWDATA)
       in(PREADY,PRDATA)
       if(config.useSlaveError) in(PSLVERROR)
     }
   }

Usage
-----

Here is a usage example of this definition:

.. code-block:: scala

   val apbConfig = Apb3Config(
     addressWidth  = 16,
     dataWidth     = 32,
     selWidth      = 1,
     useSlaveError = false
   )

   val io = new Bundle{
     val apb = slave(Apb3(apbConfig))
   }

   io.apb.PREADY := True
   when(io.apb.PSEL(0) && io.apb.PENABLE){
     //...
   }
