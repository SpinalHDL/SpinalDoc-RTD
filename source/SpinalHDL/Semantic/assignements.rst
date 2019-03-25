.. role:: raw-html-m2r(raw)
   :format: html

Assignments
===========

Assignments
-----------

There are multiple assignment operators:

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Symbole
     - Description
   * - :=
     - | Standard assignment, equivalent to '<=' in VHDL/Verilog :
       | the last assignment to a variable wins, the value is not updated until the next simulation delta cycle
   * - \\=
     - | Equivalent to := in VHDL and = in Verilog
       | the value is updated instantly
   * - <>
     - Automatic connection between 2 signals or two bundles of the same type. Direction is inferred by using signal direction (in/out). (Similar behavioral to ``:=``\ )


.. code-block:: scala

   //Because of hardware concurrency, `a` is always read as '1' by b and c
   val a,b,c = UInt(4 bits)
   a := 0
   b := a
   a := 1  //a := 1 win
   c := a  

   var x = UInt(4 bits)
   val y,z = UInt(4 bits)
   x := 0
   y := x      //y read x with the value 0
   x \= x + 1
   z := x      //z read x with the value 1

   // Automatic connection between two UART interfaces.
   uartCtrl.io.uart <> io.uart

It is important to understand that in SpinalHDL, the nature of a signal (combinatorial/sequential) is defined in its declaration, not by the way it is assigned. All datatype instances will define a cominatorial signal, while a datatype instance wrapped with a Reg(...) will define a sequential (register) signal.

.. code-block:: scala

   val a = UInt(4 bits) //Define a combinatorial signal
   val b = Reg(UInt(4 bits)) //Define a registered signal
   val c = Reg(UInt(4 bits)) init(0) //Define a registered signal which is set to 0 when a reset occurs

Width checking
--------------

SpinalHDL checks that the bit count of the left side and the right side of an assigmnet matches. There are multiple ways to adapt the width of a given BitVector (Bits, UInt, SInt):

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - Resizing ways
     - Description
   * - x := y.resized
     - Assign x wit a resized copy of y, resize value is automatically inferred to match x
   * - x := y.resize(newWidth)
     - Assign x with a resized copy of y, size is manually calculated


There is one case where Spinal automatically resizes a value:

.. list-table::
   :header-rows: 1
   :widths: 7 10 10

   * - Assignement
     - Problem
     - SpinalHDL action
   * - myUIntOf_8bit := U(3)
     - U(3) creates an UInt of 2 bits, which doesn't match the left side (8 bits)
     - Because  U(3) is a "weak" bit count inferred signal, SpinalHDL resizes it automatically


Combinatorial loops
-------------------

SpinalHDL checks that there are no combinatorial loops (latches) in your design. If one is detected, it raises an error and SpinalHDL will print the path of the loop.
