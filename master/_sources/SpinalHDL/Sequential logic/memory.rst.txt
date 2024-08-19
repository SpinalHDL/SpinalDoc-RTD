RAM/ROM Memory
==============

To create a memory in SpinalHDL, the ``Mem`` class should be used.
It allows you to define a memory and add read and write ports to it.

The following table shows how to instantiate a memory:

.. list-table::
   :header-rows: 1
   :widths: 1 1

   * - Syntax
     - Description
   * - ``Mem(type : Data, size : Int)``
     - Create a RAM
   * - ``Mem(type : Data, initialContent : Array[Data])``
     - Create a ROM. If your target is an FPGA, because the memory can be inferred as a block ram, you can still create write ports on it.


.. note::
   If you want to define a ROM, elements of the ``initialContent`` array should only be literal values (no operator, no resize functions). There is an example :ref:`here <sinus_rom>`.

.. note::
   To give a RAM initial values, you can also use the ``init`` function.
   
.. note::
   Write mask width is flexible, and subdivide the memory word in as many slices of equal width as the width of the mask. 
   For instance if you have a 32 bits memory word and provide a 4 bits mask then it will be a byte mask. If you provide a as many mask bits than you have word bits, then it is a bit mask.

.. note::
   Manipulation of ``Mem`` is possible in simulation, see section :ref:`Load and Store of Memory in Simulation <simulation_of_memory>`.

The following table show how to add access ports on a memory :

.. list-table::
   :header-rows: 1
   :widths: 1 30 1

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
     - | Synchronous write with an optional mask.
       | If no enable is specified, it's automatically inferred from the conditional scope where this function is called
     - 
   * - | mem.readAsync(
       |  address
       |  [readUnderWrite]
       | )
     - Asynchronous read with an optional read-under-write policy
     - T
   * - | mem.readSync(
       |  address
       |  [enable]
       |  [readUnderWrite]
       |  [clockCrossing]
       | )
     - Synchronous read with an optional enable, read-under-write policy, and ``clockCrossing`` mode
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
       | Return the read data, the read occurs when ``enable`` is true
     - T


.. note::
   If for some reason you need a specific memory port which is not implemented in Spinal, you can always abstract over your memory by specifying a BlackBox for it.

.. important::
   Memory ports in SpinalHDL are not inferred, but are explicitly defined. You should not use coding templates like in VHDL/Verilog to help the synthesis tool to infer memory.

Here is a example which infers a simple dual port ram (32 bits * 256):

.. code-block:: scala

   val mem = Mem(Bits(32 bits), wordCount = 256)
   mem.write(
     enable  = io.writeValid,
     address = io.writeAddress,
     data    = io.writeData
   )

   io.readData := mem.readSync(
     enable  = io.readValid,
     address = io.readAddress
   )


Synchronous enable quirk
------------------------

When enable signals are used in a block guarded by a conditional block like `when`, only the enable signal will be generated as the access condition: the `when` condition is ignored.

.. code-block:: scala

    val rom = Mem(Bits(10 bits), 32)
    when(cond) {
      io.rdata := rom.readSync(io.addr, io.rdEna)
    }


In the example above the condition `cond` will not be elaborated.
Prefer to include the condition `cond` in the enable signal directly as below.

.. code-block:: scala

    io.rdata := rom.readSync(io.addr, io.rdEna & cond)

Read-under-write policy
-----------------------

This policy specifies how a read is affected when a write occurs in the same cycle to the same address.

.. list-table::
   :header-rows: 1
   :widths: 1 3

   * - Kinds
     - Description
   * - ``dontCare``
     - Don't care about the read value when the case occurs
   * - ``readFirst``
     - The read will get the old value (before the write)
   * - ``writeFirst``
     - The read will get the new value (provided by the write)


.. important::
   The generated VHDL/Verilog is always in the ``readFirst`` mode, which is compatible with ``dontCare`` but not with ``writeFirst``. To generate a design that contains this kind of feature, you need to enable :ref:`automatic memory blackboxing <automatic_memory_blackboxing>`.

Mixed-width ram
---------------

