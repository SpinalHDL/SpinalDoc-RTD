Axi4
====

The AXI4 is a high bandwidth bus defined by ARM.

Configuration and instantiation
-------------------------------

First each time you want to create a AXI4 bus, you will need a configuration object. This configuration object is an ``Axi4Config`` and has following arguments :

Note : useXXX specify if the bus has XXX signal present.

.. list-table::
   :header-rows: 1

   * - Parameter name
     - Type
     - Default
   * - addressWidth
     - Int
     - 
   * - dataWidth
     - Int
     - 
   * - idWidth
     - Int
     - 
   * - userWidth
     - Int
     - 
   * - useId
     - Boolean
     - true
   * - useRegion
     - Boolean
     - true
   * - useBurst
     - Boolean
     - true
   * - useLock
     - Boolean
     - true
   * - useCache
     - Boolean
     - true
   * - useSize
     - Boolean
     - true
   * - useQos
     - Boolean
     - true
   * - useLen
     - Boolean
     - true
   * - useLast
     - Boolean
     - true
   * - useResp
     - Boolean
     - true
   * - useProt
     - Boolean
     - true
   * - useStrb
     - Boolean
     - true
   * - useUser
     - Boolean
     - false


There is in short how the AXI4 bus is defined in the SpinalHDL library :

.. code-block:: scala

   case class Axi4(config: Axi4Config) extends Bundle with IMasterSlave {
     val aw = Stream(Axi4Aw(config))
     val w  = Stream(Axi4W(config))
     val b  = Stream(Axi4B(config))
     val ar = Stream(Axi4Ar(config))
     val r  = Stream(Axi4R(config))

     override def asMaster(): Unit = {
       master(ar,aw,w)
       slave(r,b)
     }
   }

There is a short example of usage :

.. code-block:: scala

   val axiConfig = Axi4Config(
     addressWidth = 32,
     dataWidth    = 32,
     idWidth      = 4
   )
   val axiX = Axi4(axiConfig)
   val axiY = Axi4(axiConfig)

   when(axiY.aw.valid) {
     // ...
   }

Variations
----------

There is 3 other variation of the Axi4 bus :

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Type
     - Description
   * - Axi4ReadOnly
     - Only AR and R channels are present
   * - Axi4WriteOnly
     - Only AW, W and B channels are present
   * - Axi4Shared
     - | This variation is a library initiative.
       | It use 4 channels, W, B ,R and also a new one which is named AWR. 
       | The AWR channel can be used to transmit AR and AW transactions. To dissociate them, a signal ``write`` is present. 
       | The advantage of this Axi4Shared variation is to use less area, especially in the interconnect.


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
     - Connect X to Y. Able infer default values as specified in the AXI4 specification, and also to adapt some width in a safe manner.
   * - X << Y
     - 
     - Do the reverse of the >> operator
   * - X.toWriteOnly
     - Axi4WriteOnly
     - Return an Axi4WriteOnly bus drive by X
   * - X.toReadOnly
     - Axi4ReadOnly
     - Return an Axi4ReadOnly bus drive by X

