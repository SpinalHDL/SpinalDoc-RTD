.. role:: raw-html-m2r(raw)
   :format: html

Plugin
======

Introduction
------------

For some design, instead of implementing your Component's hardware directly in it, 
you may instead want to compose its hardware by using some sorts of Plugins. This can provide a few key features : 

- You can extend the features of your component by adding new plugins in its parameters. For instance adding Floating point support in a CPU.
- You can swap various implementations of the same functionality just by using another set of plugins. For instance one implementation of a CPU multiplier may fit well on some FPGA, while others may fit well on ASIC.
- It avoid the very very very large hand written toplevel syndrome where everything has to be connected manually. Instead plugins can discover their neighborhood by looking/using the software interface of other plugins.

VexRiscv and NaxRiscv projects are an example of this. Their are CPUs which have a mostly empty toplevel, 
and their hardware parts are injected using plugins. For instance : 

- PcPlugin
- FetchPlugin
- DecoderPlugin
- RegFilePlugin
- IntAluPlugin
- ...

And those plugins will then negotiate/propagate/interconnect to each others via their pool of services.

While VexRiscv use a strict synchronous 2 phase system (setup/build callback), NaxRiscv uses a more flexible approach which uses the spinal.core.fiber API to fork elaboration threads which can interlock each others in order to ensure a workable elaboration ordering.

The Plugin API provide a NaxRiscv like system to define composable components using plugins.

Execution order
--------------------

The main idea is that you have multiple 2 executions phases : 

- Setup phase, in which plugins can lock/retain each others. The idea is not to start negotiation / elaboration yet.
- Build phase, in which plugins can negotiation / elaboration hardware.

The build phase will not start before all FiberPlugin are done with their setup phase.

.. code-block:: scala

      class MyPlugin extends FiberPlugin {
        val logic = during setup new Area {
          // Here we are executing code in the setup phase
          awaitBuild()
          // Here we are executing code in the build phase
        }
      }

      class MyPlugin2 extends FiberPlugin {
        val logic = during build new Area {
          // Here we are executing code in the build phase
        }
      }


Simple example
--------------

Here is a simple dummy example with a SubComponent which will be composed using 2 plugins :

.. code-block:: scala

      import spinal.core._
      import spinal.lib.misc.plugin._

      // Let's define a Component with a PluginHost instance
      class SubComponent extends Component {
        val host = new PluginHost()
      }

      // Let's define a plugin which create a register
      class StatePlugin extends FiberPlugin {
        // during build new Area { body } will run the body of code in the Fiber build phase, in the context of the PluginHost
        val logic = during build new Area {
          val signal = Reg(UInt(32 bits))
        }
      }

      // Let's define a plugin which will make the StatePlugin's register increment
      class DriverPlugin extends FiberPlugin {
        // We define how to get the instance of StatePlugin.logic from the PluginHost. It is a lazy val, because we can't evaluate it until the plugin is bound to its host.
        lazy val sp = host[StatePlugin].logic.get

        val logic = during build new Area {
          // Generate the increment hardware
          sp.signal := sp.signal + 1
        }
      }

      class TopLevel extends Component {
        val sub = new SubComponent()

        // Here we create plugins and embed them in sub.host
        new DriverPlugin().setHost(sub.host)
        new StatePlugin().setHost(sub.host)
      }

Such TopLevel would generate the following Verilog code : 

.. code-block:: verilog

    module TopLevel (
      input  wire          clk,
      input  wire          reset
    );


      SubComponent sub (
        .clk   (clk  ), // i
        .reset (reset)  // i
      );

    endmodule

    module SubComponent (
      input  wire          clk,
      input  wire          reset
    );

      reg        [31:0]   StatePlugin_logic_signal; // Created by StatePlugin

      always @(posedge clk) begin
        StatePlugin_logic_signal <= (StatePlugin_logic_signal + 32'h00000001); // incremented by DriverPlugin
      end
    endmodule

Note each "during build" fork an elaboration thread, the DriverPlugin.logic thread execution will be blocked on the "sp" evaluation until the StatePlugin.logic execution is done.


Interlocking / Ordering
----------------------------------------

Plugins can interlock each others using Retainer instances.
Each plugin instance has a built in lock which can be controlled using retain/release functions.

Here is an example based on the above `Simple example` but that time, the DriverPlugin will increment the StatePlugin.logic.signal
by an amount set by other plugins (SetupPlugin in our case). And to ensure that the DriverPlugin doesn't generate the hardware too early, 
the SetupPlugin uses the DriverPlugin.retain/release functions.

.. code-block:: scala

  import spinal.core._
  import spinal.lib.misc.plugin._
  import spinal.core.fiber._

  class SubComponent extends Component {
    val host = new PluginHost()
  }

  class StatePlugin extends FiberPlugin {
    val logic = during build new Area {
      val signal = Reg(UInt(32 bits))
    }
  }

  class DriverPlugin extends FiberPlugin {
    // incrementBy will be set by others plugin at elaboration time
    var incrementBy = 0
    // retainer allows other plugins to create locks, on which this plugin will wait before using incrementBy
    val retainer = Retainer()

    val logic = during build new Area {
      val sp = host[StatePlugin].logic.get
      retainer.await()

      // Generate the incrementer hardware
      sp.signal := sp.signal + incrementBy
    }
  }

  // Let's define a plugin which will modify the DriverPlugin.incrementBy variable because letting it elaborate its hardware
  class SetupPlugin extends FiberPlugin {
    // during setup { body } will spawn the body of code in the Fiber setup phase (it is before the Fiber build phase)
    val logic = during setup new Area {
      // *** Setup phase code ***
      val dp = host[DriverPlugin]

      // Prevent the DriverPlugin from executing its build's body (until release() is called)
      val lock = dp.retainer()
      // Wait until the fiber phase reached build phase
      awaitBuild()

      // *** Build phase code ***
      // Let's mutate DriverPlugin.incrementBy
      dp.incrementBy += 1

      // Allows the DriverPlugin to execute its build's body
      lock.release()
    }
  }

  class TopLevel extends Component {
    val sub = new SubComponent()

    sub.host.asHostOf(
      new DriverPlugin(),
      new StatePlugin(),
      new SetupPlugin(),
      new SetupPlugin() // Let's add a second SetupPlugin, because we can
    )
  }

Here is the generated verilog

.. code-block:: verilog

    module TopLevel (
      input  wire          clk,
      input  wire          reset
    );


      SubComponent sub (
        .clk   (clk  ), // i
        .reset (reset)  // i
      );

    endmodule

    module SubComponent (
      input  wire          clk,
      input  wire          reset
    );

      reg        [31:0]   StatePlugin_logic_signal;

      always @(posedge clk) begin
        StatePlugin_logic_signal <= (StatePlugin_logic_signal + 32'h00000002); // + 2 as we have two SetupPlugin
      end
    endmodule

Clearly, those examples are overkilled for what they do, the idea in general is more about : 

- Negotiate / create interfaces between plugins (ex jump / flush ports)
- Schedule the elaboration (ex decode / dispatch specification)
- Provide a distributed framework which can scale up (minimal hardcoding)
