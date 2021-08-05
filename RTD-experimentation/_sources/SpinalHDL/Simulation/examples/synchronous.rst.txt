
Synchronous adder
=================

.. code-block:: scala

   import spinal.sim._
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

         var idx = 0
         var resultModel = 0
         while(idx < 100) {
           dut.io.a #= Random.nextInt(256)
           dut.io.b #= Random.nextInt(256)
           dut.io.c #= Random.nextInt(256)
           dut.clockDomain.waitSampling()
           assert(dut.io.result.toInt == resultModel)
           resultModel = (dut.io.a.toInt + dut.io.b.toInt - dut.io.c.toInt) & 0xFF
           idx += 1
         }
       }
     }
   }
