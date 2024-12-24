
Stub
======

You can emtpy an Component Hierarchy as stub:

.. code-block:: scala 

    class SubSysModule extends Component{
       val io = new Bundle{
         val dx = slave(Stream(Bits(32 bits)))
         val dy = master(Stream(Bits(32 bits)))
       }
       io.dy <-< io.dx
    }
    class TopLevle extends Component {
       val dut = new SubSysModule().stub   //instance an SubSysModule as empty stub
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


You can also emtpy the top Compoent

.. code-block:: scala

    SpinalVerilog(new Pinsec(500 MHz).stub)

what `stub` do

* first walk all the component and find out clock ,then keep clock 
* remove all children component
* reomove all assignment and logic we dont wan't 
* tile 0 to output port


