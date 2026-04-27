.. _Bits:

Bits
====

The ``Bits`` type is a vector of bits without conveying any arithmetic meaning.

Declaration
-----------

The syntax to declare a bit vector is as follows (everything between [] is optional):

.. list-table::
   :header-rows: 1
   :widths: 5 10

   * - Syntax
     - Description
   * - Bits [()]
     - Create Bits, bit count is inferred from the widest assignment statement
       after construction
   * - Bits(x bits)
     - Create Bits with x bits
   * - | B(value: Int[, x bits])
       | B(value: BigInt[, x bits])
     - Create Bits with x bits assigned with 'value'
   * - B"[[size']base]value"
     - Create Bits assigned with 'value' (base: 'h', 'd', 'o', 'b')
   * - B([x bits,] elements: Element*)
     - Create Bits assigned with the value specified by :ref:`elements <element>`


.. code-block:: scala

   val myBits1 = Bits(32 bits)   
   val myBits2 = B(25, 8 bits)
   val myBits3 = B"8'xFF"   // Base could be x,h (base 16)                         
                            //               d   (base 10)
                            //               o   (base 8)
                            //               b   (base 2)    
   val myBits4 = B"1001_0011"  // _ can be used for readability

   // Bits with all ones ("11111111")
   val myBits5 = B(8 bits, default -> True)

   // initialize with "10111000" through a few elements
   val myBits6 = B(8 bits, (7 downto 5) -> B"101", 4 -> true, 3 -> True, default -> false)

   // "10000000" (For assignment purposes, you can omit the B)
   val myBits7 = Bits(8 bits)
   myBits7 := (7 -> true, default -> false) 

When inferring the width of a ``Bits`` the sizes of assigned values still have to match 
the final size of the signal:

.. code-block:: scala

   // Declaration
   val myBits = Bits()     // the size is inferred from the widest assignment
   // ....
   // .resized needed to prevent WIDTH MISMATCH error as the constants
   // width does not match size that is inferred from assignment below
   myBits := B("1010").resized  // auto-widen Bits(4 bits) to Bits(6 bits)
   when(condxMaybe) {
     // Bits(6 bits) is inferred for myBits, this is the widest assignment
     myBits := B("110000")
   }

Operators
---------

The following operators are available for the ``Bits`` type:

Logic
^^^^^

.. list-table::
   :header-rows: 1
   :widths: 2 3 2

   * - Operator
     - Description
     - Return type
   * - ~x
     - Bitwise NOT
     - Bits(w(x) bits)
   * - x & y
     - Bitwise AND
     - Bits(w(xy) bits)
   * - x | y
     - Bitwise OR
     - Bits(w(xy) bits)
   * - x ^ y
     - Bitwise XOR
     - Bits(w(xy) bits)
   * - x.xorR
     - XOR all bits of x
     - Bool
   * - x.orR
     - OR all bits of x
     - Bool
   * - x.andR
     - AND all bits of x
     - Bool
   * - | y = 1 // Int
       | x \>\> y
     - | Logical shift right, y: Int
       | Result may reduce width
     - Bits(w(x) - y bits)
   * - | y = U(1) // UInt
       | x \>\> y
     - | Logical shift right, y: UInt
       | Result is same width
     - Bits(w(x) bits)
   * - | y = 1 // Int
       | x \<\< y
     - | Logical shift left, y: Int
       | Result may increase width
     - Bits(w(x) + y bits)
   * - | y = U(1) // UInt
       | x \<\< y
     - | Logical shift left, y: UInt
       | Result may increase width
     - Bits(w(x) + max(y) bits)
   * - x \|\>\> y
     - | Logical shift right, y: Int/UInt
       | Result is same width
     - Bits(w(x) bits)
   * - x \|\<\< y
     - | Logical shift left, y: Int/UInt
       | Result is same width
     - Bits(w(x) bits)
   * - x.rotateLeft(y)
     - | Logical left rotation, y: UInt/Int
       | Result is same width
     - Bits(w(x) bits)
   * - x.rotateRight(y)
     - | Logical right rotation, y: UInt/Int
       | Result is same width
     - Bits(w(x) bits)
   * - x.clearAll[()]
     - Clear all bits
     - *modifies x*
   * - x.setAll[()]
     - Set all bits
     - *modifies x*
   * - x.setAllTo(value: Boolean)
     - Set all bits to the given Boolean value
     - *modifies x*
   * - x.setAllTo(value: Bool)
     - Set all bits to the given Bool value
     - *modifies x*

.. code-block:: scala

   // Bitwise operator
   val a, b, c = Bits(32 bits)
   c := ~(a & b) // Inverse(a AND b)

   val all_1 = a.andR // Check that all bits are equal to 1

   // Logical shift
   val bits_10bits = bits_8bits << 2  // shift left (results in 10 bits)
   val shift_8bits = bits_8bits |<< 2 // shift left (results in 8 bits)

   // Logical rotation
   val myBits = bits_8bits.rotateLeft(3) // left bit rotation

   // Set/clear
   val a = B"8'x42"
   when(cond) {
     a.setAll() // set all bits to True when cond is True
   }

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


.. code-block:: scala

   when(myBits === 3) {
     // ...
   }

   val notMySpecialValue = myBits_32 =/= B"32'x44332211"

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
     - Cast to an array of Bools
     - Vec(Bool(), w(x))
   * - x.asBool
     - Extract LSB of :code:`x`
     - Bool(x.lsb)
   * - B(x: T)
     - Cast Data to Bits
     - Bits(w(x) bits)


To cast a ``Bool``, ``UInt`` or an ``SInt`` into a ``Bits``, you can use ``B(something)`` or ``B(something[, x bits])``:

