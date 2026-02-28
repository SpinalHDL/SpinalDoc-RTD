========
Examples
========

.. _example_list:

.. toctree::
   :hidden:
   :glob:

   Simple ones/index
   Intermediates ones/index
   Advanced ones/index


.. _example_introduction:

Examples are split into three kinds:

* Simple examples that could be used to get used to the basics of SpinalHDL.
* Intermediate examples which implement components by using a traditional approach.
* Advanced examples which go further than traditional HDL by using object-oriented programming, functional programming, and meta-hardware description.

They are all accessible in the sidebar under the corresponding sections.

.. important::
   The SpinalHDL workshop contains many labs with their solutions. See `here <https://github.com/SpinalHDL/SpinalWorkshop>`_.

.. note::
   You can also find a list of repositories using SpinalHDL :ref:`there <users_repositories>`


Getting started
===============
All examples assume that you have the following imports on the top of your scala file:

.. code-block:: scala

   import spinal.core._
   import spinal.lib._

To generate VHDL for a given component, you can place the following at the bottom of your scala file:

.. code-block:: scala

   object MyMainObject {
     def main(args: Array[String]) {
       SpinalVhdl(new TheComponentThatIWantToGenerate(constructionArguments))   // Or SpinalVerilog
     }
   }
