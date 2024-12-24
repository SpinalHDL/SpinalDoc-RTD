
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

You can display the current simulation time using the REPORT_TIME object

.. code-block:: scala

    report(L"miaou $REPORT_TIME")

will result in:

.. code-block:: verilog

    $display("NOTE miaou %t", $time);
