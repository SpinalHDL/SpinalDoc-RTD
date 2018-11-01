.. role:: raw-html-m2r(raw)
   :format: html


RISCV tool-chain
----------------

Binaries executed by the CPU can be defined in ASM/C/C++ and compiled by the GCC RISCV fork. Also, to load binaries and debug the CPU, an OpenOCD fork and RISCV GDB can be used.

RISCV official tools : `https://riscv.org/software-tools/ <https://riscv.org/software-tools/>`_\ :raw-html-m2r:`<br>`
OpenOCD fork : `https://github.com/Dolu1990/openocd_riscv <https://github.com/Dolu1990/openocd_riscv>`_\ :raw-html-m2r:`<br>`
Software examples : `https://github.com/Dolu1990/pinsecSoftware <https://github.com/Dolu1990/pinsecSoftware>`_\ :raw-html-m2r:`<br>`

OpenOCD/GDB/Eclipse  configuration
----------------------------------

About the OpenOCD fork, there is the configuration file that could be used to connect the Pinsec SoC : `https://github.com/Dolu1990/openocd_riscv/blob/riscv_spinal/tcl/target/riscv_spinal.cfg <https://github.com/Dolu1990/openocd_riscv/blob/riscv_spinal/tcl/target/riscv_spinal.cfg>`_

There is an example of arguments used to run the OpenOCD tool :

.. code-block:: text

   openocd -f ../tcl/interface/ftdi/ft2232h_breakout.cfg -f ../tcl/target/riscv_spinal.cfg -d 3

To debug with eclipse, you will need the Zylin plugin and then create an "Zynlin embedded debug (native)".

Initialize commands :

.. code-block:: text

   target remote localhost:3333
   monitor reset halt
   load

Run commands :

.. code-block:: text

   continue
