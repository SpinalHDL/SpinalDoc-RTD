Thread-full API
===============

In SpinalSim, you can write your testbench by using multiple threads in a similar way to SystemVerilog, and a bit like VHDL/Verilog process/always blocks. 
This allows you to write concurrent tasks and control the simulation time using a fluent API.


Fork and join simulation threads
--------------------------------

.. code-block:: scala

   // Create a new thread
   val myNewThread = fork {
     // New simulation thread body
   }

   // Wait until `myNewThread` is execution is done.
   myNewThread.join()

Sleep and waitUntil
-------------------

.. code-block:: scala

   // Sleep 1000 units of time
   sleep(1000)

   // waitUntil the dut.io.a value is bigger than 42 before continuing
   waitUntil(dut.io.a > 42)

