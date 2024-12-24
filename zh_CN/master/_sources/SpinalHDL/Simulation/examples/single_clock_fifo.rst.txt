.. _sim_example_single_clock_fifo:

Single clock fifo
=================

This example creates a ``StreamFifo``, and spawns 3 simulation threads.
Unlike the :ref:`Dual clock fifo <sim_example_dual_clock_fifo>` example, this FIFO does not need complex clock management.

The 3 simulation threads handle:

 - Managing the clock/reset
 - Pushing to the FIFO
 - Popping from the FIFO

The FIFO push thread randomizes the inputs.

The FIFO pop thread handles checking the the :abbr:`DUT (Device Under Test)`'s outputs against the reference model (an ordinary ``scala.collection.mutable.Queue`` instance).


.. code-block:: scala

   import spinal.core._
   import spinal.core.sim._

   import scala.collection.mutable.Queue


   object SimStreamFifoExample {
     def main(args: Array[String]): Unit = {
       // Compile the Component for the simulator.
       val compiled = SimConfig.withWave.allOptimisation.compile(
         rtl = new StreamFifo(
           dataType = Bits(32 bits),
           depth = 32
         )
       )

       // Run the simulation.
       compiled.doSimUntilVoid{dut =>
         val queueModel = mutable.Queue[Long]()

         dut.clockDomain.forkStimulus(period = 10)
         SimTimeout(1000000*10)

         // Push data randomly, and fill the queueModel with pushed transactions.
         val pushThread = fork {
           dut.io.push.valid #= false
           while(true) {
             dut.io.push.valid.randomize()
             dut.io.push.payload.randomize()
             dut.clockDomain.waitSampling()
             if(dut.io.push.valid.toBoolean && dut.io.push.ready.toBoolean) {
               queueModel.enqueue(dut.io.push.payload.toLong)
             }
           }
         }

         // Pop data randomly, and check that it match with the queueModel.
         val popThread = fork {
           dut.io.pop.ready #= true
           for(i <- 0 until 100000) {
             dut.io.pop.ready.randomize()
             dut.clockDomain.waitSampling()
             if(dut.io.pop.valid.toBoolean && dut.io.pop.ready.toBoolean) {
               assert(dut.io.pop.payload.toLong == queueModel.dequeue())
             }
           }
           simSuccess()
         }
       }
     }
   }
