RGB to gray
===========

Let's imagine a component that converts an RGB color into a gray one, and then writes it into external memory.

.. list-table::
   :header-rows: 1
   :widths: 1 1 2

   * - io name
     - Direction
     - Description
   * - clear
     - in
     - Clear all internal registers
   * - r,g,b
     - in
     - Color inputs
   * - wr
     - out
     - Memory write
   * - address
     - out
     - Memory address, incrementing each cycle
   * - data
     - out
     - Memory data, gray level


.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/RgbToGray.scala
   :language: scala
   :start-at: case class RgbToGray(
   :end-before: // end case class RgbToGray