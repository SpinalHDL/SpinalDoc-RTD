
Formal
==========

General
-------

There is limited support for SystemVerilog Assertions (SVA).

You can add formal statements (assume, assert, etc.) in the ``Component`` definition, like in the example below:

.. code-block:: scala
 
    class TopLevel extends Component {
      val io = new Bundle {
        val ready = in Bool
        val valid = out Bool
      }
      val valid = RegInit(False)

     when(io.ready) {
       valid := False
     }
     io.valid <> valid
     // some logic

     import spinal.core.GenerationFlags._
     import spinal.core.Formal._

     GenerationFlags.formal {
       when(initstate()) {
         assume(clockDomain.isResetActive)
         assume(io.ready === False)
       }.otherwise {
         assert(!(valid.fall && !io.ready))
       }
     }
    }

To generate a design which includes the formal statements you can use ``includeFormal``:

.. code-block:: scala

   object MyToplevelSystemVerilogWithFormal {
    def main(args: Array[String]) {
      val config = SpinalConfig(defaultConfigForClockDomains = ClockDomainConfig(resetKind=SYNC, resetActiveLevel=HIGH))
       config.includeFormal.generateSystemVerilog(new TopLevel())
     }
   }

Supported features
------------------
 .. list-table::
    :header-rows: 1
    :widths: 3 1 3

    * - Syntax
      - Returns
      - Creates in SystemVerilog
    * - ``assert()``
      -
      - ``assert()``
    * - ``cover()``
      -
      - ``cover()``
    * - | ``past(that : T, delay : Int)``
        | ``past(that : T)``
      - T
      - ``past(that)``
    * - ``rose(that : Bool)``
      - Bool
      - ``rose(that)``
    * - ``fell(that : Bool)``
      - Bool
      - fell(that)
    * - ``changed(that : Bool)``
      - Bool
      - ``changed(that)``
    * - ``stable(that : Bool)``
      - Bool
      - ``stable(that)``
    * - ``initstate()``
      - Bool
      - ``$initstate()``

Limitations
-----------
No support for unclocked assertions.
Everything that is described in ``GenerationFlags.formal`` will be generated in a clocked process.
