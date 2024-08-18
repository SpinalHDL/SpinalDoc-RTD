.. _Bool:

Bool
====

Description
^^^^^^^^^^^

The ``Bool`` type corresponds to a boolean value (True or False) or a single bit/wire
used in a hardware design.  While named similarly it should not be confused with
Scala `Boolean` type which does not describe hardware but truth values in the Scala
generator code.

An important concept and rule-of-thumb to understand is that the Scala
`Boolean` type is used in places where elaboration-time HDL code-generation
decision making is occurring in Scala code.  Like any regular program it affects
execution of the Scala program that is SpinalHDL at the time the program is
being run to perform HDL code generation.

Therefore the value of a Scala `Boolean` can not be observed from hardware,
because it only exists ahead-of-time in the SpinalHDL program at the time of
HDL code-gen.

In scenarios where you might need this for your design, for example to pass a
value (that maybe acting as a parameterized constant input) from Scala into your
hardware design, you can type convert it to Bool with the constructor `Bool(value: Boolean)`.

Similarly the value of a SpinalHDL `Bool` can not be seen at code-generation, all
that can be seen and manipulated is the HDL construct concerning a `wire` and how it
is routed (through modules/Components), driven (sourced) and connected (sunk).

The signal direction of assignment operators `:=` is managed by SpinalHDL.
The use of the Bool instance on the left-hand-side or the right-hand-side of the
assignment operator `:=` dictates if it is a source (provides state) or sink
(captures state) for a given assignment.

Multiple uses of the assignment operator are allowed, such that it is normal
for a signal wire to act as a source (provides a value to drive HDL state) to be
able to connect and drive multiple inputs of other HDL constructs.  When a Bool
instance used as a source the order the assignment statements appear or are
executed in Scala does not matter, unlike when it is used as a sink
(captures state).

When multiple assignment operators drive the Bool (the Bool is on the
left-hand-side of the assignment expression), the last assignment
statement wins rule; take effect.  The last would be the last to execute in
Scala code.  This matter can affect the layout and ordering of your SpinalHDL
Scala code to ensure the correct precedence order is archived in the hardware
design for assigning a new state to the Bool in hardware.

It may help to understand the concept with relating the Scala/SpinalHDL
`Bool` instance as a reference to a HDL `net` in the net-list.  Which the
assignment `:=` operator is attaching HDL constructs into the same net.


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
     - Create a Bool assigned with a value from a Scala Boolean type (true, false).
       This explicitly converts to ``True`` or ``False``.
     - Bool


.. code-block:: scala

   val myBool_1 = Bool()        // Create a Bool
   myBool_1 := False            // := is the assignment operator (like verilog <=)

   val myBool_2 = False         // Equivalent to the code above 

   val myBool_3 = Bool(5 > 12)  // Use a Scala Boolean to create a Bool

Operators
^^^^^^^^^

The following operators are available for the ``Bool`` type:

.. note:

   Both sides of logic expressions ``x`` and ``y`` need to be of type Bool.

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
   * - ~x
     - Logical NOT
     - Bool
   * - x.set[()]
     - Set x to True
     - Unit (none)
   * - x.clear[()]
     - Set x to False
     - Unit (none)
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
     d.set()                // equivalent to d := True
   }

   val e = False
   e.setWhen(cond)          // equivalent to when(cond) { d := True }

   val f = RegInit(False) fallWhen(ack) setWhen(req)
   /** equivalent to
    * when(f && ack) { f := False }
    * when(req) { f := True }
    * or
    * f := req || (f && !ack)
    */

  // mind the order of assignments!  last one wins
  val g = RegInit(False) setWhen(req) fallWhen(ack)
  // equivalent to g := ((!g) && req) || (g && !ack)

Edge detection
~~~~~~~~~~~~~~

All edge detection functions will instantiate an additional register via :ref:`RegNext <regnext>`
to get a delayed value of the ``Bool`` in question.

This feature does not reconfigure a D-type Flip-Flop to use an alternative CLK
source, it uses two D-type Flip-Flop in series chain (with both CLK pins inheriting
the default ClockDomain).  It has combinational logic to perform edge detection
based on the output Q states.

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
   * - x.toggle[()]
     - Return True at every edge
     - Bool


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
     - Bits(1 bit)
   * - x.asUInt
     - Binary cast to UInt
     - UInt(1 bit)
   * - x.asSInt
     - Binary cast to SInt
     - SInt(1 bit)
   * - x.asUInt(bitCount)
     - Binary cast to UInt and resize, putting Bool value in LSB and padding
       with zeros.
     - UInt(bitCount bits)
   * - x.asBits(bitCount)
     - Binary cast to Bits and resize, putting Bool value in LSB and padding
       with zeros.
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
   * - x #* n
     - Repeat x n-times
     - Bits(n bits)


.. code-block:: scala

   val a, b, c = Bool()

   // Concatenation of three Bool into a single Bits(3 bits) type
   val myBits = a ## b ## c


MaskedBoolean
~~~~~~~~~~~~~

A masked boolean allows don’t care values. They are usually not used on their own but through :ref:`MaskedLiteral <maskedliteral>`.

.. code-block:: scala

  // first argument: Scala Boolean value
  // second argument: do we care ? expressed as a Scala Boolean
  val masked = new MaskedBoolean(true, false)
