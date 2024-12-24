
RiscV
=====

.. warning::
   This page only documents the first generation of RISC-V CPU created in SpinalHDL. 
   This page does not document the VexRiscV CPU, which is the second generation of this CPU and is available
   `here <https://github.com/SpinalHDL/VexRiscv>`_ and offers better performance/area/features.

Features
--------

RISC-V CPU


* Pipelined on 5 stages (Fetch Decode Execute0 Execute1 WriteBack)
* Multiple branch prediction modes : (disable, static or dynamic)
* Data path parameterizable between fully bypassed to fully interlocked

Extensions


* One cycle multiplication
* 34 cycle division
* Iterative shifter (N shift -> N cycles)
* Single cycle shifter
* Interruption controller
* Debugging module (with JTAG bridge, openOCD port and GDB)
* Instruction cache with wrapped burst memory interface, one way
* Data cache with instructions to evict/flush the whole cache or a given address, one way

Performance/Area (on cyclone II)


* small core -> 846 LE, 0.6 DMIPS/Mhz
* debug module (without JTAG) -> 240 LE
* JTAG Avalon master -> 238 LE
* big core with MUL/DIV/Full shifter/I$/Interrupt/Debug -> 2200 LE, 1.15 DMIPS/Mhz, at least 100 Mhz (with default synthesis option)

Base FPGA project
-----------------

You can find a DE1-SOC project which integrate two instance of the CPU with MUL/DIV/Full shifter/I$/Interrupt/Debug there :

https://drive.google.com/drive/folders/0B-CqLXDTaMbKNkktb2k3T3lzcUk?usp=sharing

CPU/JTAG/VGA IP are pre-generated.
Quartus Prime : 15.1.

How to generate the CPU VHDL
----------------------------

.. warning::
   This avalon version of the CPU isn't present in recent releases of SpinalHDL. Please consider the `VexRiscv <https://github.com/SpinalHDL/VexRiscv>`_ instead.

.. There is an example of a top level which generate an Altera QSys component that contains the CPU with Avalon interfaces and some timing buffer :

.. https://github.com/SpinalHDL/SpinalHDL/blob/master/lib/src/main/scala/spinal/lib/cpu/riscv/impl/CoreQSysAvalon.scala#L97

.. If you want to generate it, the easiest way is to get the https://github.com/SpinalHDL/SpinalTemplateSbt and call ``QSysAvalonCore.main(null)`` from your main function.

How to debug
------------

You can find the openOCD fork here :

https://github.com/Dolu1990/openocd_riscv

An example target configuration file could be find here :

https://github.com/Dolu1990/openocd_riscv/blob/riscv_spinal/tcl/target/riscv_spinal.cfg

Then you can use the RISCV GDB.

Todo
----


* Documentation
* Optimize instruction/data caches FMax by moving line hit condition forward into combinatorial paths.

Contact spinalhdl@gmail.com for more information
