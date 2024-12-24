.. _Int:

UInt/SInt
=========

The ``UInt``/``SInt`` types are vectors of bits interpreted as two's complement unsigned/signed integers.
They can do what ``Bits`` can do, with the addition of unsigned/signed integer arithmetic and comparisons.

Declaration
-----------

The syntax to declare an integer is as follows:  (everything between [] is optional)

.. list-table::
   :header-rows: 1
   :widths: 5 10

   * - Syntax
     - Description
   * - | UInt[()]
       | SInt[()]
     - Create an unsigned/signed integer, bits count is inferred
   * - | UInt(x bits)
       | SInt(x bits)
     - Create an unsigned/signed integer with x bits
   * - | U(value: Int[,x bits])
       | U(value: BigInt[,x bits])
       | S(value: Int[,x bits])
       | S(value: BigInt[,x bits])
     - Create an unsigned/signed integer assigned with 'value'
   * - | U"[[size']base]value"
       | S"[[size']base]value"
     - | Create an unsigned/signed integer assigned with 'value'
       | (base: 'h', 'd', 'o', 'b')
   * - | U([x bits,] elements: Element*)
       | S([x bits,] elements: Element*)
     - Create an unsigned integer assigned with the value specified by :ref:`elements <element>`

.. code-block:: scala

   val myUInt = UInt(8 bit)
   myUInt := U(2, 8 bit)
   myUInt := U(2)
   myUInt := U"0000_0101"  // Base per default is binary => 5
   myUInt := U"h1A"        // Base could be x (base 16)
                           //               h (base 16)
                           //               d (base 10)
                           //               o (base 8)
                           //               b (base 2)                       
   myUInt := U"8'h1A"       
   myUInt := 2             // You can use a Scala Int as a literal value

   val myBool = Bool()
   myBool := myUInt === U(7 -> true, (6 downto 0) -> false)
   myBool := myUInt === U(8 bit, 7 -> true, default -> false)
   myBool := myUInt === U(myUInt.range -> true)

   // For assignment purposes, you can omit the U/S
   // which also allows the use of "default -> ???"
   myUInt := (default -> true)                        // Assign myUInt with "11111111"
   myUInt := (myUInt.range -> true)                   // Assign myUInt with "11111111"
   myUInt := (7 -> true, default -> false)            // Assign myUInt with "10000000"
   myUInt := ((4 downto 1) -> true, default -> false) // Assign myUInt with "00011110"

Operators
---------

The following operators are available for the ``UInt`` and ``SInt`` types:

Logic
^^^^^

.. list-table::
   :header-rows: 1
   :widths: 2 4 2

   * - Operator
     - Description
     - Return type
   * - ~x
     - Bitwise NOT
     - T(w(x) bits)
   * - x & y
     - Bitwise AND
     - T(max(w(x), w(y)) bits)
   * - x | y
     - Bitwise OR
     - T(max(w(x), w(y)) bits)
   * - x ^ y
     - Bitwise XOR
     - T(max(w(x), w(y)) bits)
   * - x.xorR
     - XOR all bits of x (reduction operator)
     - Bool
   * - x.orR
     - OR all bits of x (reduction operator)
     - Bool
   * - x.andR
     - AND all bits of x (reduction operator)
     - Bool
   * - x \>\> y
     - Arithmetic shift right, y : Int
     - T(w(x) - y bits)
   * - x \>\> y
     - Arithmetic shift right, y : UInt
     - T(w(x) bits)
   * - x \<\< y
     - Arithmetic shift left, y : Int
     - T(w(x) + y bits)
   * - x \<\< y
     - Arithmetic shift left, y : UInt
     - T(w(x) + max(y) bits)
   * - x \|\>\> y
     - Logical shift right, y : Int/UInt
     - T(w(x) bits)
   * - x \|\<\< y
     - Logical shift left, y : Int/UInt
     - T(w(x) bits)
   * - x.rotateLeft(y)
     - | Logical left rotation, y : UInt/Int
       | The width of y is constrained to the width of log2Up(x) or less
     - T(w(x) bits)
   * - x.rotateRight(y)
     - | Logical right rotation, y : UInt/Int
       | The width of y is constrained to the width of log2Up(x) or less
     - T(w(x) bits)
   * - x.clearAll[()]
     - Clear all bits
     - *modifies x*
   * - x.setAll[()]
     - Set all bits
     - *modifies x*
   * - x.setAllTo(value : Boolean)
     - Set all bits to the given Boolean value
     - *modifies x*
   * - x.setAllTo(value : Bool)
     - Set all bits to the given Bool value
     - *modifies x*

