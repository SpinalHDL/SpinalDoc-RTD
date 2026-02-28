.. role:: raw-html-m2r(raw)
   :format: html

.. _bus_slave_factory_implementation:

Bus Slave Factory Implementation
================================

Introduction
------------

This page will document the implementation of the BusSlaveFactory tool and one of those variant. You can get more information about the functionality of that tool :ref:`here <bus_slave_factory>`.

Specification
-------------

The class diagram is the following :

.. image:: /asset/picture/bus_slave_factory_classes.svg
   :align: center


The ``BusSlaveFactory`` abstract class define minimum requirements that each implementation of it should provide :

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Name
     - Description
   * - busDataWidth
     - Return the data width of the bus
   * - read(that,address,bitOffset)
     - When the bus read the ``address``\ , fill the response with ``that`` at ``bitOffset``
   * - write(that,address,bitOffset)
     - When the bus write the ``address``\ , assign ``that`` with bus's data from ``bitOffset``
   * - onWrite(address)(doThat)
     - Call ``doThat`` when a write transaction occur on ``address``
   * - onRead(address)(doThat)
     - Call ``doThat`` when a read transaction occur on ``address``
   * - nonStopWrite(that,bitOffset)
     - Permanently assign ``that`` by the bus write data from ``bitOffset``


By using them the ``BusSlaveFactory`` should also be able to provide many utilities :

.. list-table::
   :header-rows: 1
   :widths: 2 1 10


   * - Name
     - Return
     - Description
   * - readAndWrite(that,address,bitOffset)
     - 
     - Make ``that`` readable and writable at ``address`` and placed at ``bitOffset`` in the word
   * - readMultiWord(that,address)
     - 
     - | Create the memory mapping to read ``that`` from 'address'. :
       | If ``that`` is bigger than one word it extends the register on followings addresses
   * - writeMultiWord(that,address)
     - 
     - | Create the memory mapping to write ``that`` at 'address'. :
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
   * - | readStreamNonBlocking(that,address,
       | validBitOffset,payloadBitOffset)
     - 
     - | Read ``that`` and consume the transaction when a read happen at ``address``. 
       | valid <= validBitOffset bit 
       | payload <= payloadBitOffset+widthOf(payload) downto ``payloadBitOffset``
   * - | doBitsAccumulationAndClearOnRead
       | (that,address,bitOffset)
     - 
     - | Instantiate an internal register which at each cycle do :
       | reg := reg | that
       | Then when a read occur, the register is cleared. This register is readable at ``address`` and placed at ``bitOffset`` in the word


About ``BusSlaveFactoryDelayed``, it's still an abstract class, but it capture each primitives (BusSlaveFactoryElement) calls into a data-model. This datamodel is one list that contain all primitives, but also a HashMap that link each address used to a list of primitives that are using it. Then when they all are collected (at the end of the current component), it do a callback that should be implemented by classes that extends it. The implementation of this callback should implement the hardware corresponding to all primitives collected.

Implementation
--------------

BusSlaveFactory
^^^^^^^^^^^^^^^

Let's describe primitives abstract function :

.. code-block:: scala



   trait BusSlaveFactory  extends Area {

     def busDataWidth : Int

     def read(that : Data,
              address : BigInt,
              bitOffset : Int = 0) : Unit

     def write(that : Data,
               address : BigInt,
               bitOffset : Int = 0) : Unit

     def onWrite(address : BigInt)(doThat : => Unit) : Unit
     def onRead (address : BigInt)(doThat : => Unit) : Unit

     def nonStopWrite( that : Data,
                       bitOffset : Int = 0) : Unit

     // ...
   }

Then let's operate the magic to implement all utile based on them :

