.. role:: raw-html-m2r(raw)
   :format: html

.. _stream:

Stream
======

Specification
-------------

| The Stream interface is a simple handshake protocol to carry payload.
| It could be used for example to push and pop elements into a FIFO, send requests to a UART controller, etc.

.. list-table::
   :header-rows: 1
   :widths: 1 1 1 10 1

   * - Signal
     - Type
     - Driver
     - Description
     - Don't care when
   * - valid
     - Bool
     - Master
     - When high => payload present on the interface
     - 
   * - ready
     - Bool
     - Slave
     - When low => transaction are not consumed by the slave
     - valid is low
   * - payload
     - T
     - Master
     - Content of the transaction
     - valid is low

.. wavedrom::

   { "signal": [
     {"name": "clk",     "wave": "p........."},
     {"name": "valid"  , "wave": "0101..01.0"},
     {"name": "ready"  , "wave": "x1x0.1x1.x"},
     {"name": "payload", "wave": "x=x=..x==x","data":["D0","D1","D2","D3"]}
   ]}

There is some examples of usage in SpinalHDL :

.. code-block:: scala

   class StreamFifo[T <: Data](dataType: T, depth: Int) extends Component {
     val io = new Bundle {
       val push = slave Stream (dataType)
       val pop = master Stream (dataType)
     }
     ...
   }

   class StreamArbiter[T <: Data](dataType: T,portCount: Int) extends Component {
     val io = new Bundle {
       val inputs = Vec(slave Stream (dataType),portCount)
       val output = master Stream (dataType)
     }
     ...
   }

.. note::
   Each slave can or can't allow the payload to change when valid is high and ready is low. For examples:


* An priority arbiter without lock logic can switch from one input to the other (which will change the payload).
* An UART controller could directly use the write port to drive UART pins and only consume the transaction at the end of the transmission.
  Be careful with that.

Semantics
---------

When manually reading/driving the signals of a Stream keep in mind that:

* After being asserted, ``valid`` may only be deasserted once the current payload was acknowledged. This means ``valid`` can only toggle to 0 the cycle after a the slave did a read by asserting ``ready``.
* In contrast to that ``ready`` may change at any time. 
* A transfer is only done on cycles where both ``valid`` and ``ready`` are asserted.
* ``valid`` of a Stream must not depend on ``ready`` in a combinatorial way and any path between the two must be registered.
* It is recommended that ``valid`` does not depend on ``ready`` at all.

Functions
---------

.. list-table::
   :header-rows: 1
   :widths: 5 5 1 1

   * - Syntax
     - Description
     - Return
     - Latency
   * - Stream(type : Data)
     - Create a Stream of a given type
     - Stream[T]
     - 
   * - master/slave Stream(type : Data)
     - | Create a Stream of a given type
       | Initialized with corresponding in/out setup
     - Stream[T]
     - 
   * - x.fire
     - Return True when a transaction is consumed on the bus (valid && ready)
     - Bool
     - 
   * - x.isStall
     - Return True when a transaction is stall on the bus (valid && ! ready)
     - Bool
     - 
   * - x.queue(size:Int)
     - Return a Stream connected to x through a FIFO
     - Stream[T]
     - 2
   * - | x.m2sPipe()
       | x.stage()
     - | Return a Stream driven by x
       | through a register stage that cut valid/payload paths
       | Cost = (payload width + 1) flop flop
     - Stream[T]
     - 1
   * - x.s2mPipe()
     - | Return a Stream driven by x
       | ready paths is cut by a register stage
       | Cost = payload width * (mux2 + 1 flip flop)
     - Stream[T]
     - 0
   * - x.halfPipe()
     - | Return a Stream driven by x
       | valid/ready/payload paths are cut by some register
       | Cost = (payload width + 2) flip flop, bandwidth divided by two
     - Stream[T]
     - 1
   * - | x << y
       | y >> x
     - Connect y to x
     - 
     - 0
   * - | x <-< y
       | y >-> x
     - Connect y to x through a m2sPipe
     - 
     - 1
   * - | x </< y
       | y >/> x
     - Connect y to x through a s2mPipe
     - 
     - 0
   * - | x <-/< y
       | y >/-> x
     - | Connect y to x through s2mPipe().m2sPipe()
       | Which imply no combinatorial path between x and y
     - 
     - 1
   * - x.haltWhen(cond : Bool)
     - | Return a Stream connected to x
       | Halted when cond is true
     - Stream[T]
     - 0
   * - x.throwWhen(cond : Bool)
     - | Return a Stream connected to x
       | When cond is true, transaction are dropped
     - Stream[T]
     - 0
   * - x.translateWith(that : T2)
     - | Return a Stream with payload `that`
       | Modify the payload of the `x` stream, while preserving the `valid` and `ready` signals
     - Stream[T2]
     - 0
   * - x.map(translate: (T) => T2)
     - | Return a Stream with payload calculated by translate function
       | Modify the payload of the `x` stream, while preserving the `valid` and `ready` signals
     - Stream[T2]
     - 0


