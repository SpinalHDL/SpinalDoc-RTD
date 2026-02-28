
QSysify
=======

QSysify is a tool which is able to generate a QSys IP (tcl script) from a SpinalHDL component by analyzing its IO definition. It currently implement the following interfaces features :

* Master/Slave AvalonMM
* Master/Slave APB3
* Clock domain input
* Reset output
* Interrupt input
* Conduit (Used in last resort)

Example
-------

In the case of a UART controller :

.. code-block:: scala

   case class AvalonMMUartCtrl(...) extends Component {
     val io = new Bundle {
       val bus =  slave(AvalonMM(AvalonMMUartCtrl.getAvalonMMConfig))
       val uart = master(Uart())
     }

     // ...
   }

The following  ``main`` will generate the Verilog and the QSys TCL script with io.bus as an AvalonMM and io.uart as a conduit :

.. code-block:: scala

   object AvalonMMUartCtrl {
     def main(args: Array[String]) {
       // Generate the Verilog
       val toplevel = SpinalVerilog(AvalonMMUartCtrl(UartCtrlMemoryMappedConfig(...))).toplevel

       // Add some tags to the avalon bus to specify it's clock domain (information used by QSysify)
       toplevel.io.bus addTag(ClockDomainTag(toplevel.clockDomain))

       // Generate the QSys IP (tcl script)
       QSysify(toplevel)
     }
   }

tags
----

Because QSys require some information that are not specified in the SpinalHDL hardware specification, some tags should be added to interface:

AvalonMM / APB3
^^^^^^^^^^^^^^^

.. code-block:: scala

   io.bus addTag(ClockDomainTag(busClockDomain))

Interrupt input
^^^^^^^^^^^^^^^

.. code-block:: scala

   io.interrupt addTag(InterruptReceiverTag(relatedMemoryInterfacei, interruptClockDomain))

Reset output
^^^^^^^^^^^^

.. code-block:: scala

   io.resetOutput addTag(ResetEmitterTag(resetOutputClockDomain))

Adding new interface support
----------------------------

Basically, the QSysify tool can be setup with a list of interface ``emitter`` `(as you can see here) <https://github.com/SpinalHDL/SpinalHDL/blob/764193013f84cfe4f82d7d1f1739c4561ef65860/lib/src/main/scala/spinal/lib/eda/altera/QSys.scala#L12>`_

You can create your own emitter by creating a new class extending `QSysifyInterfaceEmiter <https://github.com/SpinalHDL/SpinalHDL/blob/764193013f84cfe4f82d7d1f1739c4561ef65860/lib/src/main/scala/spinal/lib/eda/altera/QSys.scala#L24>`_
