.. role:: raw-html-m2r(raw)
   :format: html

Preserving names
==================

Introduction
------------

This page will describe how SpinalHDL propagate names from the scala code to the generated hardware. Knowing them should enable you to preserve those names as much as possible to generate understandable netlists.

Nameable base class
------------------------------------------

All the things which can be named in SpinalHDL extends the Nameable base class which.

So in practice, the following classes extends Nameable :

- Component
- Area
- Data (UInt, SInt, Bundle, ...)


There is a short previous of the Nameable API :

.. code-block:: scala

    val toto = UInt(8 bits)
    val miaou = Bool()
    toto.setName("rawrr") // Force name
    toto.setName("rawrr", weak = true) // Propose a name, will not be applied if a stronger name is already applied
    toto.setCompositeName(miaou, postfix = "wuff") // Force toto to be named as miaou.getName() + _wuff"

In general, you don't realy need to access that API, unless you want to do tricky stuff for debug reasons or for elaboration purposes.

Name extraction from Scala
------------------------------------------

First, since version 1.4.0, SpinalHDL use a scala compiler plugin which can provide a call back each time a new val is defined during the construction of an class.

There is a example showing more or less how SpinalHDL itself is implemented :

.. code-block:: scala

    //spinal.idslplugin.ValCallback is the Scala compiler plugin feature which will provide the callbacks
    class Component extends spinal.idslplugin.ValCallback{
      override def valCallback[T](ref: T, name: String) : T = {
        println(s"Got $ref named $name") // Here we just print what we got as a demo.
        ref
      }
    }

    class UInt
    class Bits
    class MyComponent extends Component{
      val two = 2
      val wuff = "miaou"
      val toto = new UInt
      val rawrr = new Bits
    }

    object Debug3 extends App{
      new MyComponent()
      // ^ This will print :
      // Got 2 named two
      // Got miaou named wuff
      // Got spinal.tester.code.sandbox.UInt@691a7f8f named toto
      // Got spinal.tester.code.sandbox.Bits@161b062a named rawrr
    }

Using that ValCallback "introspection" feature, SpinalHDL's Component classes are able to be aware of their content and its name.

But this also mean that if you want something to get a name, and you only rely on this automatic naming feature, the reference to your Data (UInt, SInt, ...) instances should be stored somewhere in a Component val.

For instance :

.. code-block:: scala

  class MyComponent extends Component {
    val a,b = in UInt(8 bits) // Will be properly named
    val toto = out UInt(8 bits)   // same

    def doStuff(): Unit = {
      val tmp = UInt(8 bits) // This will not be named, as it isn't stored anywhere in a component val (but there is a solution explained later)
      tmp := 0x20
      toto := tmp
    }
    doStuff()
  }

Will generate :

.. code-block:: verilog

    module MyComponent (
      input      [7:0]    a,
      input      [7:0]    b,
      output     [7:0]    toto
    );
      //Note that the tmp signal defined in scala was "shortcuted" by SpinalHDL, as it was unamed and technicaly "shortcutable"
      assign toto = 8'h20;
    endmodule


Area in a Component
--------------------

One important aspect in the naming system is that you can define new namespaces inside components and manipulate

For instance via Area :

.. code-block:: scala

    class MyComponent extends Component {
      val logicA = new Area{   //This define a new namespace named "logicA
        val toggle = Reg(Bool) //This register will be named "logicA_toggle"
        toggle := !toggle
      }
    }

Will generate

.. code-block:: verilog

    module MyComponent (
      input               clk,
      input               reset
    );
      reg                 logicA_toggle;
      always @ (posedge clk) begin
        logicA_toggle <= (! logicA_toggle);
      end
    endmodule

Area in a function
--------------------

You can also define function which will create new Area which will provide a namespace for all its content :

.. code-block:: scala

  class MyComponent extends Component {
    def isZero(value: UInt) = new Area {
      val comparator = value === 0
    }

    val value = in UInt (8 bits)
    val someLogic = isZero(value)

    val result = out Bool()
    result := someLogic.comparator
  }

Which will generate :