.. code-block:: scala

   // cast a Bits to SInt
   val mySInt = myBits.asSInt

   // create a Vector of bool
   val myVec = myBits.asBools

   // Cast a SInt to Bits
   val myBits = B(mySInt)

   // Cast the same SInt to Bits but resize to 3 bits
   //  (will expand/truncate as necessary, retaining LSB)
   val myBits = B(mySInt, 3 bits)

Bit extraction
^^^^^^^^^^^^^^

All of the bit extraction operations can be used to read a bit / group of bits. Like in other HDLs
the extraction operators can also be used to assign a part of a ``Bits``.

All of the bit extraction operations can be used to read a bit / group of bits. Like in other HDLs They
can also be used to select a range of bits to be written.

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
     - Access most significant bit of x (highest index)
     - Bool
   * - x.lsb
     - Access lowest significant bit of x (index 0)
     - Bool


Some basic examples:

.. code-block:: scala

   // get the element at the index 4
   val myBool = myBits(4)
   // assign element 1
   myBits(1) := True

   // index dynamically
   val index = UInt(2 bit)
   val indexed = myBits(index, 2 bit)

   // range index
   val myBits_8bit = myBits_16bit(7 downto 0)
   val myBits_7bit = myBits_16bit(0 to 6)
   val myBits_6bit = myBits_16bit(0 until 6)
   // assign to myBits_16bit(3 downto 0)
   myBits_8bit(3 downto 0) := myBits_4bit

   // equivalent slices, no reversing occurs
   val a = myBits_16bit(8 downto 4)
   val b = myBits_16bit(4 to 8)

   // read / assign the msb / leftmost bit / x.high bit
   val isNegative = myBits_16bit.msb
   myBits_16bit.msb := False

Subdivide details
"""""""""""""""""

Both overloads of ``subdivideIn`` have an optional parameter ``strict`` (i.e. ``subdivideIn(slices: SlicesCount, strict: Boolean = true)``).
If ``strict`` is ``true`` an error will be raised if the input could not be divided into equal parts. If set to ``false`` the last element may
be smaller than the other (equal sized) elements.

.. code-block:: scala

   // Subdivide
   val sel = UInt(2 bits)
   val myBitsWord = myBits_128bits.subdivideIn(32 bits)(sel)
       // sel = 3 => myBitsWord = myBits_128bits(127 downto 96)
       // sel = 2 => myBitsWord = myBits_128bits( 95 downto 64)
       // sel = 1 => myBitsWord = myBits_128bits( 63 downto 32)
       // sel = 0 => myBitsWord = myBits_128bits( 31 downto  0)

    // If you want to access in reverse order you can do:
    val myVector   = myBits_128bits.subdivideIn(32 bits).reverse
    val myRevBitsWord = myVector(sel)

    // We can also assign through subdivides
    val output8 = Bits(8 bit)
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
   :widths: 2 4 2

   * - Operator
     - Description
     - Return
   * - x.getWidth
     - Return bitcount
     - Int
   * - x.bitsRange
     - Return the range (0 to x.high)
     - Range
   * - x.valueRange
     - Return the range of minimum to maximum x values, interpreted as an unsigned integer (0 to 2 \*\* width - 1).
     - Range
   * - x.high
     - Return the index of the MSB (highest allowed zero-based index for x)
     - Int
   * - x.reversed
     - Return a copy of x with reverse bit order, MSB<>LSB are mirrored.
     - Bits(w(x) bits)
   * - x ## y
     - Concatenate, x->high, y->low
     - Bits(w(x) + w(y) bits)
   * - x #* n
     - Repeat x n-times
     - Bits(w(x) * n bits)
   * - x.resize(y)
     - Return a resized representation of x, if enlarged, it is extended with zero
       padding at MSB as necessary, y: Int
     - Bits(y bits)
   * - x.resized
     - Return a version of x which is allowed to be automatically resized were
       needed.  The resize operation is deferred until the point of assignment later.
       The resize may widen or truncate, retaining the LSB.
     - Bits(w(x) bits)
   * - x.resizeLeft(x)
     - Resize by keeping MSB at the same place, x:Int
       The resize may widen or truncate, retaining the MSB.
     - Bits(x bits)
   * - x.getZero
     - Return a new instance of Bits that is assigned a constant value of zeros the same width as x.
     - Bits(0, w(x) bits)
   * - x.getAllTrue
     - Return a new instance of Bits that is assigned a constant value of ones the same width as x.
     - Bits(w(x) bits).setAll()

.. note::
  `validRange` can only be used for types where the minimum and maximum values fit into a signed
  32-bit integer. (This is a limitation given by the Scala ``scala.collection.immutable.Range``
  type which uses `Int`)

.. code-block:: scala
   
   println(myBits_32bits.getWidth) // 32

   // Concatenation
   myBits_24bits := bits_8bits_1 ## bits_8bits_2 ## bits_8bits_3
   // or
   myBits_24bits := Cat(bits_8bits_1, bits_8bits_2, bits_8bits_3)

   // Resize
   myBits_32bits := B"32'x112233344"
   myBits_8bits  := myBits_32bits.resized       // automatic resize (myBits_8bits = 0x44)
   myBits_8bits  := myBits_32bits.resize(8)     // resize to 8 bits (myBits_8bits = 0x44)
   myBits_8bits  := myBits_32bits.resizeLeft(8) // resize to 8 bits (myBits_8bits = 0x11)

.. _maskedliteral:

MaskedLiteral
-------------

MaskedLiteral values are bit vectors with don't care values denoted with ``-``.
They can be used for direct comparison or for ``switch`` statements and ``mux`` es.

.. code-block:: scala

     val myBits = B"1101"

     val test1 = myBits === M"1-01" // True
     val test2 = myBits === M"0---" // False
     val test3 = myBits === M"1--1" // True
