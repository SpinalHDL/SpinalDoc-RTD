
{% include wavedrom.html %}

Introduction
------------

The (Wishbone)[] bus is an open standard for interconnecting IP cores toghether.
The wishbone supports:


* pipelined comunication between IPs
* burst
* optional tags

Wishbone transactions
---------------------

Classic
^^^^^^^


.. raw:: html

   <div style="float:left">
   <script type="WaveDrom">
   {signal: [
     {name:'CLK',         wave: 'p...' },
     {name:'WE',          wave: 'x1.x' },
     {name:'CYC',         wave: '01.0' },
     {name:'STB',         wave: '01.0' },
     {name:'ACK',         wave: '0.10' },
     {name:'ADR',         wave: 'x2.x', data: 'addr'},
     {name:'DAT_MOSI',    wave: 'x2x.', data: 'data'},
     {name:'DAT_MISO',    wave: 'x...' },
   ],
    head:{
      text:'',
      tick:0,
    },
    foot:{
      text:'Classic Write',
      }
   }
   </script>
   </div>



.. raw:: html

   <script type="WaveDrom">
   {signal: [
     {name:'CLK',         wave: 'p...' },
     {name:'WE',          wave: 'x0.x' },
     {name:'CYC',         wave: '01.0' },
     {name:'STB',         wave: '01.0' },
     {name:'ACK',         wave: '0.10' },
     {name:'ADR',         wave: 'x2.x', data: 'addr'},
     {name:'DAT_MOSI',    wave: 'x...' },
     {name:'DAT_MISO',    wave: 'x.2x', data: 'data'},
   ],
    head:{
      text:'',
      tick:0,
    },
    foot:{
      text:'Classic Read',
      }
   }
   </script>


Pipelined
^^^^^^^^^


.. raw:: html

   <div style="float:left">
   <script type="WaveDrom">
   {signal: [
     {name:'CLK',         wave: 'p...' },
     {name:'WE',          wave: 'x1.x' },
     {name:'CYC',         wave: '01.0' },
     {name:'STB',         wave: '010.' },
     {name:'ACK',         wave: '0.10' },
     {name:'ADR',         wave: 'x2.x', data: 'addr' },
     {name:'DAT_MOSI',    wave: 'x2x.', data: 'data' },
     {name:'DAT_MISO',    wave: 'x...'},
   ],
    head:{
      text:'',
      tick:0,
    },
    foot:{
      text:'Pipelined write',
      }
   }
   </script>
   </div>



.. raw:: html

   <script type="WaveDrom">
   {signal: [
     {name:'CLK',         wave: 'p...' },
     {name:'WE',          wave: 'x0.x' },
     {name:'CYC',         wave: '01.0' },
     {name:'STB',         wave: '010.' },
     {name:'ACK',         wave: '0.10' },
     {name:'ADR',         wave: 'x2.x', data: 'addr'},
     {name:'DAT_MOSI',    wave: 'x...'  },
     {name:'DAT_MISO',    wave: 'x.2x', data: 'data'},
   ],
    head:{
      text:'',
      tick:0,
    },
    foot:{
      text:'Pipelined read',
      }
   }
   </script>


Introduction
------------

Configuration and instanciation
-------------------------------

The ``Wishbone`` Bundle has a construction argument ``WishboneConfig``. For more information the Wishbone spec could be find `there <http://cdn.opencores.org/downloads/wbspec_b4.pdf>`_.

.. code-block:: scala

   case class WishboneConfig(
     val addressWidth : Int,
     val dataWidth : Int,
     val selWidth : Int = 0,
     val useSTALL : Boolean = false,
     val useLOCK : Boolean = false,
     val useERR : Boolean = false,
     val useRTY : Boolean = false,
     val useCTI : Boolean = false,
     val tgaWidth : Int = 0,
     val tgcWidth : Int = 0,
     val tgdWidth : Int = 0,
     val useBTE : Boolean = false
   ){
     def useTGA = tgaWidth > 0
     def useTGC = tgcWidth > 0
     def useTGD = tgdWidth > 0
     def useSEL = selWidth > 0

     def isPipelined = useSTALL

     def pipelined : WishboneConfig = this.copy(useSTALL = true)

     def withDataTag(size : Int)    : WishboneConfig = this.copy(tgdWidth = size)
     def withAddressTag(size : Int) : WishboneConfig = this.copy(tgaWidth = size)
     def withCycleTag(size : Int)   : WishboneConfig = this.copy(tgdWidth = size)
     def withCycleTypeIdentifier    : WishboneConfig = this.copy(useCTI = true)
     def withBurstType              : WishboneConfig = this.copy(useCTI = true, useBTE = true)
   }

This configuration object has also some functions to provide some ``WishboneConfig`` templates :

.. list-table::
   :header-rows: 1

   * - Name
     - Return
     - Description
   * - pipelined
     - WishboneConfig
     - Return a wishbone configuration that support pipelined transaction
   * - withDataTag(size : Int)
     - WishboneConfig
     - Return a wishbone configuration with data tag of specidied size
   * - withAddressTag(size : Int)
     - WishboneConfig
     - Return a wishbone configuration with address tag of specidied size
   * - withCycleTag(size : Int)
     - WishboneConfig
     - Return a wishbone configuration with cycle tag of specidied size
   * - withCycleTypeIdentifier
     - WishboneConfig
     - Return a wishbone configuration with type identifier enabled
   * - withBurstType
     - WishboneConfig
     - Return a wishbone configuration with type identifier enabled


You can check the bus configuration with:

.. list-table::
   :header-rows: 1

   * - Name
     - Return
     - Description
   * - useTGA
     - Boolean
     - Return true if the address tag line is used
   * - useTGC
     - Boolean
     - Return true if the cycle tag line is used
   * - useTGD
     - Boolean
     - Return true if the data tag lines are used
   * - useSEL
     - Boolean
     - Return true if the selection line is used
   * - useSTALL
     - Boolean
     - Return true if the stall line is used (same as isPipelined)
   * - useLOCK
     - Boolean
     - Return true if the lock line is used
   * - useERR
     - Boolean
     - Return true if the error line is used
   * - useRTY
     - Boolean
     - Return true if the retry line is used
   * - useCTI
     - Boolean
     - Return true if the Cycle Type Identifie tag line is used
   * - useBTE
     - Boolean
     - Return true if the Burst Type Extension tag line is used
   * - isPipelined
     - Boolean
     - Return true if the bus support pipelined interfacing (same as use STALL)


.. code-block:: scala

   // You can define it in this way
   val myWishboneConfig1 =  WishboneConfig(
                             addressWidth = 8,
                             dataWidth = 16,
                             useSTALL = true
                           )

   // Or you can define it in this way
   val myWishboneConfig2 =  WishboneConfig(8,16).pipelined

   // you can create a wishbone bus in this way
   val wb = Wishbone(myWishboneConfig2)

   // You can check the configuration like this
   wb.config.isPipelined // will return true
   wb.config.dataWidth   //will return 8
