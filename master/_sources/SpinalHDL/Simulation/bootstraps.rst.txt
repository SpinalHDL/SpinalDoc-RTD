
Boot a simulation
=================

Introduction
------------

There is an example hardware definition + testbench :

.. code-block:: scala

   //Your hardware toplevel
   import spinal.core._
   class TopLevel extends Component {
     ...
   }

   // Your toplevel tester
   import spinal.sim._
   import spinal.core.sim._

   object DutTests {
     def main(args: Array[String]): Unit = {
       SimConfig.withWave.compile(new TopLevel).doSim{ dut =>
         // Simulation code here
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
     - Enable simulation wave capture
   * - ``withConfig(SpinalConfig)``
     - Specify the ``SpinalConfig`` that should be use to generate the hardware
   * - ``allOptimisation``
     - Enable all the RTL compilation optimizations to reduce simulation time (will increase compilation time)
   * - ``workspacePath(path)``
     - Change the folder where the sim files are generated


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
