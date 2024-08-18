
Introduction
============

spinal.lib.misc.pipeline provides a pipelining API. The main advantages over manual pipelining are : 

- You don't have to predefine all the signal elements needed for the entire staged system upfront. You can create and consume stagable signals in a more ad hoc fashion as your design requires - without needing to refactor all the intervening stages to know about the signal
- Signals of the pipeline can utilize the powerful parametrization capabilities of SpinalHDL and be subject to optimization/removal if a specific design build does not require a particular parametrized feature, without any need to modify the staging system design or project code base in a significant way.
- Manual retiming is much easier, as you don't have to handle the registers / arbitration manually
- Manage the arbitration by itself

The API is composed of 4 main things : 

- Node : which represents a layer in the pipeline
- Link : which allows to connect nodes to each other
- Builder : which will generate the hardware required for a whole pipeline
- Payload : which are used to retrieve hardware signals on nodes along the pipeline

It is important to understand that Payload isn't a hardware data/signal instance, but a key to retrieve a data/signal on nodes along the pipeline, and that the pipeline builder will then automatically interconnect/pipeline every occurrence of a given Payload between nodes.

Here is an example to illustrate : 

.. image:: /asset/image/pipeline/intro_pip.png
   :scale: 70 %


Here is a video about this API : 

- https://www.youtube.com/watch?v=74h_-FMWWIM

Simple example
--------------

Here is a simple example which only uses the basics of the API :


.. code-block:: scala

    import spinal.core._
    import spinal.core.sim._
    import spinal.lib._
    import spinal.lib.misc.pipeline._

    class TopLevel extends Component {
      val io = new Bundle {
        val up = slave Stream (UInt(16 bits))
        val down = master Stream (UInt(16 bits))
      }

      // Let's define 3 Nodes for our pipeline
      val n0, n1, n2 = Node()

      // Let's connect those nodes by using simples registers
      val s01 = StageLink(n0, n1)
      val s12 = StageLink(n1, n2)

      // Let's define a few Payload things that can go through the pipeline
      val VALUE = Payload(UInt(16 bits))
      val RESULT = Payload(UInt(16 bits))

      // Let's bind io.up to n0
      io.up.ready := n0.ready
      n0.valid := io.up.valid
      n0(VALUE) := io.up.payload

      // Let's do some processing on n1
      n1(RESULT) := n1(VALUE) + 0x1200

      // Let's bind n2 to io.down
      n2.ready := io.down.ready
      io.down.valid := n2.valid
      io.down.payload := n2(RESULT)

      // Let's ask the builder to generate all the required hardware
      Builder(s01, s12)
    }

This will produce the following hardware : 

.. image:: /asset/image/pipeline/simple_pip.png
   :scale: 70 %

Here is a simulation wave : 

.. wavedrom::

   {signal: [
     {name: 'clk', wave: '0...p........'},
     {name: 'reset', wave: '1..0.........'},
     {name: 'io_up_valid', wave: '0.....10.....'},
     {},
     {name: 'n0_valid', wave: '0.....10.....'},
     {name: 'n0_VALUE', wave: 'x.....2......', data: ['0042']},
     {},
     {name: 'n1_valid', wave: '0......10....'},
     {name: 'n1_VALUE', wave: 'x......2.....', data: ['0042']},
     {name: 'n1_RESULT', wave: 'x......2.....', data: ['1242']},
     {},
     {name: 'n2_valid', wave: '0.......10...'},
     {name: 'n2_RESULT', wave: 'x.......2....', data: ['1242']},
     {},
     {name: 'io_down_valid', wave: '0.......10...'},
   ]}

Here is the same example but using more of the API :


.. code-block:: scala

    import spinal.core._
    import spinal.core.sim._
    import spinal.lib._
    import spinal.lib.misc.pipeline._

    class TopLevel extends Component {
      val VALUE = Payload(UInt(16 bits))

      val io = new Bundle {
        val up = slave Stream(VALUE)  // VALUE can also be used as a HardType
        val down = master Stream(VALUE)
      }
      
      // NodesBuilder will be used to register all the nodes created, connect them via stages and generate the hardware
      val builder = new NodesBuilder()

      // Let's define a Node which connect from io.up
      val n0 = new builder.Node {
        arbitrateFrom(io.up)
        VALUE := io.up.payload
      }

      // Let's define a Node which do some processing
      val n1 = new builder.Node {
        val RESULT = insert(VALUE + 0x1200)
      }

      //  Let's define a Node which connect to io.down
      val n2 = new builder.Node {
        arbitrateTo(io.down)
        io.down.payload := n1.RESULT
      }

      // Let's connect those nodes by using registers stages and generate the related hardware
      builder.genStagedPipeline()
    }

