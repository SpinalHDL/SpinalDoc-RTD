.. role:: raw-html-m2r(raw)
   :format: html

.. _memory_mapped_uart:

Memory mapped UART
==================

Introduction
------------

This example will take the ``UartCtrl`` component implemented in the previous :ref:`example <example_uart>` to create a memory mapped UART controller.

Specification
-------------

The implementation will be based on the APB3 bus with a RX FIFO.

Here is the register mapping table:

.. list-table::
   :header-rows: 1
   :widths: 1 1 1 1 5

   * - Name
     - Type
     - Access
     - Address
     - Description
   * - clockDivider
     - UInt
     - RW
     - 0
     - Set the UartCtrl clock divider
   * - frame
     - UartCtrlFrameConfig
     - RW
     - 4
     - Set the dataLength, the parity and the stop bit configuration
   * - writeCmd
     - Bits
     - W
     - 8
     - Send a write command to UartCtrl
   * - writeBusy
     - Bool
     - R
     - 8
     - Bit 0 => zero when a new writeCmd can be sent
   * - read
     - Bool / Bits
     - R
     - 12
     - | Bits 7 downto 0 => rx payload 
       | Bit 31 => rx payload valid


Implementation
--------------

For this implementation, the Apb3SlaveFactory tool will be used. It allows you to define a APB3 slave with a nice syntax. You can find the documentation of this tool :ref:`there <bus_slave_factory>`.

First, we just need to define the ``Apb3Config`` that will be used for the controller. It is defined in a Scala object as a function to be able to get it from everywhere.

.. code-block:: scala

   object Apb3UartCtrl{
     def getApb3Config = Apb3Config(
       addressWidth = 4,
       dataWidth    = 32
     )
   }

Then we can define a ``Apb3UartCtrl`` component which instantiates a ``UartCtrl`` and creates the memory mapping logic between it and the APB3 bus:

.. image:: /asset/picture/memory_mapped_uart.svg
   :align: center

.. code-block:: scala

   class Apb3UartCtrl(uartCtrlConfig : UartCtrlGenerics, rxFifoDepth : Int) extends Component{
     val io = new Bundle{
       val bus =  slave(Apb3(Apb3UartCtrl.getApb3Config))
       val uart = master(Uart())
     }

     // Instanciate an simple uart controller
     val uartCtrl = new UartCtrl(uartCtrlConfig)
     io.uart <> uartCtrl.io.uart

     // Create an instance of the Apb3SlaveFactory that will then be used as a slave factory drived by io.bus
     val busCtrl = Apb3SlaveFactory(io.bus)

     // Ask the busCtrl to create a readable/writable register at the address 0
     // and drive uartCtrl.io.config.clockDivider with this register
     busCtrl.driveAndRead(uartCtrl.io.config.clockDivider,address = 0)

     // Do the same thing than above but for uartCtrl.io.config.frame at the address 4
     busCtrl.driveAndRead(uartCtrl.io.config.frame,address = 4)

     // Ask the busCtrl to create a writable Flow[Bits] (valid/payload) at the address 8.
     // Then convert it into a stream and connect it to the uartCtrl.io.write by using an register stage (>->)
     busCtrl.createAndDriveFlow(Bits(uartCtrlConfig.dataWidthMax bits),address = 8).toStream >-> uartCtrl.io.write

     // To avoid losing writes commands between the Flow to Stream transformation just above,
     // make the occupancy of the uartCtrl.io.write readable at address 8
     busCtrl.read(uartCtrl.io.write.valid,address = 8)

     // Take uartCtrl.io.read, convert it into a Stream, then connect it to the input of a FIFO of 64 elements
     // Then make the output of the FIFO readable at the address 12 by using a non blocking protocol
     // (Bit 7 downto 0 => read data <br> Bit 31 => read data valid )
     busCtrl.readStreamNonBlocking(uartCtrl.io.read.toStream.queue(rxFifoDepth),
                                   address = 12, validBitOffset = 31, payloadBitOffset = 0)
   }

.. important::
   | Yes, that's all it takes. It's also synthesizable. 
   | The Apb3SlaveFactory tool is not something hard-coded into the SpinalHDL compiler. It's something implemented with SpinalHDL regular hardware description syntax.
