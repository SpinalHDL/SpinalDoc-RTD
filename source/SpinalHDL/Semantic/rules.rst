
Rules
=====

Introduction
------------

The semantics behind SpinalHDL are important to learn so you understand what is really happening behind the scenes and how to control it.

These semantics are defined by multiple rules:


* Signals and registers are concurrent to each other (Parallel behavioral, as in VHDL and Verilog)
* An assignement to a combinatorial signal is like expressing a rule which is always true
* An assignement to a register is like expressing a rule which is applied on each cycle of its clock domain
* For each signal, the last valid assignement wins
* Each signal and register can be manipulated as an object during the hardware elaboration in a OOP manner

Concurrency
-----------

The order in which you assign each combinatorial or register signals as no behavioral impact.

For example, both of the following pieces of code are equivalent:

.. code-block:: scala

   val a, b, c = UInt(8 bits) // Define 3 combinatorial signals
   c := a + b   // c will be set to 7
   b := 2       // b will be set to 2
   a := b + 3   // a will be set to 5

Is equivalent to:

.. code-block:: scala

   val a, b, c = UInt(8 bits) // Define 3 combinatorial signals
   b := 2     // b will be set to 2
   a := b + 3 // a will be set to 5
   c := a + b // c will be set to 7

More generally, when you use the ``:=`` assignement operator, it's like specifying a new rule for the left side signal/register.

Last valid assignement wins
---------------------------

If a combinatorial signal or register is assigned multiple times, the last valid one wins.

As an example:

.. code-block:: scala

   val x, y = Bool             //Define two combinatorial signals
   val result = UInt(8 bits)   //Define a combinatorial signal

   result := 1
   when(x){
     result := 2
     when(y){
       result := 3
     }
   }

Will produce the following truth table:

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

In SpinalHDL, each hardware element is modeled by a class instance. This means you can manipulate them by using their referance, such as passing them as an argument to a function.

As an example, the following code implements a register which is incremented when ``inc`` is True and cleared when ``clear`` is True (clear has priority over inc) :

.. code-block:: scala

   val inc, clear = Bool            //Define two combinatorial signal/wire
   val counter = Reg(UInt(8 bits))  //Define a 8 bits register

   when(inc){
     counter := counter + 1
   }
   when(clear){
     counter := 0    //If inc and clear are True, then this  assignement wins (Last valid assignement rule)
   }

You can implement exactly the same functionality by mixing the previous example with a function that assignes to counter:

.. code-block:: scala

   val inc, clear = Bool
   val counter = Reg(UInt(8 bits))

   def setCounter(value : UInt): Unit = {
     counter := value
   }

   when(inc){
     setCounter(counter + 1)  // Set counter with counter + 1
   }
   when(clear){
     counter := 0
   }

You can also integrate the conditional check inside the function:

.. code-block:: scala

   val inc, clear = Bool
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

   val inc, clear = Bool
   val counter = Reg(UInt(8 bits))

   def setSomethingWhen(something : UInt,cond : Bool,value : UInt): Unit = {
     when(cond) {
       something := value
     }
   }

   setSomethingWhen(something = counter, cond = inc,   value = counter + 1)
   setSomethingWhen(something = counter, cond = clear, value = 0)

All previous examples are strictly equivalent in their generated RTL but also from an SpinalHDL compiler perspective. This is because SpinalHDL only cares about the Scala runtime, it doesn't care about the Scala syntax itself.

In other words, from a generated RTL generation / SpinalHDL perspective, when you use functions in Scala which generate hardware, it is like the function was inlined. This is also true case for Scala loops, as they will appear like they were unrolled in the generated RTL.
