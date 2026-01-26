
Report
======

You can add debugging in RTL for simulation, using the following syntax:

.. code-block:: scala

    object Enum extends SpinalEnum {
        val MIAOU, RAWRR = newElement()
    }

    class TopLevel extends Component {
        val a = Enum.RAWRR()
        val b = U(0x42)
        val c = out(Enum.RAWRR())
        val d = out (U(0x42))
        report(Seq("miaou ", a, b, c, d))
    }

It will generate the following Verilog code for example:

.. code-block:: verilog

    $display("NOTE miaou %s%x%s%x", a_string, b, c_string, d);

Since SpinalHDL 1.4.4, the following syntax is also supported:

.. code-block:: scala

    report(L"miaou $a $b $c $d")

You can display the current simulation time using the `REPORT_TIME` object:

.. code-block:: scala

    report(L"miaou $REPORT_TIME")

will result in:

.. code-block:: verilog

    $display("NOTE miaou %t", $time);

Including Scala Source Location
-------------------------------

After 1.13.0, You can optionally include the Scala call-site location (file and line) in the report output, which allows most IDEs to jump to the source when clicking the log line.

There are two ways to enable it:

.. code-block:: scala

    // Per-call
    report("HELLO", includeSourceLocation = true)

    // Globally (for all `report(...)` calls in this elaboration)
    SpinalConfig(reportIncludeSourceLocation = true).generateVerilog(new TopLevel)

Example generated output (Verilog):

.. code-block:: verilog

    $display("path/to/MyTopLevel.scala:123: NOTE HELLO");

**Automatic Handling of Scala Primitive Types (SpinalHDL ^1.12.2)**

You can embed Scala primitive types (e.g., `Int`, `Boolean`, `Float`, `BigInt`, `BigDecimal`, `Char`, `Byte`, `Short`, `Long`) within `L""` interpolated strings without explicit `.toString()` calls.

.. code-block:: scala

    val myInt = 123
    val myBool = True
    val myFloat = 3.14f
    val myBigInt = BigInt(0xABCD)
    report(L"My values: int=$myInt, bool=$myBool, float=$myFloat, bigInt=$myBigInt")

.. code-block:: scala

    for (i <- 0 until cacheConfig.fetchWordsPerFetchGroup) {
      report(L"AdvICache: sCompareTags - Instruction ${i}: ${io.cpu.rsp.payload.instructions(i)}")
    }

**Structured Reporting for Complex Data Types (like Bundles) (SpinalHDL ^1.12.2)**

For `Bundle`s or other complex data structures, you can define a `Formattable` trait and implement a `format()` method returning `Seq[Any]` to define a structured, nested representation. This allows for clean, one-line reporting of entire complex objects.

First, define a `Formattable` trait and implement it in your Bundles:

.. code-block:: scala

    trait Formattable {
      def format(): Seq[Any]
    }

    case class DataPayload() extends Bundle with Formattable {
      val value = UInt(16 bits)
      val checksum = UInt(8 bits)
      override def format(): Seq[Any] = Seq(L"DataPayload(value=0x${value}, checksum=0x${checksum})")
    }

    case class PacketHeader() extends Bundle with Formattable {
      val packetLength = UInt(8 bits)
      val packetType = UInt(4 bits)
      val payload = DataPayload()
      override def format(): Seq[Any] = Seq(
        L"PacketHeader(",
        L"packetLength=0x${packetLength},",
        L" packetType=0x${packetType},",
        L" payload=${payload.format},", // Nested format call
        L")"
      ).flatten
    }

Then, you can report the entire structure:

.. code-block:: scala

    class MyComponent extends Component {
      val io = PacketHeader() // Assume io is an instance of PacketHeader
      // ... some logic ...
      report(io.format)
    }

This will produce a compact, readable output like:

.. code-block:: text

    PacketHeader(packetLength=0x0c, packetType=0x1, payload=DataPayload(value=0x5678, checksum=0x78))
