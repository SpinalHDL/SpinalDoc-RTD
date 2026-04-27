.. role:: raw-html-m2r(raw)
   :format: html

.. _plic_mapper:

Plic Mapper
=================

The PLIC Mapper defines the register generation and access for a PLIC (Platform Level Interrupt Controller.

``PlicMapper.apply``
--------------------

``(bus: BusSlaveFactory, mapping: PlicMapping)(gateways : Seq[PlicGateway], targets : Seq[PlicTarget])``

args for PlicMapper:

* **bus**: bus to which this ctrl is attached
* **mapping**: a mapping configuration (see above)
* **gateways**: a sequence of PlicGateway (interrupt sources) to generate the bus access control
* **targets**: the sequence of PlicTarget (eg. multiple cores) to generate the bus access control


It follows the interface given by riscv: https://github.com/riscv/riscv-plic-spec/blob/master/riscv-plic.adoc

As of now, two memory mappings are available : 

``PlicMapping.sifive``
-----------------------------
Follows the SiFive PLIC mapping (eg. `E31 core complex Manual <https://sifive.cdn.prismic.io/sifive/9169d157-0d50-4005-a289-36c684de671b_e31_core_complex_manual_21G1.pdf>`_ ), basically a full fledged PLIC

``PlicMapping.light``
----------------------------
This mapping generates a lighter PLIC, at the cost of some missing optional features:

* not reading the interrupt's priority
* not reading the interrupt's pending bit (must use the claim/complete mechanism)
* not reading the target's threshold

The rest of the registers & logic is generated.

