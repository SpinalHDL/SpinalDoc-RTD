

=================
Simulation engine
=================

This page explains the internals of the simulation engine. 

The simulation engine emulates an event-driven simulator (VHDL/Verilog like) by applying the following simulation loop on the top of the Verilator C++ simulation model:

.. image:: /asset/picture/simEngine.png
   :align: center

At a low level, the simulation engine manages the following primitives:

 - *Sensitive callbacks*, which allow users to call a function on each simulation delta cycle.
 - *Delayed callbacks*, which allow users to call a function at a future simulation time.
 - *Simulation threads*, which allow users to describe concurrent processes.
 - *Command buffer*, which allows users to delay write access to the :abbr:`DUT (Device Under Test)` until the end of the current delta cycle.

There are some practical uses of those primitives:

 - Sensitive callbacks can be used to wake up a simulation thread when a given condition happens, like a rising edge on a clock.
 - Delayed callbacks can be used to schedule stimuli, such as deasserting a reset after a given time, or toggling the clock.
 - Both sensitive and delayed callbacks can be used to resume a simulation thread.
 - A simulation thread can be used (for instance) to produce stimulus and check the DUT's output values.
 - The command buffer's purpose is mainly to avoid all concurrency issues between the DUT and the testbench.



