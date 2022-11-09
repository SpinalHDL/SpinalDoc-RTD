
When/Switch/Mux
===============

When
----

As in VHDL and Verilog, signals can be conditionally assigned when a specified condition is met:

.. code-block:: scala

   when(cond1) {
     // Execute when cond1 is true
   }.elsewhen(cond2) {
     // Execute when (not cond1) and cond2
   }.otherwise {
     // Execute when (not cond1) and (not cond2)
   }

.. warning::

     If the keyword ``otherwise`` is on the same line as the closing bracket ``}`` of the ``when`` condition, no dot is needed.

     .. code-block:: scala

            when(cond1) {
                // Execute when cond1 is true
            } otherwise {
                // Execute when (not cond1) and (not cond2)
            }

     But if ``.otherwise`` is on another line, a dot is **required**:

     .. code-block:: scala

            when(cond1) {
                // Execute when cond1 is true
            }
            .otherwise {
                // Execute when (not cond1) and (not cond2)
            }

Switch
------

As in VHDL and Verilog, signals can be conditionally assigned when a signal has a defined value:

.. code-block:: scala

   switch(x) {
     is(value1) {
       // Execute when x === value1
     }
     is(value2) {
       // Execute when x === value2
     }
     default {
       // Execute if none of precedent conditions met
     }
   }

``is`` clauses can be factorized by separating them with a comma ``is(value1, value2)``.

Example
^^^^^^^

.. code-block:: scala

  switch(aluop) {
    is(ALUOp.add) {
      immediate := instruction.immI.signExtend
    }
    is(ALUOp.slt) {
      immediate := instruction.immI.signExtend
    }
    is(ALUOp.sltu) {
      immediate := instruction.immI.signExtend
    }
    is(ALUOp.sll) {
      immediate := instruction.shamt
    }
    is(ALUOp.sra) {
      immediate := instruction.shamt
    }
  }

is equivalent to

.. code-block:: scala

  switch(aluop) {
    is(ALUOp.add, ALUOp.slt, ALUOp.sltu) {
        immediate := instruction.immI.signExtend
    }
    is(ALUOp.sll, ALUOp.sra) {
        immediate := instruction.shamt
    }
  }


Additional options
^^^^^^^^^^^^^^^^^^

Sometimes handling all cases can become unwieldy and error prone so SpinalHDL, by default, handles the uncovered cases with the last ``is`` block.
To explicitly declare and define a ``default`` block the option ``coverUnreachable`` can be passed to the switch

.. code-block:: scala

  switch(aluop, coverUnreachable = true) {
    is(ALUOp.add, ALUOp.slt, ALUOp.sltu) {
        immediate := instruction.immI.signExtend
    }
    is(ALUOp.sll, ALUOp.sra) {
        immediate := instruction.shamt
    }
    default{
        immediate := 0
    }
  }

If the used values of ``ALUOp`` are all available elements of the SpinalEnum type, then without the ``coverUnreachable = true`` option SpinalHDL would throw an UNREACHABLE STATEMENT error upon elaboration.
Without the ``default`` block the encoding for ``ALUOp.sll`` and ``ALUOp.sra`` are the default cases in the generated HDL.

When using defined constants to compare against within a ``switch`` block it can occasionally happen that duplicates within a ``is`` value occur. 
By default duplicates of ``is`` conditions are not ignored but identified as an error. 
To relax the strictness of the ``switch`` elaboration the ``strict = false`` can be passed (by default ``strict = true`` thus preventing duplicate ``is`` conditions).

.. code-block:: scala
  
  // OP_ADD and OP_SUB share the same code
  def OP_ADD = M"000"
  def OP_SUB = M"000"
  def OP_SLT = M"001"
  def OP_JMP = M"010"
  def OP_BRK = M"101"
  val foo = UInt(8 bits)
  val bar = UInt(8 bits)
  switch(io.instruction, strict = false) {
      // 
      is(OP_ADD, OP_SUB) {
          foo := 4
          bar := 2
      }
      is(OP_SLT) {
          foo := 2
          bar := 8
      }
      is(OP_JMP, OP_BRK) {
          foo := 2
          bar := 8
      }
      default {
          foo := 0
          bar := 0
      }
  }


