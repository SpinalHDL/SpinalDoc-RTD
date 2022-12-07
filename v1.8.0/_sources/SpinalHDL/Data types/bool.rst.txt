.. role:: raw-html-m2r(raw)
   :format: html

.. _Bool:

Bool
====

Description
^^^^^^^^^^^

The ``Bool`` type corresponds to a boolean value (True or False).

Declaration
^^^^^^^^^^^

The syntax to declare a boolean value is as follows: (everything between [] is optional)

.. list-table::
   :header-rows: 1
   :widths: 1 3 1

   * - Syntax
     - Description
     - Return
   * - Bool()
     - Create a Bool
     - Bool
   * - True
     - Create a Bool assigned with ``true``
     - Bool
   * - False
     - Create a Bool assigned with ``false``
     - Bool
   * - Bool(value: Boolean)
     - Create a Bool assigned with a Scala Boolean(true, false)
     - Bool


.. code-block:: scala

   val myBool_1 = Bool()          // Create a Bool
   myBool_1 := False            // := is the assignment operator

   val myBool_2 = False         // Equivalent to the code above 

   val myBool_3 = Bool(5 > 12)  // Use a Scala Boolean to create a Bool

Operators
^^^^^^^^^

The following operators are available for the ``Bool`` type:

Logic
~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return type
   * - !x
     - Logical NOT
     - Bool
   * - | x && y
       | x & y
     - Logical AND
     - Bool
   * - | x || y
       | x | y
     - Logical OR
     - Bool
   * - x ^ y
     - Logical XOR
     - Bool
   * - x.set[()]
     - Set x to True
     - 
   * - x.clear[()]
     - Set x to False
     - 
   * - x.setWhen(cond)
     - Set x when cond is True
     - Bool
   * - x.clearWhen(cond)
     - Clear x when cond is True
     - Bool
   * - x.riseWhen(cond)
     - Set x when x is False and cond is True
     - Bool
   * - x.fallWhen(cond)
     - Clear x when x is True and cond is True
     - Bool


.. code-block:: scala

   val a, b, c = Bool()
   val res = (!a & b) ^ c   // ((NOT a) AND b) XOR c

   val d = False
   when(cond) {
     d.set()    // equivalent to d := True
   }

   val e = False
   e.setWhen(cond) // equivalent to when(cond) { d := True }

   val f = RegInit(False) fallWhen(ack) setWhen(req)
   /** equivalent to
    * when(f && ack) { f := False }
    * when(req) { f := True }
    * or
    * f := req || (f && !ack)
    */

  // mind the order of assignments!
  val g = RegInit(False) setWhen(req) fallWhen(ack)
  // equivalent to g := ((!g) && req) || (g && !ack)

Edge detection
~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1
   :widths: 2 5 1

   * - Operator
     - Description
     - Return type
   * - x.edge[()]
     - Return True when x changes state
     - Bool
   * - x.edge(initAt: Bool)
     - Same as x.edge but with a reset value
     - Bool
   * - x.rise[()]
     - Return True when x was low at the last cycle and is now high
     - Bool
   * - x.rise(initAt: Bool)
     - Same as x.rise but with a reset value
     - Bool
   * - x.fall[()]
     - Return True when x was high at the last cycle and is now low
     - Bool
   * - x.fall(initAt: Bool)
     - Same as x.fall but with a reset value
     - Bool
   * - x.edges[()]
     - Return a bundle (rise, fall, toggle)
     - BoolEdges
   * - x.edges(initAt: Bool)
     - Same as x.edges but with a reset value
     - BoolEdges


.. code-block:: scala

   when(myBool_1.rise(False)) {
       // do something when a rising edge is detected 
   } 


   val edgeBundle = myBool_2.edges(False)
   when(edgeBundle.rise) {
       // do something when a rising edge is detected
   }
   when(edgeBundle.fall) {
       // do something when a falling edge is detected
   }
   when(edgeBundle.toggle) {
       // do something at each edge
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

   when(myBool) { // Equivalent to when(myBool === True)
       // do something when myBool is True
   }

   when(!myBool) { // Equivalent to when(myBool === False)
       // do something when myBool is False
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
   * - x.asUInt(bitCount)
     - Binary cast to UInt and resize
     - UInt(bitCount bits)
   * - x.asBits(bitCount)
     - Binary cast to Bits and resize
     - Bits(bitCount bits)


.. code-block:: scala

   // Add the carry to an SInt value
   val carry = Bool()
   val res = mySInt + carry.asSInt

Misc
~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x ## y
     - Concatenate, x->high, y->low
     - Bits(w(x) + w(y) bits)


.. code-block:: scala

   val a, b, c = Bool()

   // Concatenation of three Bool into a Bits
   val myBits = a ## b ## c

MaskedBoolean
~~~~~~~~~~~~~

A masked boolean allows don’t care values. They are usually not used on their own but through :ref:`MaskedLiteral <maskedliteral>`.

.. code-block:: scala

  // first argument: boolean value
  // second argument: do we care ?
  val masked = new MaskedBoolean(true, false)
