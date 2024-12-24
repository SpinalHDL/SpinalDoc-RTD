When/Switch/Mux
===============

When
----

As in VHDL and Verilog, signals can be conditionally assigned when a specified condition is met:

.. code-block:: scala

   when(cond1) {
     // Execute when cond1 is true
   } elsewhen(cond2) {
     // Execute when (not cond1) and cond2
   } otherwise {
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


WhenBuilder
-----------

Sometimes we need to generate some parameters for the when condition, 
and the original structure of when else is not very suitable. 
Therefore, we provide a 'whenBuilder' method to achieve this goal

.. code-block:: scala

    import spinal.lib._

    val conds = Bits(8 bits)
    val result = UInt(8 bits)

    val ctx = WhenBuilder()
    ctx.when(conds(0)) {
      result := 0
    }
    ctx.when(conds(1)) {
      result := 1
    }
    if(true) {
      ctx.when(conds(2)) {
        result := 2
      }
    }
    ctx.when(conds(3)) {
      result := 3
    }

Compared to the when/elsewhen/otherwise approach, it might be more convenient for parameterization. 
we can also use like this

.. code-block:: scala

    for(i <- 5 to 7) ctx.when(conds(i)) {
      result := i
    }

    ctx.otherwise {
      result := 255
    }

    switch(addr) {
      for (i <- addressElements ) {
        is(i) {
          rdata :=  buffer(i)
        }
      }
    }

This way, we can parameterize priority circuits similar to how we use 'foreach' inside 'switch()', and generate code in a more intuitive if-else format.


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

``is`` clauses can be factorized (logical OR) by separating them with a comma ``is(value1, value2)``.

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

By default, SpinalHDL will generate an "UNREACHABLE DEFAULT STATEMENT" error if a ``switch`` contains a ``default`` statement while all the possible logical values of the ``switch`` are already covered by the ``is`` statements. You can drop this error reporting by specifying `` switch(myValue, coverUnreachable = true) { ... }``.

.. code-block:: scala
  
  switch(my2Bits, coverUnreachable = true) {
      is(0) { ... }
      is(1) { ... } 
      is(2) { ... }
      is(3) { ... }
      default { ... } // This will parse and validate without error now
  }
  
.. note::

   This check is done on the logical values, not on the physical values. For instance, if you have a SpinalEnum(A,B,C) encoded in a one-hot manner, SpinalHDL will only care about the A,B,C values ("001" "010" "100"). Physical values as "000" "011" "101" "110" "111" will not be taken in account.


By default, SpinalHDL will generate a "DUPLICATED ELEMENTS IN SWITCH IS(...) STATEMENT" error if a given ``is`` statement provides multiple times the same value. For instance ``is(42,42) { ... }`` 
You can drop this error reporting by specifying ``switch(myValue, strict = true){ ... }``. SpinalHDL will then take care of removing duplicated values.

.. code-block:: scala
  
  switch(value, strict = false) {
      is(0) { ... }
      is(1,1,1,1,1) { ... } // This will be okay
      is(2) { ... }
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

   val cond = Bool()
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
   val dataWord = sel.muxList(for (index <- 0 until 4)
                              yield (index, data(index*32+32-1 downto index*32)))

   // A shorter way to do the same thing:
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
