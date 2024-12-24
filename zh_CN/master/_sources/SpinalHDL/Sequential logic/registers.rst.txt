.. _Reg:

Registers
=========

Creating registers in SpinalHDL is very different than in VHDL or Verilog.

In Spinal, there are no process/always blocks. Registers are explicitly defined at declaration.
This difference from traditional event-driven HDL has a big impact:

* You can assign registers and wires in the same scope, meaning the code doesn't need to be split between process/always blocks
* It make things much more flexible (see :ref:`Functions <function>`)

Clocks and resets are handled separately, see the :ref:`Clock domain <clock_domain>` chapter for details.

Instantiation
-------------

There are 4 ways to instantiate a register:

.. list-table::
   :header-rows: 1
   :widths: 50 55

   * - Syntax
     - Description
   * - ``Reg(type : Data)``
     - Register of the given type
   * - ``RegInit(resetValue : Data)``
     - Register loaded with the given ``resetValue`` when a reset occurs
   * - ``RegNext(nextValue : Data)``
     - Register that samples the given ``nextValue`` each cycle
   * - ``RegNextWhen(nextValue : Data, cond : Bool)``
     - Register that samples the given ``nextValue`` when a condition occurs

Here is an example declaring some registers:

.. code-block:: scala

   // UInt register of 4 bits
   val reg1 = Reg(UInt(4 bits))

   // Register that updates itself every cycle with a sample of reg1 incremented by 1
   val reg2 = RegNext(reg1 + 1)

   // UInt register of 4 bits initialized with 0 when the reset occurs
   val reg3 = RegInit(U"0000")
   reg3 := reg2
   when(reg2 === 5) {
     reg3 := 0xF
   }

   // Register that samples reg3 when cond is True
   val reg4 = RegNextWhen(reg3, cond)

The code above will infer the following logic:

.. image:: /asset/picture/register.svg
   :align: center

.. note::
   The ``reg3`` example above shows how you can assign the value of a ``RegInit`` register.
   It's possible to use the same syntax to assign to the other register types as well (``Reg``, ``RegNext``, ``RegNextWhen``).
   Just like in combinational assignments, the rule is 'Last assignment wins', but if no assignment is done, the register keeps its value.
   If the Reg is declared in a design and does not have suitable assignment and consumption it is likely to be pruned (removed from design) at some point by EDA flows after being deemed unnecessary.


.. _RegNext:

Also, ``RegNext`` is an abstraction which is built over the ``Reg`` syntax. The two following sequences of code are strictly equivalent:

.. code-block:: scala

   // Standard way
   val something = Bool()
   val value = Reg(Bool())
   value := something

   // Short way
   val something = Bool()
   val value = RegNext(something)


It is possible to have multiple options at the same time in other ways and so
slightly more advanced compositions built on top of the basic understand of
the above:

.. code-block:: scala

   // UInt register of 6 bits (initialized with 42 when the reset occurs)
   val reg1 = Reg(UInt(6 bits)) init(42)

   // Register that samples reg1 each cycle (initialized with 0 when the reset occurs)
   // using Scala named parameter argument format
   val reg2 = RegNext(reg1, init=0)

   // Register that has multiple features combined

   // My register enable signal
   val reg3Enable = Bool()
   // UInt register of 6 bits (inferred from reg1 type)
   //   assignment preconfigured to update from reg1
   //   only updated when reg3Enable is set
   //   initialized with 99 when the reset occurs
   val reg3 = RegNextWhen(reg1, reg3Enable, U(99))
   // when(reg3Enable) {
   //   reg3 := reg1; // this expression is implied in the constructor use case
   // }

   when(cond2) {      // this is a valid assignment, will take priority when executed
      reg3 := U(0)    //  (due to last assignment wins rule), assignment does not require
   }                  //  reg3Enable condition, you would use `when(cond2 & reg3Enable)` for that

   // UInt register of 8 bits, initialized with 99 when the reset occurs
   val reg4 = Reg(UInt(8 bits), U(99))
   // My register enable signal
   val reg4Enable = Bool()
   // no implied assignments exist, you must use enable explicitly as necessary
   when(reg4Enable) {
      reg4 := newValue
   }


