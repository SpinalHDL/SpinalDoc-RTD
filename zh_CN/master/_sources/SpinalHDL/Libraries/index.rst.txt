.. _lib_introduction:

=========
Libraries
=========

The spinal.lib package goals are :

* Provide things that are commonly used in hardware design (FIFO, clock crossing bridges, useful functions)
* Provide simple peripherals (UART, JTAG, VGA, ..)
* Provide some bus definition (Avalon, AMBA, ..)
* Provide some methodology (Stream, Flow, Fragment)
* Provide some example to get the spirit of spinal
* Provide some tools and facilities (latency analyzer, QSys converter, ...)

To use features introduced in followings chapter you need, in most of cases, to ``import spinal.lib._`` in your sources.

.. important::
   | This package is currently under construction. Documented features could be considered as stable. 
   | Do not hesitate to use github for suggestions/bug/fixes/enhancements

.. toctree::
   :hidden:

   utils
   stream
   flow
   fragment
   fsm
   vexriscv
   bus_slave_factory
   fiber
   binarySystem
   regIf
   Bus/index
   Com/index
   IO/index
   Graphics/index
   EDA/index
   Pipeline/index
   Misc/index

