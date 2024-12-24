.. _sim_example_dual_clock_fifo:

Dual clock fifo
===============

This example creates a ``StreamFifoCC``, which is designed for crossing clock domains, along with 3 simulation threads.

The threads handle:

 - Management of the two clocks
 - Pushing to the FIFO
 - Popping from the FIFO

The FIFO push thread randomizes the inputs.

The FIFO pop thread handles checking the the :abbr:`DUT (Device Under Test)`'s outputs against the reference model (an ordinary ``scala.collection.mutable.Queue`` instance).

.. code-block:: scala

   import spinal.core._
   import spinal.core.sim._

   import scala.collection.mutable.Queue


   object SimStreamFifoCCExample {
     def main(args: Array[String]): Unit = {
       // Compile the Component for the simulator.
       val compiled = SimConfig.withWave.allOptimisation.compile(
         rtl = new StreamFifoCC(
           dataType = Bits(32 bits),
           depth = 32,
           pushClock = ClockDomain.external("clkA"),
           popClock = ClockDomain.external("clkB",withReset = false)
         )
       )

       // Run the simulation.
       compiled.doSimUntilVoid{dut =>
         val queueModel = mutable.Queue[Long]()

         // Fork a thread to manage the clock domains signals
         val clocksThread = fork {
           // Clear the clock domains' signals, to be sure the simulation captures their first edges.
           dut.pushClock.fallingEdge()
           dut.popClock.fallingEdge()
           dut.pushClock.deassertReset()
           sleep(0)

           // Do the resets.
           dut.pushClock.assertReset()
           sleep(10)
           dut.pushClock.deassertReset()
           sleep(1)

           // Forever, randomly toggle one of the clocks.
           // This will create asynchronous clocks without fixed frequencies.
           while(true) {
             if(Random.nextBoolean()) {
               dut.pushClock.clockToggle()
             } else {
               dut.popClock.clockToggle()
             }
             sleep(1)
           }
         }

         // Push data randomly, and fill the queueModel with pushed transactions.
         val pushThread = fork {
           while(true) {
             dut.io.push.valid.randomize()
             dut.io.push.payload.randomize()
             dut.pushClock.waitSampling()
             if(dut.io.push.valid.toBoolean && dut.io.push.ready.toBoolean) {
               queueModel.enqueue(dut.io.push.payload.toLong)
             }
           }
         }

         // Pop data randomly, and check that it match with the queueModel.
         val popThread = fork {
           for(i <- 0 until 100000) {
             dut.io.pop.ready.randomize()
             dut.popClock.waitSampling()
             if(dut.io.pop.valid.toBoolean && dut.io.pop.ready.toBoolean) {
               assert(dut.io.pop.payload.toLong == queueModel.dequeue())
             }
           }
           simSuccess()
         }
       }
     }
   }
