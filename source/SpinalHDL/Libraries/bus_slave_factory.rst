.. role:: raw-html-m2r(raw)
   :format: html

.. _bus_slave_factory:

Bus Slave Factory
=================

Introduction
------------

In many situation it's needed to implement a bus register bank. The ``BusSlaveFactory`` is a tool that provide an abstract and smooth way to define them.  

To see capabilities of the tool, an simple example use the Apb3SlaveFactory variation to implement an :ref:`memory mapped UART <memory_mapped_uart>`. There is also another example with an :ref:`Timer <timer>` which contain a memory mapping function.

You can find more documentation about the internal implementation of the ``BusSlaveFactory`` tool :ref:`there <bus_slave_factory_implementation>`

Functionality
-------------

| There are many implementations of the ``BusSlaveFactory`` tool : AHB3-lite, APB3, APB4, AvalonMM, AXI-lite 3, AXI4, BMB, Wishbone, Tilelink, BRAM bus and PipelinedMemoryBus.
| Each implementation of that tool take as an argument one instance of the corresponding bus and then offers the following functions to map your hardware into the memory mapping :

.. list-table::
   :header-rows: 1
   :widths: 2 1 10

   * - Name
     - Return
     - Description
   * - busDataWidth
     - Int
     - Return the data width of the bus
   * - read(that,address,bitOffset)
     - 
     - When the bus read the ``address``\ , fill the response with ``that`` at ``bitOffset``
   * - write(that,address,bitOffset)
     - 
     - When the bus write the ``address``\ , assign ``that`` with bus's data from ``bitOffset``
   * - onWrite(address)(doThat)
     - 
     - Call ``doThat`` when a write transaction occur on ``address``
   * - onRead(address)(doThat)
     - 
     - Call ``doThat`` when a read transaction occur on ``address``
   * - nonStopWrite(that,bitOffset)
     - 
     - Permanently assign ``that`` by the bus write data from ``bitOffset``
   * - readAndWrite(that,address,bitOffset)
     - 
     - Make ``that`` readable and writable at ``address`` and placed at ``bitOffset`` in the word
   * - readMultiWord(that,address)
     - 
     - | Create the memory mapping to read ``that`` from 'address'. 
       | If ``that`` is bigger than one word it extends the register on followings addresses
   * - writeMultiWord(that,address)
     - 
     - | Create the memory mapping to write ``that`` at 'address'. 
       | If ``that`` is bigger than one word it extends the register on followings addresses
   * - createWriteOnly(dataType,address,bitOffset)
     - T
     - Create a write only register of type ``dataType`` at ``address`` and placed at ``bitOffset`` in the word
   * - createReadWrite(dataType,address,bitOffset)
     - T
     - Create a read write register of type ``dataType`` at ``address`` and placed at ``bitOffset`` in the word
   * - createAndDriveFlow(dataType,address,bitOffset)
     - Flow[T]
     - Create a writable Flow register of type ``dataType`` at ``address`` and placed at ``bitOffset`` in the word
   * - drive(that,address,bitOffset)
     - 
     - Drive ``that`` with a register writable at ``address`` placed at ``bitOffset`` in the word
   * - driveAndRead(that,address,bitOffset)
     - 
     - Drive ``that`` with a register writable and readable at ``address`` placed at ``bitOffset`` in the word
   * - driveFlow(that,address,bitOffset)
     - 
     - Emit on ``that`` a transaction when a write happen at ``address`` by using data placed at ``bitOffset`` in the word
   * - | readStreamNonBlocking(that,
       |                       address,
       |                       validBitOffset,
       |                       payloadBitOffset)
     - 
     - | Read ``that`` and consume the transaction when a read happen at ``address``. 
       | valid <= validBitOffset bit
       | payload <= payloadBitOffset+widthOf(payload) downto ``payloadBitOffset``
   * - | doBitsAccumulationAndClearOnRead(that,
       |                                  address,
       |                                  bitOffset)
     - 
     - | Instantiate an internal register which at each cycle do :
       | reg := reg | that
       | Then when a read occur, the register is cleared. This register is readable at ``address`` and placed at ``bitOffset`` in the word
   * - setOnSet(that,address,bitOffset)
     - T
     - | Set bits of ``that`` when the corresponding write bit is ``1``.
       | Writing a ``1`` to a bit position sets that bit; writing ``0`` has no effect.
   * - clearOnSet(that,address,bitOffset)
     - T
     - | Clear bits of ``that`` when the corresponding write bit is ``1``.
       | Writing a ``1`` to a bit position clears that bit; writing ``0`` has no effect.
   * - readAndSetOnSet(that,address,bitOffset)
     - T
     - Map ``that`` as readable at ``address`` and apply the ``setOnSet`` behaviour on write
   * - readAndClearOnSet(that,address,bitOffset)
     - T
     - Map ``that`` as readable at ``address`` and apply the ``clearOnSet`` behaviour on write
   * - createReadAndSetOnSet(dataType,address,bitOffset)
     - T
     - Create a register of ``dataType``, make it readable at ``address``, and apply the ``setOnSet`` behaviour on write
   * - createReadAndClearOnSet(dataType,address,bitOffset)
     - T
     - Create a register of ``dataType``, make it readable at ``address``, and apply the ``clearOnSet`` behaviour on write
   * - createReadMultiWord(that,address)
     - T
     - | Create a register initialised from ``that``, and map it as a multi-word read starting at ``address``.
       | Extends across consecutive addresses when ``that`` is wider than one bus word.
   * - createWriteMultiWord(that,address)
     - T
     - | Create a register initialised from ``that``, and map it as a multi-word write starting at ``address``.
       | Extends across consecutive addresses when ``that`` is wider than one bus word.
   * - createWriteAndReadMultiWord(that,address)
     - T
     - | Create a register initialised from ``that``, and map it as both readable and writable across multiple words starting at ``address``.
   * - multiCycleRead(address,cycles)
     - Unit
     - | Insert a read latency of ``cycles`` bus clock cycles for accesses to ``address``.
       | Useful when the read data requires more than one cycle to become available (e.g. synchronous RAM reads).
   * - readSyncMemWordAligned(mem,addressOffset,bitOffset,memOffset)
     - Mem[T]
     - | Memory-map a synchronous-read ``Mem`` at ``addressOffset`` for word-aligned bus access.
       | Each bus word corresponds to one memory word. Automatically inserts a 2-cycle read latency.
   * - readSyncMemMultiWord(mem,addressOffset,memOffset)
     - Mem[T]
     - | Memory-map a synchronous-read ``Mem`` at ``addressOffset`` when each memory element spans multiple bus words.
       | Automatically inserts a 2-cycle read latency.
   * - writeMemWordAligned(mem,addressOffset,bitOffset,memOffset)
     - Mem[T]
     - Memory-map a ``Mem`` for word-aligned bus write access at ``addressOffset``. Supports byte-enable masks when available.
   * - writeMemMultiWord(mem,addressOffset)
     - Mem[T]
     - | Memory-map a ``Mem`` at ``addressOffset`` for write access when each memory element spans multiple bus words.
       | The memory element width must be a multiple of the bus data width.

