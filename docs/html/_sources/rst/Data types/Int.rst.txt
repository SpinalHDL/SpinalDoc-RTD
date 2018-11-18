.. role:: raw-html-m2r(raw)
   :format: html

.. _Int:

Unit/SInt
=========

Description
^^^^^^^^^^^

The ``UInt``\ /\ ``SInt`` type corresponds to a vector of bits that can be used for signed/unsigned integer arithmetic.

Declaration
^^^^^^^^^^^

The syntax to declare an integer is as follows:  (everything between [] is optional)

.. list-table::
   :header-rows: 1

   * - Syntax
     - Description
     - Return
   * - | UInt[()]
       | SInt[()]
     - Create an unsigned/signed integer, bits count is inferred
     - | UInt
       | SInt
   * - | UInt(x bits)
       | SInt(x bits)
     - Create an unsigned/signed integer with x bits
     - | UInt
       | SInt
   * - | U(value: Int[,x bits])
       | S(value: Int[,x bits])
     - Create an unsigned/signed integer assigned with 'value'
     - | UInt
       | SInt
   * - | U"[[size']base]value"
       | S"[[size']base]value"
     - Create an unsigned/signed integer assigned with 'value' (Base : 'h', 'd', 'o', 'b')
     - | UInt
       | SInt
   * - | U([x bits,] :ref:`element <element>`, ...)
       | S([x bits,] :ref:`element <element>`, ...)
     - Create an unsigned integer assigned with the value specified by elements
     - | UInt
       | SInt


.. code-block:: scala

   val myUInt = UInt(8 bits)
   myUInt := U(2,8 bits)
   myUInt := U(2)
   myUInt := U"0000_0101"  // Base per default is binary => 5
   myUInt := U"h1A"        // Base could be x (base 16)
                           //               h (base 16)
                           //               d (base 10)
                           //               o (base 8)
                           //               b (base 2)                       
   myUInt := U"8'h1A"       
   myUInt := 2             // You can use scala Int as literal value

   val myBool := myUInt === U(7 -> true,(6 downto 0) -> false)
   val myBool := myUInt === U(myUInt.range -> true)

   // For assignement purposes, you can omit the U/S, which also alow the use of the [default -> ???] feature
   myUInt := (default -> true)                        //Assign myUInt with "11111111"
   myUInt := (myUInt.range -> true)                   //Assign myUInt with "11111111"
   myUInt := (7 -> true, default -> false)            //Assign myUInt with "10000000"
   myUInt := ((4 downto 1) -> true, default -> false) //Assign myUInt with "00011110"

Operators
^^^^^^^^^

The following operators are available for the ``UInt`` and ``SInt`` type

