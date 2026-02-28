Color summing
=============

First let's define a Color ``Bundle`` with an addition operator.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/ColorSumming.scala
   :language: scala
   :start-at: case class Color(
   :end-before: // end case class Color

Then let's define a component with a ``sources`` input which is a vector of colors, and a ``result`` output which is the sum of the ``sources`` input.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/ColorSumming.scala
   :language: scala
   :start-at: case class ColorSumming(
   :end-before: // end case class ColorSumming
