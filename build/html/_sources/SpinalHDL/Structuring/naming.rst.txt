Preserving names
================

This page will describe how SpinalHDL propagate names from the scala code to the generated hardware. Knowing them should enable you to preserve those names as much as possible to generate understandable netlists.

Nameable base class
-------------------

All the things which can be named in SpinalHDL extends the Nameable base class which.

So in practice, the following classes extends Nameable :

- Component
- Area
- Data (UInt, SInt, Bundle, ...)


There is a few example of that Nameable API

.. code-block:: scala

  class MyComponent extends Component {
    val a, b, c, d = Bool()
    b.setName("rawrr") // Force name
    c.setName("rawrr", weak = true) // Propose a name, will not be applied if a stronger name is already applied
    d.setCompositeName(b, postfix = "wuff") // Force toto to be named as b.getName() + _wuff"
  }

Will generation :

.. code-block:: verilog

    module MyComponent (
    );
      wire                a;
      wire                rawrr;
      wire                c;
      wire                rawrr_wuff;
    endmodule

In general, you don't really need to access that API, unless you want to do tricky stuff for debug reasons or for elaboration purposes.

Name extraction from Scala
--------------------------

First, since version 1.4.0, SpinalHDL use a scala compiler plugin which can provide a call back each time a new val is defined during the construction of an class.

There is a example showing more or less how SpinalHDL itself is implemented :

.. code-block:: scala

    // spinal.idslplugin.ValCallback is the Scala compiler plugin feature which will provide the callbacks
    class Component extends spinal.idslplugin.ValCallback {
      override def valCallback[T](ref: T, name: String) : T = {
        println(s"Got $ref named $name") // Here we just print what we got as a demo.
        ref
      }
    }

    class UInt
    class Bits
    class MyComponent extends Component {
      val two = 2
      val wuff = "miaou"
      val toto = new UInt
      val rawrr = new Bits
    }

    object Debug3 extends App {
      new MyComponent()
      // ^ This will print :
      // Got 2 named two
      // Got miaou named wuff
      // Got spinal.tester.code.sandbox.UInt@691a7f8f named toto
      // Got spinal.tester.code.sandbox.Bits@161b062a named rawrr
    }

Using that ValCallback "introspection" feature, SpinalHDL's Component classes are able to be aware of their content and the content's name.

But this also mean that if you want something to get a name, and you only rely on this automatic naming feature, the reference to your Data (UInt, SInt, ...) instances should be stored somewhere in a Component val.

For instance :