Logic
~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return type
   * - x ^ y
     - Logical XOR
     - Bool
   * - ~x
     - Bitwise NOT
     - T(w(x) bits)
   * - x & y
     - Bitwise AND
     - T(max(w(xy) bits)
   * - x | y
     - Bitwise OR
     - T(max(w(xy) bits)
   * - x ^ y
     - Bitwise XOR
     - T(max(w(xy) bits)
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
     - Logical left rotation, y : UInt/Int
     - T(w(x) bits)
   * - x.rotateRight(y)
     - Logical right rotation, y : UInt/Int
     - T(w(x) bits)
   * - x.clearAll[()]
     - Clear all bits
     - 
   * - x.setAll[()]
     - Set all bits
     - 
   * - x.setAllTo(value : Boolean)
     - Set all bits to the given Boolean value
     - 
   * - x.setAllTo(value : Bool)
     - Set all bits to the given Bool value
     - 


.. code-block:: scala

   // Bitwise operator
   val a, b, c = SInt(32 bits)
   c := ~(a & b) //  Inverse(a AND b)

   val all_1 = a.andR // Check that all bit are equal to 1

   // Logical shift
   val uint_10bits = uint_8bits << 2  // shift left (result on 10 bits)
   val shift_8bits = uint_8bits |<< 2 // shift left (result on 8 bits)

   // Logical rotation
   val myBits = uint_8bits.rotateLeft(3) // left bit rotation

   // Set/clear
   val a = B"8'x42"
   when(cond){
     a.setAll() // set all bits to True when cond is True
   }

Arithmetic
~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x + y
     - Addition
     - T(max(w(x), w(y) bits)
   * - x - y
     - Subtraction
     - T(max(w(x), w(y) bits)
   * - x * y
     - Multiplication
     - T(w(x) + w(y) bits)
   * - x / y
     - Division
     - T(w(x) bits)
   * - x % y
     - Modulo
     - T(w(x) bits)


.. code-block:: scala

   // Addition
   val res = mySInt_1 + mySInt_2

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

   // Comparaison between two SInt
   myBool := mySInt_1 > mySInt_2

   // Comparaison between UInt and a literal
   myBool := myUInt_8bits >= U(3, 8 bits)

   when(myUInt_8bits === 3){

   }

Type cast
~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x.asBits
     - Binary cast in Bits
     - Bits(w(x) bits)
   * - x.asUInt
     - Binary cast in UInt
     - UInt(w(x) bits)
   * - x.asSInt
     - Binary cast in SInt
     - SInt(w(x) bits)
   * - x.asBools
     - Cast into a array of Bool
     - Vec(Bool, w(x))
   * - S(x: T)
     - Cast a Data into a SInt
     - SInt(w(x) bits)
   * - U(x: T)
     - Cast a Data into an UInt
     - UInt(w(x) bits)


To cast a Bool, a Bits or a SInt into a UInt, you can use ``U(something)``. To cast things into a SInt, you can use ``S(something)``

.. code-block:: scala

   // cast a SInt to Bits
   val myBits = mySInt.asBits

   // create a Vector of bool
   val myVec = myUInt.asBools

   // Cast a Bits to SInt
   val mySInt = S(myBits)

Bit extraction
~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x(y)
     - Readbit, y : Int/UInt
     - Bool
   * - x(offset, width)
     - Read bitfield, offset: UInt, width: Int
     - T(width bits)
   * - x(\ :ref:`range <range>`\ )
     - Read a range of bit. Ex : myBits(4 downto 2)
     - T(range bits)
   * - x(y) := z
     - Assign bits, y : Int/UInt
     - Bool
   * - x(offset, width) := z
     - Assign bitfield, offset: UInt, width: Int
     - T(width bits)
   * - x(\ :ref:`range <range>`\ ) := z
     - Assign a range of bit. Ex : myBits(4 downto 2) := U"010"
     - T(range bits)


.. code-block:: scala

   // get the element at the index 4
   val myBool = myUInt(4)

   // assign
   mySInt(1) := True

   // Range
   val myUInt_8bits = myUInt_16bits(7 downto 0)
   val myUInt_7bits = myUInt_16bits(0 to 6)
   val myUInt_6bits = myUInt_16Bits(0 until 6)

   mySInt_8bits(3 downto 0) := mySInt_4bits

Misc
~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x.getWidth
     - Return bitcount
     - Int
   * - x.msb
     - Return the most significant bit
     - Bool
   * - x.lsb
     - Return the least significant bit
     - Bool
   * - x.range
     - Return the range (x.high downto 0)
     - Range
   * - x.high
     - Return the upper bound of the type x
     - Int
   * - x ## y
     - Concatenate, x->high, y->low
     - Bits(w(x) + w(y) bits)
   * - x @@ y
     - Concatenate x:T with y:Bool/SInt/UInt
     - T(w(x) + w(y) bits)
   * - x.subdivideIn(y slices)
     - Subdivide x in y slices, y: Int
     - Vec(T,  y)
   * - x.subdivideIn(y bits)
     - Subdivide x in multiple slices of y bits, y: Int
     - Vec(T, w(x)/y)
   * - x.resize(y)
     - | Return a resized copy of x, if enlarged, it is filled with zero
       | for UInt or filled with the sign for SInt, y: Int
     - T(y bits)
   * - x.resized
     - | Return a version of x which is allowed to be automatically 
       | resized were needed
     - T(w(x) bits)
   * - myUInt.twoComplement(en: Bool)
     - Use the two's complement to transform an UInt into an SInt
     - SInt(w(myUInt) + 1)
   * - mySInt.abs
     - Return the absolute value of the SInt value
     - SInt(w(mySInt))
   * - mySInt.abs(en: Bool)
     - Return the absolute value of the SInt value when en is True
     - SInt(w(mySInt))


.. code-block:: scala

   myBool := mySInt.lsb  // equivalent to mySInt(0)

   // Concatenation
   val mySInt = mySInt_1 @@ mySInt_1 @@ myBool   
   val myBits = mySInt_1 ## mySInt_1 ## myBool   

   // Subdivide
   val sel = UInt(2 bits)
   val mySIntWord = mySInt_128bits.subdivideIn(32 bits)(sel)
       // sel = 0 => mySIntWord = mySInt_128bits(127 downto 96)
       // sel = 1 => mySIntWord = mySInt_128bits( 95 downto 64)
       // sel = 2 => mySIntWord = mySInt_128bits( 63 downto 32)
       // sel = 3 => mySIntWord = mySInt_128bits( 31 downto  0)

    // if you want to access in a reverse order you can do
    val myVector   = mySInt_128bits.subdivideIn(32 bits).reverse
    val mySIntWord = myVector(sel)

   // Resize
   myUInt_32bits := U"32'x112233344"
   myUInt_8bits  := myUInt_32bits.resized       // automatic resize (myUInt_8bits = 0x44)
   myUInt_8bits  := myUInt_32bits.resize(8)     // resize to 8 bits (myUInt_8bits = 0x44)

   // Two's complement
   mySInt := myUInt.twoComplement(myBool)

   // Absolute value
   mySInt_abs := mySInt.abs
