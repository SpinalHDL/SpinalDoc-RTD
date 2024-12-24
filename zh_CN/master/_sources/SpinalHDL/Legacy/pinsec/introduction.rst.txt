.. role:: raw-html-m2r(raw)
   :format: html

.. _pinsec_introduction:

Introduction
============

.. note::
   This page only documents the SoC implemented with the first generation of RISC-V CPU created in SpinalHDL.
   This page does not document the VexRiscV CPU, which is the second generation of this SoC (and CPU) is available
   `here <https://github.com/SpinalHDL/VexRiscv>`__ and offers better performance/area/features.

Introduction
------------

Pinsec is the name of a little FPGA SoC fully written in SpinalHDL. Goals of this project are multiple :


* Prove that SpinalHDL is a viable HDL alternative in non-trivial projects.
* Show advantage of SpinalHDL meta-hardware description capabilities in a concrete project.
* Provide a fully open source SoC.


Pinsec has followings hardware features:


* AXI4 interconnect for high speed busses
* APB3 interconnect for peripherals
* RISCV CPU with instruction cache, MUL/DIV extension and interrupt controller
* JTAG bridge to load binaries and debug the CPU
* SDRAM SDR controller
* On chip ram
* One UART controller
* One VGA controller
* Some timer module
* Some GPIO

The toplevel code explanation could be find :ref:`there <pinsec_hardware_toplevel>`

Board support
-------------

A DE1-SOC FPGA project can be find `here <https://drive.google.com/drive/folders/0B-CqLXDTaMbKOGhIU0JGdHVVSk0?usp=sharing>`__ with some demo binaries.
