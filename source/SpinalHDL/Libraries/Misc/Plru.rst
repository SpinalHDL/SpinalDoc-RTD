.. role:: raw-html-m2r(raw)
   :format: html

Plru
====

Introduction
------------

The `Pseudo Least Recently Used <https://en.wikipedia.org/wiki/Pseudo-LRU>`_
algorithm can be used for example in cache to select efficiently a line
for eviction.

It is used for example `in the VexiiRiscV core <https://github.com/SpinalHDL/VexiiRiscv/blob/3cff1cc2411ca97e6ee9ef64414ef274d30371e1/src/main/scala/vexiiriscv/execute/lsu/LsuL1Plugin.scala#L830>`_
or in `spinal.lib.bus.tilelink.coherent.Cache <https://github.com/SpinalHDL/SpinalHDL/blob/78f29dc66110fc099a777992b6daa2f803ab445e/lib/src/main/scala/spinal/lib/bus/tilelink/coherent/Cache.scala#L1026>`_.

- ``io.context.state`` need to be handled externally.
- When you want to specify a access to a entry, you can use the ``io.update``
  interface to get the new state value.
- ``plru.io.evict.id`` tells you the id of the next block to be evicted
- ``plru.io.update.id`` lets you update what you recently used


PLRU Code:

.. code-block:: scala

   val io = new Bundle {
    val context = new Bundle {
      // user -> plru, specify the current state
      val state = Plru.State(entries) 
      // user -> plru, allow to specify preferred entries to remove. Each bit
      // set mean : "i would prefer that way to not to be selected by PLRU"
      val valids = withEntriesValid generate Bits(entries bits) 
    }
    val evict = new Bundle {
      // PLRU -> user, Tells you the least recently used entry for the
      // given context provided above
      val id =  UInt(log2Up(entries) bits)
    }
    val update = new Bundle {
      // user -> PLRU specify which entry the user want to mark as
      // most recently used
      val id = UInt(log2Up(entries) bits)
      // PLRU -> user specfy what should then be the new value of the PLRU status 
      val state = Plru.State(entries)
    }
  }


Example usage in a cache:

.. code-block:: scala

   val plru = new Area {
      // Define a Mem, to track the state of each set
      val ram = Mem.fill(nSets)(Plru.State(wayCount))
      val write = ram.writePort
      val fromLoad, fromStore = cloneOf(write)
      write.valid := fromLoad.valid || fromStore.valid
      write.payload := fromLoad.valid.mux(fromLoad.payload, fromStore.payload)
   }


Get the ID of the way to evict from:

.. code-block:: scala

   val replacedWay = plru.io.evict.id

Update recently used way:

.. code-block:: scala

   plru.update.id := refillWay




