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
   * - Vec.fill(size: Int)(type: Data)
     - Create a vector of ``size`` elements of type ``Data``
   * - Vec(x, y, ...)
     - | Create a vector where indexes point to the provided elements. 
       | Does not create new hardware signals.
       | This constructor supports mixed element width.


Examples
~~~~~~~~

.. code-block:: scala

   // Create a vector of 2 signed integers
   val myVecOfSInt = Vec.fill(2)(SInt(8 bits))
   myVecOfSInt(0) := 2                   // assignment to populate index 0
   myVecOfSInt(1) := myVecOfSInt(0) + 3  // assignment to populate index 1

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
   val vec2 = Vec.fill(2)(SInt(8 bits))
   val vec1 = Vec.fill(2)(SInt(8 bits))

   myBool := vec2 === vec1  // Compare all elements
   // is equivalent to:
   // myBool := vec2(0) === vec1(0) && vec2(1) === vec1(1)

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
   val vec1 = Vec.fill(2)(SInt(8 bits))

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
   val vec1 = Vec.fill(2)(SInt(8 bits))

   println(widthOf(vec1)) // 16


Lib helper functions
~~~~~~~~~~~~~~~~~~~~

.. note::
    You need to import ``import spinal.lib._`` to put these functions in scope.

.. list-table::
   :header-rows: 1
   :widths: 3 4 1

   * - Operator
     - Description
     - Return
   * - x.sCount(condition: T => Bool)
     - Count the number of occurrence matching a given condition in the Vec.
     - UInt
   * - x.sCount(value: T)
     - Count the number of occurrence of a value in the Vec.
     - UInt
   * - x.sExists(condition: T => Bool)
     - Check if there is a matching condition in the Vec.
     - Bool
   * - x.sContains(value: T)
     - Check if there is an element with a given value present in the Vec.
     - Bool
   * - x.sFindFirst(condition: T => Bool)
     - Find the first element matching the given condition in the Vec, return if any index was successfully found and the index of that element.
     - (Bool, UInt)
   * - x.reduceBalancedTree(op: (T, T) => T)
     - Balanced reduce function, to try to minimize the depth of the resulting circuit. ``op`` should be commutative and associative.
     - T
   * - x.shuffle(indexMapping: Int => Int)
     - Shuffle the Vec using a function that maps the old indexes to new ones.
     - Vec[T]

.. code-block:: scala

    import spinal.lib._

    // Create a vector with 4 unsigned integers
    val vec1 = Vec.fill(4)(UInt(8 bits))

    // ... the vector is actually assigned somewhere

    val c1: UInt = vec1.sCount(_ < 128) // how many values are lower than 128 in vec
    val c2: UInt = vec1.sCount(0) // how many values are equal to zero in vec

    val b1: Bool = vec1.sExists(_ > 250) // is there a element bigger than 250
    val b2: Bool = vec1.sContains(0) // is there a zero in vec

    val (u1Found, u1): (Bool, UInt) = vec1.sFindFirst(_ < 10) // get the index of the first element lower than 10
    val u2: UInt = vec1.reduceBalancedTree(_ + _) // sum all elements together


.. note::
    The sXXX prefix is used to disambiguate with respect to identically named Scala functions that accept a lambda function as argument.
