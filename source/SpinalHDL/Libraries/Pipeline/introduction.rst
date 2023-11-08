
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
- Stageable : which represent a data thing that you can retrieve Nodes the pipeline 

It is important to understande that Stageable isn't a hardware data instance, but a key to retrieve a data along the pipeline on nodes.

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

      // Let's define a few stageable things that can go through the pipeline
      val VALUE = Stageable(UInt(16 bits))
      val RESULT = Stageable(UInt(16 bits))

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
      val VALUE = Stageable(UInt(16 bits))
      
      val io = new Bundle{
        val up = slave Stream(VALUE)  // Stageable can also be used as a HardType
        val down = master Stream(VALUE)
      }

      // Let's define 3 Nodes for our pipeline
      val n0, n1, n2 = Node()

      // Let's connect those nodes by using simples registers
      val s01 = StageConnector(n0, n1)
      val s12 = StageConnector(n1, n2)

      // Let's bind io.up to n0
      n0.arbitrateFrom(io.up)
      n0(VALUE) := io.up.payload

      // Let's do some processing on n1
      val RESULT = n1.insert(n1(VALUE) + 0x1200)

      // Let's bind n2 to io.down
      n2.arbitrateTo(io.down)
      io.down.payload := n2(RESULT)

      // Let's ask the builder to generate all the required hardware
      Builder(s01, s12)
    }

Stageable
============

Stageable class can be instanciated to represent some data which can go through the pipeline. Technicaly speaking, Stageable is a HardType which has a name and is used as a "key" to retrieve stuff.

.. code-block:: scala
    
    val PC = Stageable(UInt(32 bits))
    val PC_PLUS_4 = Stageable(UInt(32 bits))

    val n0, n1 = Node()
    val s01 = StageConnector(n0, n1)

    n0(PC) := 0x42
    n1(PC_PLUS_4) := n1(PC) + 4

Note that I got used to name the Stageable instances using uppercase. This is to make it very explicit that the thing isn't a hardware signal, but are more like a "key" to access things.

Node
============

Node mostly host the valid/ready arbitration signal, and the hardware signal required for all the Stageable values going through it.

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
   * - node.isFireing
     - True when the node transaction is successfuly moving futher (isValid && isReady && !isRemoved). Usefull to commit state changes
   * - node.isMoving
     - True when the node transaction is moving (isValid && (isReady || isRemoved)). Usefull to "reset" states
   * - node.isRemoved
     - True when the node is being flushed

You can access its stageable's signals via : 

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - API
     - Description
   * - node(Stageable)
     - Return the corresponding hardware signal
   * - node(Stageable, Any)
     - Same as above, but include a second argument which is used as a "secondary key". This ease the construction of multi lane hardware. For instance, when you have a multi issue CPU pipeline, you can use the lane Int id as secondary key
   * - node.insert(Data)
     - Return a new Stageable instance which is connected to the given Data hardware signal



.. code-block:: scala
    
    val n0, n1 = Node()

    val PC = Stageable(UInt(32 bits))
    n0(PC) := 0x42
    n0(PC, "true") := 0x42
    n0(PC, 0x666) := 0xEE
    val SOMETHING = n0.insert(myHardwareSignal) //This create a new Stageable
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
    val OUT = Stageable(UInt(16 bits))

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

    val IN = Stageable(UInt(16 bits))
    val OUT = Stageable(UInt(16 bits))

    n1(OUT) := n1(IN) + 0x42

    // Define the input / output stream that will be later connected to the pipeline
    val up = slave Stream(UInt(16 bits))
    val down = master Stream(UInt(16 bits)) //Note master Stream(OUT) is good aswell

    n0.driveFrom(up)((self, payload) => self(IN) := payload)
    n2.driveTo(down)((payload, self) => payload := self(OUT))


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

The CtrlConnector also provide an API to access stageable :

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - API
     - Description
   * - connector(Stageable)
     - Same as connector.down(Stageable)
   * - connector(Stageable, Any)
     - Same as connector.down(Stageable, Any)
   * - connector.insert(Data)
     - Same as connector.down.insert(Data)
   * - connector.bypass(Stageable)
     - Allows to conditionaly override a stageable value between connector.up -> connector.down. This can be used to fix data hazard in CPU pipelines for instance.


.. code-block:: scala
    
    val c01 = CtrlConnector(n0, n1)

    val PC = Stageable(UInt(32 bits))
    c01(PC) := 0x42
    c01(PC, 0x666) := 0xEE

    val DATA = Stageable(UInt(32 bits))
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

