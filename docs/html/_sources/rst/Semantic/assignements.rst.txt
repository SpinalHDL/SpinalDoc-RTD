.. role:: raw-html-m2r(raw)
   :format: html

Assignments
===========

Assignments
-----------

There are multiple assignment operator :

.. list-table::
   :header-rows: 1

   * - Symbole
     - Description
   * - :=
     - | Standard assignment, equivalent to '<=' in VHDL/Verilog :
       | last assignment win, value updated one the next simulation delta cycle
   * - \=
     - | Equivalent to := in VHDL and = in Verilog
       | value updated instantly
   * - <>
     - Automatic connection between 2 signals or two bundles of the same type. Direction is inferred by using signal direction (in/out). (Similar behavioral than ``:=``\ )


.. code-block:: scala

   //Because of hardware concurrency, `a` is always read with the value '1' by b and c
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

It is important to understand that in SpinalHDL, the nature of a signal (combinatorial/sequancial) is defined in its declaration and not by the way it is assigned. All datatype instances will define a cominatorial signal, while a datatype instance wrapped into a Reg(...) sementic will define a sequancial (register) signal.

.. code-block:: scala

   val a = UInt(4 bits) //Define a combinatorial signal
   val b = Reg(UInt(4 bits)) //Define a registred signal
   val c = Reg(UInt(4 bits)) init(0) //Define a registred signal which is set to 0 when a reset occure

Width checking
--------------

SpinalHDL checks that bit count of left and right assignment side match. There is multiple ways to adapt the width of a given BitVector (Bits, UInt, SInt) :

.. list-table::
   :header-rows: 1

   * - Resizing ways
     - Description
   * - x := y.resized
     - Assign x wit a resized copy of y, resize value is automatically inferred to match x
   * - x := y.resize(newWidth)
     - Assign x with a resized copy of y, size is manually calculated


There are one cases where spinal automatically resize things :

.. list-table::
   :header-rows: 1

   * - Assignement
     - Problem
     - SpinalHDL action
   * - myUIntOf_8bit := U(3)
     - U(3) create an UInt of 2 bits, which don't match with left side
     - Because  U(3) is a "weak" bits count inferred signal, SpinalHDL resizes it automatically


Combinatorial loops
-------------------

SpinalHDL check that there is no combinatorial loops (latch) in your design. If one is detected, it rises an error and SpinalHDL will print you the path of the loop.
