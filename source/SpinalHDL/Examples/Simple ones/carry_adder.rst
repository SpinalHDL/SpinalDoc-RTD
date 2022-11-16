
Carry adder
===========

This example defines a component with inputs ``a`` and ``b``\ , and a ``result`` output.
At any time, ``result`` will be the sum of ``a`` and ``b`` (combinatorial).
This sum is manually done by a carry adder logic.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/CarryAdder.scala
   :language: scala
   :start-at: case class CarryAdder(