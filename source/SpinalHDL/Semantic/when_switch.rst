
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

Also, if all possible values are covered in your mux, you can omit the default value:

.. code-block:: scala

   val bitwiseSelect = UInt(2 bits)
   val bitwiseResult = bitwiseSelect.mux(
     0 -> (io.src0 & io.src1),
     1 -> (io.src0 | io.src1),
     2 -> (io.src0 ^ io.src1),
     3 -> (io.src0)
   )

``muxLists(...)`` is another bitwise selection which takes a sequence of tuples as input. Below is an example of dividing a ``Bits`` of 128 bits into 32 bits:

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
