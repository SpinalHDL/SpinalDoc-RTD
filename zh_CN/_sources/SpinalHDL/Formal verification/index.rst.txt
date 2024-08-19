===================
Formal verification
===================


General
-------

SpinalHDL allows to generate a subset of the SystemVerilog Assertions (SVA). Mostly assert, assume, cover and a few others. 

In addition it provide a formal verification backend which allows to directly run the formal verification in the open-source Symbi-Yosys toolchain.



Formal backend
--------------

You can run the formal verification of a component via: 


.. code-block:: scala

    import spinal.core.formal._
    FormalConfig.withBMC(15).doVerify(new Component {
        // Toplevel to verify
    })

Currently, 3 modes are supported : 

- withBMC(depth)
- withProve(depth)
- withCover(depth)

Installing requirements
-----------------------

To install the Symbi-Yosys, you have a few options. You can fetch a precompiled package at: 

- https://github.com/YosysHQ/oss-cad-suite-build/releases
- https://github.com/YosysHQ/fpga-toolchain/releases (EOL - superseded by oss-cad-suite)

Or you can compile things from scratch : 

- https://symbiyosys.readthedocs.io/en/latest/install.html


Example
-------


External assertions
^^^^^^^^^^^^^^^^^^^

Here is an example of a simple counter and the corresponding formal testbench.


.. code-block:: scala

    import spinal.core._
    
    // Here is our DUT
    class LimitedCounter extends Component {
      // The value register will always be between [2:10]
      val value = Reg(UInt(4 bits)) init(2)
      when(value < 10) {
        value := value + 1
      }
    }

    object LimitedCounterFormal extends App {
      // import utilities to run the formal verification, but also some utilities to describe formal stuff
      import spinal.core.formal._

      // Here we run a formal verification which will explore the state space up to 15 cycles to find an assertion failure
      FormalConfig.withBMC(15).doVerify(new Component {
        // Instantiate our LimitedCounter DUT as a FormalDut, which ensure that all the outputs of the dut are:
        // - directly and indirectly driven (no latch / no floating wire)
        // - allows the current toplevel to read every signal across the hierarchy
        val dut = FormalDut(new LimitedCounter())

        // Ensure that the state space start with a proper reset
        assumeInitial(ClockDomain.current.isResetActive)

        // Check a few things
        assert(dut.value >= 2)
        assert(dut.value <= 10)
      })
    }
    
Internal assertions
^^^^^^^^^^^^^^^^^^^
    
If you want you can embed formal statements directly into the DUT: 

.. code-block:: scala

    class LimitedCounterEmbedded extends Component {
      val value = Reg(UInt(4 bits)) init(2)
      when(value < 10) {
        value := value + 1
      }

      // That code block will not be in the SpinalVerilog netlist by default. (would need to enable SpinalConfig().includeFormal. ...
      GenerationFlags.formal {
        assert(value >= 2)
        assert(value <= 10)
      }
    }

    object LimitedCounterEmbeddedFormal extends App {
      import spinal.core.formal._

      FormalConfig.withBMC(15).doVerify(new Component {
        val dut = FormalDut(new LimitedCounterEmbedded())
        assumeInitial(ClockDomain.current.isResetActive)
      })
    }    

External stimulus
^^^^^^^^^^^^^^^^^

If your DUT has inputs, you need to drive them from the testbench. You can use all the regular hardware statements to do it, 
but you can also use the formal `anyseq`, `anyconst`, `allseq`, `allconst` statement:

.. code-block:: scala

    class LimitedCounterInc extends Component {
      // Only increment the value when the inc input is set
      val inc = in Bool()
      val value = Reg(UInt(4 bits)) init(2)
      when(inc && value < 10) {
        value := value + 1
      }
    }

    object LimitedCounterIncFormal extends App {
      import spinal.core.formal._

      FormalConfig.withBMC(15).doVerify(new Component {
        val dut = FormalDut(new LimitedCounterInc())
        assumeInitial(ClockDomain.current.isResetActive)
        assert(dut.value >= 2)
        assert(dut.value <= 10)

        // Drive dut.inc with random values
        anyseq(dut.inc)
      })
    }
    
More assertions / past    
^^^^^^^^^^^^^^^^^^^^^^

For instance we can check that the value is counting up (if not already at 10): 

.. code-block:: scala

  FormalConfig.withBMC(15).doVerify(new Component {
    val dut = FormalDut(new LimitedCounter())
    assumeInitial(ClockDomain.current.isResetActive)

    // Check that the value is incrementing.
    // hasPast is used to ensure that the past(dut.value) had at least one sampling out of reset
    when(pastValid() && past(dut.value) =/= 10) {
      assert(dut.value === past(dut.value) + 1)
    }
  })