Payload
=======

Payload objects are used to refer to data which can go through the pipeline. Technically speaking, Payload is a HardType which has a name and is used as a "key" to retrieve the signals in a certain pipeline stage.

.. code-block:: scala
    
    val PC = Payload(UInt(32 bits))
    val PC_PLUS_4 = Payload(UInt(32 bits))

    val n0, n1 = Node()
    val s01 = StageLink(n0, n1)

    n0(PC) := 0x42
    n1(PC_PLUS_4) := n1(PC) + 4

Note that I got used to name the Payload instances using uppercase. This is to make it very explicit that the thing isn't a hardware signal, but are more like a "key/type" to access things.

Node
====

Node mostly hosts the valid/ready arbitration signals, and the hardware signals required for all the Payload values going through it.

You can access its arbitration via :


.. list-table::
   :header-rows: 1
   :widths: 2 1 10

   * - API
     - Access
     - Description
   * - node.valid
     - RW
     - Is the signal which specifies if a transaction is present on the node. It is driven by the upstream. Once asserted, it must only be de-asserted the cycle after which either both valid and ready or node.cancel are high. valid must not depend on ready.
   * - node.ready
     - RW
     - Is the signal which specifies if the node's transaction can proceed downstream. It is driven by the downstream to create backpressure. The signal has no meaning when there is no transaction (node.valid being deasserted)
   * - node.cancel
     - RW
     - Is the signal which specifies if the node's transaction in being canceled from the pipeline. It is driven by the downstream. The signal has no meaning when there is no transaction (node.valid being deasserted)
   * - node.isValid
     - RO
     - node.valid's read only accessor
   * - node.isReady
     - RO
     - node.ready's read only accessor
   * - node.isCancel
     - RO
     - node.cancel's read only accessor
   * - node.isFiring
     - RO
     - True when the node transaction is successfully moving further (valid && ready && !cancel). Useful to commit state changes.
   * - node.isMoving
     - RO
     - True when the node transaction will not be present anymore on the node (starting from the next cycle),
       either because downstream is ready to take the transaction,
       or because the transaction is canceled from the pipeline. (valid && (ready || cancel)). Useful to "reset" states.
   * - node.isCanceling
     - RO
     - True when the node transaction is being canceled. Meaning that it will not appear anywhere in the pipeline in future cycles.

Note that the node.valid/node.ready signals follows the same conventions than the :doc:`../stream`'s ones .

The Node controls (valid/ready/cancel) and status (isValid, isReady, isCancel, isFiring, ...) signals are created on demand.
So for instance you can create pipeline with no backpressure by never referring to the ready signal. That's why it is important to use status signals when you want to read the status of something and only use control signals when you to drive something.

Here is a list of arbitration cases you can have on a node. valid/ready/cancel define the state we are in, while isFiring/isMoving result of those :

+-------+-------+-----------+------------------------------+----------+----------+
| valid | ready | cancel    | Description                  | isFiring | isMoving |
+=======+=======+===========+==============================+==========+==========+
|   0   |   X   |     X     | No transaction               |    0     |    0     |
+-------+-------+-----------+------------------------------+----------+----------+
|   1   |   1   |     0     | Going through                |    1     |    1     |
+-------+-------+-----------+------------------------------+----------+----------+
|   1   |   0   |     0     | Blocked                      |    0     |    0     |
+-------+-------+-----------+------------------------------+----------+----------+
|   1   |   X   |     1     | Canceled                     |    0     |    1     |
+-------+-------+-----------+------------------------------+----------+----------+


Note that if you want to model things like for instance a CPU stage which can block and flush stuff, take a look a the CtrlLink, as it provides the API to do such things.

You can access signals referenced by a Payload via: 

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - API
     - Description
   * - node(Payload)
     - Return the corresponding hardware signal
   * - node(Payload, Any)
     - Same as above, but include a second argument which is used as a "secondary key". This eases the construction of multi-lane hardware. For instance, when you have a multi issue CPU pipeline, you can use the lane Int id as secondary key
   * - node.insert(Data)
     - Return a new Payload instance which is connected to the given Data hardware signal



