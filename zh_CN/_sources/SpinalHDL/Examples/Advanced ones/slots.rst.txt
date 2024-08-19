.. _Slots:

Slots
=====

Introduction
------------

Let's say you have some hardware which has to keep track of multiple similar ongoing activities, you may want to implement an array of "slots" to do so. This example show how to do it using Area, OHMasking.first, onMask and reader.


Implementation
^^^^^^^^^^^^^^

This implementation avoid the use of Vec. Instead, it use Area which allow to mix signal, registers and logic definitions in each slot.

Note that the `reader` API is for SpinalHDL version coming after 1.9.1

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/Slots.scala
   :language: scala
   
   
In practice
^^^^^^^^^^^^^^

For instance, this kind of slot pattern is used in Tilelink coherency hub to keep track of all ongoing memory probes in flight:

https://github.com/SpinalHDL/SpinalHDL/blob/008c73f1ce18e294f137efe7a1442bd3f8fa2ee0/lib/src/main/scala/spinal/lib/bus/tilelink/coherent/Hub.scala#L376   

As well in the DRAM / SDR / DDR memory controller to implement the handling of multiple memory transactions at once (having multiple precharge / active / read / write running at the same time to improve performances) : 

https://github.com/SpinalHDL/SpinalHDL/blob/1edba1890b5f629b28e5171b3c449155337d2548/lib/src/main/scala/spinal/lib/memory/sdram/xdr/Tasker.scala#L202

As well in the NaxRiscv (out of order CPU) load-store-unit to handle the store-queue / load-queue hardware (a bit too scary to show here in the doc XD)


