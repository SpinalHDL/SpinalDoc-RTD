
Carry adder
===========

This example defines a component with inputs ``a`` and ``b``\ , and a ``result`` output.
At any time, ``result`` will be the sum of ``a`` and ``b`` (combinatorial).
This sum is manually done by a carry adder logic.

.. code-block:: scala

   class CarryAdder(size : Int) extends Component{
     val io = new Bundle{
       val a = in UInt(size bits)
       val b = in UInt(size bits)
       val result = out UInt(size bits)      //result = a + b
     }

     var c = False                   //Carry, like a VHDL variable
     for (i <- 0 until size) {
       //Create some intermediate value in the loop scope.
       val a = io.a(i)
       val b = io.b(i)

       //The carry adder's asynchronous logic
       io.result(i) := a ^ b ^ c
       c \= (a & b) | (a & c) | (b & c);    //variable assignment
     }
   }


   object CarryAdderProject {
     def main(args: Array[String]) {
       SpinalVhdl(new CarryAdder(4))
     }
   }
