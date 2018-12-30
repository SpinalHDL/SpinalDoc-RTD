==========
Simulation
==========

.. toctree::
   :hidden:
   :glob:
   
   install
   bootstraps
   signal
   clock
   threadFull
   threadLess
   engine
   examples/index

.. role:: raw-html-m2r(raw)
   :format: html


Introduction
------------

As always you can use your standard simulation tools to simulate the VHDL/Verilog generated out from SpinalHDL, but since SpinalHDL 1.0.0 the language integrate an API to write testbenches and test your hardware directly in Scala. This API provide the capabilities to read and write the DUT signals, fork and join simulation processes, sleep and wait until a given condition is filled.

How SpinalHDL simulate the hardware
-----------------------------------

| Behind the scene SpinalHDL is generating a C++ cycle accurate model of your hardware by generating the equivalent Verilog and asking Verilator to convert it into a C++ model.
| Then SpinalHDL ask GCC to compile the C++ model into an shared object (.so) and bind it back to Scala via JNR-FFI.
| Finaly, as the native Verilator API is rude, SpinalHDL abstract it by providing an simulation multi-threaded API to help the user in his testbench implementation.

This method has several advantage :

* The C++ simulation model is realy fast to process simulation steps
* It test the generated Verilog hardware instead of the SpinalHDL internal model
* It doesn't require SpinalHDL to be able itself to simulate the hardware (Less codebase, less bugs as Verilator is a reliable tool)

And some limitations :


* Verilator will only accept to translate Synthetisable Verilog code

Performance
-----------

As verilator is currently the simulation backend, the simulation speed is realy high. On a little SoC like `Murax <https://github.com/SpinalHDL/VexRiscv>`_ my laptop is capable to simulate 1.2 million clock cycles per realtime seconds.