The following code will create this logic :

.. image:: /asset/picture/stream_throw_m2spipe.svg
   :align: center

.. code-block:: scala

   case class RGB(channelWidth : Int) extends Bundle {
     val red   = UInt(channelWidth bits)
     val green = UInt(channelWidth bits)
     val blue  = UInt(channelWidth bits)

     def isBlack : Bool = red === 0 && green === 0 && blue === 0
   }

   val source = Stream(RGB(8))
   val sink   = Stream(RGB(8))
   sink <-< source.throwWhen(source.payload.isBlack)

Utils
-----

There is many utils that you can use in your design in conjunction with the Stream bus, this chapter will document them.

StreamFifo
^^^^^^^^^^

On each stream you can call the .queue(size) to get a buffered stream. But you can also instantiate the FIFO component itself :

.. code-block:: scala

   val streamA,streamB = Stream(Bits(8 bits))
   // ...
   val myFifo = StreamFifo(
     dataType = Bits(8 bits),
     depth    = 128
   )
   myFifo.io.push << streamA
   myFifo.io.pop  >> streamB

.. list-table::
   :header-rows: 1
   :widths: 1 1 2

   * - parameter name
     - Type
     - Description
   * - dataType
     - T
     - Payload data type
   * - depth
     - Int
     - Size of the memory used to store elements


.. list-table::
   :header-rows: 1
   :widths: 1 4 5

   * - io name
     - Type
     - Description
   * - push
     - Stream[T]
     - Used to push elements
   * - pop
     - Stream[T]
     - Used to pop elements
   * - flush
     - Bool
     - Used to remove all elements inside the FIFO
   * - occupancy
     - UInt of log2Up(depth + 1) bits
     - Indicate the internal memory occupancy


StreamFifoCC
^^^^^^^^^^^^

You can instantiate the dual clock domain version of the fifo the following way :

.. code-block:: scala

   val clockA = ClockDomain(???)
   val clockB = ClockDomain(???)
   val streamA,streamB = Stream(Bits(8 bits))
   // ...
   val myFifo = StreamFifoCC(
     dataType  = Bits(8 bits),
     depth     = 128,
     pushClock = clockA,
     popClock  = clockB
   )
   myFifo.io.push << streamA
   myFifo.io.pop  >> streamB

.. list-table::
   :header-rows: 1
   :widths: 1 1 2

   * - parameter name
     - Type
     - Description
   * - dataType
     - T
     - Payload data type
   * - depth
     - Int
     - Size of the memory used to store elements
   * - pushClock
     - ClockDomain
     - Clock domain used by the push side
   * - popClock
     - ClockDomain
     - Clock domain used by the pop side


.. list-table::
   :header-rows: 1
   :widths: 1 4 5

   * - io name
     - Type
     - Description
   * - push
     - Stream[T]
     - Used to push elements
   * - pop
     - Stream[T]
     - Used to pop elements
   * - pushOccupancy
     - UInt of log2Up(depth + 1) bits
     - Indicate the internal memory occupancy (from the push side perspective)
   * - popOccupancy
     - UInt of log2Up(depth + 1) bits
     - Indicate the internal memory occupancy  (from the pop side perspective)


StreamCCByToggle
^^^^^^^^^^^^^^^^

| Component that connects Streams across clock domains based on toggling signals.
| This way of implementing a cross clock domain bridge is characterized by a small area usage but also a low bandwidth.

.. code-block:: scala

   val clockA = ClockDomain(???)
   val clockB = ClockDomain(???)
   val streamA,streamB = Stream(Bits(8 bits))
   // ...
   val bridge = StreamCCByToggle(
     dataType    = Bits(8 bits),
     inputClock  = clockA,
     outputClock = clockB
   )
   bridge.io.input  << streamA
   bridge.io.output >> streamB

.. list-table::
   :header-rows: 1
   :widths: 1 1 2

   * - parameter name
     - Type
     - Description
   * - dataType
     - T
     - Payload data type
   * - inputClock
     - ClockDomain
     - Clock domain used by the push side
   * - outputClock
     - ClockDomain
     - Clock domain used by the pop side


.. list-table::
   :header-rows: 1
   :widths: 1 1 2

   * - io name
     - Type
     - Description
   * - input
     - Stream[T]
     - Used to push elements
   * - output
     - Stream[T]
     - Used to pop elements


Alternatively you can also use a this shorter syntax which directly return you the cross clocked stream:

