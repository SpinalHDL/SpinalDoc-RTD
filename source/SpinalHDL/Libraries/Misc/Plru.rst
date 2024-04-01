.. role:: raw-html-m2r(raw)
   :format: html

Plru
==========================

Introduction
--------------------
Pseudo least recently used combinatorial logic
io.context.state need to be handled externaly.
When you want to specify a access to a entry, you can use the io.update interface
to get the new state value.

Defined as

.. code-block:: scala

   object Plru{
     def State(entries : Int) = Vec.tabulate(log2Up(entries))(l =>  Bits(1 << l bits))
   }


PLRU Code

.. code-block:: scala

   val io = new Bundle{
    val context = new Bundle{
      //user -> plru, specify the current state
      val state = Plru.State(entries) 
      //user -> plru, allow to specify prefered entries to remove. each bit set mean : "i would prefer that way to not to be selected by PLRU"
      val valids = withEntriesValid generate Bits(entries bits) 
    }
    val evict = new Bundle{
      //PLRU -> user, Tells you the least recently used entry for the given context provided above
      val id =  UInt(log2Up(entries) bits)
    }
    val update = new Bundle{
      // user -> PLRU specify which entry the user want to mark as most recently used
      val id = UInt(log2Up(entries) bits)
     // PLRU -> user specfy what should then be the new value of the PLRU status 
      val state = Plru.State(entries)
    }
  }


Example usage in a cache 

.. code-block:: scala

   val plru = new Area {
      // Define a Mem, to track the state of each set
      val ram = Mem.fill(nSets)(Plru.State(wayCount))
      val write = ram.writePort
      val fromLoad, fromStore = cloneOf(write)
      write.valid := fromLoad.valid || fromStore.valid
      write.payload := fromLoad.valid.mux(fromLoad.payload, fromStore.payload)
   }