.. note::

   Notice the difference in behavior between ``x >> 2`` (result 2 bit narrower than x) and ``x >> U(2)`` (keeping width)
   due to the Scala type of :code:`y`.

   In the first case "2" is an ``Int`` (which can be seen as an
   "elaboration integer constant"), and in the second case it is a hardware signal
   (type ``UInt``) that may or may not be a constant.

.. code-block:: scala

   val a, b, c = SInt(32 bits)
   a := S(5)
   b := S(10)

   // Bitwise operators
   c := ~(a & b)     // Inverse(a AND b)
   assert(c.getWidth == 32)

   // Shift
   val arithShift = UInt(8 bits) << 2      // shift left (resulting in 10 bits)
   val logicShift = UInt(8 bits) |<< 2     // shift left (resulting in 8 bits)
   assert(arithShift.getWidth == 10)
   assert(logicShift.getWidth == 8)

   // Rotation
   val rotated = UInt(8 bits) rotateLeft 3 // left bit rotation
   assert(rotated.getWidth == 8)

   // Set all bits of b to True when all bits of a are True
   when(a.andR) { b.setAll() }

Arithmetic
^^^^^^^^^^

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x + y
     - Addition
     - T(max(w(x), w(y)) bits)
   * - x +^ y
     - Addition with carry
     - T(max(w(x), w(y)) + 1 bits)
   * - x +| y
     - Addition of addend with `saturation`_ (see also `T.maxValue` and `T.minValue`)
     - T(max(w(x), w(y)) bits)
   * - x - y
     - Subtraction
     - T(max(w(x), w(y)) bits)
   * - x -^ y
     - Subtraction with carry
     - T(max(w(x), w(y)) + 1 bits)
   * - x -| y
     - Subtraction of subtrahend with `saturation`_ (see also `T.minValue` and `T.maxValue`)
     - T(max(w(x), w(y)) bits)
   * - x * y
     - Multiplication
     - T(w(x) + w(y)) bits)
   * - x / y
     - Division
     - T(w(x) bits)
   * - x % y
     - Modulo
     - T(min(w(x), w(y)) bits)
   * - ~x
     - Unary One's compliment, Bitwise NOT
     - T(w(x) bits)
   * - -x
     - Unary Two's compliment of SInt type.  Not available for UInt.
     - SInt(w(x) bits)

.. code-block:: scala

   val a, b, c = UInt(8 bits)
   a := U"xf0"
   b := U"x0f"

   c := a + b
   assert(c === U"8'xff")

   val d = a +^ b
   assert(d === U"9'x0ff")

   // 0xf0 + 0x20 would overflow, the result therefore saturates
   val e = a +| U"8'x20"
   assert(e === U"8'xff")

.. note::

   Notice how simulation assertions are made here (with ``===``), as opposed to elaboration
   assertions in the previous example (with ``==``).

Comparison
^^^^^^^^^^

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return type
   * - x === y
     - Equality
     - Bool
   * - x =/= y
     - Inequality
     - Bool
   * - x > y
     - Greater than
     - Bool
   * - x >= y
     - Greater than or equal
     - Bool
   * - x < y
     - Less than
     - Bool
   * - x <= y
     - Less than or equal
     - Bool

.. code-block:: scala

   val a = U(5, 8 bits)
   val b = U(10, 8 bits)
   val c = UInt(2 bits)

   when (a > b) {
     c := U"10"
   } elsewhen (a =/= b) {
     c := U"01"
   } elsewhen (a === U(0)) {
     c.setAll()
   } otherwise {
     c.clearAll()
   }

.. note::

   When comparing ``UInt`` values in a way that allows for "wraparound" behavior, meaning that the values will "wrap around" to the minimum value when they exceed the maximum value.
   The ``wrap`` method of ``UInt`` can be used as ``x.wrap < y`` for ``UInt`` variables ``x, y``, the result will be true if ``x`` is less than ``y`` in the wraparound sense.