.. code-block:: scala

   val clockA = ClockDomain(???)
   val clockB = ClockDomain(???)
   val streamA = Stream(Bits(8 bits))
   val streamB = StreamCCByToggle(
     input       = streamA,
     inputClock  = clockA,
     outputClock = clockB
   )

StreamWidthAdapter
^^^^^^^^^^^^^^^^^^

This component adapts the width of the input stream to the output stream.
When the width of the ``outStream`` payload is greater than the ``inStream``, by combining the payloads of several input transactions into one; conversely, if the payload width of the ``outStream`` is less than the ``inStream``, one input transaction will be split into several output transactions.

In the best case, the width of the payload of the ``inStream`` should be an integer multiple of the ``outStream`` as shown below.

.. code-block:: scala

   val inStream = Stream(Bits(8 bits))
   val outStream = Stream(Bits(16 bits))
   val adapter = StreamWidthAdapter(inStream, outStream)

As in the example above, the two ``inStream`` transactions will be merged into one ``outStream`` transaction, and the payload of the first input transaction will be placed on the lower bits of the output payload by default.

If the expected order of input transaction payload placement is different from the default setting, here is an example.

.. code-block:: scala

   val inStream = Stream(Bits(8 bits))
   val outStream = Stream(Bits(16 bits))
   val adapter = StreamWidthAdapter(inStream, outStream, order = SlicesOrder.HIGHER_FIRST)

There is also a traditional parameter called ``endianness``, which has the same effect as ``ORDER``. 
The value of ``endianness`` is the same as ``LOWER_FIRST`` of ``order`` when it is ``LITTLE``, and the same as ``HIGHER_FIRST`` when it is ``BIG``.
The ``padding`` parameter is an optional boolean value to determine whether the adapter accepts non-integer multiples of the input and output payload width.


StreamArbiter
^^^^^^^^^^^^^

When you have multiple Streams and you want to arbitrate them to drive a single one, you can use the StreamArbiterFactory.

.. code-block:: scala

   val streamA, streamB, streamC = Stream(Bits(8 bits))
   val arbitredABC = StreamArbiterFactory.roundRobin.onArgs(streamA, streamB, streamC)

   val streamD, streamE, streamF = Stream(Bits(8 bits))
   val arbitredDEF = StreamArbiterFactory.lowerFirst.noLock.onArgs(streamD, streamE, streamF)

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Arbitration functions
     - Description
   * - lowerFirst
     - Lower port have priority over higher port
   * - roundRobin
     - Fair round robin arbitration
   * - sequentialOrder
     - | Could be used to retrieve transaction in a sequential order
       | First transaction should come from port zero, then from port one, ...


.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Lock functions
     - Description
   * - noLock
     - The port selection could change every cycle, even if the transaction on the selected port is not consumed.
   * - transactionLock
     - The port selection is locked until the transaction on the selected port is consumed.
   * - fragmentLock
     - | Could be used to arbitrate Stream[Flow[T]].
       | In this mode, the port selection is locked until the selected port finish is burst (last=True).


.. list-table::
   :header-rows: 1
   :widths: 2 1

   * - Generation functions
     - Return
   * - on(inputs : Seq[Stream[T]])
     - Stream[T]
   * - onArgs(inputs : Stream[T]*)
     - Stream[T]

StreamJoin
^^^^^^^^^^

This utility takes multiple input streams and waits until all of them fire `valid` before letting all of them through by providing `ready`.

.. code-block:: scala

   val cmdJoin = Stream(Cmd())
   cmdJoin.arbitrationFrom(StreamJoin.arg(cmdABuffer, cmdBBuffer))


StreamFork
^^^^^^^^^^

A StreamFork will clone each incoming data to all its output streams. If synchronous is true,
all output streams will always fire together, which means that the stream will halt until all output streams are ready. 
If synchronous is false, output streams may be ready one at a time,
at the cost of an additional flip flop (1 bit per output). The input stream will block until
all output streams have processed each item regardlessly.


.. code-block:: scala

   val inputStream = Stream(Bits(8 bits))
   val (outputstream1, outputstream2) = StreamFork2(inputStream, synchronous=false)

or

.. code-block:: scala

   val inputStream = Stream(Bits(8 bits))
   val outputStreams = StreamFork(inputStream,portCount=2, synchronous=true)

StreamMux
^^^^^^^^^

A mux implementation for ``Stream``. 
It takes a ``select`` signal and streams in ``inputs``, and returns a ``Stream`` which is connected to one of the input streams specified by ``select``.
``StreamArbiter`` is a facility works similar to this but is more powerful.

.. code-block:: scala

   val inputStreams = Vec(Stream(Bits(8 bits)), portCount)
   val select = UInt(log2Up(inputStreams.length) bits)
   val outputStream = StreamMux(select, inputStreams)

