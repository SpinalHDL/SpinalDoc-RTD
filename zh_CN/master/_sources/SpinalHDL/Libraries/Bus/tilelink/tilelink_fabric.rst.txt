
tilelink.fabric.Node
====================

tilelink.fabric.Node is an additional layer over the regular tilelink hardware instantiation which handle negotiation and parameters propagation at a SoC level.

It is mostly based on the Fiber API, which allows to create elaboration time fibers (user-space threads), allowing to schedule future parameter propagation / negotiation and hardware elaboration.

A Node can be created in 3 ways : 

- tilelink.fabric.Node.down() : To create a node which can connect downward (toward slaves), so it would be used in a CPU / DMA / bridges agents
- tilelink.fabric.Node() : To create an intermediate nodes 
- tilelink.fabric.Node.up() : To create a node which can connect upward (toward masters), so it would be used in peripherals / memories / bridges agents

Nodes mostly have the following attributes :

- bus : Handle[tilelink.Bus]; the hardware instance of the bus
- m2s.proposed : Handle[tilelink.M2sSupport]; The set of features which is proposed by the upward connections
- m2s.supported : Handle[tilelink.M2sSupport] : The set of feature supported by the downward connections
- m2s.parameter : Handle[tilelink.M2sParameter] : The final bus parameter

You can note that they all are Handles. Handle is a way in SpinalHDL to have share a value between fibers. If a fiber read a Handle while this one has no value yet, it will block the execution of that fiber until another fiber provide a value to the Handle.

There is also a set of attributes like m2s, but reversed (named s2m) which specify the parameters for the transactions initiated by the slave side of the interconnect (ex memory coherency).

There is two talks which where introducing the tilelink.fabric.Node. Those talk may not exactly follow the actual syntax, they are still follow the concepts : 

- Introduction : https://youtu.be/hVi9xOGuuek
- In depth : https://peertube.f-si.org/videos/watch/bcf49c84-d21d-4571-a73e-96d7eb89e907

Example Toplevel
----------------

Here is an example of a simple fictive SoC toplevel :

.. code-block:: scala

      val cpu = new CpuFiber()

      val ram = new RamFiber()
      ram.up at(0x10000, 0x200) of cpu.down // map the ram at [0x10000-0x101FF], the ram will infer its own size from it

      val gpio = new GpioFiber()
      gpio.up at 0x20000 of cpu.down // map the gpio at [0x20000-0x20FFF], its range of 4KB being fixed internally

You can also define intermediate nodes in the interconnect as following : 

.. code-block:: scala

      val cpu = new CpuFiber()

      val ram = new RamFiber()
      ram.up at(0x10000, 0x200) of cpu.down
        
      // Create a peripherals namespace to keep things clean
      val peripherals = new Area {
        // Create a intermediate node in the interconnect
        val access = tilelink.fabric.Node()
        access at 0x20000 of cpu.down

        val gpioA = new GpioFiber()
        gpioA.up at 0x0000 of access

        val gpioB = new GpioFiber()
        gpioB.up at 0x1000 of access
      }


Example GpioFiber
-----------------

GpioFiber is a simple tilelink peripheral which can read / drive a 32 bits tristate array.

.. code-block:: scala

    import spinal.lib._
    import spinal.lib.bus.tilelink
    import spinal.core.fiber.Fiber
    class GpioFiber extends Area {
      // Define a node facing upward (toward masters only)
      val up = tilelink.fabric.Node.up()

      // Define a elaboration thread to specify the "up" parameters and generate the hardware
      val fiber = Fiber build new Area {
        // Here we first define what our up node support. m2s mean master to slave requests
        up.m2s.supported load tilelink.M2sSupport(
          addressWidth = 12,
          dataWidth = 32,
          // Transfers define which kind of memory transactions our up node will support.
          // Here it only support 4 bytes get/putfull
          transfers = tilelink.M2sTransfers(
            get = tilelink.SizeRange(4),
            putFull = tilelink.SizeRange(4)
          )
        )
        // s2m mean slave to master requests, those are only use for memory coherency purpose
        // So here we specify we do not need any
        up.s2m.none()

        // Then we can finally generate some hardware
        // Starting by defining a 32 bits TriStateArray (Array meaning that each pin has its own writeEnable bit
        val pins = master(TriStateArray(32 bits)) 
        
        // tilelink.SlaveFactory is a utility allowing to easily generate the logic required 
        // to control some hardware from a tilelink bus.
        val factory = new tilelink.SlaveFactory(up.bus, allowBurst = false)
        
        // Use the SlaveFactory API to generate some hardware to read / drive the pins
        val writeEnableReg = factory.drive(pins.writeEnable, 0x0) init (0)
        val writeReg = factory.drive(pins.write, 0x4) init(0)
        factory.read(pins.read, 0x8)
      }
    }

