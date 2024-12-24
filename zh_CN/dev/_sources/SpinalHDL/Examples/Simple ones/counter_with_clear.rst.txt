
Counter with clear
==================

This example defines a component with a ``clear`` input and a ``value`` output.
Each clock cycle, the ``value`` output is incrementing, but when ``clear`` is high, ``value`` is cleared.

.. code-block:: scala

   class Counter(width : Int) extends Component{
     val io = new Bundle{
       val clear = in Bool()
       val value = out UInt(width bits)
     }
     val register = Reg(UInt(width bits)) init(0)
     register := register + 1
     when(io.clear){
       register := 0
     }
     io.value := register
   }
