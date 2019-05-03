.. _Bits:

Bits
====

Description
^^^^^^^^^^^

The ``Bits`` type corresponds to a vector of bits that does not convey any arithmetic meaning.

Declaration
^^^^^^^^^^^

The syntax to declare a bit vector is as follows: (everything between [] is optional)

.. list-table::
   :header-rows: 1
   :widths: 2 5 1

   * - Syntax
     - Description
     - Return
   * - Bits [()]
     - Create a BitVector, bits count is inferred
     - Bits
   * - Bits(x bits)
     - Create a BitVector with x bits
     - Bits
   * - B(value: Int[, x bits])
     - Create a BitVector with x bits assigned with 'value'
     - Bits
   * - B"[[size']base]value"
     - Create a BitVector assigned with 'value' (Base: 'h', 'd', 'o', 'b')
     - Bits
   * - B([x bits,] :ref:`element <element>`\ , ...)
     - Create a BitVector assigned with the value specified by elements
     - Bits


.. code-block:: scala

   // Declaration
   val myBits  = Bits()     // the size is inferred     
   val myBits1 = Bits(32 bits)   
   val myBits2 = B(25, 8 bits)
   val myBits3 = B"8'xFF"   // Base could be x,h (base 16)                         
                            //               d   (base 10)
                            //               o   (base 8)
                            //               b   (base 2)    
   val myBits4 = B"1001_0011"  // _ can be used for readability

   // Element
   val myBits5 = B(8 bits, default -> True) // "11111111"
   val myBits6 = B(8 bits, (7 downto 5) -> B"101", 4 -> true, 3 -> True, default -> false ) // "10111000"
   val myBits7 = Bits(8 bits)
   myBits7 := (7 -> true, default -> false) // "10000000" (For assignement purposes, you can omit the B)

Operators
^^^^^^^^^

The following operators are available for the ``Bits`` type

Logic
~~~~~

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
   * - x \>\> y
     - Logical shift right, y: Int
     - Bits(w(x) - y bits)
   * - x \>\> y
     - Logical shift right, y: UInt
     - Bits(w(x) bits)
   * - x \<\< y
     - Logical shift left, y: Int
     - Bits(w(x) + y bits)
   * - x \<\< y
     - Logical shift left, y: UInt
     - Bits(w(x) + max(y) bits)
   * - x \|\>\> y
     - Logical shift right, y: Int/UInt
     - Bits(w(x) bits)
   * - x \|\<\< y
     - Logical shift left, y: Int/UInt
     - Bits(w(x) bits)
   * - x.rotateLeft(y)
     - Logical left rotation, y: UInt/Int
     - Bits(w(x) bits)
   * - x.rotateRight(y)
     - Logical right rotation, y: UInt/Int
     - Bits(w(x) bits)
   * - x.clearAll[()]
     - Clear all bits
     - 
   * - x.setAll[()]
     - Set all bits
     - 
   * - x.setAllTo(value: Boolean)
     - Set all bits to the given Boolean value
     - 
   * - x.setAllTo(value: Bool)
     - Set all bits to the given Bool value
     - 


.. code-block:: scala

   // Bitwise operator
   val a, b, c = Bits(32 bits)
   c := ~(a & b) //  Inverse(a AND b)

   val all_1 = a.andR // Check that all bits are equal to 1

   // Logical shift
   val bits_10bits = bits_8bits << 2  // shift left (results in 10 bits)
   val shift_8bits = bits_8bits |<< 2 // shift left (results in 8 bits)

   // Logical rotation
   val myBits = bits_8bits.rotateLeft(3) // left bit rotation

   // Set/clear
   val a = B"8'x42"
   when(cond){
     a.setAll() // set all bits to True when cond is True
   }

Comparison
~~~~~~~~~~

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

   when(myBits === 3){
   }

   when(myBits_32 =/= B"32'x44332211"){
   }

Type cast
~~~~~~~~~

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
     - Vec(Bool, w(x))
   * - B(x: T)
     - Cast Data to Bits
     -  Bits(w(x) bits)


To cast a Bool, UInt or a SInt into a Bits, you can use ``B(something)``