Type cast
^^^^^^^^^

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x.asBits
     - Binary cast to Bits
     - Bits(w(x) bits)
   * - x.asUInt
     - Binary cast to UInt
     - UInt(w(x) bits)
   * - x.asSInt
     - Binary cast to SInt
     - SInt(w(x) bits)
   * - x.asBools
     - Cast into a array of Bool
     - Vec(Bool(), w(x))
   * - x.asBool
     - Extract LSB of :code:`x`
     - Bool(x.lsb)
   * - S(x: T)
     - Cast a Data into a SInt
     - SInt(w(x) bits)
   * - U(x: T)
     - Cast a Data into an UInt
     - UInt(w(x) bits)
   * - x.intoSInt
     - Convert to SInt expanding sign bit
     - SInt(w(x) + 1 bits)
   * - myUInt.twoComplement(en: Bool)
     - Generate two's complement of number if ``en`` is ``True``, unchanged otherwise. (``en`` makes result negative)
     - SInt(w(myUInt) + 1, bits)
   * - mySInt.abs
     - Return the absolute value as a UInt value
     - UInt(w(mySInt) bits)
   * - mySInt.abs(en: Bool)
     - Return the absolute value as a UInt value when ``en`` is ``True``, otherwise just reinterpret bits as unsigned
     - UInt(w(mySInt) bits)
   * - mySInt.absWithSym
     - Return the absolute value of the UInt value with symmetric, shrink 1 bit
     - UInt(w(mySInt) - 1 bits)


To cast a ``Bool``, a ``Bits``, or an ``SInt`` into a ``UInt``, you can use ``U(something)``. To cast things into an ``SInt``, you can use ``S(something)``.

.. code-block:: scala

   // Cast an SInt to Bits
   val myBits = mySInt.asBits

   // Create a Vector of Bool
   val myVec = myUInt.asBools

   // Cast a Bits to SInt
   val mySInt = S(myBits)

   // UInt to SInt conversion
   val UInt_30 = U(30, 8 bit)

   val SInt_30 = UInt_30.intoSInt
   assert(SInt_30 === S(30, 9 bit))

   mySInt := UInt_30.twoComplement(booleanDoInvert)
       // if booleanDoInvert is True then we get S(-30, 9 bit)
       // otherwise we get S(30, 9 bit)

   // absolute values
   val SInt_n_4 = S(-3, 3 bit)
   val abs_en = SInt_n_3.abs(booleanDoAbs)
       // if booleanDoAbs is True we get U(3, 3 bit)
       // otherwise we get U"3'b101" or U(5, 3 bit) (raw bit pattern of -3)

   val SInt_n_128 = S(-128, 8 bit)
   val abs = SInt_n_128.abs
   assert(abs === U(128, 8 bit))
   val sym_abs = SInt_n_128.absWithSym
   assert(sym_abs === U(127, 7 bit))

Bit extraction
^^^^^^^^^^^^^^

All of the bit extraction operations can be used to read a bit / group of bits. Like in other HDLs
the extraction operators can also be used to assign a part of a ``UInt`` / ``SInt`` .

.. list-table::
   :header-rows: 1
   :widths: 2 4 2

   * - Operator
     - Description
     - Return
   * - x(y: Int)
     - Static bit access of y-th bit
     - Bool
   * - x(y: UInt)
     - Variable bit access of y-th bit
     - Bool
   * - x(offset: Int, width bits)
     - Fixed part select of fixed width, offset is LSB index
     - Bits(width bits)
   * - x(offset: UInt, width bits)
     - Variable part-select of fixed width, offset is LSB index
     - Bits(width bits)
   * - x(range: Range)
     - Access a :ref:`range <range>` of bits. Ex : myBits(4 downto 2)
     - Bits(range.size bits)
   * - x.subdivideIn(y slices, [strict: Boolean])
     - Subdivide x into y slices, y: Int
     - Vec(Bits(...), y)
   * - x.subdivideIn(y bits, [strict: Boolean])
     - Subdivide x in multiple slices of y bits, y: Int
     - Vec(Bits(y bit), ...)
   * - x.msb
     - Access most significant bit of x (highest index, sign bit for SInt)
     - Bool
   * - x.lsb
     - Access lowest significant bit of x (index 0)
     - Bool
   * - mySInt.sign
     - Access most sign bit, only SInt
     - Bool



Some basic examples:

