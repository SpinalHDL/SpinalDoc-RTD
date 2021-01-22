
UART
====

Introduction
------------

The UART protocol could be used, for instance, to emit an receive RS232 / RS485 frames.

There is an example of an 8 bits frame, with no parity and one stop bit :

.. image:: /asset/picture/uart.png

Bus definition
--------------

.. code-block:: scala

   case class Uart() extends Bundle with IMasterSlave {
     val txd = Bool  // Used to emit frames
     val rxd = Bool  // Used to receive frames

     override def asMaster(): Unit = {
       out(txd)
       in(rxd)
     }
   }

UartCtrl
--------

An Uart controller is implemented in the library. This controller has the specificity to use a sampling window to read the ``rxd`` pin and then to using an majority vote to filter its value.

.. list-table::
   :header-rows: 1
   :widths: 1 1 1 10

   * - IO name
     - direction
     - type
     - Description
   * - config
     - in
     - UartCtrlConfig
     - Used to set the clock divider/parity/stop/data length of the controller
   * - write
     - slave
     - Stream[Bits]
     - Stream port used to request a frame transmission
   * - read
     - master
     - Flow[Bits]
     - Flow port used to receive decoded frames
   * - uart
     - master
     - Uart
     - Interface to the real world


The controller could be instantiated via an ``UartCtrlGenerics`` configuration object :

.. list-table::
   :header-rows: 1
   :widths: 1 1 10

   * - Attribute
     - type
     - Description
   * - dataWidthMax
     - Int
     - Maximal number of bit inside a frame
   * - clockDividerWidth
     - Int
     - Width of the internal clock divider
   * - preSamplingSize
     - Int
     - Specify how many samplingTick are drop at the beginning of a UART baud
   * - samplingSize
     - Int
     - Specify how many samplingTick are used to sample ``rxd`` values in the middle of the UART baud
   * - postSamplingSize
     - Int
     - Specify how many samplingTick are drop at the end of a UART baud