.. code-block:: verilog

    module MyComponent (
      input      [7:0]    value,
      output              result
    );
      wire                someLogic_comparator;

      assign someLogic_comparator = (value == 8'h0);
      assign result = someLogic_comparator;

    endmodule

Composite in a function
----------------------------------------------

Added in SpinalHDL 1.5.0, Composite which allow you to create a scope which will use as prefix another Nameable:

.. code-block:: scala

  class MyComponent extends Component {
    //Basicaly, a Composite is an Area that use its construction parameter as namespace prefix
    def isZero(value: UInt) = new Composite(value) {
      val comparator = value === 0
    }.comparator  //Note we don't return the Composite, but the element of the composite that we are interested in

    val value = in UInt (8 bits)
    val result = out Bool()
    result := isZero(value)
  }

Will generate :

.. code-block:: verilog

    module MyComponent (
      input      [7:0]    value,
      output              result
    );
      wire                value_comparator;

      assign value_comparator = (value == 8'h0);
      assign result = value_comparator;

    endmodule

Composite chains
----------------------------

You can also chain composites :

.. code-block:: scala

  class MyComponent extends Component {
    def isZero(value: UInt) = new Composite(value) {
      val comparator = value === 0
    }.comparator


    def inverted(value: Bool) = new Composite(value) {
      val inverter = !value
    }.inverter

    val value = in UInt(8 bits)
    val result = out Bool()
    result := inverted(isZero(value))
  }

Will generate :

.. code-block:: verilog

    module MyComponent (
      input      [7:0]    value,
      output              result
    );
      wire                value_comparator;
      wire                value_comparator_inverter;

      assign value_comparator = (value == 8'h0);
      assign value_comparator_inverter = (! value_comparator);
      assign result = value_comparator_inverter;

    endmodule

Composite in a Bundle's function
------------------------------------


This behaviour can be very useful when implementing Bundles utilities. For instance in the spinal.lib.Stream class is defined the following :

.. code-block:: scala

    class Stream[T <: Data](val payloadType :  HardType[T]) extends Bundle {
      val valid   = Bool()
      val ready   = Bool()
      val payload = payloadType()

      def queue(size: Int): Stream[T] = new Composite(this){
        val fifo = new StreamFifo(payloadType, size)
        fifo.io.push << self    // 'self' refer to the Composite construction argument (this in that example). It avoid having to do a boring 'Stream.this'
      }.fifo.io.pop

      def m2sPipe(): Stream[T] = new Composite(this) {
        val m2sPipe = Stream(payloadType)

        val rValid = RegInit(False)
        val rData = Reg(payloadType)

        self.ready := (!m2sPipe.valid) || m2sPipe.ready

        when(self.ready) {
          rValid := self.valid
          rData := self.payload
        }

        m2sPipe.valid := rValid
        m2sPipe.payload := rData
      }.m2sPipe
    }

Which allow nested calls while preserving the names :

.. code-block:: scala

  class MyComponent extends Component {
    val source = slave(Stream(UInt(8 bits)))
    val sink = master(Stream(UInt(8 bits)))
    sink << source.queue(size = 16).m2sPipe()
  }

Will generate

.. code-block:: verilog

    module MyComponent (
      input               source_valid,
      output              source_ready,
      input      [7:0]    source_payload,
      output              sink_valid,
      input               sink_ready,
      output     [7:0]    sink_payload,
      input               clk,
      input               reset
    );
      wire                source_fifo_io_pop_ready;
      wire                source_fifo_io_push_ready;
      wire                source_fifo_io_pop_valid;
      wire       [7:0]    source_fifo_io_pop_payload;
      wire       [4:0]    source_fifo_io_occupancy;
      wire       [4:0]    source_fifo_io_availability;
      wire                source_fifo_io_pop_m2sPipe_valid;
      wire                source_fifo_io_pop_m2sPipe_ready;
      wire       [7:0]    source_fifo_io_pop_m2sPipe_payload;
      reg                 source_fifo_io_pop_rValid;
      reg        [7:0]    source_fifo_io_pop_rData;

      StreamFifo source_fifo (
        .io_push_valid      (source_valid                 ), //i
        .io_push_ready      (source_fifo_io_push_ready    ), //o
        .io_push_payload    (source_payload               ), //i
        .io_pop_valid       (source_fifo_io_pop_valid     ), //o
        .io_pop_ready       (source_fifo_io_pop_ready     ), //i
        .io_pop_payload     (source_fifo_io_pop_payload   ), //o
        .io_flush           (1'b0                         ), //i
        .io_occupancy       (source_fifo_io_occupancy     ), //o
        .io_availability    (source_fifo_io_availability  ), //o
        .clk                (clk                          ), //i
        .reset              (reset                        )  //i
      );
      assign source_ready = source_fifo_io_push_ready;
      assign source_fifo_io_pop_ready = ((1'b1 && (! source_fifo_io_pop_m2sPipe_valid)) || source_fifo_io_pop_m2sPipe_ready);
      assign source_fifo_io_pop_m2sPipe_valid = source_fifo_io_pop_rValid;
      assign source_fifo_io_pop_m2sPipe_payload = source_fifo_io_pop_rData;
      assign sink_valid = source_fifo_io_pop_m2sPipe_valid;
      assign source_fifo_io_pop_m2sPipe_ready = sink_ready;
      assign sink_payload = source_fifo_io_pop_m2sPipe_payload;
      always @ (posedge clk or posedge reset) begin
        if (reset) begin
          source_fifo_io_pop_rValid <= 1'b0;
        end else begin
          if(source_fifo_io_pop_ready)begin
            source_fifo_io_pop_rValid <= source_fifo_io_pop_valid;
          end
        end
      end

      always @ (posedge clk) begin
        if(source_fifo_io_pop_ready)begin
          source_fifo_io_pop_rData <= source_fifo_io_pop_payload;
        end
      end
    endmodule


Unamed signal handling
----------------------------------------

Since 1.5.0, for signal which end up without name, SpinalHDL will find a signal which is driven by that unamed signal and propagate its name. This can produce useful results as long you don't have too large island of unamed stuff.

The name attributed to such unamed signal is : _zz_ + drivenSignal.getName()

Note that this naming pattern is also used by the generation backend when they need to breakup some specific expressions or long chain of expression into multiple signals.