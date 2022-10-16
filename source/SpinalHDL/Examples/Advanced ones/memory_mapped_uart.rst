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

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/MemoryMappedUart.scala
   :language: scala
   :start-at: object Apb3UartCtrl
   :end-before: // end object Apb3UartCtrl

Then we can define a ``Apb3UartCtrl`` component which instantiates a ``UartCtrl`` and creates the memory mapping logic between it and the APB3 bus:

.. image:: /asset/picture/memory_mapped_uart.svg
   :align: center

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/MemoryMappedUart.scala
   :language: scala
   :start-at: case class Apb3UartCtrl(
   :end-before: // end case class Apb3UartCtrl

.. important::
   | Yes, that's all it takes. It's also synthesizable. 
   | The Apb3SlaveFactory tool is not something hard-coded into the SpinalHDL compiler. It's something implemented with SpinalHDL regular hardware description syntax.