Reset value
-----------

In addition to the ``RegInit(value : Data)`` syntax which directly creates the register with a reset value,
you can also set the reset value by calling the ``init(value : Data)`` function on the register.

.. code-block:: scala

   // UInt register of 4 bits initialized with 0 when the reset occurs
   val reg1 = Reg(UInt(4 bits)) init(0)

If you have a register containing a Bundle, you can use the ``init`` function on each element of the Bundle.

.. code-block:: scala

   case class ValidRGB() extends Bundle {
     val valid   = Bool()
     val r, g, b = UInt(8 bits)
   }

   val reg = Reg(ValidRGB())
   reg.valid init(False)  // Only the valid if that register bundle will have a reset value.

Initialization value for simulation purposes
--------------------------------------------

For registers that don't need a reset value in RTL, but need an initialization value for simulation (to avoid x-propagation), you can ask for a random initialization value by calling the ``randBoot()`` function.

.. code-block:: scala

   // UInt register of 4 bits initialized with a random value
   val reg1 = Reg(UInt(4 bits)) randBoot()

Register vectors
----------------

As for wires, it is possible to define a vector of registers with ``Vec``.

.. code-block:: scala
   
   val vecReg1 = Vec(Reg(UInt(8 bits)), 4)
   val vecReg2 = Vec.fill(8)(Reg(Bool()))

Initialization can be done with the ``init`` method as usual, which can be combined with the ``foreach`` iteration on the registers.

.. code-block:: scala

   val vecReg1 = Vec(Reg(UInt(8 bits)) init(0), 4)
   val vecReg2 = Vec.fill(8)(Reg(Bool()))
   vecReg2.foreach(_ init(False))

In case where the initialization must be deferred since the init value is not known, use a function as in the example below.

.. code-block:: scala

   case class ShiftRegister[T <: Data](dataType: HardType[T], depth: Int, initFunc: T => Unit) extends Component {
      val io = new Bundle {
         val input  = in (dataType())
         val output = out(dataType())
      }

      val regs = Vec.fill(depth)(Reg(dataType()))
      regs.foreach(initFunc)

      for (i <- 1 to (depth-1)) {
            regs(i) := regs(i-1)
      }

      regs(0) := io.input
      io.output := regs(depth-1)
   }

   object SRConsumer {
      def initIdleFlow[T <: Data](flow: Flow[T]): Unit = {
         flow.valid init(False)
      }
   }

   class SRConsumer() extends Component {
      // ...
      val sr = ShiftRegister(Flow(UInt(8 bits)), 4, SRConsumer.initIdleFlow[UInt])
   }

Transforming a wire into a register
-----------------------------------

Sometimes it is useful to transform an existing wire into a register. For
instance, when you are using a Bundle, if you want some outputs of the bundle to
be registers, you might prefer to write ``io.myBundle.PORT := newValue`` without
declaring registers with ``val PORT = Reg(...)`` and connecting their output to
the port with ``io.myBundle.PORT := PORT``. To do this, you just need to use
``.setAsReg()`` on the ports you want to control as registers:

.. code-block:: scala

   val io = new Bundle {
      val apb = master(Apb3(apb3Config))
   }

   io.apb.PADDR.setAsReg()
   io.apb.PWRITE.setAsReg() init(False)

   when(someCondition) {
      io.apb.PWRITE := True
   }

Notice in the code above that you can also specify an initialization value.

.. note::

   The register is created in the clock domain of the wire, and does not depend
   on the place where ``.setAsReg()`` is used.

   In the example above, the wire is defined in the ``io`` Bundle, in the same
   clock domain as the component. Even if ``io.apb.PADDR.setAsReg()`` was
   written in a ``ClockingArea`` with a different clock domain, the register
   would use the clock domain of the component and not the one of the
   ``ClockingArea``.
