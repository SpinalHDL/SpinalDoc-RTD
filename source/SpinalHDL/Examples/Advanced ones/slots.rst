.. _Slots:

Slots
=====

Introduction
------------

Let's say you have some hardware which has to keep track of multiple similar ongoing activities, you may want to implement an array of "slots" to do so. This example show how to do it using Area, OHMasking.first, onMask and reader.


Implementation
^^^^^^^^^^^^^^

This implementation avoid the use of Vec. Instead, it use Area which allow to mix signal, registers and logic definitions in each slot.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/Timer.scala
   :language: scala