Local declaration
-----------------

It is possible to define new signals inside a when/switch statement:

.. code-block:: scala

   val x, y = UInt(4 bits)
   val a, b = UInt(4 bits)

   when(cond) {
     val tmp = a + b
     x := tmp
     y := tmp + 1
   } otherwise {
     x := 0
     y := 0
   }

.. note::
   SpinalHDL checks that signals defined inside a scope are only assigned inside that scope.

Mux
---

If you just need a ``Mux`` with a ``Bool`` selection signal, there are two equivalent syntaxes:

.. list-table::
   :header-rows: 1
   :widths: 4 1 4

   * - Syntax
     - Return
     - Description
   * - Mux(cond, whenTrue, whenFalse)
     - T
     - Return ``whenTrue`` when ``cond`` is True, ``whenFalse`` otherwise
   * - cond ? whenTrue | whenFalse
     - T
     - Return ``whenTrue`` when ``cond`` is True, ``whenFalse`` otherwise

.. code-block:: scala

   val cond = Bool
   val whenTrue, whenFalse = UInt(8 bits)
   val muxOutput  = Mux(cond, whenTrue, whenFalse)
   val muxOutput2 = cond ? whenTrue | whenFalse

Bitwise selection
-----------------

A bitwise selection looks like the VHDL ``when`` syntax.

Example
^^^^^^^

.. code-block:: scala

   val bitwiseSelect = UInt(2 bits)
   val bitwiseResult = bitwiseSelect.mux(
     0 -> (io.src0 & io.src1),
     1 -> (io.src0 | io.src1),
     2 -> (io.src0 ^ io.src1),
     default -> (io.src0)
   )

``mux`` checks that all possible values are covered to prevent generation of latches.
If all possible values are covered, the default statement must not be added:

.. code-block:: scala

   val bitwiseSelect = UInt(2 bits)
   val bitwiseResult = bitwiseSelect.mux(
     0 -> (io.src0 & io.src1),
     1 -> (io.src0 | io.src1),
     2 -> (io.src0 ^ io.src1),
     3 -> (io.src0)
   )

``muxList(...)`` and ``muxListDc(...)`` are alternatives bitwise selectors that take a sequence of tuples or mappings as input.

``muxList`` can be used as a direct replacement for ``mux``, providing a easier to use interface in code that generates the cases.
It has the same checking behavior as ``mux`` does, requiring full coverage and prohibiting listing a default if it is not needed.

``muxtListDc`` can be used if the uncovered values are not important, they can be left unassigned by using ``muxListDc``.
This will add a default case if needed. This default case will generate X's during the simulation if ever encountered.
``muxListDc(...)`` is often a good alternative in generic code.

Below is an example of dividing a ``Bits`` of 128 bits into 32 bits:

.. image:: /asset/picture/MuxList.png
   :align: center
   :width: 300px

.. code-block:: scala

   val sel  = UInt(2 bits)
   val data = Bits(128 bits)

   // Dividing a wide Bits type into smaller chunks, using a mux:
   val dataWord = sel.muxList(for (index <- 0 until 4) yield (index, data(index*32+32-1 downto index*32)))

   // A shorter way to do the same thing:
   val dataWord = data.subdivideIn(32 bits)(sel)

Example for ``muxListDc`` selecting bits from a configurable width vector:

.. code-block:: scala

  case class Example(width: Int = 3) extends Component {
    // 2 bit wide for default width
    val sel = UInt(log2Up(count) bit)
    val data = Bits(width*8 bit)
    // no need to cover missing case 3 for default width
    val dataByte = sel.muxListDc(for(i <- 0 until count) yield (i, data(index*8, 8 bit)))
  }