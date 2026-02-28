
VCS Simulation Configuration
==============================

.. _vcs_env:

Environment variable
----------------------

You should have several environment variables defined before:

* ``VCS_HOME``: The home path to your VCS installation.
* ``VERDI_HOME``: The home path to your Verdi installation.
* Add ``$VCS_HOME/bin`` and ``$VERDI_HOME/bin`` to your ``PATH``.

Prepend the following paths to your ``LD_LIBRARY_PATH`` to enable PLI features.

.. code-block:: bash

  export LD_LIBRARY_PATH=$VERDI_HOME/share/PLI/VCS/LINUX64:$LD_LIBRARY_PATH 
  export LD_LIBRARY_PATH=$VERDI_HOME/share/PLI/IUS/LINUX64:$LD_LIBRARY_PATH 
  export LD_LIBRARY_PATH=$VERDI_HOME/share/PLI/lib/LINUX64:$LD_LIBRARY_PATH 
  export LD_LIBRARY_PATH=$VERDI_HOME/share/PLI/Ius/LINUX64:$LD_LIBRARY_PATH 
  export LD_LIBRARY_PATH=$VERDI_HOME/share/PLI/MODELSIM/LINUX64:$LD_LIBRARY_PATH 

If you encounter the ``Compilation of SharedMemIface.cpp failed`` error, make sure that you have installed C++ boost library correctly.
The header and library files path should be added to ``CPLUS_INCLUDE_PATH``, ``LIBRARY_PATH`` and ``LD_LIBRARY_PATH`` respectively.

User defined environment setup
------------------------------

Sometimes a VCS environment setup file `synopsys_sim.setup` is required to run VCS simulation. Also you may want to run some scripts or code 
to setup the environment just before VCS starting compilation. You can do this by `withVCSSimSetup`.

.. code-block:: scala
  
  val simConfig = SimConfig
    .withVCS
    .withVCSSimSetup(
      setupFile = "~/work/myproj/sim/synopsys_sim.setup",
      beforeAnalysis = () => { // this code block will be run before VCS analysis step.
        "pwd".!
        println("Hello, VCS")
      }
    )

This method will copy your own `synopsys_sim.setup` file to the VCS work directory under the `workspacePath` (default as `simWorkspace`) directory,
and run your scripts.

VCS Flags
---------

The VCS backend follows the three step compilation flow:

1. Analysis step: analysis the HDL model using ``vlogan`` and ``vhdlan``.

2. Elaborate step: elaborate the model using ``vcs`` and generate the executable hardware model.

3. Simulation step: run the simulation.

In each step, user can pass some specific flags through ``VCSFlags`` to enable some features like SDF back-annotation or multi-threads.

``VCSFlags`` takes three parameters,

.. list-table::
   :header-rows: 1
   :widths: 2 5 5

   * - Name
     - Type
     - Description
   * - ``compileFlags``
     - ``List[String]``
     - Flags pass to ``vlogan`` or ``vhdlan``.
   * - ``elaborateFlags``
     - ``List[String]``
     - Flags pass to ``vcs``.
   * - ``runFlags``
     - ``List[String]``
     - Flags pass to executable hardware model.

For example, you pass the ``-kdb`` flags to both compilation step and elaboration step, for Verdi debugging,

.. code-block:: scala

   val flags = VCSFlags(
       compileFlags = List("-kdb"),
       elaborateFlags = List("-kdb")
   )

   val config = 
     SimConfig
       .withVCS(flags)
       .withFSDBWave
       .workspacePath("tb")
       .compile(UIntAdder(8))

   ...

Waveform generation
--------------------

VCS backend can generate three waveform format: ``VCD``, ``VPD`` and ``FSDB`` (Verdi required).

You can enable them by the following methods of ``SpinalSimConfig``,

.. list-table::
   :header-rows: 1
   :widths: 2 5

   * - Method
     - Description
   * - ``withWave``
     - Enable ``VCD`` waveform.
   * - ``withVPDWave``
     - Enable ``VPD`` waveform.
   * - ``withFSDBWave``
     - Enable ``FSDB`` waveform.

Also, you can control the wave trace depth by using ``withWaveDepth(depth: Int)``.

Simulation with ``Blackbox``
----------------------------

Sometimes, IP vendors will provide you with some design entities in Verilog/VHDL format and you want to integrate them into your SpinalHDL design. 
The integration can done by following two ways:

1. In a ``Blackbox`` definition, use ``addRTLPath(path: String)`` to assign a external Verilog/VHDL file to this blackbox.
2. Use the method ``mergeRTLSource(fileName: String=null)`` of ``SpinalReport``.
