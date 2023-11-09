
Introduction
============

spinal.lib.misc.pipeline provide a pipelining API. The main advantages over manual pipelining are : 

- You don't have to pre define data structure with all the element which goes through each individual stage
- You can compose the pipeline (as a consequence of the above point)
- Manual retiming is much easier, as you don't have to handle the registers / arbitration manualy
- Manage the arbitration by itself

The API is composed of 4 main things : 

- Node : which represent a layer in the pipeline
- Connector : which allows to connect nodes to each others
- Builder : which will generate the hardware required for a whole pipeline
- NamedType : which are used to define a hardware signal which can go through the pipeline

It is important to understande that NamedType isn't a hardware data instance, but a key to retrieve a data along the pipeline on nodes.

Here is an example to illustrate : 

.. image:: /asset/image/pipeline/intro_pip.png
   :scale: 70 %

Simple example
----------------

Here is a simple example which only use the basics of the API :


.. code-block:: scala

    import spinal.core._
    import spinal.core.sim._
    import spinal.lib._
    import spinal.lib.misc.pipeline._

    class TopLevel extends Component {
      val io = new Bundle{
        val up = slave Stream (UInt(16 bits))
        val down = master Stream (UInt(16 bits))
      }

      // Let's define 3 Nodes for our pipeline
      val n0, n1, n2 = Node()

      // Let's connect those nodes by using simples registers
      val s01 = StageConnector(n0, n1)
      val s12 = StageConnector(n1, n2)

      // Let's define a few NamedType things that can go through the pipeline
      val VALUE = NamedType(UInt(16 bits))
      val RESULT = NamedType(UInt(16 bits))

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

.. image:: /asset/image/pipeline/simple_wave.png

Here is the same example but using more of the API :


.. code-block:: scala

    import spinal.core._
    import spinal.core.sim._
    import spinal.lib._
    import spinal.lib.misc.pipeline._

    class TopLevel extends Component {
      val VALUE = NamedType(UInt(16 bits))

      val io = new Bundle{
        val up = slave Stream(VALUE)  //VALUE can also be used as a HardType
        val down = master Stream(VALUE)
      }
      
      // NodesBuilder will be used to register all the nodes created, connect them via stages and generate the hardware
      val builder = new NodesBuilder()

      // Let's define a Node which connect from io.up
      val n0 = new builder.Node{
        arbitrateFrom(io.up)
        VALUE := io.up.payload
      }

      // Let's define a Node which do some processing
      val n1 = new builder.Node{
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

NamedType
============

NamedType class can be instanciated to represent some data which can go through the pipeline. Technicaly speaking, NamedType is a HardType which has a name and is used as a "key" to retrieve stuff.

.. code-block:: scala
    
    val PC = NamedType(UInt(32 bits))
    val PC_PLUS_4 = NamedType(UInt(32 bits))

    val n0, n1 = Node()
    val s01 = StageConnector(n0, n1)

    n0(PC) := 0x42
    n1(PC_PLUS_4) := n1(PC) + 4

Note that I got used to name the NamedType instances using uppercase. This is to make it very explicit that the thing isn't a hardware signal, but are more like a "key/type" to access things.

Node
============

Node mostly host the valid/ready arbitration signal, and the hardware signal required for all the NamedType values going through it.

You can access its arbitration via :


.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - API
     - Description
   * - node.valid
     - Is the signal which specify if a transaction is present on the node
   * - node.ready
     - Is the signal which specify if the node transaction can move away.
   * - node.isValid
     - node.valid's read only accessor
   * - node.isReady
     - node.ready's read only accessor
   * - node.isFiring
     - True when the node transaction is successfuly moving futher (isValid && isReady && !isRemoved). Usefull to commit state changes
   * - node.isMoving
     - True when the node transaction is moving (isValid && (isReady || isRemoved)). Usefull to "reset" states
   * - node.isRemoved
     - True when the node is being flushed

You can access its NamedType's signals via : 

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - API
     - Description
   * - node(NamedType)
     - Return the corresponding hardware signal
   * - node(NamedType, Any)
     - Same as above, but include a second argument which is used as a "secondary key". This ease the construction of multi lane hardware. For instance, when you have a multi issue CPU pipeline, you can use the lane Int id as secondary key
   * - node.insert(Data)
     - Return a new NamedType instance which is connected to the given Data hardware signal



.. code-block:: scala
    
    val n0, n1 = Node()

    val PC = NamedType(UInt(32 bits))
    n0(PC) := 0x42
    n0(PC, "true") := 0x42
    n0(PC, 0x666) := 0xEE
    val SOMETHING = n0.insert(myHardwareSignal) //This create a new NamedType
    when(n1(SOMETHING) === 0xFFAA){ ... }
    

Also, there is an API to define nodes which are always valid / ready 

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - API
     - Description
   * - node.setAlwaysValid()
     - Specify that the valid signal of the given node is always True. To use on the first node of a pipeline
   * - node.setAlwaysReady()
     - Specify that the ready signal of the given node is always True. To use on the last node of a pipeline, usefull if you don't have to implement backpresure.

.. code-block:: scala
    
    val n0, n1, n2 = Node()
    val OUT = NamedType(UInt(16 bits))

    val outputFlow = master Flow(UInt(16 bits))
    outputFlow.valid := n2.valid
    outputFlow.payload := n2(OUT)
    n2.setAlwaysReady() // Equivalent to n2.ready := True, but also notify the pipeline elaboration about it, leading to eventual optimisations

While you can manualy drive/read the arbitration/data of the first/last stage of your pipeline, there is a few utilities to connect its boundaries.


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
     - Drive a node from a stream. The provided landa function can be use to connect the data
   * - node.driveFrom(Flow[T]])((Node, T) => Unit)
     - Same as above but for Flow
   * - node.driveTo(Stream[T]])((T, Node) => Unit)
     - Drive a stream from the node. The provided landa function can be use to connect the data
   * - node.driveTo(Flow[T]])((T, Node) => Unit)
     - Same as above but for Flow


