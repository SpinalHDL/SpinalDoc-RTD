.. role:: raw-html-m2r(raw)
   :format: html

Bus Slave Factory
=================

Introduction
------------

In many situation it's needed to implement a bus register bank. The ``BusSlaveFactory`` is a tool that provide an abstract and smooth way to define them.  

To see capabilities of the tool, an simple example use the Apb3SlaveFactory variation to implement an `memory mapped UART </SpinalDoc/spinal/examples/memory_mapped_uart/>`_. There is also another example with an `Timer </SpinalDoc/spinal/examples/timer/>`_ which contain a memory mapping function.

You can find more documentation about the internal implementation of the ``BusSlaveFactory`` tool `there </SpinalDoc/spinal/lib/bus_slave_factory_impl/>`_

Functionality
-------------

Currently there is three implementation of the ``BusSlaveFactory`` tool : APB3, AXI-lite 3 and Avalon. :raw-html-m2r:`<br>` Each implementation of that tool take as argument one instance of the corresponding bus and then offer following functions to map your hardware into the memory mapping :

.. list-table::
   :header-rows: 1

   * - Name
     - Return
     - Description
   * - busDataWidth
     - Int
     - Return the data width of the bus
   * - read(that,address,bitOffset)
     - -
     - When the bus read the ``address``\ , fill the response with ``that`` at ``bitOffset``
   * - write(that,address,bitOffset)
     - -
     - When the bus write the ``address``\ , assign ``that`` with bus's data from ``bitOffset``
   * - onWrite(address)(doThat)
     - -
     - Call ``doThat`` when a write transaction occur on ``address``
   * - onRead(address)(doThat)
     - -
     - Call ``doThat`` when a read transaction occur on ``address``
   * - nonStopWrite(that,bitOffset)
     - 
     - Permanently assign ``that`` by the bus write data from ``bitOffset``
   * - readAndWrite(that,address,bitOffset)
     - -
     - Make ``that`` readable and writable at ``address`` and placed at ``bitOffset`` in the word
   * - readMultiWord(that,address)
     - -
     - Create the memory mapping to read ``that`` from 'address'.\ :raw-html-m2r:`<br>` If ``that`` is bigger than one word it extends the register on followings addresses
   * - writeMultiWord(that,address)
     - -
     - Create the memory mapping to write ``that`` at 'address'.\ :raw-html-m2r:`<br>` If ``that`` is bigger than one word it extends the register on followings addresses
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
     - -
     - Drive ``that`` with a register writable at ``address`` placed at ``bitOffset`` in the word
   * - driveAndRead(that,address,bitOffset)
     - -
     - Drive ``that`` with a register writable and readable at ``address`` placed at ``bitOffset`` in the word
   * - driveFlow(that,address,bitOffset)
     - -
     - Emit on ``that`` a transaction when a write happen at ``address`` by using data placed at ``bitOffset`` in the word
   * - readStreamNonBlocking(that,address,\ :raw-html-m2r:`<br>`\ validBitOffset,payloadBitOffset)
     - -
     - Read ``that`` and consume the transaction when a read happen at ``address``. :raw-html-m2r:`<br>` valid  &nbsp;  &nbsp; <= validBitOffset bit :raw-html-m2r:`<br>` payload <= payloadBitOffset+widthOf(payload) downto ``payloadBitOffset``
   * - doBitsAccumulationAndClearOnRead\ :raw-html-m2r:`<br>` (that,address,bitOffset)
     - -
     - Instanciate an internal register which at each cycle do :\ :raw-html-m2r:`<br>` reg := reg | that :raw-html-m2r:`<br>` Then when a read occur, the register is cleared. This register is readable at ``address`` and placed at ``bitOffset`` in the word

