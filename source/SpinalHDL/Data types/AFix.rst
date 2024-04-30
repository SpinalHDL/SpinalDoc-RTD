
.. _AFix:

AFix
====

Description
^^^^^^^^^^^

Auto-ranging Fixed-Point, ``AFix``, is a fixed-point class which tracks the representable range of values while preforming fixed-point operations.

**Warning: Much of this code is still under development. API and function calls may change.**

User feedback is appreciated!


Declaration
^^^^^^^^^^^

AFix can be created using bit sizes or exponents:

.. code-block:: scala

  AFix.U(12 bits)             // U12.0
  AFix(QFormat(12, 0, false)) // U12.0
  AFix.UQ(8 bits, 4 bits)     // U8.4
  AFix.U(8 exp, 12 bits)      // U8.4
  AFix.U(8 exp, -4 exp)       // U8.4
  AFix.U(8 exp, 4 exp)        // U8.-4
  AFix(QFormat(12, 4, false)) // U8.4

  AFix.S(12 bits)             // S11.0 + sign
  AFix(QFormat(12, 0, true))  // S11.0 + sign
  AFix.SQ(8 bits, 4 bits)     // S8.4  + sign
  AFix.S(8 exp, 12 bits)      // S8.3  + sign
  AFix.S(8 exp, -4 exp)       // S8.4  + sign
  AFix(QFormat(12, 4, true))  // S7.4  + sign


These will have representable ranges for all bits.

For example:

``AFix.U(12 bits)`` will have a range of 0 to 4095.

``AFix.SQ(8 bits, 4 bits)`` will have a range of -4096 (-256) to 4095 (255.9375)

``AFix.U(8 exp, 4 exp)`` will have a range of 0 to 256


Custom range ``AFix`` values can be created be directly instantiating the class.

.. code-block:: scala

  class AFix(val maxValue: BigInt, val minValue: BigInt, val exp: ExpNumber)

  new AFix(4096, 0, 0 exp)     // [0 to 4096, 2^0]
  new AFix(256, -256, -2 exp)  // [-256 to 256, 2^-2]
  new AFix(16, 8, 2 exp)       // [8 to 16, 2^2]


The ``maxValue`` and ``minValue`` stores what backing integer values are representable.
These values represent the true fixed-point value after multiplying by ``2^exp``.

``AFix.U(2 exp, -1 exp)`` can represent:
``0, 0.5, 1.0, 1.5, 2, 2.5, 3, 3.5``

``AFix.S(2 exp, -2 exp)`` can represent:
``-2.0, -1.75, -1.5, -1.25, -1, -0.75, -0.5, -0.25, 0, 0.25, 0.5, 0.75, 1, 1.25, 1.5, 1.75``

Exponent values greater 0 are allowed and represent values which are larger than 1.

``AFix.S(2 exp, 1 exp)`` can represent:
``-4, 2, 0, 2``

``AFix(8, 16, 2 exp)`` can represent:
``32, 36, 40, 44, 48, 52, 56, 60, 64``

Note: ``AFix`` will use 5 bits to save this type as that can store ``16``, its ``maxValue``.


Mathematical Operations
^^^^^^^^^^^^^^^^^^^^^^^

``AFix`` supports Addition (``+``), Subtraction (``-``), and Multiplication (``*``) at the hardware level.
Division (``\``) and Modulo (``%``) operators are provided but are not recommended for hardware elaboration.


Operations are preformed as if the ``AFix`` value is a regular ``Int`` number.
Signed and unsigned numbers are interoperable. There are no type differences between signed or unsigned values.

