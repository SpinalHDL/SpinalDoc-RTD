
Introduction
------------

The core of the language define the syntax that provide many features :


* Types / Literals
* Register / Clock domains
* Component / Area
* RAM / ROM
* When / Switch / Mux
* BlackBox (to integrate VHDL or Verilog IP inside Spinal)
* SpinalHDL to VHDL converter

Then, by using these features, you can of course define your digital hardware, but also build powerful libraries and abstractions. It's one of the biggest advantages of SpinalHDL over commonly used HDL, the language is not stuck in the rock, you can extend it without having knowledge about the compiler.

One example of that is the `SpinalHDL lib </SpinalDoc/spinal/lib/introduction/>`_ which add many utils, tools, buses and methodology.

To use features introduced in followings chapter you need to ``import spinal.core._`` in your sources.
