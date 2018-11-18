.. role:: raw-html-m2r(raw)
   :format: html

RAM/ROM
=======

Syntax
------

To specify memory of your RTL you can use the Mem tool of Spinal. It allow you to define a memory, and than add ports on it.

The following table show how to instantiate a memory :

.. list-table::
   :header-rows: 1

   * - Syntax
     - Description
   * - Mem(type : Data,size : Int)
     - Create a RAM
   * - Mem(type : Data,initialContent : Array[Data])
     - Create a ROM. If your target is an FPGA, because it can be inferred as a block ram, you can still create write ports on it.


.. note::
   If you want to define a ROM, elements of the ``initialContent`` array should only be literal value (no operator, no resize functions). There is an example :ref:`here <sinus_rom>`.

.. note::
   To give an initial content to a RAM, you can also use the ``init`` function.

The following table show how to add access ports on a memory :

.. list-table::
   :header-rows: 1

   * - Syntax
     - Description
     - Return
   * - mem(address) := data
     - Synchronous write
     - 
   * - mem(x)
     - Asynchronous read
     - T
   * - | mem.write(
       |  address
       |  data
       |  [enable]
       |  [mask]
       | )
     - Synchronous write with an optional mask.
       If no enable is specified, it's automatically inferred from the conditional scope where this function is called
     - 
   * - | mem.readAsync(
       |  address
       |  [readUnderWrite]
       | )
     - Asynchronous read with an optional read under write policy
     - T
   * - | mem.readSync(
       |  address
       |  [enable]
       |  [readUnderWrite]
       |  [clockCrossing]
       | )
     - Synchronous read with an optional enable, read under write policy and clockCrossing mode
     - T
   * - | mem.readWriteSync(
       |  address
       |  data
       |  enable
       |  write
       |  [mask]
       |  [readUnderWrite]
       |  [clockCrossing]
       | )
     - | Infer a read/write port.
       | ``data`` is written when ``enable && write``.
       | Return the read data, the read occur when ``enable``
     - T


.. note::
   If for some reason you need a specific memory port which is not implemented in Spinal, you can always abstract your memory by specifying a BlackBox for it.

.. warning::
   Memories ports in SpinalHDL are not inferred but explicitly defined. You should not use coding templates like in VHDL/Verilog to help the synthesis tool to infer memory.

There is a example which infer an simple dual port ram (32 bits * 256):

.. code-block:: scala

   val mem = Mem(Bits(32 bits),wordCount = 256)
   mem.write(
     enable  = io.writeValid,
     address = io.writeAddress,
     data    = io.writeData
   )

   io.readData := mem.readSync(
     enable  = io.readValid,
     address = io.readAddress
   )

Read under write policy
-----------------------

This policy specify how a read is affected when a write occur in the same cycle on the same address.

.. list-table::
   :header-rows: 1

   * - Kinds
     - Description
   * - ``dontCare``
     - Don't care about the read value when the case occur
   * - ``readFirst``
     - The read will get the old value (before the write)
   * - ``writeFirst``
     - The read will get the new value (provided by the write)


.. warning::
   The generated VHDL/Verilog is always in the 'readFirst' mode, which is compatible with 'dontCare' but not with 'writeFirst'. To generate a design that contains this kind of feature, you need to enable the automatic memory blackboxing.

Mixed width ram
---------------

You can specify ports that interface the memory with a data width of a power of two fraction of the memory one.

.. list-table::
   :header-rows: 1

   * - Syntax
     - Description
   * - | mem.writeMixedWidth(
       |  address
       |  data
       |  [readUnderWrite]
       | )
     - Similar to mem.write
   * - | mem.readAsyncMixedWidth(
       |  address
       |  data
       |  [readUnderWrite]
       | )
     - Similar to mem.readAsync, but in place to return the read value, it drive the data structure given as argument
   * - | mem.readSyncMixedWidth(
       |  address
       |  data
       |  [enable]
       |  [readUnderWrite]
       |  [clockCrossing]
       | )
     - Similar to mem.readSync, but in place to return the read value, it drive the data structure given as argument
   * - | mem.readWriteSyncMixedWidth(
       |  address
       |  data
       |  enable
       |  write
       |  [mask]
       |  [readUnderWrite]
       |  [clockCrossing]
       | )
     - Equivalent to mem.readWriteSync


.. warning::
   As for Read under write policy, to use this feature you need to enable the automatic memory blackboxing, because there is no universal VHDL/Verilog language template to infer mixed width ram.

Automatic blackboxing
---------------------

Because it's impossible to infer all ram kinds by using regular VHDL/Verilog, SpinalHDL integrate an optional automatic blackboxing system. This system look all Mem present in your RTL netlist and replace them by using BlackBox. Then the generated code will rely third party IP to provide memories features like read during write policy and mixed width ports.

There is an example to enable the default automatic blackboxing.

