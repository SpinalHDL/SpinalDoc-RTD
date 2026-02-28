
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

   import spinal.core.sim._

   object TestIdentity extends App {
     // Use the component with n = 3 bits as "dut" (device under test)
     SimConfig.withWave.compile(new Identity(3)).doSim{ dut =>
       // For each number from 3'b000 to 3'b111 included
       for (a <- 0 to 7) {
         // Apply input
         dut.io.a #= a
         // Wait for a simulation time unit
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

``doSim[(simName[, seed])]{dut => /* main stimulus code */}``
  Run the simulation until the main thread runs to completion and exits/returns.
  It will detect and report an error if the simulation gets fully stuck. As long as
  e.g. a clock is running the simulation can continue forever, it is therefore recommended
  to use ``SimTimeout(cycles)`` to limit the possible runtime.

``doSimUntilVoid[(simName[, seed])]{dut => ...}``
  Run the simulation until it is ended by calling either ``simSuccess()`` or ``simFailure()``.
  The main stimulus thread can continue or exit early. As long as there are events to process,
  the simulation will continue. The simulation will report an error if it gets fully stuck.

The following testbench template will use the following toplevel : 

.. code-block:: scala
    
   class TopLevel extends Component {
      val counter = out(Reg(UInt(8 bits)) init (0))
      counter := counter + 1
   }

Here is a template with many simulation configurations:

.. code-block:: scala
    
   val spinalConfig = SpinalConfig(defaultClockDomainFrequency = FixedFrequency(10 MHz))

   SimConfig
     .withConfig(spinalConfig)
     .withWave
     .allOptimisation
     .workspacePath("~/tmp")
     .compile(new TopLevel)
     .doSim { dut =>
       SimTimeout(1000)
       // Simulation code here
   }

Here is a template where the simulation ends by completing the simulation main thread execution:

.. code-block:: scala

    SimConfig.compile(new TopLevel).doSim { dut =>
      SimTimeout(1000)
      dut.clockDomain.forkStimulus(10)
      dut.clockDomain.waitSamplingWhere(dut.counter.toInt == 20)
      println("done")
    }
    
Here is a template where the simulation ends by explicitly calling `simSuccess()`:

.. code-block:: scala

    SimConfig.compile(new TopLevel).doSimUntilVoid{ dut =>
      SimTimeout(1000)
      dut.clockDomain.forkStimulus(10)
      fork {
        dut.clockDomain.waitSamplingWhere(dut.counter.toInt == 20)
        println("done")
        simSuccess()
      }
    }

Note is it equivalent to:

.. code-block:: scala

    SimConfig.compile(new TopLevel).doSim{ dut =>
      SimTimeout(1000)
      dut.clockDomain.forkStimulus(10)
      fork {
        dut.clockDomain.waitSamplingWhere(dut.counter.toInt == 20)
        println("done")
        simSuccess()
      }
      simThread.suspend() // Avoid the "doSim" completion
    }

The location where the simulation files will be placed is defined by default in $WORKSPACE/$COMPILED. 

- $WORKSPACE being by default ``simWorkspace``, you can override it with the ``SPINALSIM_WORKSPACE`` environnement variable.
- $COMPILED being the name of the toplevel being simulated.
- The location of the wave file depend the backend used. For verilator it will be in the folder (``$WORKSPACE/$COMPILED/$TEST`` by default). 
- For the verilator backend, you can override the location of the test folder via the ``SimConfig.setTestPath(path)`` function.
- You can retrieve the location of the test path during simulation by calling the currentTestPath() function.


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

Capturing wave for a given window before failure
------------------------------------------------

In the case you have a very long simulation, and you don't want to capture the wave on all of it (too bug, too slow), you have mostly 2 ways to do it.

Either you know already at which ``simTime`` the simulation failed, in which case you can do the following in your testbench : 

.. code-block:: scala
    
    disableSimWave()
    delayed(timeFromWhichIWantToCapture)(enableSimWave())

Or you can run a dual lock-step simulation, with one running a bit delayed from the the other one, and which will start recording the wave once the leading simulation had a failure.

To do this, you can use the DualSimTracer utility, with parameters for the compiled hardware, the window of time you want to capture before failure, and a seed.

Here is an example :

.. literalinclude:: /../examples/src/main/scala/spinaldoc/libraries/sim/DualSimExample.scala
   :language: scala

This will generate the following file structure : 

- simWorkspace/Toplevel/explorer/stdout.log : stdout of the simulation which is ahead
- simWorkspace/Toplevel/tracer/stdout.log : stdout of the simulation doing the wave tracing
- simWorkspace/Toplevel/tracer.fst : Waveform of the failure

The scala terminal will show the explorer simulation stdout.

