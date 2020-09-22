Accessing signals of the simulation
==========================================

Read and write signals
----------------------

Each interface signal of the toplevel can be read and written from Scala:

.. list-table::
   :header-rows: 1
   :widths: 3 5

   * - Syntax
     - Description
   * - Bool.toBoolean
     - Read a hardware ``Bool`` as a Scala ``Boolean`` value
   * - Bits/UInt/SInt.toInt
     - Read a hardware ``BitVector`` as a Scala ``Int`` value
   * - Bits/UInt/SInt.toLong
     - Read a hardware ``BitVector`` as a Scala ``Long`` value
   * - Bits/UInt/SInt.toBigInt
     - Read a hardware ``BitVector`` as a Scala ``BigInt`` value
   * - SpinalEnumCraft.toEnum
     - Read a hardware ``SpinalEnumCraft`` as a Scala ``SpinalEnumElement`` value
   * - Bool #= Boolean
     - Assign a hardware ``Bool`` from an Scala ``Boolean``
   * - Bits/UInt/SInt #= Int
     - Assign a hardware ``BitVector`` from a Scala ``Int``
   * - Bits/UInt/SInt #= Long
     - Assign a hardware ``BitVector`` from a Scala ``Long``
   * - Bits/UInt/SInt #= BigInt
     - Assign a hardware ``BitVector`` from a Scala ``BigInt``
   * - SpinalEnumCraft #= SpinalEnumElement
     - Assign a hardware ``SpinalEnumCraft`` from a Scala ``SpinalEnumElement``


.. code-block:: scala

   dut.io.a #= 42
   dut.io.a #= 42l
   dut.io.a #= BigInt("101010", 2)
   dut.io.a #= BigInt("0123456789ABCDEF", 16)
   println(dut.io.b.toInt)

Accessing signals inside the component's hierarchy
--------------------------------------------------

To access signals which are inside the component's hierarchy, you have first to set the given signal as ``simPublic``.

You can add this ``simPublic`` tag directly in the hardware description:

.. code-block:: scala

   object SimAccessSubSignal {
     import spinal.core.sim._

     class TopLevel extends Component {
       val counter = Reg(UInt(8 bits)) init(0) simPublic() // Here we add the simPublic tag on the counter register to make it visible
       counter := counter + 1
     }

     def main(args: Array[String]) {
       SimConfig.compile(new TopLevel).doSim{dut =>
         dut.clockDomain.forkStimulus(10)

         for(i <- 0 to 3) {
           dut.clockDomain.waitSampling()
           println(dut.counter.toInt)
         }
       }
     }
   }

Or you can add it later, after having instantiated your toplevel for the simulation:


.. code-block:: scala

   object SimAccessSubSignal {
     import spinal.core.sim._
     class TopLevel extends Component {
       val counter = Reg(UInt(8 bits)) init(0)
       counter := counter + 1
     }

     def main(args: Array[String]) {
       SimConfig.compile {
         val dut = new TopLevel
         dut.counter.simPublic()
         dut
       }.doSim{dut =>
         dut.clockDomain.forkStimulus(10)

         for(i <- 0 to 3) {
           dut.clockDomain.waitSampling()
           println(dut.counter.toInt)
         }
       }
     }
   }