.. note::

   The ``UInt`` type of ``select`` signal could not be changed while output stream is stalled, or it might break the transaction on the fly.
   Use ``Stream`` typed ``select`` can generate a stream interface which only fire and change the routing when it is safe.


StreamDemux
^^^^^^^^^^^

A demux implementation for ``Stream``. 
It takes a ``input``, a ``select`` and a ``portCount`` and returns a ``Vec(Stream)`` where the output stream specified by ``select`` is connected to ``input``, the other output streams are inactive. 
For safe transaction, refer the notes above.

.. code-block:: scala

   val inputStream = Stream(Bits(8 bits))
   val select = UInt(log2Up(portCount) bits)
   val outputStreams = StreamDemux(inputStream, select, portCount)

StreamDispatcherSequencial
^^^^^^^^^^^^^^^^^^^^^^^^^^

This util take its input stream and routes it to ``outputCount`` stream in a sequential order.

.. code-block:: scala

   val inputStream = Stream(Bits(8 bits))
   val dispatchedStreams = StreamDispatcherSequencial(
     input = inputStream,
     outputCount = 3
   )

StreamTransactionExtender
^^^^^^^^^^^^^^^^^^^^^^^^^

This utility will take one input transfer and generate several output transfers, it provides the facility to repeat the payload value ``count+1`` times into output transfers.
The ``count`` is captured and registered each time inputStream fires for an individual payload.

.. code-block:: scala

   val inputStream = Stream(Bits(8 bits))
   val outputStream = Stream(Bits(8 bits))
   val count = UInt(3 bits)
   val extender = StreamTransactionExtender(inputStream, outputStream, count) {
      // id, is the 0-based index of total output transfers so far in the current input transaction.
      // last, is the last transfer indication, same as the last signal for extender.
      // the returned payload is allowed to be modified only based on id and last signals, other translation should be done outside of this.
       (id, payload, last) => payload
   }

This ``extender`` provides several status signals, such as ``working``, ``last``, ``done`` where ``working`` means there is one input transfer accepted and in-progress, ``last`` indicates the last output transfer is prepared and waiting to complete, ``done`` become valid represents the last output transfer is fireing and making the current input transaction process complete and ready to start another transaction.

.. wavedrom::

  { "signal": [
    { "name": "clk",         "wave": "p........." },
    { "name": "inputStream",        "wave": "x3x.....4x", "data": ["T1", "T2"] },
    { "name": "count",        "wave": "x3x.....4x", "data": ["2", "4"] },
    { "name": "outputStream",       "wave": "x..2x2x.2x", "data": ["D1", "D2", "D3"] },
    { "name": "working",      "wave": "0.1......."},
    { "name": "done",      "wave": "0.......10"},
    { "name": "first",      "wave": "0.1.0....."},
    { "name": "last",      "wave": "0.....1..0"},
  ]}

.. note::

   If only count for output stream is required then use ``StreamTransactionCounter`` instead. 

Simulation support
------------------

For simulation master and slave implementations are available:

.. list-table::
  :header-rows: 1
  :widths: 1 5
  
  * - Class
    - Usage
  * - StreamMonitor
    - Used for both master and slave sides, calls function with payload if Stream fires.
  * - StreamDriver
    - Testbench master side, drives values by calling function to apply value (if available). Function must return if value was available. Supports random delays.
  * - StreamReadyRandomizer
    - Randomizes ``ready`` for reception of data, testbench is the slave side.
  * - ScoreboardInOrder
    - Often used to compare reference/dut data

.. code-block:: scala

  import spinal.core._
  import spinal.core.sim._
  import spinal.lib._
  import spinal.lib.sim.{StreamMonitor, StreamDriver, StreamReadyRandomizer, ScoreboardInOrder}

  object Example extends App {
    val dut = SimConfig.withWave.compile(StreamFifo(Bits(8 bits), 2))

    dut.doSim("simple test") { dut =>
      SimTimeout(10000)
      
      val scoreboard = ScoreboardInOrder[Int]()
      
      dut.io.flush #= false
      
      // drive random data and add pushed data to scoreboard
      StreamDriver(dut.io.push, dut.clockDomain) { payload =>
        payload.randomize()
        true
      }
      StreamMonitor(dut.io.push, dut.clockDomain) { payload =>
        scoreboard.pushRef(payload.toInt)
      }

      // randmize ready on the output and add popped data to scoreboard
      StreamReadyRandomizer(dut.io.pop, dut.clockDomain)
      StreamMonitor(dut.io.pop, dut.clockDomain) { payload =>
        scoreboard.pushDut(payload.toInt)
      }

      dut.clockDomain.forkStimulus(10)

      dut.clockDomain.waitActiveEdgeWhere(scoreboard.matches == 100)
    }
  }