.. code-block:: scala
    
    val n0, n1, n2 = Node()

    val IN = NamedType(UInt(16 bits))
    val OUT = NamedType(UInt(16 bits))

    n1(OUT) := n1(IN) + 0x42

    // Define the input / output stream that will be later connected to the pipeline
    val up = slave Stream(UInt(16 bits))
    val down = master Stream(UInt(16 bits)) //Note master Stream(OUT) is good aswell

    n0.driveFrom(up)((self, payload) => self(IN) := payload)
    n2.driveTo(down)((payload, self) => payload := self(OUT))


In order to reduce verbosity, there is a set of implicit convertions between NamedType toward their data representation which can be used when you are in the context of a Node : 

.. code-block:: scala

    val VALUE = NamedType(UInt(16 bits))
    val n1 = new Node{
        val PLUS_ONE = insert(VALUE + 1) // VALUE is implicitly converted into its n1(VALUE) representation
    }

You can also use those implicit convertions by importing them : 

.. code-block:: scala

    val VALUE = NamedType(UInt(16 bits))
    val n1 = Node()

    val n1Stuff = new Area {
        import n1._
        val PLUS_ONE = insert(VALUE) + 1 // Equivalent to n1.insert(n1(VALUE)) + 1
    }


There is also an API which alows you to create new Area which provide the whole API of a given node instance (including implicit convertion) without import : 

.. code-block:: scala

    val n1 = Node()
    val VALUE = NamedType(UInt(16 bits))

    val n1Stuff = new n1.Area{
        val PLUS_ONE = insert(VALUE) + 1 // Equivalent to n1.insert(n1(VALUE)) + 1
    }

Such feature is very usefull when you have parametrable pipelines locations for your hardware (see retiming example).


Connectors
============

There is few different connectors already implemented (but you could also create your own custom one) :

DirectConnector
------------------

Very simple, it connect two nodes with wires only. Here is an example : 


.. code-block:: scala
    
    val c01 = DirectConnector(n0, n1)



StageConnector
------------------

This connect two nodes using registers on the data / valid signals and some arbitration on the ready.

