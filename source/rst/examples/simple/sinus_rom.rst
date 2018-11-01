.. role:: raw-html-m2r(raw)
   :format: html

Sinus rom
=========

Let's imagine that you want to generate a sine wave and also have a filtered version of it (which is completely useless in practical, but let's do it as an example).

.. list-table::
   :header-rows: 1

   * - Parameters name
     - Type
     - Description
   * - resolutionWidth
     - Int
     - Number of bits used to represent numbers
   * - sampleCount
     - Int
     - Number of samples in a sine period


.. list-table::
   :header-rows: 1

   * - IO name
     - Direction
     - Type
     - Description
   * - sin
     - out
     - SInt(resolutionWidth bits)
     - Output which plays the sine wave
   * - sinFiltred
     - out
     - SInt(resolutionWidth bits)
     - Output which plays the filtered version of the sine


So let's define the ``Component``\ :

.. code-block:: scala

   class TopLevel(resolutionWidth : Int,sampleCount : Int) extends Component {
     val io = new Bundle {
       val sin = out SInt(resolutionWidth bits)
       val sinFiltred = out SInt(resolutionWidth bits)
     }
     // Here will come the logic implementation
   }

To play the sine wave on the ``sin`` output, you can define a ROM which contain all samples of a sine period (tt could be just a quarter, but let's do things by the simplest way).\ :raw-html-m2r:`<br>` Then you can read that ROM with an phase counter and this will generate your sine wave.

.. code-block:: scala

     //Function used to generate the rom (later)
     def sinTable = for(sampleIndex <- 0 until sampleCount) yield {
       val sinValue = Math.sin(2 * Math.PI * sampleIndex / sampleCount)
       S((sinValue * ((1<<resolutionWidth)/2-1)).toInt,resolutionWidth bitss)
     }

     val rom =  Mem(SInt(resolutionWidth bits),initialContent = sinTable)
     val phase = Reg(UInt(log2Up(sampleCount) bitss)) init(0)
     phase := phase + 1

     io.sin := rom.readSync(phase)

Then to generate ``sinFiltred``\ , you can for example use a first order low pass filter implementation:

.. code-block:: scala

     io.sinFiltred := RegNext(io.sinFiltred  - (io.sinFiltred  >> 5) + (io.sin >> 5)) init(0)

Here is the complete code:

.. code-block:: scala

   class TopLevel(resolutionWidth : Int,sampleCount : Int) extends Component {
     val io = new Bundle {
       val sin = out SInt(resolutionWidth bits)
       val sinFiltred = out SInt(resolutionWidth bits)
     }

     def sinTable = for(sampleIndex <- 0 until sampleCount) yield {
       val sinValue = Math.sin(2 * Math.PI * sampleIndex / sampleCount)
       S((sinValue * ((1<<resolutionWidth)/2-1)).toInt,resolutionWidth bitss)
     }

     val rom =  Mem(SInt(resolutionWidth bits),initialContent = sinTable)
     val phase = Reg(UInt(log2Up(sampleCount) bitss)) init(0)
     phase := phase + 1

     io.sin := rom.readSync(phase)
     io.sinFiltred := RegNext(io.sinFiltred  - (io.sinFiltred  >> 5) + (io.sin >> 5)) init(0)
   }
