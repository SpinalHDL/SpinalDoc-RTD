
Apb3
====

The AMBA3-APB bus is commonly used to interface low bandwidth peripherals.

Configuration and instantiation
-------------------------------

First each time you want to create a APB3 bus, you will need a configuration object. This configuration object is an ``Apb3Config`` and has following arguments :

.. list-table::
   :header-rows: 1
   :widths: 1 1 1 2

   * - Parameter name
     - Type
     - Default
     - Description
   * - addressWidth
     - Int
     - 
     - Width of PADDR (byte granularity)
   * - dataWidth
     - Int
     - 
     - Width of PWDATA and PRDATA
   * - selWidth
     - Int
     - 1
     - With of PSEL
   * - useSlaveError
     - Boolean
     - false
     - Specify the presence of PSLVERROR


There is in short how the APB3 bus is defined in the SpinalHDL library :

.. code-block:: scala

   case class Apb3(config: Apb3Config) extends Bundle with IMasterSlave {
     val PADDR      = UInt(config.addressWidth bits)
     val PSEL       = Bits(config.selWidth bits)
     val PENABLE    = Bool()
     val PREADY     = Bool()
     val PWRITE     = Bool()
     val PWDATA     = Bits(config.dataWidth bits)
     val PRDATA     = Bits(config.dataWidth bits)
     val PSLVERROR  = if(config.useSlaveError) Bool() else null
     // ...
   }

There is a short example of usage :

.. code-block:: scala

   val apbConfig = Apb3Config(
     addressWidth = 12,
     dataWidth    = 32
   )
   val apbX = Apb3(apbConfig)
   val apbY = Apb3(apbConfig)

   when(apbY.PENABLE) {
     // ...
   }

Functions and operators
-----------------------

.. list-table::
   :header-rows: 1
   :widths: 1 1 5

   * - Name
     - Return
     - Description
   * - X >> Y
     - 
     - Connect X to Y. Address of Y could be smaller than the one of X
   * - X << Y
     - 
     - Do the reverse of the >> operator