.. code-block:: scala
    
    val c01 = StageConnector(n0, n1)


S2mConnector
------------------

This connect two nodes using registers on the ready signal, which can be usefull to improve backpresure combinatorial timings.

.. code-block:: scala
    
    val c01 = S2mConnector(n0, n1)

CtrlConnector
------------------

This is kind of a special connector, as connect two nodes with optional flow control / bypass logic. Its API should be flexible enough to implement a CPU stage with it.

Here is its flow control API (The Bool argument enable the feature) :

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - API
     - Description
   * - haltWhen(Bool)
     - Allows to block the current transaction (clear up.ready down.valid)
   * - throwWhen(Bool)
     - Allows to remove the current transaction from the pipeline (clear down.valid and remove the transaction driver)
   * - removeSeedWhen(Bool)
     - Allows to remove the transaction driver (but doesn't clear the down.valid)
   * - duplicateWhen(Bool)
     - Allows to duplicate the current transaction (clear up.ready)
   * - terminateWhen(Bool)
     - Allows to hide the current transaction from downstream (clear down.valid)

Also note that if you want to do flow control in a conditional scope (ex in a when statement), you can call the following functions :

- haltIt(), duplicateIt(), terminateIt(), removeSeedIt(), throwIt()

.. code-block:: scala
    
    val c01 = CtrlConnector(n0, n1)

    c01.haltWhen(something)
    when(somethingElse){
        c01.haltIt()
    }

You can retrieve which node are connected using node.up / node.down.

The CtrlConnector also provide an API to access NamedType :

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - API
     - Description
   * - connector(NamedType)
     - Same as connector.down(NamedType)
   * - connector(NamedType, Any)
     - Same as connector.down(NamedType, Any)
   * - connector.insert(Data)
     - Same as connector.down.insert(Data)
   * - connector.bypass(NamedType)
     - Allows to conditionaly override a NamedType value between connector.up -> connector.down. This can be used to fix data hazard in CPU pipelines for instance.


.. code-block:: scala
    
    val c01 = CtrlConnector(n0, n1)

    val PC = NamedType(UInt(32 bits))
    c01(PC) := 0x42
    c01(PC, 0x666) := 0xEE

    val DATA = NamedType(UInt(32 bits))
    // Let's say Data is inserted in the pipeline before c01
    when(hazard){
        c01.bypass(DATA) := fixedValue
    }
    
    // c01(DATA) and below will get the hazard patch

Note that if you create a CtrlConnector without node arguements, it will create its own nodes internaly.

.. code-block:: scala

    val decode = CtrlConnector()
    val execute = CtrlConnector()

    val d2e = StageConnector(decode.down, execute.up)


Other connectors
------------------------------------

There is also a JoinConnector / ForkConnector implemented.

Your custom connector
------------------------------------

You can implement your custom connectors by implementing the Connector base class.

.. code-block:: scala

    trait Connector extends Area{
      def ups : Seq[Node]
      def downs : Seq[Node]

      def propagateDown(): Unit
      def propagateUp(): Unit
      def build() : Unit
    }


Builder
============

To generate the hardware of your pipeline, you need to give a list of all the connectors used in your pipeline.


.. code-block:: scala

      // Let's define 3 Nodes for our pipeline
      val n0, n1, n2 = Node()

      // Let's connect those nodes by using simples registers
      val s01 = StageConnector(n0, n1)
      val s12 = StageConnector(n1, n2)

      // Let's ask the builder to generate all the required hardware
      Builder(s01, s12)

There is also a set of "all in one" builders that you can instanciate to help yourself. 

For instance there is the NodesBuilder class which can be used to create sequencialy staged pipelines : 

.. code-block:: scala
  
      val builder = new NodesBuilder()

      // Let's define a few nodes
      val n0, n1, n2 = new builder.Node

      // Let's connect those nodes by using registers and generate the related hardware
      builder.genStagedPipeline()

Composability
========================

One good thing about the API, is that it easily allows to compose a pipeline with multiple parallel things. What i mean by "compose" is that sometime the pipeline you need to design has parallel processing to do. 

Imagine you need to do floating point multiplication on 4 pairs of numbers (to later sum them). If those 4 pairs a provided at the same time by a single stream of data, then you don't want 4 different pipeline to multiple them, instead you want to process them all in parallel in the same pipeline.

The example below show a pattern which compose a pipeline with multiple lanes to process them in parallel.


.. code-block:: scala

    // This area allows to take a input value and do +1 +1 +1 over 3 stages.
    // I know that's useless, but let's pretend that instead it does a multiplication between two numbers over 3 stages (for FMax reasons)
    class PLus3(INPUT: NamedType[UInt], stage1: Node, stage2: Node, stage3: Node) extends Area {
      val ONE = stage1.insert(stage1(INPUT) + 1)
      val TWO = stage2.insert(stage2(ONE) + 1)
      val THREE = stage3.insert(stage3(TWO) + 1)
    }

    // Let's define a component which takes a stream as input, 
    // which carries 'lanesCount' values that we want to process in parallel
    // and put the result on an output stream
    class TopLevel(lanesCount : Int) extends Component {
      val io = new Bundle{
        val up = slave Stream(Vec.fill(lanesCount)(UInt(16 bits))) 
        val down = master Stream(Vec.fill(lanesCount)(UInt(16 bits)))
      }

      // Let's define 3 Nodes for our pipeline
      val n0, n1, n2 = Node()

      // Let's connect those nodes by using simples registers
      val s01 = StageConnector(n0, n1)
      val s12 = StageConnector(n1, n2)

      // Let's bind io.up to n0
      n0.arbitrateFrom(io.up)
      val LANES_INPUT = io.up.payload.map(n0.insert(_))

      // Let's use our "reusable" Plus3 area to generate each processing lane
      val lanes = for(i <- 0 until lanesCount) yield new PLus3(LANES_INPUT(i), n0, n1, n2)

      // Let's bind n2 to io.down
      n2.arbitrateTo(io.down)
      for(i <- 0 until lanesCount) io.down.payload(i) := n2(lanes(i).THREE)

      // Let's ask the builder to generate all the required hardware
      Builder(s01, s12)
    }

This will produce the following data path (assuming lanesCount = 2), abitration not being shown :

.. image:: /asset/image/pipeline/composable_lanes.png
   :scale: 70 %


Retiming / Variable lenth
================================================

Sometime you want to design a pipeline, but you don't realy know where will be the critical paths / right balance between stages, and you can't realy rely on the synthesis tool doing a good job with automatic retiming.

So, you kind of need a easy way to move around the logic of your pipeline.

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

      // multiplie the inverted bits with 0xEE
      val multiplier = new mulNode.Area {
        val MUL = insert(inverter.INV*0xEE)
      }

      // Connect the end of the pipeline to the io.down stream
      val resulter = new resultNode.Area {
        arbitrateTo(io.down)
        io.down.payload := multiplier.MUL
      }

      // Let's connect those nodes sequencialy by using simples registers
      val connectors = for (i <- 0 to resultAt - 1) yield StageConnector(nodes(i), nodes(i + 1))

      // Let's ask the builder to generate all the required hardware
      Builder(connectors)
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
      wire                when_StageConnector_l56;
      wire                when_StageConnector_l56_1;
      wire                when_StageConnector_l56_2;

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
        if(when_StageConnector_l56) begin
          nodes_0_ready = 1'b1;
        end
      end

      assign when_StageConnector_l56 = (! nodes_1_valid);
      always @(*) begin
        nodes_1_ready = nodes_2_ready;
        if(when_StageConnector_l56_1) begin
          nodes_1_ready = 1'b1;
        end
      end

      assign when_StageConnector_l56_1 = (! nodes_2_valid);
      always @(*) begin
        nodes_2_ready = nodes_3_ready;
        if(when_StageConnector_l56_2) begin
          nodes_2_ready = 1'b1;
        end
      end

      assign when_StageConnector_l56_2 = (! nodes_3_valid);
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


Also, you can easily tweek how many stages and where you want the processing to be done, for instance you may want to move the invertion hardware in the same stage as the adder. This can be done the following way : 


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


