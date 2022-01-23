=======================
Other language features
=======================

.. toctree::
   :hidden:

   utils
   stub
   assertion
   report
   scope_property
   formal
   analog_inout
   vhdl_generation


Introduction
============

Introduction
------------

The core of the language defines the syntax for many features:

* Types / Literals
* Register / Clock domains
* Component / Area
* RAM / ROM
* When / Switch / Mux
* BlackBox (to integrate VHDL or Verilog IPs inside Spinal)
* SpinalHDL to VHDL converter

Then, by using these features, you can define digital hardware, and also build powerful libraries and abstractions.
It's one of the major advantages of SpinalHDL over other commonly used HDLs, because you can extend the language without having knowledge about the compiler.

One good example of this is the :ref:`SpinalHDL lib <lib_introduction>` which adds many utilities, tools, buses, and methodologies.

To use features introduced in the following chapter you need to ``import spinal.core._`` in your sources.
