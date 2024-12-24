Rules
=====

The semantics behind SpinalHDL are important to learn, so that you understand what is really happening behind the scenes, and how to control it.

These semantics are defined by multiple rules:

* Signals and registers are operating concurrently with each other (parallel behavioral, as in VHDL and Verilog)
* An assignment to a combinational signal is like expressing a rule which is always true
* An assignment to a register is like expressing a rule which is applied on each cycle of its clock domain
* For each signal, the last valid assignment wins
* Each signal and register can be manipulated as an object during hardware elaboration in a `OOP <https://en.wikipedia.org/wiki/Object-oriented_programming>`_ manner

Concurrency
-----------

The order in which you assign each combinational or registered signal has no behavioral impact.

For example, both of the following pieces of code are equivalent:

.. code-block:: scala

   val a, b, c = UInt(8 bits) // Define 3 combinational signals
   c := a + b  // c will be set to 7
   b := 2      // b will be set to 2
   a := b + 3  // a will be set to 5

This is equivalent to:

.. code-block:: scala

   val a, b, c = UInt(8 bits) // Define 3 combinational signals
   b := 2      // b will be set to 2
   a := b + 3  // a will be set to 5
   c := a + b  // c will be set to 7

More generally, when you use the ``:=`` assignment operator, it's like specifying an additional new rule for the left side signal/register.

Last valid assignment wins
--------------------------

If a combinational signal or register is assigned multiple times through the
use of the SpinalHDL ``:=`` operator, the last assignment that may execute wins
(and so gets to set the value as a result for this state).

It could be said that top to bottom evaluation occurs based on the state that
exists at that time.  If your upstream signal inputs are driven from registers
and so have synchronous behavior, then it could be said that at each clock
cycle the assignments are re-evaluated based on the new state at the time.

Some reasons why an assignment statement may not get to execute in hardware this
clock cycle, maybe due to it being wrapped in a ``when(cond)`` clause.

Another reason maybe that the SpinalHDL code never made it through elaboration
because the feature was parameterized and disabled during HDL code-generation,
see ``paramIsFalse`` use below.

As an example:

.. code-block:: scala

   // Every clock cycle  evaluation starts here
   val paramIsFalse = false
   val x, y = Bool()           // Define two combinational signals
   val result = UInt(8 bits)   // Define a combinational signal

   result := 1
   when(x) {
     result := 2
     when(y) {
       result := 3
     }
   }
   if(paramIsFalse) {          // This assignment should win as it is last, but it was never elaborated
     result := 4               //  into hardware due to the use of if() and it evaluating to false at the time
   }                           //  of elaboration.  The three := assignments above are elaborated into hardware.

This will produce the following truth table:

.. list-table::
   :header-rows: 1

   * - x
     - y
     - =>
     - result
   * - False
     - False
     - 
     - 1
   * - False
     - True
     - 
     - 1
   * - True
     - False
     - 
     - 2
   * - True
     - True
     - 
     - 3


Signal and register interactions with Scala (OOP reference + Functions)
------------------------------------------------------------------------

In SpinalHDL, each hardware element is modeled by a class instance. This means you can manipulate instances by using their references, such as passing them as arguments to a function.

As an example, the following code implements a register which is incremented when ``inc`` is True and cleared when ``clear`` is True (``clear`` has priority over ``inc``) :

.. code-block:: scala

   val inc, clear = Bool()          // Define two combinational signals/wires
   val counter = Reg(UInt(8 bits))  // Define an 8 bit register

   when(inc) {
     counter := counter + 1
   }
   when(clear) {
     counter := 0    // If inc and clear are True, then this  assignment wins
   }                 //  (last value assignment wins rule)

You can implement exactly the same functionality by mixing the previous example with a function that assigns to ``counter``:

.. code-block:: scala

   val inc, clear = Bool()
   val counter = Reg(UInt(8 bits))

   def setCounter(value : UInt): Unit = {
     counter := value
   }

   when(inc) {
     setCounter(counter + 1)  // Set counter with counter + 1
   }
   when(clear) {
     counter := 0
   }

You can also integrate the conditional check inside the function:

.. code-block:: scala

   val inc, clear = Bool()
   val counter = Reg(UInt(8 bits))

   def setCounterWhen(cond : Bool,value : UInt): Unit = {
     when(cond) {
       counter := value
     }
   }

   setCounterWhen(cond = inc,   value = counter + 1)
   setCounterWhen(cond = clear, value = 0)

And also specify what should be assigned to the function:

.. code-block:: scala

   val inc, clear = Bool()
   val counter = Reg(UInt(8 bits))

   def setSomethingWhen(something : UInt, cond : Bool, value : UInt): Unit = {
     when(cond) {
       something := value
     }
   }

   setSomethingWhen(something = counter, cond = inc,   value = counter + 1)
   setSomethingWhen(something = counter, cond = clear, value = 0)

All of the previous examples are strictly equivalent both in their generated RTL and also in the SpinalHDL compiler's perspective.
This is because SpinalHDL only cares about the Scala runtime and the objects instantiated there, it doesn't care about the Scala syntax itself.

In other words, from a generated RTL generation / SpinalHDL perspective, when you use functions in Scala which generate hardware, it is like the function was inlined.
This is also true case for Scala loops, as they will appear in unrolled form in the generated RTL.
