
QuartusFlow
===========

Introduction
------------

A compilation flow is an Altera-defined sequence of commands that use a combination of command-line executables.
A full compilation flow launches all Compiler modules in sequence to synthesize, fit, analyze final timing, and generate a device programming file. 
Tools in `this file <https://github.com/SpinalHDL/SpinalHDL/blob/dev/lib/src/main/scala/spinal/lib/eda/altera/QuartusFlow.scala>`_ help you get rid of redundant Quartus GUI.

For a single rtl file
---------------------

The object ``spinal.lib.eda.altera.QuartusFlow`` can automatically report the area and maximum frequence of a single rtl file.

Example
^^^^^^^

.. code-block:: scala

   val report = QuartusFlow(
      quartusPath="/eda/intelFPGA_lite/17.0/quartus/bin/",
      workspacePath="/home/spinalvm/tmp",
      toplevelPath="TopLevel.vhd",
      family="Cyclone V",
      device="5CSEMA5F31C6",
      frequencyTarget = 1 MHz
   )
   println(report)

The code above will create a new Quartus project with ``TopLevel.vhd``. By the way, if you use the device ``EP4CE6E22C8N``, the family should be ``Cyclone IV E``.

.. warning::
   This operation will remove the folder ``workspacePath``!

For an existing project
-----------------------

The class ``spinal.lib.eda.altera.QuartusProject`` can automatically find configuration files in an existing project that are used to call a full compilation and program the device. 

Example
^^^^^^^

Specify the path that contains your project files like ``.qpf`` and ``.cdf``.  

.. code-block:: scala

   val prj = new QuartusProject(
      quartusPath = "F:/intelFPGA_lite/20.1/quartus/bin64/",
      workspacePath = "G:/"
   )
   prj.compile()
   prj.program()  // automatically find Chain Description File of the project 

.. important::
   Remember to save the ``.cdf`` of your project before calling ``prj.program()``.
   