.. code-block:: scala
    
    val n0, n1 = Node()

    val PC = Payload(UInt(32 bits))
    n0(PC) := 0x42
    n0(PC, "true") := 0x42
    n0(PC, 0x666) := 0xEE
    val SOMETHING = n0.insert(myHardwareSignal) // This create a new Payload
    when(n1(SOMETHING) === 0xFFAA){ ... }
    

While you can manually drive/read the arbitration/data of the first/last stage of your pipeline, there is a few utilities to connect its boundaries.


.. list-table::
   :header-rows: 1
   :widths: 5 5

   * - API
     - Description
   * - node.arbitrateFrom(Stream[T]])
     - Drive a node arbitration from a stream.
   * - node.arbitrateFrom(Flow[T]])
     - Drive a node arbitration from the Flow. 
   * - node.arbitrateTo(Stream[T]])
     - Drive a stream arbitration from the node. 
   * - node.arbitrateTo(Flow[T]])
     - Drive a Flow arbitration from the node. 
   * - node.driveFrom(Stream[T]])((Node, T) => Unit)
     - Drive a node from a stream. The provided lambda function can be use to connect the data
   * - node.driveFrom(Flow[T]])((Node, T) => Unit)
     - Same as above but for Flow
   * - node.driveTo(Stream[T]])((T, Node) => Unit)
     - Drive a stream from the node. The provided lambda function can be use to connect the data
   * - node.driveTo(Flow[T]])((T, Node) => Unit)
     - Same as above but for Flow


.. code-block:: scala
    
    val n0, n1, n2 = Node()

    val IN = Payload(UInt(16 bits))
    val OUT = Payload(UInt(16 bits))

    n1(OUT) := n1(IN) + 0x42

    // Define the input / output stream that will be later connected to the pipeline
    val up = slave Stream(UInt(16 bits))
    val down = master Stream(UInt(16 bits)) // Note master Stream(OUT) is good as well

    n0.driveFrom(up)((self, payload) => self(IN) := payload)
    n2.driveTo(down)((payload, self) => payload := self(OUT))


In order to reduce verbosity, there is a set of implicit conversions between Payload toward their data representation which can be used when you are in the context of a Node : 

.. code-block:: scala

    val VALUE = Payload(UInt(16 bits))
    val n1 = new Node {
        val PLUS_ONE = insert(VALUE + 1) // VALUE is implicitly converted into its n1(VALUE) representation
    }

You can also use those implicit conversions by importing them : 

.. code-block:: scala

    val VALUE = Payload(UInt(16 bits))
    val n1 = Node()

    val n1Stuff = new Area {
        import n1._
        val PLUS_ONE = insert(VALUE) + 1 // Equivalent to n1.insert(n1(VALUE)) + 1
    }


There is also an API which allows you to create new Area which provide the whole API of a given node instance (including implicit conversion) without import : 

.. code-block:: scala

    val n1 = Node()
    val VALUE = Payload(UInt(16 bits))

    val n1Stuff = new n1.Area {
        val PLUS_ONE = insert(VALUE) + 1 // Equivalent to n1.insert(n1(VALUE)) + 1
    }

Such feature is very useful when you have parametrizable pipeline locations for your hardware (see retiming example).


Links
=====

There is few different Links already implemented (but you could also create your own custom one).
The idea of Links is to connect two nodes together in various ways.
They generally have a `up` Node and a `down` Node.

DirectLink
----------

Very simple, it connect two nodes with wires only. Here is an example : 


.. code-block:: scala
    
    val c01 = DirectLink(n0, n1)



StageLink
---------

This connect two nodes using registers on the data / valid signals and some arbitration on the ready.

.. code-block:: scala
    
    val c01 = StageLink(n0, n1)


S2mLink
-------

This connect two nodes using registers on the ready signal, which can be useful to improve backpressure combinatorial timings.

.. code-block:: scala
    
    val c01 = S2mLink(n0, n1)

CtrlLink
--------

This is kind of a special Link, as connect two nodes with optional flow control / bypass logic. Its API should be flexible enough to implement a CPU stage with it.