Example RamFiber
----------------------

RamFiber is the integration layer of a regular tilelink Ram component.


.. code-block:: scala

    import spinal.lib.bus.tilelink
    import spinal.core.fiber.Fiber
    class RamFiber() extends Area {
      val up = tilelink.fabric.Node.up()

      val thread = Fiber build new Area {
        // Here the supported parameters are function of what the master would like us to ideally support.
        // The tilelink.Ram support all addressWidth / dataWidth / burst length / get / put accesses
        // but doesn't support atomic / coherency. So we take what is proposed to use and restrict it to 
        // all sorts of get / put request
        up.m2s.supported load up.m2s.proposed.intersect(M2sTransfers.allGetPut)
        up.s2m.none()

        // Here we infer how many bytes our ram need to be, by looking at the memory mapping of the connected masters
        val bytes = up.ups.map(e => e.mapping.value.highestBound - e.mapping.value.lowerBound + 1).max.toInt
        
        // Then we finally generate the regular hardware
        val logic = new tilelink.Ram(up.bus.p.node, bytes)
        logic.io.up << up.bus
      }
    }

Example CpuFiber
----------------------

CpuFiber is an fictive example of a master integration.


.. code-block:: scala

    import spinal.lib.bus.tilelink
    import spinal.core.fiber.Fiber

    class CpuFiber extends Area {
      // Define a node facing downward (toward slaves only)
      val down = tilelink.fabric.Node.down()

      val fiber = Fiber build new Area {
        // Here we force the bus parameters to a specific configurations
        down.m2s forceParameters tilelink.M2sParameters(
          addressWidth = 32,
          dataWidth = 64,
          // We define the traffic of each master using this node. (one master => one M2sAgent)
          // In our case, there is only the CpuFiber.
          masters = List(
            tilelink.M2sAgent(
              name = CpuFiber.this, // Reference to the original agent.
              // A agent can use multiple sets of source ID for different purposes
              // Here we define the usage of every sets of source ID
              // In our case, let's say we use ID [0-3] to emit get/putFull requests
              mapping = List(
                tilelink.M2sSource(
                  id = SizeMapping(0, 4),
                  emits = M2sTransfers(
                    get = tilelink.SizeRange(1, 64), // Meaning the get access can be any power of 2 size in [1, 64]
                    putFull = tilelink.SizeRange(1, 64)
                  )
                )
              )
            )
          )
        )

        // Lets say the CPU doesn't support any slave initiated requests (memory coherency)
        down.s2m.supported load tilelink.S2mSupport.none()

        // Then we can generate some hardware (nothing useful in this example)
        down.bus.a.setIdle()
        down.bus.d.ready := True
      }
    }

One particularity of Tilelink, is that it assumes a master will not emit requests to a unmapped memory space.
To allow a master to identify what memory access it is allowed to do, you can use the spinal.lib.system.tag.MemoryConnection.getMemoryTransfers tool as following : 

.. code-block:: scala

        val mappings = spinal.lib.system.tag.MemoryConnection.getMemoryTransfers(down)
        // Here we just print the values out in stdout, but instead you can generate some hardware from it.
        for(mapping <- mappings) {
          println(s"- ${mapping.where} -> ${mapping.transfers}")
        }

If you run this in the Cpu's fiber, in the following soc : 