.. code-block:: scala

   // get the element at the index 4
   val myBool = myUInt(4)
   // assign element 1
   myUInt(1) := True

   // index dynamically
   val index = UInt(2 bit)
   val indexed = myUInt(index, 2 bit)

   // range index
   val myUInt_8bit = myUInt_16bit(7 downto 0)
   val myUInt_7bit = myUInt_16bit(0 to 6)
   val myUInt_6bit = myUInt_16bit(0 until 6)
   // assign to myUInt_16bit(3 downto 0)
   myUInt_8bit(3 downto 0) := myUInt_4bit

   // equivalent slices, no reversing occurs
   val a = myUInt_16bit(8 downto 4)
   val b = myUInt_16bit(4 to 8)

   // read / assign the msb / leftmost bit / x.high bit
   val isNegative = mySInt_16bit.sign
   myUInt_16bit.msb := False

Subdivide details
"""""""""""""""""

Both overloads of ``subdivideIn`` have an optional parameter ``strict`` (i.e. ``subdivideIn(slices: SlicesCount, strict: Boolean = true)``).
If ``strict`` is ``true`` an error will be raised if the input could not be divided into equal parts. If set to ``false`` the last element may
be smaller than the other (equal sized) elements.

.. code-block:: scala

   // Subdivide
   val sel = UInt(2 bits)
   val myUIntWord = myUInt_128bits.subdivideIn(32 bits)(sel)
       // sel = 3 => myUIntWord = myUInt_128bits(127 downto 96)
       // sel = 2 => myUIntWord = myUInt_128bits( 95 downto 64)
       // sel = 1 => myUIntWord = myUInt_128bits( 63 downto 32)
       // sel = 0 => myUIntWord = myUInt_128bits( 31 downto  0)

    // If you want to access in reverse order you can do:
    val myVector   = myUInt_128bits.subdivideIn(32 bits).reverse
    val myRevUIntWord = myVector(sel)

    // We can also assign through subdivides
    val output8 = UInt(8 bit)
    val pieces = output8.subdivideIn(2 slices)
    // assign to output8
    pieces(0) := 0xf
    pieces(1) := 0x5

Misc
^^^^

In contrast to the bit extraction operations listed above it's not possible
to use the return values to assign to the original signal.

.. list-table::
   :header-rows: 1
   :widths: 2 5 1

   * - Operator
     - Description
     - Return
   * - x.getWidth
     - Return bitcount
     - Int
   * - x.high
     - Return the index of the MSB (highest allowed index for Int)
     - Int
   * - x.bitsRange
     - Return the range (0 to x.high)
     - Range
   * - x.minValue
     - Lowest possible value of x (e.g. 0 for UInt)
     - BigInt
   * - x.maxValue
     - Highest possible value of x
     - BigInt
   * - x.valueRange
     - Return the range from minimum to maximum possible value of x (x.minValue to x.maxValue).
     - Range
   * - x ## y
     - Concatenate, x->high, y->low
     - Bits(w(x) + w(y) bits)
   * - x #* n
     - Repeat x n-times
     - Bits(w(x) * n bits)
   * - x @@ y
     - Concatenate x:T with y:Bool/SInt/UInt
     - T(w(x) + w(y) bits)
   * - x.resize(y)
     - | Return a resized copy of x, if enlarged, it is filled with zero
       | for UInt or filled with the sign for SInt, y: Int
     - T(y bits)
   * - x.resized
     - | Return a version of x which is allowed to be automatically 
       | resized where needed
     - T(w(x) bits)
   * - x.expand
     - Return x with 1 bit expand
     - T(w(x)+1 bits)
   * - x.getZero
     - Return a new instance of type T that is assigned a constant value of zeros the same width as x.
     - T(0, w(x) bits).clearAll()
   * - x.getAllTrue
     - Return a new instance of type T that is assigned a constant value of ones the same width as x.
     - T(w(x) bits).setAll()

.. note::
  `validRange` can only be used for types where the minimum and maximum values fit into a signed
  32-bit integer. (This is a limitation given by the Scala ``scala.collection.immutable.Range``
  type which uses `Int`)

.. code-block:: scala

   myBool := mySInt.lsb  // equivalent to mySInt(0)

   // Concatenation
   val mySInt = mySInt_1 @@ mySInt_1 @@ myBool   
   val myBits = mySInt_1 ## mySInt_1 ## myBool

   // Resize
   myUInt_32bits := U"32'x112233344"
   myUInt_8bits  := myUInt_32bits.resized      // automatic resize (myUInt_8bits = 0x44)
   val lowest_8bits = myUInt_32bits.resize(8)  // resize to 8 bits (myUInt_8bits = 0x44)


FixPoint operations
-------------------

For fixpoint, we can divide it into two parts:

 - Lower bit operations (rounding methods)
 - High bit operations (saturation operations)

Lower bit operations
^^^^^^^^^^^^^^^^^^^^

.. image:: /asset/image/fixpoint/lowerBitOperation.png

About Rounding: https://en.wikipedia.org/wiki/Rounding

================ ================= ============= ======================== ====================== ===========
 SpinalHDL-Name   Wikipedia-Name    API           Mathematic Algorithm     return(align=false)    Supported
================ ================= ============= ======================== ====================== ===========
 FLOOR            RoundDown         floor         floor(x)                  w(x)-n   bits         Yes
 FLOORTOZERO      RoundToZero       floorToZero   sign*floor(abs(x))        w(x)-n   bits         Yes
 CEIL             RoundUp           ceil          ceil(x)                   w(x)-n+1 bits         Yes
 CEILTOINF        RoundToInf        ceilToInf     sign*ceil(abs(x))         w(x)-n+1 bits         Yes
 ROUNDUP          RoundHalfUp       roundUp       floor(x+0.5)              w(x)-n+1 bits         Yes
 ROUNDDOWN        RoundHalfDown     roundDown     ceil(x-0.5)               w(x)-n+1 bits         Yes
 ROUNDTOZERO      RoundHalfToZero   roundToZero   sign*ceil(abs(x)-0.5)     w(x)-n+1 bits         Yes
 ROUNDTOINF       RoundHalfToInf    roundToInf    sign*floor(abs(x)+0.5)    w(x)-n+1 bits         Yes
 ROUNDTOEVEN      RoundHalfToEven   roundToEven                                                   No
 ROUNDTOODD       RoundHalfToOdd    roundToOdd                                                    No
================ ================= ============= ======================== ====================== ===========

.. note::
   The **RoundToEven** and **RoundToOdd** modes are very special, and are used in some big data statistical fields with high accuracy concerns, SpinalHDL doesn't support them yet.

You will find `ROUNDUP`, `ROUNDDOWN`, `ROUNDTOZERO`, `ROUNDTOINF`, `ROUNDTOEVEN`, `ROUNTOODD` are very close in behavior, `ROUNDTOINF` is the most common. The behavior of rounding in different programming languages may be different.

====================== =================== ========================================================= ====================
 Programming language  default-RoundType   Example                                                   comments
====================== =================== ========================================================= ====================
 Matlab                 ROUNDTOINF          round(1.5)=2,round(2.5)=3;round(-1.5)=-2,round(-2.5)=-3   round to ±Infinity
 python2                ROUNDTOINF          round(1.5)=2,round(2.5)=3;round(-1.5)=-2,round(-2.5)=-3   round to ±Infinity
 python3                ROUNDTOEVEN         round(1.5)=round(2.5)=2;  round(-1.5)=round(-2.5)=-2      close to Even
 Scala.math             ROUNDTOUP           round(1.5)=2,round(2.5)=3;round(-1.5)=-1,round(-2.5)=-2   always to +Infinity
 SpinalHDL              ROUNDTOINF          round(1.5)=2,round(2.5)=3;round(-1.5)=-2,round(-2.5)=-3   round to ±Infinity
====================== =================== ========================================================= ====================

.. note::
   In SpinalHDL `ROUNDTOINF` is the default RoundType (``round = roundToInf``)

.. code-block:: scala

   val A  = SInt(16 bits)
   val B  = A.roundToInf(6 bits)         // default 'align = false' with carry, got 11 bit
   val B  = A.roundToInf(6 bits, align = true) // sat 1 carry bit, got 10 bit
   val B  = A.floor(6 bits)              // return 10 bit
   val B  = A.floorToZero(6 bits)        // return 10 bit
   val B  = A.ceil(6 bits)               // ceil with carry so return 11 bit
   val B  = A.ceil(6 bits, align = true) // ceil with carry then sat 1 bit return 10 bit
   val B  = A.ceilToInf(6 bits)
   val B  = A.roundUp(6 bits)
   val B  = A.roundDown(6 bits)
   val B  = A.roundToInf(6 bits)
   val B  = A.roundToZero(6 bits)
   val B  = A.round(6 bits)              // SpinalHDL uses roundToInf as the default rounding mode

   val B0 = A.roundToInf(6 bits, align = true)         //  ---+
                                                       //     |--> equal
   val B1 = A.roundToInf(6 bits, align = false).sat(1) //  ---+

.. note::
   Only ``floor`` and ``floorToZero`` work without the ``align`` option; they do not need a carry bit. Other rounding operations default to using a carry bit.

**round Api**

============= =========== ============================ ===================== ====================
 API           UInt/SInt   description                  Return(align=false)   Return(align=true)
============= =========== ============================ ===================== ====================
 floor         Both                                     w(x)-n   bits         w(x)-n bits
 floorToZero   SInt        equal to floor in UInt       w(x)-n   bits         w(x)-n bits
 ceil          Both                                     w(x)-n+1 bits         w(x)-n bits
 ceilToInf     SInt        equal to ceil in UInt        w(x)-n+1 bits         w(x)-n bits
 roundUp       Both        simple for HW                w(x)-n+1 bits         w(x)-n bits
 roundDown     Both                                     w(x)-n+1 bits         w(x)-n bits
 roundToInf    SInt        most Common                  w(x)-n+1 bits         w(x)-n bits
 roundToZero   SInt        equal to roundDown in UInt   w(x)-n+1 bits         w(x)-n bits
 round         Both        SpinalHDL chose roundToInf   w(x)-n+1 bits         w(x)-n bits
============= =========== ============================ ===================== ====================

.. note::
   Although ``roundToInf`` is very common, ``roundUp`` has the least cost and good timing, with almost no performance loss.
   As a result, ``roundUp`` is strongly recommended for production use.

High bit operations
^^^^^^^^^^^^^^^^^^^

.. image:: /asset/image/fixpoint/highBitOperation.png

========== ============ ====================================== =======================================
 function   Operation    Positive-Op                            Negative-Op                           
========== ============ ====================================== =======================================
 sat        Saturation   when(Top[w-1, w-n].orR) set maxValue   When(Top[w-1, w-n].andR) set minValue 
 trim       Discard      N/A                                    N/A                                  
 symmetry   Symmetric    N/A                                    minValue = -maxValue                 
========== ============ ====================================== =======================================

Symmetric is only valid for ``SInt``.

.. code-block:: scala

   val A  = SInt(8 bits)
   val B  = A.sat(3 bits)      // return 5 bits with saturated highest 3 bits
   val B  = A.sat(3)           // equal to sat(3 bits)
   val B  = A.trim(3 bits)     // return 5 bits with the highest 3 bits discarded
   val B  = A.trim(3 bits)     // return 5 bits with the highest 3 bits discarded
   val C  = A.symmetry         // return 8 bits and symmetry as (-128~127 to -127~127)
   val C  = A.sat(3).symmetry  // return 5 bits and symmetry as (-16~15 to -15~15)

fixTo function
^^^^^^^^^^^^^^

Two ways are provided in ``UInt``/``SInt`` to do fixpoint:

.. image:: /asset/image/fixpoint/fixPoint.png

``fixTo`` is strongly recommended in your RTL work, you don't need to handle carry bit alignment and bit width calculations manually like **Way1** in the above diagram.

Factory Fix function with Auto Saturation:

===================================== ===================== ===================
 Function                              Description           Return
===================================== ===================== ===================
 fixTo(section, roundType, symmetric)  Factory FixFunction   section.size bits
===================================== ===================== ===================

.. code-block:: scala

   val A  = SInt(16 bits)
   val B  = A.fixTo(10 downto 3) // default RoundType.ROUNDTOINF, sym = false
   val B  = A.fixTo( 8 downto 0, RoundType.ROUNDUP)
   val B  = A.fixTo( 9 downto 3, RoundType.CEIL,       sym = false)
   val B  = A.fixTo(16 downto 1, RoundType.ROUNDTOINF, sym = true )
   val B  = A.fixTo(10 downto 3, RoundType.FLOOR) // floor 3 bit, sat 5 bit @ highest
   val B  = A.fixTo(20 downto 3, RoundType.FLOOR) // floor 3 bit, expand 2 bit @ highest


.. _saturation: https://en.wikipedia.org/wiki/Saturation_arithmetic
