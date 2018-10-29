
VexRiscv is an fpga friendly RISC-V ISA CPU implementation with following features :


* RV32IM instruction set
* Pipelined on 5 stages (Fetch, Decode, Execute, Memory, WriteBack)
* 1.44 DMIPS/Mhz when all features are enabled
* Optimized for FPGA
* Optional MUL/DIV extension
* Optional instruction and data caches
* Optional MMU
* Optional debug extension allowing eclipse debugging via an GDB >> openOCD >> JTAG connection
* Optional interrupts and exception handling with the Machine and the User mode from the riscv-privileged-v1.9.1 spec.
* Two implementation of shift instructions, Single cycle / shiftNumber cycles
* Each stage could have bypass or interlock hazard logic
* FreeRTOS port https://github.com/Dolu1990/FreeRTOS-RISCV

Much more information there : `https://github.com/SpinalHDL/VexRiscv <https://github.com/SpinalHDL/VexRiscv>`_
