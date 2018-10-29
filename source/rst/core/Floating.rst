
Description
^^^^^^^^^^^

The ``Floating`` type corresponds to IEEE-754 encoded numbers. A second type called ``RecFloating`` helps simplifying design by recoding
the floating point value.

It's composed of a sign bit, an exponent field and a mantissa field. The widths of the different fields are defined in the IEEE-754 or
de-facto standards.

This type can be used by using the following import

.. code-block:: scala

   import spinal.lib.experimental.math._

IEEE-754 floating format
~~~~~~~~~~~~~~~~~~~~~~~~

The numbers are encoded into IEEE-754 `floating point format <https://en.wikipedia.org/wiki/IEEE_floating_point>`_.

Recoded floating format
~~~~~~~~~~~~~~~~~~~~~~~

Since IEEE-754 has some quirks about denormalized numbers and special values, Berkeley proposed another way of recoding floating
point values.

The mantissa is modified so that denormalized values can be treated the same as the normalized ones.

The exponent field is one bit larger that one of the IEEE-754 number.

The sign bit is kept unchanged between the two
encodings.

Examples can be found `here <https://github.com/ucb-bar/berkeley-hardfloat/blob/master/README.md>`_

Zero
""""

The zero is encoded with the three leading zeros of the exponent field being stuck to zero.

Denormalized values
"""""""""""""""""""

Denormalized values are encoded in the same way as a normal floating point number. The mantissa is shifted so that the first one
becomes implicit. The exponent is encoded as 107 (decimal) plus the index of the highest bit set to 1.

Normalized values
"""""""""""""""""

The recoded mantissa for normalized values is exactly the same as the original IEEE-754 mantissa. The recoded exponent
is encoded as 130 (decimal) plus the original exponent value.

Infinity
""""""""

The recoded mantissa value is treated as don't care. The recoded exponent three highest bits is 6 (decimal), the rest of the exponent can be treated as don't care.

NaN
"""

The recoded mantissa for normalized values is exactly the same as the original IEEE-754 mantissa. The recoded exponent three highest bits is 7 (decimal), the rest of the exponent can be treated as don't care.

Declaration
^^^^^^^^^^^

The syntax to declare a floating point number is as follows:

IEEE-754 Number
~~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Syntax
     - Description
   * - Floating(exponentSize: Int, mantissaSize: Int)
     - IEEE-754 Floating point value with a custom exponent and mantissa size
   * - Floating16()
     - IEEE-754 Half floating point number
   * - Floating32()
     - IEEE-754 Single floating point number
   * - Floating64()
     - IEEE-754 Double floating point number
   * - Floating128()
     - IEEE-754 Quad floating point number


Recoded floating point number
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Syntax
     - Description
   * - RecFloating(exponentSize: Int, mantissaSize: Int)
     - Recoded Floating point value with a custom exponent and mantissa size
   * - RecFloating16()
     - Recoded Half floating point number
   * - RecFloating32()
     - Recoded Single floating point number
   * - RecFloating64()
     - Recoded Double floating point number
   * - RecFloating128()
     - Recoded Quad floating point number


Operators
^^^^^^^^^

The following operators are available for the ``Floating`` and ``RecFloating`` types

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
   * - x.asBools
     - Cast into a array of Bool
     - Vec(Bool,width(x))
   * - x.toUInt(size: Int)
     - Return the corresponding UInt (with truncation)
     - UInt
   * - x.toSInt(size: Int)
     - Return the corresponding SInt (with truncation)
     - SInt
   * - x.fromUInt
     - Return the corresponding Floating (with truncation)
     - UInt
   * - x.fromSInt
     - Return the corresponding Floating (with truncation)
     - SInt

