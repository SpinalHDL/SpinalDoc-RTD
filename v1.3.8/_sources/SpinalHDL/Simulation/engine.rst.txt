

========================
Simulation engine
========================

This page explain the internals of the simulation engine. 

Basicaly, the simulation engine emulate a event-driven simulator (VHDL/Verilog like) by applying the following simulation loop on the top of the Verilator C++ simulation model :

.. image:: /asset/picture/simEngine.png
   :align: center

At a low level, the simulation engine manage the following primitives :

 - Sensitive callbacks, which allow to call a function on each simulation delta cycle
 - Delayed callbacks, which allow to call a function in a future simulation time
 - Simulation threads, which allow to describe concurrent with many times utilities
 - Command buffer, which allow to delay the write access to the dut to the end of the current delta cycle

There is some practical uses of those primitives :
 - Sensitive callbacks can be used to wakeup a simulation thread when a given condition happen, like a rising edge on a clock
 - Delayed callbacks can be used to schedule stimulus as desaserting a reset after a given time or toggle the clock
 - Both sensitive and delayed callbacks can be used to resume a simulation thread
 - A simulation thread can be used (for instance) to produce stimulus and check the dut output values 
 - The command buffer purpose is mainly avoid all concurrency issues between the DUT and the testbench



