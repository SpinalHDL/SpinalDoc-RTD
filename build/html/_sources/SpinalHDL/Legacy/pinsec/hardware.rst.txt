.. role:: raw-html-m2r(raw)
   :format: html

Hardware
========

Introduction
------------

There is the Pinsec toplevel hardware diagram :

.. image:: /asset/picture/pinsec_hardware.svg
   :align: center

RISCV
-----

The RISCV is a 5 stage pipelined CPU with following features :


* Instruction cache
* Single cycle Barrel shifter
* Single cycle MUL, 34 cycle DIV
* Interruption support
* Dynamic branch prediction
* Debug port

AXI4
----

As previously said, Pinsec integrates an AXI4 bus fabric. AXI4 is not the easiest bus to work with but has many advantages like:


* A flexible topology
* High bandwidth potential
* Potential out of order request completion
* Easy methods to meets clocks timings
* Standard used by many IP cores
* A handshake methodology that fits with SpinalHDL Stream.

From an Area utilization perspective, AXI4 is for sure not the lightest solution, but some techniques could dramatically reduce that concern :


* Using Read-Only/Write-Only AXI4 variations where that is possible
* Introducing an Axi4-Shared variation where a new ARW channel is introduced to replace and combine AR and AW channels. This solution reduces resource usage by a factor of two for the address decoding and the address arbitration.
* Timing relaxation is possible depending upon the interconnect implementation, and if all masters never stall the R/B channel (RREADY and BREADY are strapped to 1). Both xREADY signals can be removed by synthesis in this case, relaxing timings.
* As the AXI4 spec suggests, the interconnect can expand the transactions ID by aggregating the corresponding input port ID. This allows the interconnect to have an infinite number of pending requests and also to support out of order completion with a negligible area cost (transaction ID expand).

The Pinsec interconnect doesn't introduce latency cycles.

APB3
----

In Pinsec, all peripherals implement an APB3 bus to be interfaced. The APB3 choice was motivated by following reasons :


* Very simple bus (no burst)
* Use very few resources
* Standard used by many IP cores

Generate the RTL
----------------

To generate the RTL, you have multiple solutions :

You can download the SpinalHDL source code, and then run :

.. code-block:: scala

   sbt "project SpinalHDL-lib" "run-main spinal.lib.soc.pinsec.Pinsec"

Or you can create your own main into your own SBT project and then run it :

.. code-block:: scala

   import spinal.lib.soc.pinsec._

   object PinsecMain {
     def main(args: Array[String]) {
       SpinalVhdl(new Pinsec(100 MHz))
       SpinalVerilog(new Pinsec(100 MHz))
     }
   }

.. note::
   Currently, only the verilog version was tested in simulation and in FPGA because the last release of GHDL is not compatible with cocotb.
