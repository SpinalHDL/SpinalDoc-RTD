
Description
^^^^^^^^^^^

The ``UFix`` and ``SFix`` types correspond to a vector of bits that can be used for fixed point
arithmetic.

Declaration
^^^^^^^^^^^

The syntax to declare a fixed point number is as follows:

Unsigned Fixed Point
~~~~~~~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Syntax
     - bit width
     - resolution
     - max
     - min
   * - UFix(peak:ExpNumber, resolution:ExpNumber)
     - peak-resolution
     - 2^resolution
     - 2^peak-2^resolution
     - 0
   * - UFix(peak:ExpNumber, width:BitCount)
     - width
     - 2^(peak-width)
     - 2^peak-2^(peak-width)
     - 0


Signed Fixed Point
~~~~~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Syntax
     - bit width
     - resolution
     - max
     - min
   * - SFix(peak:ExpNumber, resolution:ExpNumber)
     - peak-resolution
     - 2^resolution
     - 2^peak-2^resolution
     - 0
   * - SFix(peak:ExpNumber, width:BitCount)
     - width
     - 2^(peak-width)
     - 2^peak-2^(peak-width)
     - 0


Format
~~~~~~

The chosen format follows the usual way of defining fixed point number format using Q notation. More information `there <https://en.wikipedia.org/wiki/Q_(number_format>`_\ ).

For example Q8.2 will mean an fixed point of 8+2 bits, where 8 bit are used for the natural part and 2 bits for the fractional part.
If the fixed point number is signed, one more bit is used for the sign.

The resolution is defined as being the smallest power of two that can be represented in this number.

Examples
~~~~~~~~

.. code-block:: scala

   // Unsigned Fixed Point
   val UQ_8_2 = UFix(peak = 8 exp,resolution = -2 exp)
   val UQ_8_2 = UFix(8 exp, -2 exp)

   val UQ_8_2 = UFix(peak = 8 exp,width = 10 bits)
   val UQ_8_2 = UFix(8 exp, 10 bits)

   // Signed Fixed Point
   val Q_8_2 = SFix(peak = 8 exp,resolution = -2 exp)
   val Q_8_2 = SFix(8 exp, -2 exp)

   val Q_8_2 = SFix(peak = 8 exp,width = 11 bits)
   val Q_8_2 = SFix(8 exp, 11 bits)

Assignments
^^^^^^^^^^^

Valid Assignments
~~~~~~~~~~~~~~~~~

An assignment to a fixed point value is valid when there is no bit loss. A bit loss occurrence
will result in an error.

If the source fixed point value is too big, the .truncated function will allow you to
resize the source number to match the destination size.

Example
"""""""

.. code-block:: scala

   val i16_m2 = SFix(16 exp,-2 exp)
   val i16_0  = SFix(16 exp, 0 exp)
   val i8_m2  = SFix( 8 exp,-2 exp)
   val o16_m2 = SFix(16 exp,-2 exp)
   val o16_m0 = SFix(16 exp, 0 exp)
   val o14_m2 = SFix(14 exp,-2 exp)

   o16_m2 := i16_m2            // OK
   o16_m0 := i16_m2            // Not OK, Bit loss
   o14_m2 := i16_m2            // Not OK, Bit loss
   o16_m0 := i16_m2.truncated  // OK, as it is resized
   o14_m2 := i16_m2.truncated  // OK, as it is resized

From a Scala constant
~~~~~~~~~~~~~~~~~~~~~

It is allowed to assign a Scala Double or a Scala BigInt into a fixed point.

Example
"""""""

.. code-block:: scala

   val i4_m2 = SFix(4 exp,-2 exp)
   i4_m2 := 1.25    //Will load 5 in i4_m2.raw
   i4_m2 := 4       //Will load 16 in i4_m2.raw

Raw value
^^^^^^^^^

The integer representation of the fixed point number can be read or written using the
``raw`` property.

Example
~~~~~~~

.. code-block:: scala

   val UQ_8_2 = UFix(8 exp, 10 bits)
   UQ_8_2.raw := 4        //Assign the value corresponding to 1.0
   UQ_8_2.raw := U(17)    //Assign the value corresponding to 4.25

Operators
^^^^^^^^^

The following operators are available for the ``UFix`` type

Arithmetic
~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Returned resolution
     - Returned amplitude
   * - x + y
     - Addition
     - Min(x.resolution, y.resolution)
     - Max(x.amplitude, y.amplitude)
   * - x - y
     - Subtraction
     - Min(x.resolution, y.resolution)
     - Max(x.amplitude, y.amplitude)
   * - x * y
     - Multiplication
     - x.resolution * y.resolution)
     - x.amplitude * y.amplitude
   * - x >> y
     - Arithmetic shift right, y : Int
     - x.amplitude >> y
     - x.resolution >> y
   * - x << y
     - Arithmetic shift left, y : Int
     - x.amplitude << y
     - x.resolution << y
   * - x >>| y
     - Arithmetic shift right, y : Int
     - x.amplitude >> y
     - x.resolution
   * - x <<| y
     - Arithmetic shift left, y : Int
     - x.amplitude << y
     - x.resolution


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
   * - x > y
     - Less than
     - Bool
   * - x >= y
     - Less than or equal
     - Bool


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
     - Vec(Bool,width(x))
   * - x.toUInt
     - Return the corresponding UInt (with truncation)
     - UInt
   * - x.toSInt
     - Return the corresponding SInt (with truncation)
     - SInt
   * - x.toUFix
     - Return the corresponding UFix
     - UFix
   * - x.toSFix
     - Return the corresponding SFix
     - SFix


Misc
~~~~

.. list-table::
   :header-rows: 1

   * - Name
     - Return
     - Description
   * - x.maxValue
     - Return the maximum value storable
     - Double
   * - x.minValue
     - Return the minimum value storable
     - Double
   * - x.resolution
     - x.amplitude * y.amplitude
     - Double