.. code-block:: scala

   // cast a Bits to SInt
   val mySInt = myBits.asSInt

   // create a Vector of bool
   val myVec = myBits.asBools

   // Cast a SInt to Bits
   val myBits = B(mySInt)

Bit extraction
~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1
   :widths: 2 5 2

   * - Operator
     - Description
     - Return
   * - x(y)
     - Readbit, y: Int/UInt
     - Bool
   * - x(hi,lo)
     - Read bitfield, hi: Int, lo: Int
     - Bits(hi-lo+1 bits)
   * - x(offset,width bits)
     - Read bitfield, offset: UInt, width: Int
     - Bits(width bits)
   * - x(\ :ref:`range <range>`\ )
     - Read a range of bit. Ex : myBits(4 downto 2)
     - Bits(range bits)
   * - x(y) := z
     - Assign bits, y: Int/UInt
     - Bool
   * - x(offset, width bits) := z
     - Assign bitfield, offset: UInt, width: Int
     - Bits(width bits)
   * - x(\ :ref:`range <range>`\ ) := z
     - Assign a range of bit. Ex : myBits(4 downto 2) := B"010"
     - Bits(range bits)


.. code-block:: scala

   // get the element at the index 4
   val myBool = myBits(4)

   // assign
   myBits(1) := True

   // Range
   val myBits_8bits = myBits_16bits(7 downto 0)
   val myBits_7bits = myBits_16bits(0 to 6)
   val myBits_6bits = myBits_16Bits(0 until 6)

   myBits_8bits(3 downto 0) := myBits_4bits

Misc
~~~~

.. list-table::
   :header-rows: 1
   :widths: 2 4 2

   * - Operator
     - Description
     - Return
   * - x.getWidth
     - Return bitcount
     - Int
   * - x.range
     - Return the range (x.high downto 0)
     - Range
   * - x.high
     - Return the upper bound of the type x
     - Int
   * - x.msb
     - Return the most significant bit
     - Bool
   * - x.lsb
     - Return the least significant bit
     - Bool
   * - x ## y
     - Concatenate, x->high, y->low
     - Bits(w(x) + w(y) bits)
   * - x.subdivideIn(y slices)
     - Subdivide x in y slices, y: Int
     - Vec(Bits, y)
   * - x.subdivideIn(y bits)
     - Subdivide x in multiple slices of y bits, y: Int
     - Vec(Bits, w(x)/y)
   * - x.resize(y)
     - Return a resized copy of x, if enlarged, it is filled with zero, y: Int
     - Bits(y bits)
   * - x.resized
     - Return a version of x which is allowed to be automatically resized were needed
     - Bits(w(x) bits)
   * - x.resizeLeft(x)
     - Resize by keeping MSB at the same place, x:Int
     - Bits(x bits)


.. code-block:: scala

   println(myBits_32bits.getWidth) // 32

   myBool := myBits.lsb  // equivalent to myBits(0)

   // concatenation
   myBits_24bits := bits_8bits_1 ## bits_8bits_2 ## bits_8bits_3

   // Subdivide
   val sel = UInt(2 bits)
   val myBitsWord = myBits_128bits.subdivideIn(32 bits)(sel)
       // sel = 0 => myBitsWord = myBits_128bits(127 downto 96)
       // sel = 1 => myBitsWord = myBits_128bits( 95 downto 64)
       // sel = 2 => myBitsWord = myBits_128bits( 63 downto 32)
       // sel = 3 => myBitsWord = myBits_128bits( 31 downto  0)

    // if you want to access in a reverse order you can do
    val myVector   = myBits_128bits.subdivideIn(32 bits).reverse
    val myBitsWord = myVector(sel)

   // Resize
   myBits_32bits := B"32'x112233344"
   myBits_8bits  := myBits_32bits.resized       // automatic resize (myBits_8bits = 0x44)
   myBits_8bits  := myBits_32bits.resize(8)     // resize to 8 bits (myBits_8bits = 0x44)
   myBits_8bits  := myBits_32bits.resizeLeft(8) // resize to 8 bits (myBits_8bits = 0x11)
