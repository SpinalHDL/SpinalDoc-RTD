.. _sim_example_asynchronous_adder:

Asynchronous adder
==================

This example creates a ``Component`` out of combinational logic that does some simple arithmetic on 3 operands.

The test bench performs the following steps 100 times:

 * Initialize ``a``, ``b``, and ``c`` to random integers in the 0..255 range.
 * Stimulate the :abbr:`DUT (Device Under Test)`'s matching ``a``, ``b``, ``c`` inputs.
 * Wait 1 simulation timestep (to allow the inputs to propagate).
 * Check for correct output.

.. code-block:: scala

   import spinal.core._
   import spinal.core.sim._

   import scala.util.Random


   object SimAsynchronousExample {
     class Dut extends Component {
       val io = new Bundle {
         val a, b, c = in UInt (8 bits)
         val result = out UInt (8 bits)
       }
       io.result := io.a + io.b - io.c
     }

     def main(args: Array[String]): Unit = {
       SimConfig.withWave.compile(new Dut).doSim{ dut =>
         var idx = 0
         while(idx < 100) {
           val a, b, c = Random.nextInt(256)
           dut.io.a #= a
           dut.io.b #= b
           dut.io.c #= c
           sleep(1) // Sleep 1 simulation timestep
           assert(dut.io.result.toInt == ((a + b - c) & 0xFF))
           idx += 1
         }
       }
     }
   }
