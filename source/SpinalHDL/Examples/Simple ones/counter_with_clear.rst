Counter with clear
==================

This example defines a component with a ``clear`` input and a ``value`` output.
Each clock cycle, the ``value`` output is incrementing, but when ``clear`` is high, ``value`` is cleared.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/Counter.scala
   :language: scala
   :start-at: case class Counter(
   :end-before: // end case class Counter