Here is its flow control API (The Bool arguments enable the features) :

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - API
     - Description
   * - haltWhen(Bool)
     - Allows to block the current transaction (clear up.ready down.valid)
   * - throwWhen(Bool)
     - Allows to cancel the current transaction from the pipeline (clear down.valid and make the transaction driver forget its current state)
   * - forgetOneWhen(Bool)
     - Allows to request the upstream to forget its current transaction  (but doesn't clear the down.valid)
   * - ignoreReadyWhen(Bool)
     - Allows to ignore the downstream ready (set up.ready)
   * - duplicateWhen(Bool)
     - Allows to duplicate the current transaction (clear up.ready)
   * - terminateWhen(Bool)
     - Allows to hide the current transaction from downstream (clear down.valid)

Also note that if you want to do flow control in a conditional scope (ex in a when statement), you can call the following functions :

- haltIt(), duplicateIt(), terminateIt(), forgetOneNow(), ignoreReadyNow(), throwIt()

.. code-block:: scala
    
    val c01 = CtrlLink(n0, n1)

    c01.haltWhen(something) // Explicit halt request

    when(somethingElse) {
        c01.haltIt() // Conditional scope sensitive halt request, same as c01.haltWhen(somethingElse)
    }

You can retrieve which nodes are connected to the Link using node.up / node.down.

The CtrlLink also provide an API to access Payload :

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - API
     - Description
   * - link(Payload)
     - Same as Link.down(Payload)
   * - link(Payload, Any)
     - Same as Link.down(Payload, Any)
   * - link.insert(Data)
     - Same as Link.down.insert(Data)
   * - link.bypass(Payload)
     - Allows to conditionally override a Payload value between link.up -> link.down. This can be used to fix data hazard in CPU pipelines for instance.


.. code-block:: scala
    
    val c01 = CtrlLink(n0, n1)

    val PC = Payload(UInt(32 bits))
    c01(PC) := 0x42
    c01(PC, 0x666) := 0xEE

    val DATA = Payload(UInt(32 bits))
    // Let's say Data is inserted in the pipeline before c01
    when(hazard) {
        c01.bypass(DATA) := fixedValue
    }
    
    // c01(DATA) and below will get the hazard patch

Note that if you create a CtrlLink without node arguments, it will create its own nodes internally.

.. code-block:: scala

    val decode = CtrlLink()
    val execute = CtrlLink()

    val d2e = StageLink(decode.down, execute.up)


Other Links
-----------

There is also a JoinLink / ForkLink implemented.

Your custom Link
----------------

You can implement your custom links by implementing the Link base class.

.. code-block:: scala

    trait Link extends Area {
      def ups : Seq[Node]
      def downs : Seq[Node]

      def propagateDown(): Unit
      def propagateUp(): Unit
      def build() : Unit
    }

But that API may change a bit, as it is still fresh.

Builder
=======

To generate the hardware of your pipeline, you need to give a list of all the Links used in your pipeline.


.. code-block:: scala

      // Let's define 3 Nodes for our pipeline
      val n0, n1, n2 = Node()

      // Let's connect those nodes by using simples registers
      val s01 = StageLink(n0, n1)
      val s12 = StageLink(n1, n2)

      // Let's ask the builder to generate all the required hardware
      Builder(s01, s12)

There is also a set of "all in one" builders that you can instantiate to help yourself. 

For instance there is the NodesBuilder class which can be used to create sequentially staged pipelines : 

.. code-block:: scala
  
      val builder = new NodesBuilder()

      // Let's define a few nodes
      val n0, n1, n2 = new builder.Node

      // Let's connect those nodes by using registers and generate the related hardware
      builder.genStagedPipeline()

Composability
=============

One good thing about the API is that it easily allows to compose a pipeline with multiple parallel things. What i mean by "compose" is that sometime the pipeline you need to design has parallel processing to do. 

Imagine you need to do floating point multiplication on 4 pairs of numbers (to later sum them). If those 4 pairs a provided at the same time by a single stream of data, then you don't want 4 different pipelines to multiply them, instead you want to process them all in parallel in the same pipeline.

The example below show a pattern which composes a pipeline with multiple lanes to process them in parallel.


.. code-block:: scala

    // This area allows to take a input value and do +1 +1 +1 over 3 stages.
    // I know that's useless, but let's pretend that instead it does a multiplication between two numbers over 3 stages (for FMax reasons)
    class Plus3(INPUT: Payload[UInt], stage1: Node, stage2: Node, stage3: Node) extends Area {
      val ONE = stage1.insert(stage1(INPUT) + 1)
      val TWO = stage2.insert(stage2(ONE) + 1)
      val THREE = stage3.insert(stage3(TWO) + 1)
    }

    // Let's define a component which takes a stream as input, 
    // which carries 'lanesCount' values that we want to process in parallel
    // and put the result on an output stream
    class TopLevel(lanesCount : Int) extends Component {
      val io = new Bundle {
        val up = slave Stream(Vec.fill(lanesCount)(UInt(16 bits))) 
        val down = master Stream(Vec.fill(lanesCount)(UInt(16 bits)))
      }

      // Let's define 3 Nodes for our pipeline
      val n0, n1, n2 = Node()

      // Let's connect those nodes by using simples registers
      val s01 = StageLink(n0, n1)
      val s12 = StageLink(n1, n2)

      // Let's bind io.up to n0
      n0.arbitrateFrom(io.up)
      val LANES_INPUT = io.up.payload.map(n0.insert(_))

      // Let's use our "reusable" Plus3 area to generate each processing lane
      val lanes = for(i <- 0 until lanesCount) yield new Plus3(LANES_INPUT(i), n0, n1, n2)

      // Let's bind n2 to io.down
      n2.arbitrateTo(io.down)
      for(i <- 0 until lanesCount) io.down.payload(i) := n2(lanes(i).THREE)

      // Let's ask the builder to generate all the required hardware
      Builder(s01, s12)
    }

This will produce the following data path (assuming lanesCount = 2), arbitration not being shown :

.. image:: /asset/image/pipeline/composable_lanes.png
   :scale: 70 %


Retiming / Variable length
==========================

Sometime you want to design a pipeline, but you don't really know where the critical paths will be and what the right balance between stages is. And often you can't rely on the synthesis tool doing a good job with automatic retiming.

So, you kind of need a easy way to move the logic of your pipeline around.

Here is how it can be done with this pipelining API : 


.. code-block:: scala
    
    // Define a component which will take a input stream of RGB value
    // Process (~(R + G + B)) * 0xEE
    // And provide that result into an output stream
    class RgbToSomething(addAt : Int,
                         invAt : Int,
                         mulAt : Int,
                         resultAt : Int) extends Component {

      val io = new Bundle {
        val up = slave Stream(spinal.lib.graphic.Rgb(8, 8, 8))
        val down = master Stream (UInt(16 bits))
      }

      // Let's define the Nodes for our pipeline
      val nodes = Array.fill(resultAt+1)(Node())

      // Let's specify which node will be used for what part of the pipeline
      val insertNode = nodes(0)
      val addNode = nodes(addAt)
      val invNode = nodes(invAt)
      val mulNode = nodes(mulAt)
      val resultNode = nodes(resultAt)

      // Define the hardware which will feed the io.up stream into the pipeline
      val inserter = new insertNode.Area {
        arbitrateFrom(io.up)
        val RGB = insert(io.up.payload)
      }

      // sum the r g b values of the color
      val adder = new addNode.Area {
        val SUM = insert(inserter.RGB.r + inserter.RGB.g + inserter.RGB.b)
      }

      // flip all the bit of the RGB sum
      val inverter = new invNode.Area {
        val INV = insert(~adder.SUM)
      }

      // multiply the inverted bits with 0xEE
      val multiplier = new mulNode.Area {
        val MUL = insert(inverter.INV*0xEE)
      }

      // Connect the end of the pipeline to the io.down stream
      val resulter = new resultNode.Area {
        arbitrateTo(io.down)
        io.down.payload := multiplier.MUL
      }

      // Let's connect those nodes sequentially by using simples registers
      val links = for (i <- 0 to resultAt - 1) yield StageLink(nodes(i), nodes(i + 1))

      // Let's ask the builder to generate all the required hardware
      Builder(links)
    }

If then you generate this component like this : 

.. code-block:: scala
    
      SpinalVerilog(
        new RgbToSomething(
          addAt    = 0,
          invAt    = 1,
          mulAt    = 2,
          resultAt = 3
        )
      )

You will get a 4 stages separated by 3 layer of flip flop doing your processing : 

.. image:: /asset/image/pipeline/rgbToSomething.png
   :scale: 70 %

Note the generated hardware verilog is kinda clean (by my standards at least :P) : 

.. code-block:: verilog

    // Generator : SpinalHDL dev    git head : 1259510dd72697a4f2c388ad22b269d4d2600df7
    // Component : RgbToSomething
    // Git hash  : 63da021a1cd082d22124888dd6c1e5017d4a37b2

    `timescale 1ns/1ps

    module RgbToSomething (
      input  wire          io_up_valid,
      output wire          io_up_ready,
      input  wire [7:0]    io_up_payload_r,
      input  wire [7:0]    io_up_payload_g,
      input  wire [7:0]    io_up_payload_b,
      output wire          io_down_valid,
      input  wire          io_down_ready,
      output wire [15:0]   io_down_payload,
      input  wire          clk,
      input  wire          reset
    );

      wire       [7:0]    _zz_nodes_0_adder_SUM;
      reg        [15:0]   nodes_3_multiplier_MUL;
      wire       [15:0]   nodes_2_multiplier_MUL;
      reg        [7:0]    nodes_2_inverter_INV;
      wire       [7:0]    nodes_1_inverter_INV;
      reg        [7:0]    nodes_1_adder_SUM;
      wire       [7:0]    nodes_0_adder_SUM;
      wire       [7:0]    nodes_0_inserter_RGB_r;
      wire       [7:0]    nodes_0_inserter_RGB_g;
      wire       [7:0]    nodes_0_inserter_RGB_b;
      wire                nodes_0_valid;
      reg                 nodes_0_ready;
      reg                 nodes_1_valid;
      reg                 nodes_1_ready;
      reg                 nodes_2_valid;
      reg                 nodes_2_ready;
      reg                 nodes_3_valid;
      wire                nodes_3_ready;
      wire                when_StageLink_l56;
      wire                when_StageLink_l56_1;
      wire                when_StageLink_l56_2;

      assign _zz_nodes_0_adder_SUM = (nodes_0_inserter_RGB_r + nodes_0_inserter_RGB_g);
      assign nodes_0_valid = io_up_valid;
      assign io_up_ready = nodes_0_ready;
      assign nodes_0_inserter_RGB_r = io_up_payload_r;
      assign nodes_0_inserter_RGB_g = io_up_payload_g;
      assign nodes_0_inserter_RGB_b = io_up_payload_b;
      assign nodes_0_adder_SUM = (_zz_nodes_0_adder_SUM + nodes_0_inserter_RGB_b);
      assign nodes_1_inverter_INV = (~ nodes_1_adder_SUM);
      assign nodes_2_multiplier_MUL = (nodes_2_inverter_INV * 8'hee);
      assign io_down_valid = nodes_3_valid;
      assign nodes_3_ready = io_down_ready;
      assign io_down_payload = nodes_3_multiplier_MUL;
      always @(*) begin
        nodes_0_ready = nodes_1_ready;
        if(when_StageLink_l56) begin
          nodes_0_ready = 1'b1;
        end
      end

      assign when_StageLink_l56 = (! nodes_1_valid);
      always @(*) begin
        nodes_1_ready = nodes_2_ready;
        if(when_StageLink_l56_1) begin
          nodes_1_ready = 1'b1;
        end
      end

      assign when_StageLink_l56_1 = (! nodes_2_valid);
      always @(*) begin
        nodes_2_ready = nodes_3_ready;
        if(when_StageLink_l56_2) begin
          nodes_2_ready = 1'b1;
        end
      end

      assign when_StageLink_l56_2 = (! nodes_3_valid);
      always @(posedge clk or posedge reset) begin
        if(reset) begin
          nodes_1_valid <= 1'b0;
          nodes_2_valid <= 1'b0;
          nodes_3_valid <= 1'b0;
        end else begin
          if(nodes_0_ready) begin
            nodes_1_valid <= nodes_0_valid;
          end
          if(nodes_1_ready) begin
            nodes_2_valid <= nodes_1_valid;
          end
          if(nodes_2_ready) begin
            nodes_3_valid <= nodes_2_valid;
          end
        end
      end

      always @(posedge clk) begin
        if(nodes_0_ready) begin
          nodes_1_adder_SUM <= nodes_0_adder_SUM;
        end
        if(nodes_1_ready) begin
          nodes_2_inverter_INV <= nodes_1_inverter_INV;
        end
        if(nodes_2_ready) begin
          nodes_3_multiplier_MUL <= nodes_2_multiplier_MUL;
        end
      end


    endmodule


Also, you can easily tweak how many stages and where you want the processing to be done, for instance you may want to move the inversion hardware in the same stage as the adder. This can be done the following way : 


.. code-block:: scala
    
      SpinalVerilog(
        new RgbToSomething(
          addAt    = 0,
          invAt    = 0,
          mulAt    = 1,
          resultAt = 2
        )
      )

Then you may want to remove the output register stage : 

.. code-block:: scala
    
      SpinalVerilog(
        new RgbToSomething(
          addAt    = 0,
          invAt    = 0,
          mulAt    = 1,
          resultAt = 1
        )
      )


One thing about this example is the necessity intermediate val as `addNode`. I mean : 

.. code-block:: scala

      val addNode = nodes(addAt)
      // sum the r g b values of the color
      val adder = new addNode.Area {
        ...
      }

Unfortunately, scala doesn't allow to replace `new addNode.Area` with `new nodes(addAt).Area`.
One workaround is to define a class as : 

.. code-block:: scala

    class NodeArea(at : Int) extends NodeMirror(nodes(at))
    val adder = new NodeArea(addAt) {
        ...
    }

Depending the scale of your pipeline, it can payoff.

Simple CPU example
==================

Here is a simple/stupid 8 bits CPU example with : 

- 3 stages (fetch, decode, execute)
- embedded fetch memory
- add / jump / led /delay instructions

.. code-block:: scala

  class Cpu extends Component {
    val fetch, decode, execute = CtrlLink()
    val f2d = StageLink(fetch.down, decode.up)
    val d2e = StageLink(decode.down, execute.up)

    val PC = Payload(UInt(8 bits))
    val INSTRUCTION = Payload(Bits(16 bits))

    val led = out(Reg(Bits(8 bits))) init(0)

    val fetcher = new fetch.Area {
      val pcReg = Reg(PC) init (0)
      up(PC) := pcReg
      up.valid := True
      when(up.isFiring) {
        pcReg := PC + 1
      }

      val mem = Mem.fill(256)(INSTRUCTION).simPublic
      INSTRUCTION := mem.readAsync(PC)
    }

    val decoder = new decode.Area {
      val opcode = INSTRUCTION(7 downto 0)
      val IS_ADD   = insert(opcode === 0x1)
      val IS_JUMP  = insert(opcode === 0x2)
      val IS_LED   = insert(opcode === 0x3)
      val IS_DELAY = insert(opcode === 0x4)
    }


    val alu = new execute.Area {
      val regfile = Reg(UInt(8 bits)) init(0)
      
      val flush = False
      for (stage <- List(fetch, decode)) {
        stage.throwWhen(flush, usingReady = true)
      }

      val delayCounter = Reg(UInt(8 bits)) init (0)

      when(isValid) {
        when(decoder.IS_ADD) {
          regfile := regfile + U(INSTRUCTION(15 downto 8))
        }
        when(decoder.IS_JUMP) {
          flush := True
          fetcher.pcReg := U(INSTRUCTION(15 downto 8))
        }
        when(decoder.IS_LED) {
          led := B(regfile)
        }
        when(decoder.IS_DELAY) {
          delayCounter := delayCounter + 1
          when(delayCounter === U(INSTRUCTION(15 downto 8))) {
            delayCounter := 0
          } otherwise {
            execute.haltIt()
          }
        }
      }
    }

    Builder(fetch, decode, execute, f2d, d2e)
  }


Here is a simple testbench which implement a loop which will make the led counting up.

.. code-block:: scala

  SimConfig.withFstWave.compile(new Cpu).doSim(seed = 2){ dut =>
    def nop() = BigInt(0)
    def add(value: Int) = BigInt(1 | (value << 8))
    def jump(target: Int) = BigInt(2 | (target << 8))
    def led() = BigInt(3)
    def delay(cycles: Int) = BigInt(4 | (cycles << 8))
    val mem = dut.fetcher.mem
    mem.setBigInt(0, nop())
    mem.setBigInt(1, nop())
    mem.setBigInt(2, add(0x1))
    mem.setBigInt(3, led())
    mem.setBigInt(4, delay(16))
    mem.setBigInt(5, jump(0x2))

    dut.clockDomain.forkStimulus(10)
    dut.clockDomain.waitSampling(100)
  }