Assuming memory content
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Here is an example where we want to prevent the value ``1`` from ever being present in a memory :

.. code-block:: scala

    class DutWithRam extends Component {
      val ram = Mem.fill(4)(UInt(8 bits))
      val write = slave(ram.writePort)
      val read = slave(ram.readAsyncPort)
    }

    object FormalRam extends App {
      import spinal.core.formal._

      FormalConfig.withBMC(15).doVerify(new Component {
        val dut = FormalDut(new DutWithRam())
        assumeInitial(ClockDomain.current.isResetActive)

        // assume that no word in the ram has the value 1
        for(i <- 0 until dut.ram.wordCount) {
          assumeInitial(dut.ram(i) =/= 1)
        }

        // Allow the write anything but value 1 in the ram
        anyseq(dut.write)
        clockDomain.withoutReset() { // As the memory write can occur during reset, we need to ensure the assume apply there too
          assume(dut.write.data =/= 1)
        }

        // Check that no word in the ram is set to 1
        anyseq(dut.read.address)
        assert(dut.read.data =/= 1)
      })
    }


Utilities and primitives
------------------------

Assertions / clock / reset
^^^^^^^^^^^^^^^^^^^^^^^^^^

Assertions are always clocked and disabled during resets. This also apply for assumes and covers.

If you want to keep your assertion enabled during reset you can do: 

.. code-block:: scala

   ClockDomain.current.withoutReset() {
     assert(wuff === 0)
   }   
   

Specifying the initial value of a signal 
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

For instance, for the reset signal of the current clockdomain (useful at the top)

.. code-block:: scala

    ClockDomain.current.readResetWire initial(False)

Specifying a initial assumption
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: scala

    assumeInitial(clockDomain.isResetActive)
    
Memory content (Mem)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

If you have a Mem in your design, and you want to check its content, you can do it the following ways : 

.. code-block:: scala

    // Manual access
    for(i <- 0 until dut.ram.wordCount) {
      assumeInitial(dut.ram(i) =/= X) // No occurrence of the word X
    }
    
    assumeInitial(!dut.ram.formalContains(X)) // No occurrence of the word X
    
    assumeInitial(dut.ram.formalCount(X) === 1) // only one occurrence of the word X
    

Specifying assertion in the reset scope
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: scala

    ClockDomain.current.duringReset {
      assume(rawrrr === 0)
      assume(wuff === 3)
    }

Formal primitives
^^^^^^^^^^^^^^^^^

 .. list-table::
    :header-rows: 1
    :widths: 3 1 3

    * - Syntax
      - Returns
      - Description
    * - ``assert(Bool)``
      -
      - 
    * - ``assume(Bool)``
      -
      - 
    * - ``cover(Bool)``
      -
      - 
    * - | ``past(that : T, delay : Int)``
        | ``past(that : T)``
      - T
      - Return ``that`` delayed by ``delay`` cycles. (default 1 cycle)
    * - ``rose(that : Bool)``
      - Bool
      - Return True when ``that`` transitioned from False to True
    * - ``fell(that : Bool)``
      - Bool
      - Return True when ``that`` transitioned from True to False
    * - ``changed(that : Bool)``
      - Bool
      - Return True when ``that`` current value changed between compared to the last cycle
    * - ``stable(that : Bool)``
      - Bool
      - Return True when ``that`` current value didn't changed between compared to the last cycle
    * - ``initstate()``
      - Bool
      - Return True the first cycle
    * - ``pastValid()``
      - Bool
      - Returns True when the past value is valid (False on the first cycle). Recommended to be used with each application of ``past``, ``rose``, ``fell``, ``changed`` and ``stable``.
    * - ``pastValidAfterReset()``
      - Bool
      - Similar to ``pastValid``, where only difference is that this would take reset into account. Can be understood as ``pastValid & past(!reset)``.

Note that you can use the init statement on past: 

.. code-block:: scala

    when(past(enable) init(False)) { ... }



Limitations
-----------

There is no support for unclocked assertions. But their usage in third party formal verification examples seems mostly code style related.


Naming polices
--------------

All formal validation related functions return Area or Composite (preferred), and naming as formalXXXX.
``formalContext`` can be used to create formal related logic, there could be ``formalAsserts``, ``formalAssumes`` and ``formalCovers`` in it.

For Component
^^^^^^^^^^^^^
The minimum required assertions internally in a ``Component`` for "prove" can be named as ``formalAsserts``.

For interfaces implement IMasterSlave
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
There could be functions in name ``formalAssertsMaster``, ``formalAssertsSlave``, ``formalAssumesMaster``, ``formalAssumesSlave`` or ``formalCovers``.
Master/Slave are target interface type, so that ``formalAssertsMaster`` can be understand as "formal verification assertions for master interface".