.. code-block:: scala

   def main(args: Array[String]) {
     SpinalConfig()
       .addStandardMemBlackboxing(blackboxAll)
       .generateVhdl(new TopLevel)
   }

If the standard blackboxing tools doesn't do enough for your design, do not hesitate to do a git issue. There is also a way to define your own blackboxing tool.

Blackboxing policy
^^^^^^^^^^^^^^^^^^

There is multiple policy that you can use to select which memory you want to blackbox and also what to do when the blackboxing is not feasable :

.. list-table::
   :header-rows: 1

   * - Kinds
     - Description
   * - blackboxAll
     - | Blackbox all memory.
       | Throw an error on unblackboxable memory
   * - blackboxAllWhatsYouCan
     - Blackbox all memory which are blackboxable
   * - blackboxRequestedAndUninferable
     - | Blackbox memory specified by the user and memory which are known to be uninferable (mixed width, ...).
       | Throw an error on unblackboxable memory
   * - blackboxOnlyIfRequested
     - | Blackbox memory specified by the user
       | Throw an error on unblackboxable memory


To explicitly set a memory to be blackboxed, you can its ``generateAsBlackBox`` function.

.. code-block:: scala

   val mem = Mem(Rgb(rgbConfig),1 << 16)
   mem.generateAsBlackBox()

You can also define your own blackboxing policy by extending the MemBlackboxingPolicy class.

Standard memory blackboxes
^^^^^^^^^^^^^^^^^^^^^^^^^^

There are the VHDL definition of used blackboxes :

.. code-block:: ada

   -- Simple asynchronous dual port (1 write port, 1 read port)
   component Ram_1w_1ra is
     generic(
       wordCount : integer;
       wordWidth : integer;
       technology : string;
       readUnderWrite : string;
       wrAddressWidth : integer;
       wrDataWidth : integer;
       wrMaskWidth : integer;
       wrMaskEnable : boolean;
       rdAddressWidth : integer;
       rdDataWidth : integer
     );
     port(
       clk : in std_logic;
       wr_en : in std_logic;
       wr_mask : in std_logic_vector;
       wr_addr : in unsigned;
       wr_data : in std_logic_vector;
       rd_addr : in unsigned;
       rd_data : out std_logic_vector
     );
   end component;

   -- Simple synchronous dual port (1 write port, 1 read port)
   component Ram_1w_1rs is
     generic(
       wordCount : integer;
       wordWidth : integer;
       clockCrossing : boolean;
       technology : string;
       readUnderWrite : string;
       wrAddressWidth : integer;
       wrDataWidth : integer;
       wrMaskWidth : integer;
       wrMaskEnable : boolean;
       rdAddressWidth : integer;
       rdDataWidth : integer;
       rdEnEnable : boolean
     );
     port(
       wr_clk : in std_logic;
       wr_en : in std_logic;
       wr_mask : in std_logic_vector;
       wr_addr : in unsigned;
       wr_data : in std_logic_vector;
       rd_clk : in std_logic;
       rd_en : in std_logic;
       rd_addr : in unsigned;
       rd_data : out std_logic_vector
     );
   end component;

   -- Single port (1 readWrite port)
   component Ram_1wrs is
     generic(
       wordCount : integer;
       wordWidth : integer;
       readUnderWrite : string;
       technology : string
     );
     port(
       clk : in std_logic;
       en : in std_logic;
       wr : in std_logic;
       addr : in unsigned;
       wrData : in std_logic_vector;
       rdData : out std_logic_vector
     );
   end component;

   --True dual port (2 readWrite port)
   component Ram_2wrs is
     generic(
       wordCount : integer;
       wordWidth : integer;
       clockCrossing : boolean;
       technology : string;
       portA_readUnderWrite : string;
       portA_addressWidth : integer;
       portA_dataWidth : integer;
       portA_maskWidth : integer;
       portA_maskEnable : boolean;
       portB_readUnderWrite : string;
       portB_addressWidth : integer;
       portB_dataWidth : integer;
       portB_maskWidth : integer;
       portB_maskEnable : boolean
     );
     port(
       portA_clk : in std_logic;
       portA_en : in std_logic;
       portA_wr : in std_logic;
       portA_mask : in std_logic_vector;
       portA_addr : in unsigned;
       portA_wrData : in std_logic_vector;
       portA_rdData : out std_logic_vector;
       portB_clk : in std_logic;
       portB_en : in std_logic;
       portB_wr : in std_logic;
       portB_mask : in std_logic_vector;
       portB_addr : in unsigned;
       portB_wrData : in std_logic_vector;
       portB_rdData : out std_logic_vector
     );
   end component;

As you can see, blackboxes have a technology parameter. To set it you can use the setTechnology function on the corresponding memory.
There is currently 4 kinds of technogy possible :


* auto
* ramBlock
* distributedLut
* registerFile
