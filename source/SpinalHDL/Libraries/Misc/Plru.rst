.. role:: raw-html-m2r(raw)
   :format: html

Plru
==========================

Introduction
--------------------
/**
  * Pseudo least recently used combinatorial logic
  * io.context.state need to be handled externaly.
  * When you want to specify a access to a entry, you can use the io.update interface
  * to get the new state value.
  */


.. code-block:: scala

      val PLRU = Payload(Plru.State(wayCount))
    val plru = new Area {
        val ram = Mem.fill(nSets)(Plru.State(wayCount))
        val write = ram.writePort 
        val fromLoad, fromStore = cloneOf(write)
        write.valid := fromLoad.valid || fromStore.valid
        write.payload := fromLoad.valid.mux(fromLoad.payload, fromStore.payload)
        
    }


