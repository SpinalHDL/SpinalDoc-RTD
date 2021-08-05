.. role:: raw-html-m2r(raw)
   :format: html

.. _pinsec_introduction:

Introduction
============

.. note::
   This page document the SoC implemented with the first RISC-V cpu iteration done in SpinalHDL. The second iteration of this SoC (and CPU) is available `there <https://github.com/SpinalHDL/VexRiscv>`_ and offer better perforance/area/features.

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

A DE1-SOC FPGA project can be find `there <https://drive.google.com/folderview?id=0B-CqLXDTaMbKOGhIU0JGdHVVSk0&usp=sharing>`_ with some demo binaries.
