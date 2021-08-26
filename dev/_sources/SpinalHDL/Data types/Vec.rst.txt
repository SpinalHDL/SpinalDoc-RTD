.. role:: raw-html-m2r(raw)
   :format: html

.. _Vec:

Vec
===

Description
^^^^^^^^^^^

A ``Vec`` is a composite type that defines a group of indexed signals (of any SpinalHDL basic type) under a single name.

Declaration
^^^^^^^^^^^

The syntax to declare a vector is as follows:

.. list-table::
   :header-rows: 1
   :widths: 1 2

   * - Declaration
     - Description
   * - Vec(type: Data, size: Int)
     - Create a vector capable of holding ``size`` elements of type ``Data``
   * - Vec(x, y, ...)
     - | Create a vector where indexes point to the provided elements.
       | This constructor supports mixed element width.


Examples
~~~~~~~~

.. code-block:: scala

   // Create a vector of 2 signed integers
   val myVecOfSInt = Vec(SInt(8 bits), 2)
   myVecOfSInt(0) := 2
   myVecOfSInt(1) := myVecOfSInt(0) + 3

   // Create a vector of 3 different type elements
   val myVecOfMixedUInt = Vec(UInt(3 bits), UInt(5 bits), UInt(8 bits))

   val x, y, z = UInt(8 bits)
   val myVecOf_xyz_ref = Vec(x, y, z)

   // Iterate on a vector
   for(element <- myVecOf_xyz_ref) {
     element := 0   // Assign x, y, z with the value 0
   }

   // Map on vector
   myVecOfMixedUInt.map(_ := 0) // Assign all elements with value 0

   // Assign 3 to the first element of the vector
   myVecOf_xyz_ref(1) := 3

Operators
^^^^^^^^^

The following operators are available for the ``Vec`` type:

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

   // Create a vector of 2 signed integers
   val vec2 = Vec(SInt(8 bits), 2)
   val vec1 = Vec(SInt(8 bits), 2)

   myBool := vec2 === vec1  // Compare all elements

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


.. code-block:: scala

   // Create a vector of 2 signed integers
   val vec1 = Vec(SInt(8 bits), 2)

   myBits_16bits := vec1.asBits

Misc
~~~~

.. list-table::
   :header-rows: 1
   :widths: 2 5 1

   * - Operator
     - Description
     - Return
   * - x.getBitsWidth
     - Return the full size of the Vec
     - Int


.. code-block:: scala

   // Create a vector of 2 signed integers
   val vec1 = Vec(SInt(8 bits), 2)

   println(vec1.getBitsWidth) // 16
