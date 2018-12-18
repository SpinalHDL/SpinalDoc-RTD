
Single clock fifo
=================

.. code-block:: scala

   //Compile the simulator
   val compiled = SimConfig.withWave.allOptimisation.compile(
     rtl = new StreamFifo(
       dataType = Bits(32 bits),
       depth = 32
     )
   )

   //Run the simulation
   compiled.doSimUntilVoid{dut =>
     val queueModel = mutable.Queue[Long]()

     dut.clockDomain.forkStimulus(period = 10)
     SimTimeout(1000000*10)

     //Push data randomly and fill the queueModel with pushed transactions
     val pushThread = fork{
       dut.io.push.valid #= false
       while(true){
         dut.io.push.valid.randomize()
         dut.io.push.payload.randomize()
         dut.clockDomain.waitSampling()
         if(dut.io.push.valid.toBoolean && dut.io.push.ready.toBoolean){
           queueModel.enqueue(dut.io.push.payload.toLong)
         }
       }
     }

     //Pop data randomly and check that it match with the queueModel
     val popThread = fork{
       dut.io.pop.ready #= true
       Suspendable.repeat(1000000){
         dut.io.pop.ready.randomize()
         dut.clockDomain.waitSampling()
         if(dut.io.pop.valid.toBoolean && dut.io.pop.ready.toBoolean){
           assert(dut.io.pop.payload.toLong == queueModel.dequeue())
         }
       }
       simSuccess()
     }
   }