You can specify ports that access the memory with a width that is a power of two fraction of the memory width using these functions:

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Syntax
     - Description
   * - | mem.writeMixedWidth(
       |  address
       |  data
       |  [readUnderWrite]
       | )
     - Similar to ``mem.write``
   * - | mem.readAsyncMixedWidth(
       |  address
       |  data
       |  [readUnderWrite]
       | )
     - Similar to ``mem.readAsync``, but in place of returning the read value, it drives the signal/object given as the ``data`` argument
   * - | mem.readSyncMixedWidth(
       |  address
       |  data
       |  [enable]
       |  [readUnderWrite]
       |  [clockCrossing]
       | )
     - Similar to ``mem.readSync``, but in place of returning the read value, it drives the signal/object given as the ``data`` argument
   * - | mem.readWriteSyncMixedWidth(
       |  address
       |  data
       |  enable
       |  write
       |  [mask]
       |  [readUnderWrite]
       |  [clockCrossing]
       | )
     - Equivalent to ``mem.readWriteSync``


.. important::
   As for read-under-write policy, to use this feature you need to enable :ref:`automatic memory blackboxing <automatic_memory_blackboxing>`, because there is no universal VHDL/Verilog language template to infer mixed-width ram.

.. _automatic_memory_blackboxing:

Automatic blackboxing
---------------------

Because it's impossible to infer all ram kinds by using regular VHDL/Verilog, SpinalHDL integrates an optional automatic blackboxing system. This system looks at all memories present in your RTL netlist and replaces them with blackboxes. Then the generated code will rely on third party IP to provide the memory features, such as the read-during-write policy and mixed-width ports.

Here is an example of how to enable blackboxing of memories by default:

.. code-block:: scala

   def main(args: Array[String]) {
     SpinalConfig()
       .addStandardMemBlackboxing(blackboxAll)
       .generateVhdl(new TopLevel)
   }

If the standard blackboxing tools don't do enough for your design, do not hesitate to create a `Github issue <https://github.com/SpinalHDL/SpinalHDL/issues>`_. There is also a way to create your own blackboxing tool.

Blackboxing policy
^^^^^^^^^^^^^^^^^^

There are multiple policies that you can use to select which memory you want to blackbox and also what to do when the blackboxing is not feasible:

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - Kinds
     - Description
   * - ``blackboxAll``
     - | Blackbox all memory.
       | Throw an error on unblackboxable memory
   * - ``blackboxAllWhatsYouCan``
     - Blackbox all memory that is blackboxable
   * - ``blackboxRequestedAndUninferable``
     - | Blackbox memory specified by the user and memory that is known to be uninferable (mixed-width, ...).
       | Throw an error on unblackboxable memory
   * - ``blackboxOnlyIfRequested``
     - | Blackbox memory specified by the user
       | Throw an error on unblackboxable memory


To explicitly set a memory to be blackboxed, you can use its ``generateAsBlackBox`` function.

.. code-block:: scala

   val mem = Mem(Rgb(rgbConfig), 1 << 16)
   mem.generateAsBlackBox()

You can also define your own blackboxing policy by extending the ``MemBlackboxingPolicy`` class.

Standard memory blackboxes
^^^^^^^^^^^^^^^^^^^^^^^^^^

Shown below are the VHDL definitions of the standard blackboxes used in SpinalHDL:

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

As you can see, blackboxes have a technology parameter. To set it, you can use the ``setTechnology`` function on the corresponding memory.
There are currently 4 kinds of technologies possible:

* ``auto``
* ``ramBlock``
* ``distributedLut``
* ``registerFile``

Blackboxing can insert HDL attributes if ``SpinalConfig#setDevice(Device)``
has been configured for your device-vendor.

The resulting HDL attributes might look like:

.. code-block:: verilog

   (* ram_style = "distributed" *)
   (* ramsyle = "no_rw_check" *)

SpinalHDL tries to support many common memory types provided by well known
vendors and devices, however this is an ever moving landscape and project
requirements can be very specific in this area.

If this is important to your design flow then check the output HDL for the
expected attributes/generic insertion, while consulting your vendor's
platform documentation.

HDL attributes can also be added manually using the `addAttribute()` :ref:`addAttribute <vhdl-and-verilog-attributes>`
mechanism.

