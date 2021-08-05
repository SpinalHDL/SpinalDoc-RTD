=============
Design errors
=============

.. toctree::
   :glob:

   *


Introduction
============

The SpinalHDL compiler will perform many checks on your design to be sure that the generated VHDL/Verilog will be safe for simulation and synthesis.
Basically, it should not be possible to generate a broken VHDL/Verilog design.
Below is a non-exhaustive list of SpinalHDL checks:

* Assignment overlapping
* Clock crossing
* Hierarchy violation
* Combinatorial loops
* Latches
* Undriven signals
* Width mismatch
* Unreachable switch statements

On each SpinalHDL error report, you will find a stack trace, which can be useful to accurately find out where the design error is.
These design checks may look like overkill at first glance, but they becomes invaluable as soon as you start to move away from the traditional way of doing hardware description.
