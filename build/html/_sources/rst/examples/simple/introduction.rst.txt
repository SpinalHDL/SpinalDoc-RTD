
All examples assume that you have the following imports on the top of your scala file:

.. code-block:: scala

   import spinal.core._
   import spinal.lib._

To generate VHDL for a given component, you can place the following at the bottom of your scala file:

.. code-block:: scala

   object MyMainObject {
     def main(args: Array[String]) {
       SpinalVhdl(new TheComponentThatIWantToGenerate(constructionArguments))   //Or SpinalVerilog
     }
   }