.. code-block:: scala

  // Integer and fractional expansion
  val a = AFix.U(4 bits)          // [   0 (  0.)     to  15 (15.  )]  4 bits, 2^0
  val b = AFix.UQ(2 bits, 2 bits) // [   0 (  0.)     to  15 ( 3.75)]  4 bits, 2^-2
  val c = a + b                   // [   0 (  0.)     to  77 (19.25)]  7 bits, 2^-2
  val d = new AFix(-4, 8, -2 exp) // [-  4 (- 1.25)   to   8 ( 2.00)]  5 bits, 2^-2
  val e = c * d                   // [-308 (-19.3125) to 616 (38.50)] 11 bits, 2^-4

  // Integer without expansion
  val aa = new AFix(8, 16, -4 exp) // [8 to 16] 5 bits, 2^-4
  val bb = new AFix(1, 15, -4 exp) // [1 to 15] 4 bits, 2^-4
  val cc = aa + bb                 // [9 to 31] 5 bits, 2^-4


``AFix`` supports operations without without range expansion.
It does this by selecting the aligned maximum and minimum ranges from each of the inputs.

``+|`` Add without expansion.
``-|`` Subtract without expansion.


Inequality Operations
^^^^^^^^^^^^^^^^^^^^^

``AFix`` supports standard inequality operations.

.. code-block:: scala

  A === B
  A =\= B
  A < B
  A <= B
  A > B
  A >= B

Warning: Operations which are out of range at compile time will be optimized out!


Bitshifting
^^^^^^^^^^^

``AFix`` supports decimal and bit shifting

``<<`` Shifts the decimal to the left. Adds to the exponent.
``>>`` Shifts the decimal to the right. Subtracts from the exponent.
``<<|`` Shifts the bits to the left. Adds fractional zeros.
``>>|`` Shifts the bits to the right. Removes fractional bits.


Saturation and Rounding
^^^^^^^^^^^^^^^^^^^^^^^

``AFix`` implements saturation and all common rounding methods.

Saturation works by saturating the backing value range of an ``AFix`` value. There are multiple helper functions which
consider the exponent.

.. code-block:: scala

  val a = new AFix(63, 0, -2 exp) // [0 to 63, 2^-2]
  a.sat(63, 0)                    // [0 to 63, 2^-2]
  a.sat(63, 0, -3 exp)            // [0 to 31, 2^-2]
  a.sat(new AFix(31, 0, -1 exp))  // [0 to 31, 2^-2]

``AFix`` rounding modes:

.. code-block:: scala

  // The following require exp < 0
  .floor() or .truncate()
  .ceil()
  .floorToZero()
  .ceilToInf()
  // The following require exp < -1
  .roundHalfUp()
  .roundHalfDown()
  .roundHalfToZero()
  .roundHalfToInf()
  .roundHalfToEven()
  .roundHalfToOdd()

A mathematical example of these rounding modes is better explained here: `Rounding - Wikipedia <https://en.wikipedia.org/wiki/Rounding>`_

All of these modes will result in an ``AFix`` value with 0 exponent. If rounding to a different exponent is required
consider shifting or use an assignment with the ``truncated`` tag.


Assignment
^^^^^^^^^^

``AFix`` will automatically check and expand range and precision during assignment. By default, it is an error to assign
an ``AFix`` value to another ``AFix`` value with smaller range or precision.

The ``.truncated`` function is used to control how assignments to smaller types.

.. code-block:: scala

  def truncated(saturation: Boolean = false,
                overflow  : Boolean = true,
                rounding  : RoundType = RoundType.FLOOR)

  def saturated(): AFix = this.truncated(saturation = true, overflow = false)

``RoundType``:

.. code-block:: scala

  RoundType.FLOOR
  RoundType.CEIL
  RoundType.FLOORTOZERO
  RoundType.CEILTOINF
  RoundType.ROUNDUP
  RoundType.ROUNDDOWN
  RoundType.ROUNDTOZERO
  RoundType.ROUNDTOINF
  RoundType.ROUNDTOEVEN
  RoundType.ROUNDTOODD

The ``saturation`` flag will add logic to saturate to the assigned datatype range.

The ``overflow`` flag will allow assignment directly after rounding without range checking.

Rounding is always required when assigning a value with more precision to one with lower precision.
