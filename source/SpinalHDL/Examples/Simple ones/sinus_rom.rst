.. _sinus_rom:

Sinus ROM
=========

Let's imagine that you want to generate a sine wave and also have a filtered version of it (which is completely useless in practice, but let's do it as an example).

.. list-table::
   :header-rows: 1
   :widths: 1 1 2

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
   :widths: 1 1 4 7

   * - IO name
     - Direction
     - Type
     - Description
   * - sin
     - out
     - SInt(resolutionWidth bits)
     - Output which plays the sine wave
   * - sinFiltered
     - out
     - SInt(resolutionWidth bits)
     - Output which plays the filtered version of the sine


So let's define the ``Component``\ :

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/SineRom.scala
   :language: scala
   :start-at: case class SineRom(
   :end-at: }
   :append: ...

To play the sine wave on the ``sin`` output, you can define a ROM which contain all samples of a sine period (it could be just a quarter, but let's do things the most simple way).
Then you can read that ROM with an phase counter and this will generate your sine wave.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/SineRom.scala
   :language: scala
   :start-at: // Calculate
   :end-at: io.sin :=

Then to generate ``sinFiltered``\ , you can for example use a first order low pass filter implementation:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/SineRom.scala
   :language: scala
   :start-at: io.sinFiltered :=
   :end-at: io.sinFiltered :=

Here is the complete code:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/SineRom.scala
   :language: scala
   :start-at: case class SineRom(
   :end-before: // end case class SineRom