
Introduction
------------

The semantic behind SpinalHDL is an important point to understand what is realy happening behind the scene and how you can twist it.

This semantic is defined by multiples rules :


* Signals and register are concurrent to each other (Parallel behavioral, as in VHDL and Verilog)
* An assignement on a combinatorial signal is like expressing a rule which is always true
* An assignement on a register is like expressing a rule which applied on each cycle of its clock domain
* For each signal, the last valid assignement win
* Each signal and register can be manipulated as an object during the hardware elaboration in a OOP manner

Concurrency
-----------

The order in which you assign each combinatorial or register signals as no behavioral impact.

For example both following code are equivalents :

.. code-block:: scala

   val a, b, c = UInt(8 bits) // Define 3 combinatorial signals
   c := a + b   // c will be set to 7
   b := 2       // b will be set to 2
   a := b + 3   // a will be set to 5

Is equivalent to :

.. code-block:: scala

   val a, b, c = UInt(8 bits) // Define 3 combinatorial signals
   b := 2     // b will be set to 2
   a := b + 3 // a will be set to 5
   c := a + b // c will be set to 7

More generaly speaking, when you use the ``:=`` assignement operator, it's like specifying a new rule for the left side signal/register.

Last valid assignement win
--------------------------

If a combinatorial signal or register is assigned multiple time, the last valid one win.

As example :

.. code-block:: scala

   val x, y = Bool             //Define two combinatorial signal
   val result = UInt(8 bits)   //Define one combinatorial signal

   result := 1
   when(x){
     result := 2
     when(y){
       result := 3
     }
   }

Will produce the following truth table on result:

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


Signals and register interactions with Scala (OOP reference + Functions)
------------------------------------------------------------------------

In SpinalHDL, each hardware elements is modelised by an class instance. Which mean you can manipulate each of them by using their referance, give them as argument of an function and then work on them (read/write).

As reference example, the following code implement an register which is incremented when ``inc`` is True and cleared when ``clear`` is True (clear has the priority over inc) :

.. code-block:: scala

   val inc, clear = Bool            //Define two combinatorial signal/wire
   val counter = Reg(UInt(8 bits))  //Define a 8 bits register

   when(inc){
     counter := counter + 1
   }
   when(clear){
     counter := 0    //If inc and clear are True, then this  assignement win (Last valid assignement rule)
   }

You can do exactly the same functionality by mixing the precedent example with a function to assign the counter :

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

You can also integrate the conditional check inside the function :

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

And also specify to the function, who should be assigned :

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

All precedent examples are strictly equivalent in their generated RTL but also from an SpinalHDL compiler perspective. It is the case, because SpinalHDL only care about the Scala runtime, it doesn't care about the Scala syntax itself.

In other word, from an generated RTL generation / SpinalHDL perspective, when you use function in Scala which generate hardware, it is like if that function where inlined. It is also the case for Scala loop, they will appear as unrolled in the generated RTL.