.. code-block:: scala

  class MyComponent extends Component {
    val a,b = in UInt(8 bits) // Will be properly named
    val toto = out UInt(8 bits)   // same

    def doStuff(): Unit = {
      val tmp = UInt(8 bits) // This will not be named, as it isn't stored anywhere in a
                             // component val (but there is a solution explained later)
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
      // Note that the tmp signal defined in scala was "shortcuted" by SpinalHDL,
      //  as it was unnamed and technically "shortcutable"
      assign toto = 8'h20;
    endmodule


Area in a Component
-------------------

One important aspect in the naming system is that you can define new namespaces inside components and manipulate

For instance via Area :

.. code-block:: scala

    class MyComponent extends Component {
      val logicA = new Area {    // This define a new namespace named "logicA
        val toggle = Reg(Bool()) // This register will be named "logicA_toggle"
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
------------------

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
    // Basically, a Composite is an Area that use its construction parameter as namespace prefix
    def isZero(value: UInt) = new Composite(value) {
      val comparator = value === 0
    }.comparator  // Note we don't return the Composite,
                  //  but the element of the composite that we are interested in

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
----------------

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
--------------------------------


This behavior can be very useful when implementing Bundle utilities. For instance in the spinal.lib.Stream class is defined the following :

.. code-block:: scala

    class Stream[T <: Data](val payloadType :  HardType[T]) extends Bundle {
      val valid   = Bool()
      val ready   = Bool()
      val payload = payloadType()

      def queue(size: Int): Stream[T] = new Composite(this) {
        val fifo = new StreamFifo(payloadType, size)
        fifo.io.push << self    // 'self' refers to the Composite construction argument ('this' in
                                //  the example). It avoids having to do a boring 'Stream.this'
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


Unnamed signal handling
-----------------------

Since 1.5.0, for signal which end up without name, SpinalHDL will find a signal which is driven by that unnamed signal and propagate its name. This can produce useful results as long you don't have too large island of unnamed stuff.

The name attributed to such unnamed signal is : _zz_ + drivenSignal.getName()

Note that this naming pattern is also used by the generation backend when they need to breakup some specific expressions or long chain of expression into multiple signals.

Verilog expression splitting
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

There is an instance of expressions (ex : the + operator) that SpinalHDL need to express in dedicated signals to match the behavior with the Scala API :

.. code-block:: scala

  class MyComponent extends Component {
    val a,b,c,d = in UInt(8 bits)
    val result = a + b + c + d
  }

Will generate

.. code-block:: verilog

    module MyComponent (
      input      [7:0]    a,
      input      [7:0]    b,
      input      [7:0]    c,
      input      [7:0]    d
    );
      wire       [7:0]    _zz_result;
      wire       [7:0]    _zz_result_1;
      wire       [7:0]    result;

      assign _zz_result = (_zz_result_1 + c);
      assign _zz_result_1 = (a + b);
      assign result = (_zz_result + d);

    endmodule

Verilog long expression splitting
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

There is a instance of how a very long expression chain will be split up by SpinalHDL :

.. code-block:: scala

  class MyComponent extends Component {
    val conditions = in Vec(Bool(), 64)
    // Perform a logical OR between all the condition elements
    val result = conditions.reduce(_ || _)

    // For Bits/UInt/SInt signals the 'orR' methods implements this reduction operation
  }

Will generate

.. code-block:: verilog

    module MyComponent (
      input               conditions_0,
      input               conditions_1,
      input               conditions_2,
      input               conditions_3,
      ...
      input               conditions_58,
      input               conditions_59,
      input               conditions_60,
      input               conditions_61,
      input               conditions_62,
      input               conditions_63
    );
      wire                _zz_result;
      wire                _zz_result_1;
      wire                _zz_result_2;
      wire                result;

      assign _zz_result = ((((((((((((((((_zz_result_1 || conditions_32) || conditions_33) || conditions_34) || conditions_35) || conditions_36) || conditions_37) || conditions_38) || conditions_39) || conditions_40) || conditions_41) || conditions_42) || conditions_43) || conditions_44) || conditions_45) || conditions_46) || conditions_47);
      assign _zz_result_1 = ((((((((((((((((_zz_result_2 || conditions_16) || conditions_17) || conditions_18) || conditions_19) || conditions_20) || conditions_21) || conditions_22) || conditions_23) || conditions_24) || conditions_25) || conditions_26) || conditions_27) || conditions_28) || conditions_29) || conditions_30) || conditions_31);
      assign _zz_result_2 = (((((((((((((((conditions_0 || conditions_1) || conditions_2) || conditions_3) || conditions_4) || conditions_5) || conditions_6) || conditions_7) || conditions_8) || conditions_9) || conditions_10) || conditions_11) || conditions_12) || conditions_13) || conditions_14) || conditions_15);
      assign result = ((((((((((((((((_zz_result || conditions_48) || conditions_49) || conditions_50) || conditions_51) || conditions_52) || conditions_53) || conditions_54) || conditions_55) || conditions_56) || conditions_57) || conditions_58) || conditions_59) || conditions_60) || conditions_61) || conditions_62) || conditions_63);

    endmodule

When statement condition
~~~~~~~~~~~~~~~~~~~~~~~~

The `when(cond) { }` statements condition are generated into separated signals named `when_` + fileName + line. A similar thing will also be done for switch statements.

.. code-block:: scala

  // In file Test.scala
  class MyComponent extends Component {
    val value = in UInt(8 bits)
    val isZero = out(Bool())
    val counter = out(Reg(UInt(8 bits)))

    isZero := False
    when(value === 0) { // At line 117
      isZero := True
      counter := counter + 1
    }
  }

Will generate

.. code-block:: verilog

    module MyComponent (
      input      [7:0]    value,
      output reg          isZero,
      output reg [7:0]    counter,
      input               clk,
      input               reset
    );
      wire                when_Test_l117;

      always @ (*) begin
        isZero = 1'b0;
        if(when_Test_l117)begin
          isZero = 1'b1;
        end
      end

      assign when_Test_l117 = (value == 8'h0);
      always @ (posedge clk) begin
        if(when_Test_l117)begin
          counter <= (counter + 8'h01);
        end
      end
    endmodule





In last resort
~~~~~~~~~~~~~~

In last resort, if a signal has no name (anonymous signal), SpinalHDL will seek for a named signal which is driven by the anonymous signal, and use it as a name postfix :

.. code-block:: scala

  class MyComponent extends Component {
    val enable = in Bool()
    val value = out UInt(8 bits)

    def count(cond : Bool): UInt = {
      val ret = Reg(UInt(8 bits)) // This register is not named (on purpose for the example)
      when(cond) {
        ret := ret + 1
      }
      return ret
    }

    value := count(enable)
  }

Will generate

.. code-block:: verilog

    module MyComponent (
      input               enable,
      output     [7:0]    value,
      input               clk,
      input               reset
    );
      // Name given to the register in last resort by looking what was driven by it
      reg        [7:0]    _zz_value;

      assign value = _zz_value;
      always @ (posedge clk) begin
        if(enable)begin
          _zz_value <= (_zz_value + 8'h01);
        end
      end
    endmodule

This last resort naming skim isn't ideal in all cases, but can help out.

Note that signal starting with a underscore aren't stored in the Verilator waves (on purpose)