.. code-block:: scala

   trait BusSlaveFactory  extends Are {
     // ...
     def readAndWrite(that : Data,
                      address: BigInt,
                      bitOffset : Int = 0): Unit = {
       write(that,address,bitOffset)
       read(that,address,bitOffset)
     }

     def drive(that : Data,
               address : BigInt,
               bitOffset : Int = 0) : Unit = {
       val reg = Reg(that)
       write(reg,address,bitOffset)
       that := reg
     }

     def driveAndRead(that : Data,
                      address : BigInt,
                      bitOffset : Int = 0) : Unit = {
       val reg = Reg(that)
       write(reg,address,bitOffset)
       read(reg,address,bitOffset)
       that := reg
     }

     def driveFlow[T <: Data](that : Flow[T],
                              address: BigInt,
                              bitOffset : Int = 0) : Unit = {
       that.valid := False
       onWrite(address) {
         that.valid := True
       }
       nonStopWrite(that.payload,bitOffset)
     }

     def createReadWrite[T <: Data](dataType: T,
                                    address: BigInt,
                                    bitOffset : Int = 0): T = {
       val reg = Reg(dataType)
       write(reg,address,bitOffset)
       read(reg,address,bitOffset)
       reg
     }

     def createAndDriveFlow[T <: Data](dataType : T,
                                    address: BigInt,
                                    bitOffset : Int = 0) : Flow[T] = {
       val flow = Flow(dataType)
       driveFlow(flow,address,bitOffset)
       flow
     }

     def doBitsAccumulationAndClearOnRead(   that : Bits,
                                             address : BigInt,
                                             bitOffset : Int = 0): Unit = {
       assert(that.getWidth <= busDataWidth)
       val reg = Reg(that)
       reg := reg | that
       read(reg,address,bitOffset)
       onRead(address) {
         reg := that
       }
     }

     def readStreamNonBlocking[T <: Data] (that : Stream[T],
                                           address: BigInt,
                                           validBitOffset : Int,
                                           payloadBitOffset : Int) : Unit = {
       that.ready := False
       onRead(address) {
         that.ready := True
       }
       read(that.valid  ,address,validBitOffset)
       read(that.payload,address,payloadBitOffset)
     }

     def readMultiWord(that : Data,
                   address : BigInt) : Unit  = {
       val wordCount = (widthOf(that) - 1) / busDataWidth + 1
       val valueBits = that.asBits.resize(wordCount*busDataWidth)
       val words = (0 until wordCount).map(id => valueBits(id * busDataWidth , busDataWidth bits))
       for (wordId <- (0 until wordCount)) {
         read(words(wordId), address + wordId*busDataWidth/8)
       }
     }

     def writeMultiWord(that : Data,
                    address : BigInt) : Unit  = {
       val wordCount = (widthOf(that) - 1) / busDataWidth + 1
       for (wordId <- (0 until wordCount)) {
         write(
           that = new DataWrapper {
             override def getBitsWidth: Int =
               Math.min(busDataWidth, widthOf(that) - wordId * busDataWidth)

             override def assignFromBits(value : Bits): Unit = {
               that.assignFromBits(
                 bits     = value.resized,
                 offset   = wordId * busDataWidth,
                 bitCount = getBitsWidth bits)
             }
           },address = address + wordId * busDataWidth / 8,0
         )
       }
     }
   }

BusSlaveFactoryDelayed
^^^^^^^^^^^^^^^^^^^^^^

Let's implement classes that will be used to store primitives :

.. code-block:: scala

   trait BusSlaveFactoryElement

   // Ask to make `that` readable when a access is done on `address`.
   // bitOffset specify where `that` is placed on the answer
   case class BusSlaveFactoryRead(that : Data,
                                  address : BigInt,
                                  bitOffset : Int) extends BusSlaveFactoryElement

   // Ask to make `that` writable when a access is done on `address`.
   // bitOffset specify where `that` get bits from the request
   case class BusSlaveFactoryWrite(that : Data,
                                   address : BigInt,
                                   bitOffset : Int) extends BusSlaveFactoryElement

   // Ask to execute `doThat` when a write access is done on `address`
   case class BusSlaveFactoryOnWrite(address : BigInt,
                                     doThat : () => Unit) extends BusSlaveFactoryElement

   // Ask to execute `doThat` when a read access is done on `address`
   case class BusSlaveFactoryOnRead( address : BigInt,
                                     doThat : () => Unit) extends BusSlaveFactoryElement

   // Ask to constantly drive `that` with the data bus
   // bitOffset specify where `that` get bits from the request
   case class BusSlaveFactoryNonStopWrite(that : Data,
                                          bitOffset : Int) extends BusSlaveFactoryElement

Then let's implement the ``BusSlaveFactoryDelayed`` itself :

