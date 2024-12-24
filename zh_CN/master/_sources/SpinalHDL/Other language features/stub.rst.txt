
Stub
====

You can empty an Component Hierarchy as stub:

.. code-block:: scala 

    class SubSysModule extends Component {
       val io = new Bundle {
         val dx = slave(Stream(Bits(32 bits)))
         val dy = master(Stream(Bits(32 bits)))
       }
       io.dy <-< io.dx
    }
    class TopLevel extends Component {
       val dut = new SubSysModule().stub   // instance an SubSysModule as empty stub
    }
   
It will generate the following Verilog code for example:

.. code-block:: verilog

    module SubSysModule (
      input               io_dx_valid,
      output              io_dx_ready,
      input      [31:0]   io_dx_payload,
      output              io_dy_valid,
      input               io_dy_ready,
      output     [31:0]   io_dy_payload,
      input               clk,
      input               reset
    );


      assign io_dx_ready = 1'b0;
      assign io_dy_valid = 1'b0;
      assign io_dy_payload = 32'h0;

    endmodule


You can also empty the top Component

.. code-block:: scala

    SpinalVerilog(new Pinsec(500 MHz).stub)

What does `stub` do ?

* first walk all the components and find out clock, then keep clock 
* then remove all children component
* then remove all assignment and logic we don't want 
* tile 0 to output port


