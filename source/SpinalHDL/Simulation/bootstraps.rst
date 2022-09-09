
Boot a simulation
=================

Introduction
------------

Below is an example hardware definition + testbench:

.. code-block:: scala

   import spinal.core._

   // Identity takes n bits in a and gives them back in z
   class Identity(n: Int) extends Component {
     val io = new Bundle {
       val a = in Bits(n bits)
       val z = out Bits(n bits)
     }
   
     io.z := io.a
   }

.. code-block:: scala

   import spinal.sim._
   import spinal.core.sim._

   object TestIdentity extends App {
     // Use the component with n = 3 bits as "dut" (device under test)
     SimConfig.withWave.compile(new Identity(3)).doSim{ dut =>
       // For each number from 3'b000 to 3'b111 included
       for (a <- 0 to 7) {
         // Apply input
         dut.io.a #= a
         // Wait for a simulation tick
         sleep(1)
         // Read output
         val z = dut.io.z.toInt
         // Check result
         assert(z == a, s"Got $z, expected $a")
       }
     }
   }

Configuration
-------------

``SimConfig`` will return a default simulation configuration instance on which you can call multiple functions to configure your simulation:

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - Syntax
     - Description
   * - ``withWave``
     - Enable simulation wave capture (default format)
   * - ``withVcdWave``
     - Enable simulation wave capture (VCD text format)
   * - ``withFstWave``
     - Enable simulation wave capture (FST binary format)
   * - ``withConfig(SpinalConfig)``
     - Specify the ``SpinalConfig`` that should be use to generate the hardware
   * - ``allOptimisation``
     - Enable all the RTL compilation optimizations to reduce simulation time (will increase compilation time)
   * - ``workspacePath(path)``
     - Change the folder where the sim files are generated
   * - ``withVerilator``
     - Use Verilator as simulation backend (default)
   * - ``withGhdl``
     - Use GHDL as simulation backend
   * - ``withIVerilog``
     - Use Icarus Verilog as simulation backend
   * - ``withVCS``
     - Use Synopsys VCS as simulation backend

Then you can call the ``compile(rtl)`` function to compile the hardware and warm up the simulator.
This function will return a ``SimCompiled`` instance.

On this ``SimCompiled`` instance you can run your simulation with the following functions:

.. list-table::
   :header-rows: 1

   * - Syntax
     - Description
   * - ``doSim[(simName[, seed])]{dut => ...}``
     - Run the simulation until the main thread is done (doesn't wait on forked threads) or until all threads are stuck
   * - ``doSimUntilVoid[(simName[, seed])]{dut => ...}``
     - Run the simulation until all threads are done or stuck


For example :

.. code-block:: scala

   val spinalConfig = SpinalConfig(defaultClockDomainFrequency = FixedFrequency(10 MHz))

   SimConfig
     .withConfig(spinalConfig)
     .withWave
     .allOptimisation
     .workspacePath("~/tmp")
     .compile(new TopLevel)
     .doSim { dut =>
       // Simulation code here
   }

Note that by default, the simulation files will be placed into the ``simWorkspace/xxx`` folders. You can override the simWorkspace location by setting the ``SPINALSIM_WORKSPACE`` environnement variable.

Running multiple tests on the same hardware
-------------------------------------------

.. code-block:: scala

    val compiled = SimConfig.withWave.compile(new Dut)

    compiled.doSim("testA") { dut =>
       // Simulation code here
    }

    compiled.doSim("testB") { dut =>
       // Simulation code here
    }

Throw Success or Failure of the simulation from a thread
--------------------------------------------------------

At any moment during a simulation you can call ``simSuccess`` or ``simFailure`` to end it.

It is possible to make a simulation fail when it is too long, for instance because the test-bench is waiting for a condition which never occurs. To do so, call ``SimTimeout(maxDuration)`` where ``maxDuration`` is the time (in simulation units of time) after the which the simulation should be considered to have failed.

For instance, to make the simulation fail after 1000 times the duration of a clock cycle:

.. code-block:: scala

    val period = 10
    dut.clockDomain.forkStimulus(period)
    SimTimeout(1000 * period)
