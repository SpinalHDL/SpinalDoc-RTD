
AvalonMM
========

The AvalonMM bus fit very well in FPGA. It is very flexible :

* Able of the same simplicity than APB
* Better for than AHB in many application that need bandwidth because AvalonMM has a mode that decouple read response from commands (reduce latency read latency impact).
* Less performance than AXI but use much less area (Read and write command use the same handshake channel. The master don't need to store address of pending request to avoid Read/Write hazard)

Configuration and instantiation
-------------------------------

The ``AvalonMM`` Bundle has a construction argument ``AvalonMMConfig``. Because of the flexible nature of the Avalon bus, the ``AvalonMMConfig`` as many configuration elements. For more information the Avalon spec could be find on the intel website.

.. code-block:: scala

   case class AvalonMMConfig( addressWidth : Int,
                              dataWidth : Int,
                              burstCountWidth : Int,
                              useByteEnable : Boolean,
                              useDebugAccess : Boolean,
                              useRead : Boolean,
                              useWrite : Boolean,
                              useResponse : Boolean,
                              useLock : Boolean,
                              useWaitRequestn : Boolean,
                              useReadDataValid : Boolean,
                              useBurstCount : Boolean,
                              // useEndOfPacket : Boolean,

                              addressUnits : AddressUnits = symbols,
                              burstCountUnits : AddressUnits = words,
                              burstOnBurstBoundariesOnly : Boolean = false,
                              constantBurstBehavior : Boolean = false,
                              holdTime : Int = 0,
                              linewrapBursts : Boolean = false,
                              maximumPendingReadTransactions : Int = 1,
                              maximumPendingWriteTransactions : Int = 0, // unlimited
                              readLatency : Int = 0,
                              readWaitTime : Int = 0,
                              setupTime : Int = 0,
                              writeWaitTime : Int = 0
                              )

This configuration class has also some functions :

.. list-table::
   :header-rows: 1
   :widths: 1 1 5

   * - Name
     - Return
     - Description
   * - getReadOnlyConfig
     - AvalonMMConfig
     - Return a similar configuration but with all write feature disabled
   * - getWriteOnlyConfig
     - AvalonMMConfig
     - Return a similar configuration but with all read feature disabled


This configuration companion object has also some functions to provide some ``AvalonMMConfig`` templates :

.. list-table::
   :header-rows: 1
   :widths: 1 1 4

   * - Name
     - Return
     - Description
   * - fixed(addressWidth,dataWidth,readLatency)
     - AvalonMMConfig
     - Return a simple configuration with fixed read timings
   * - pipelined(addressWidth,dataWidth)
     - AvalonMMConfig
     - Return a configuration with variable latency read (readDataValid)
   * - bursted(addressWidth,dataWidth,burstCountWidth)
     - AvalonMMConfig
     - Return a configuration with variable latency read and burst capabilities


.. code-block:: scala

   // Create a write only AvalonMM configuration with burst capabilities and byte enable
   val myAvalonConfig =  AvalonMMConfig.bursted(
                           addressWidth = addressWidth,
                           dataWidth = memDataWidth,
                           burstCountWidth = log2Up(burstSize + 1)
                         ).copy(
                           useByteEnable = true,
                           constantBurstBehavior = true,
                           burstOnBurstBoundariesOnly = true
                         ).getWriteOnlyConfig

   // Create an instance of the AvalonMM bus by using this configuration
   val bus = AvalonMM(myAvalonConfig)
