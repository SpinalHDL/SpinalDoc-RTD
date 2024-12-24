.. role:: raw-html-m2r(raw)
   :format: html

Utils
=====

Some utils are also present in :ref:`spinal.core <utils>`

State less utilities
--------------------

.. list-table::
   :header-rows: 1
   :widths: 2 1 5

   * - Syntax
     - Return
     - Description
   * - toGray(x : UInt)
     - Bits
     - Return the gray value converted from ``x`` (UInt)
   * - fromGray(x : Bits)
     - UInt
     - Return the UInt value converted value from ``x`` (gray)
   * - Reverse(x : T)
     - T
     - Flip all bits (lsb + n -> msb - n)
   * - | OHToUInt(x : Seq[Bool])
       | OHToUInt(x : BitVector)
     - UInt
     - Return the index of the single bit set (one hot) in ``x``
   * - | CountOne(x : Seq[Bool])
       | CountOne(x : BitVector)
     - UInt
     - Return the number of bit set in ``x``
   * - | CountLeadingZeroes(x : Bits)
     - UInt
     - Return the number of consecutive zero bits starting from the MSB
   * - | MajorityVote(x : Seq[Bool])
       | MajorityVote(x : BitVector)
     - Bool
     - Return True if the number of bit set is > x.size / 2
   * - EndiannessSwap(that: T[, base:BitCount])
     - T
     - Big-Endian <-> Little-Endian
   * - OHMasking.first(x : Bits)
     - Bits
     - Apply a mask on x to only keep the first bit set
   * - OHMasking.last(x : Bits)
     - Bits
     - Apply a mask on x to only keep the last bit set
   * - | OHMasking.roundRobin(
       |  requests : Bits,
       |  ohPriority : Bits
       | )
     - Bits
     - | Apply a mask on x to only keep the bit set from ``requests``.
       | it start looking in ``requests`` from the ``ohPriority`` position.
       | For example if ``requests`` is "1001" and ``ohPriority`` is "0010", the ``roundRobin`` function will start looking in `requests` from its second bit and will return "1000".
   * - | MuxOH (
       |   oneHot : IndexedSeq[Bool],
       |   inputs : Iterable[T]
       | )
     - T
     - Returns the muxed ``T`` from the ``inputs`` based on the ``oneHot`` vector.
   * - | PriorityMux (
       |    sel: Seq[Bool],
       |    in:  Seq[T]
       | )
     - T
     - Return the first ``in`` element whose ``sel`` is ``True``.
   * - | PriorityMux (
       |    in:  Seq[(Bool, T)]
       | )
     - T
     - Return the first ``in`` element whose ``sel`` is ``True``.


State full utilities
--------------------

.. list-table::
   :header-rows: 1
   :widths: 3 1 5

   * - Syntax
     - Return
     - Description
   * - Delay(that: T, cycleCount: Int)
     - T
     - Return ``that`` delayed by ``cycleCount`` cycles
   * - | History (
       |   that: T, length: Int
       |   *[*\ , when : Bool\ *][*\ , init : T\ *]*
       | )
     - Vec[T]
     - | Return a Vec of ``length`` elements
       | The first element is ``that``\ , the last one is ``that`` delayed by ``length`` - 1
       | The internal shift register sample when ``when`` is asserted
   * - | History (
       |   that: T, range: Range
       |   *[*\ , when : Bool\ *][*\ , init : T\ *]*
       | )
     - Vec[T]
     - | Same as ``History(that, length)`` 
       | but return a Vec of size ``range.length``
       | where the first element is delayed by ``range.low``
       | and the last by ``range.high``
   * - BufferCC(input : T)
     - T
     - Return the input signal synchronized with the current clock domain by using 2 flip flop


Counter
^^^^^^^

The Counter tool can be used to easily instantiate a hardware counter.

.. list-table::
   :header-rows: 1
   :widths: 1 1

   * - Instantiation syntax
     - Notes
   * - ``Counter(start: BigInt, end: BigInt[, inc : Bool])``
     - 
   * - ``Counter(range : Range[, inc : Bool])``
     - Compatible with the  ``x to y`` ``x until y`` syntaxes
   * - ``Counter(stateCount: BigInt[, inc : Bool])``
     - Starts at zero and ends at ``stateCount - 1``
   * - ``Counter(bitCount: BitCount[, inc : Bool])``
     - Starts at zero and ends at ``(1 << bitCount) - 1``

A counter can be controlled by methods, and wires can be read:

.. code-block:: scala

   val counter = Counter(2 to 9) // Creates a counter of 8 states (2 to 9)
   // Methods
   counter.clear()               // Resets the counter
   counter.increment()           // Increments the counter
   // Wires
   counter.value                 // Current value
   counter.valueNext             // Next value
   counter.willOverflow          // True if the counter overflows this cycle
   counter.willOverflowIfInc     // True if the counter would overflow this cycle if an increment was done
   // Cast
   when(counter === 5){ ... }    // counter is implicitly casted to its current value

When a ``Counter`` overflows (reached end value), it restarts the next cycle to its start value.

.. note::
   Currently, only up counter are supported.

``CounterFreeRun`` builds an always running counter: ``CounterFreeRun(stateCount: BigInt)``.

Timeout
^^^^^^^

The Timeout tool can be used to easily instantiate an hardware timeout.

.. list-table::
   :header-rows: 1
   :widths: 1 1

   * - Instantiation syntax
     - Notes
   * - Timeout(cycles : BigInt)
     - Tick after ``cycles`` clocks
   * - Timeout(time : TimeNumber)
     - Tick after a ``time`` duration
   * - Timeout(frequency : HertzNumber)
     - Tick at an ``frequency`` rate


There is an example of different syntaxes which could be used with the Counter tool

.. code-block:: scala

   val timeout = Timeout(10 ms)  // Timeout who tick after 10 ms
   when(timeout) {               // Check if the timeout has tick
       timeout.clear()           // Ask the timeout to clear its flag
   }

.. note::
   If you instantiate an ``Timeout`` with an time or frequency setup, the implicit ``ClockDomain`` should have an frequency setting.

ResetCtrl
^^^^^^^^^

The ResetCtrl provide some utilities to manage resets.

asyncAssertSyncDeassert
~~~~~~~~~~~~~~~~~~~~~~~

You can filter an asynchronous reset by using an asynchronously asserted synchronously deasserted logic. To do it you can use the ``ResetCtrl.asyncAssertSyncDeassert`` function which will return you the filtered value.

.. list-table::
   :header-rows: 1
   :widths: 1 1 4

   * - Argument name
     - Type
     - Description
   * - input
     - Bool
     - Signal that should be filtered
   * - clockDomain
     - ClockDomain
     - ClockDomain which will use the filtered value
   * - inputPolarity
     - Polarity
     - HIGH/LOW (default=HIGH)
   * - outputPolarity
     - Polarity
     - HIGH/LOW (default=clockDomain.config.resetActiveLevel)
   * - bufferDepth
     - Int
     - Number of register stages used to avoid metastability (default=2)


There is also an ``ResetCtrl.asyncAssertSyncDeassertDrive`` version of tool which directly assign the ``clockDomain`` reset with the filtered value.

Special utilities
-----------------

.. list-table::
   :header-rows: 1
   :widths: 3 1 5

   * - Syntax
     - Return
     - Description
   * - LatencyAnalysis(paths : Node*)
     - Int
     - | Return the shortest path, in terms of cycles, that travel through all nodes,
       | from the first one to the last one