.. code-block:: scala

      val cpu = new CpuFiber()

      val ram = new RamFiber()
      ram.up at(0x10000, 0x200) of cpu.down
        
      // Create a peripherals namespace to keep things clean
      val peripherals = new Area {
        // Create a intermediate node in the interconnect
        val access = tilelink.fabric.Node()
        access at 0x20000 of cpu.down

        val gpioA = new GpioFiber()
        gpioA.up at 0x0000 of access

        val gpioB = new GpioFiber()
        gpioB.up at 0x1000 of access
      }

You will get : 

.. code-block:: 

    - toplevel/ram_up mapped=SM(0x10000, 0x200) through=List(OT(0x10000))  -> GF
    - toplevel/peripherals_gpioA_up mapped=SM(0x20000, 0x1000) through=List(OT(0x20000), OT(0x0))  -> GF
    - toplevel/peripherals_gpioB_up mapped=SM(0x21000, 0x1000) through=List(OT(0x20000), OT(0x1000))  -> GF

- "through=" specify the chain of address transformations done to reach the target.
- "SM" means SizeMapping(address, size)
- "OT" means OffsetTransformer(offset)

Note that you can also add PMA (Physical Memory Attributes) to nodes and retrieves them via this getMemoryTransfers utilities.

The currently defined PMA are : 

.. code-block:: 

  object MAIN          extends PMA
  object IO            extends PMA
  object CACHABLE      extends PMA // an intermediate agent may have cached a copy of the region for you
  object TRACEABLE     extends PMA // the region may have been cached by another master, but coherence is being provided
  object UNCACHABLE    extends PMA // the region has not been cached yet, but should be cached when possible
  object IDEMPOTENT    extends PMA // reads return most recently put content, but content should not be cached
  object EXECUTABLE    extends PMA // Allows an agent to fetch code from this region
  object VOLATILE      extends PMA // content may change without a write
  object WRITE_EFFECTS extends PMA // writes produce side effects and so must not be combined/delayed
  object READ_EFFECTS  extends PMA // reads produce side effects and so must not be issued speculatively


The getMemoryTransfers utility rely on a dedicated SpinalTag :

.. code-block:: 

    trait MemoryConnection extends SpinalTag {
      def up : Nameable with SpinalTagReady // Side toward the masters of the system
      def down : Nameable with SpinalTagReady // Side toward the slaves of the system
      def mapping : AddressMapping // Specify the memory mapping of the slave from the master address (before transformers are applied)
      def transformers : List[AddressTransformer]  // List of alteration done to the address on this connection (ex offset, interleaving, ...)
      def sToM(downs : MemoryTransfers, args : MappedNode) : MemoryTransfers = downs // Convert the slave MemoryTransfers capabilities into the master ones
    }

That SpinalTag can be used applied to both ends of a given memory bus connection to keep this connection discoverable at elaboration time, creating a graph of MemoryConnection. One good thing about it is that is is bus agnostic, meaning it isn't tilelink specific.


Example WidthAdapter
---------------------

The width adapter is a simple example of bridge.

.. code-block:: 

    class WidthAdapterFiber() extends Area {
      val up = Node.up()
      val down = Node.down()

      // Populate the MemoryConnection graph
      new MemoryConnection {
        override def up = up
        override def down = down
        override def transformers = Nil
        override def mapping = SizeMapping(0, BigInt(1) << WidthAdapterFiber.this.up.m2s.parameters.addressWidth)
        populate()
      }

      // Fiber in which we will negotiate the data width parameters and generate the hardware
      val logic = Fiber build new Area {
        // First, we propagate downward the parameter proposal, hopping that the downward side will agree
        down.m2s.proposed.load(up.m2s.proposed)

        // Second, we will propagate upward what is actually supported, but will take care of any dataWidth mismatch
        up.m2s.supported load down.m2s.supported.copy(
          dataWidth = up.m2s.proposed.dataWidth
        )

        // Third, we propagate downward the final bus parameter, but will take care of any dataWidth mismatch
        down.m2s.parameters load up.m2s.parameters.copy(
          dataWidth = down.m2s.supported.dataWidth
        )

        // No alteration on s2m parameters
        up.s2m.from(down.s2m)

        // Finally, we generate the hardware
        val bridge = new tilelink.WidthAdapter(up.bus.p, down.bus.p)
        bridge.io.up << up.bus
        bridge.io.down >> down.bus
      }
    }


