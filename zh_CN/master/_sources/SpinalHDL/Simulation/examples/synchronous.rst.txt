.. _sim_example_synchronous_adder:

Synchronous adder
=================

This example creates a ``Component`` out of sequential logic that does some simple arithmetic on 3 operands.

The test bench performs the following steps 100 times:

 * Initialize ``a``, ``b``, and ``c`` to random integers in the 0..255 range.
 * Stimulate the :abbr:`DUT (Device Under Test)`'s matching ``a``, ``b``, ``c`` inputs.
 * Wait until the simulation samples the DUT's signals again.
 * Check for correct output.

The main difference between this example and the :ref:`Asynchronous adder <sim_example_asynchronous_adder>` example is that this ``Component`` has to use ``forkStimulus`` to generate a clock signal, since it is using sequential logic internally.

.. code-block:: scala

   import spinal.core._
   import spinal.core.sim._

   import scala.util.Random


   object SimSynchronousExample {
     class Dut extends Component {
       val io = new Bundle {
         val a, b, c = in UInt (8 bits)
         val result = out UInt (8 bits)
       }
       io.result := RegNext(io.a + io.b - io.c) init(0)
     }

     def main(args: Array[String]): Unit = {
       SimConfig.withWave.compile(new Dut).doSim{ dut =>
         dut.clockDomain.forkStimulus(period = 10)

         var resultModel = 0
         for(idx <- 0 until 100) {
           dut.io.a #= Random.nextInt(256)
           dut.io.b #= Random.nextInt(256)
           dut.io.c #= Random.nextInt(256)
           dut.clockDomain.waitSampling()
           assert(dut.io.result.toInt == resultModel)
           resultModel = (dut.io.a.toInt + dut.io.b.toInt - dut.io.c.toInt) & 0xFF
         }
       }
     }
   }