.. code-block:: scala

   trait BusSlaveFactoryDelayed extends BusSlaveFactory {
     // elements is an array of all BusSlaveFactoryElement requested
     val elements = ArrayBuffer[BusSlaveFactoryElement]()


     // elementsPerAddress is more structured than elements, it group all BusSlaveFactoryElement per requested addresses
     val elementsPerAddress = collection.mutable.HashMap[BigInt,ArrayBuffer[BusSlaveFactoryElement]]()

     private def addAddressableElement(e : BusSlaveFactoryElement,address : BigInt) = {
       elements += e
       elementsPerAddress.getOrElseUpdate(address, ArrayBuffer[BusSlaveFactoryElement]()) += e
     }

     override def read(that : Data,
              address : BigInt,
              bitOffset : Int = 0) : Unit  = {
       assert(bitOffset + that.getBitsWidth <= busDataWidth)
       addAddressableElement(BusSlaveFactoryRead(that,address,bitOffset),address)
     }

     override def write(that : Data,
               address : BigInt,
               bitOffset : Int = 0) : Unit  = {
       assert(bitOffset + that.getBitsWidth <= busDataWidth)
       addAddressableElement(BusSlaveFactoryWrite(that,address,bitOffset),address)
     }

     def onWrite(address : BigInt)(doThat : => Unit) : Unit = {
       addAddressableElement(BusSlaveFactoryOnWrite(address,() => doThat),address)
     }
     def onRead (address : BigInt)(doThat : => Unit) : Unit = {
       addAddressableElement(BusSlaveFactoryOnRead(address,() => doThat),address)
     }

     def nonStopWrite( that : Data,
                       bitOffset : Int = 0) : Unit = {
       assert(bitOffset + that.getBitsWidth <= busDataWidth)
       elements += BusSlaveFactoryNonStopWrite(that,bitOffset)
     }

     // This is the only thing that should be implement by class that extends BusSlaveFactoryDelayed
     def build() : Unit

     component.addPrePopTask(() => build())
   }

AvalonMMSlaveFactory
^^^^^^^^^^^^^^^^^^^^

First let's implement the companion object that provide the compatible AvalonMM configuration object that correspond to the following table :

.. list-table::
   :header-rows: 1
   :widths: 2 3 4

   * - Pin name
     - Type
     - Description
   * - read
     - Bool
     - High one cycle to produce a read request
   * - write
     - Bool
     - High one cycle to produce a write request
   * - address
     - UInt(addressWidth bits)
     - Byte granularity but word aligned
   * - writeData
     - Bits(dataWidth bits)
     - 
   * - readDataValid
     - Bool
     - High to respond a read command
   * - readData
     - Bits(dataWidth bits)
     - Valid when readDataValid is high


.. code-block:: scala

   object AvalonMMSlaveFactory {
     def getAvalonConfig( addressWidth : Int,
                          dataWidth : Int) = {
       AvalonMMConfig.pipelined(   // Create a simple pipelined configuration of the Avalon Bus
         addressWidth = addressWidth,
         dataWidth = dataWidth
       ).copy(                     // Change some parameters of the configuration
         useByteEnable = false,
         useWaitRequestn = false
       )
     }

     def apply(bus : AvalonMM) = new AvalonMMSlaveFactory(bus)
   }

Then, let's implement the AvalonMMSlaveFactory itself.

.. code-block:: scala

   class AvalonMMSlaveFactory(bus : AvalonMM) extends BusSlaveFactoryDelayed {
     assert(bus.c == AvalonMMSlaveFactory.getAvalonConfig(bus.c.addressWidth,bus.c.dataWidth))

     val readAtCmd = Flow(Bits(bus.c.dataWidth bits))
     val readAtRsp = readAtCmd.stage()

     bus.readDataValid := readAtRsp.valid
     bus.readData := readAtRsp.payload

     readAtCmd.valid := bus.read
     readAtCmd.payload := 0

     override def build(): Unit = {
       for(element <- elements) element match {
         case element : BusSlaveFactoryNonStopWrite =>
           element.that.assignFromBits(bus.writeData(element.bitOffset, element.that.getBitsWidth bits))
         case _ =>
       }

       for((address,jobs) <- elementsPerAddress) {
         when(bus.address === address) {
           when(bus.write) {
             for(element <- jobs) element match {
               case element : BusSlaveFactoryWrite => {
                 element.that.assignFromBits(bus.writeData(element.bitOffset, element.that.getBitsWidth bits))
               }
               case element : BusSlaveFactoryOnWrite => element.doThat()
               case _ =>
             }
           }
           when(bus.read) {
             for(element <- jobs) element match {
               case element : BusSlaveFactoryRead => {
                 readAtCmd.payload(element.bitOffset, element.that.getBitsWidth bits) := element.that.asBits
               }
               case element : BusSlaveFactoryOnRead => element.doThat()
               case _ =>
             }
           }
         }
       }
     }

     override def busDataWidth: Int = bus.c.dataWidth
   }

Conclusion
----------

That's all, you can check one example that use this ``Apb3SlaveFactory`` to create an Apb3UartCtrl :ref:`here <memory_mapped_uart>`.

If you want to add the support of a new memory bus, it's very simple you just need to implement another variation of the ``BusSlaveFactoryDelayed`` trait. The ``Apb3SlaveFactory`` is probably a good starting point :D
