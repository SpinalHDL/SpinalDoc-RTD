
VCS Simulation Configuration
==============================

Environnement variable
----------------------

You should have several environnement variables defined before:

* ``VCS_HOME``: The home path to your VCS installation.
* ``VERDI_HOME``: The home path to your Verdi installation.

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

1. In a ``Blackbox`` definition, use ``addRTLPath(path: String)`` to assign a external Verilog/VHDL file to this blackbox.
2. Use the method ``mergeRTLSource(fileName: String=null)`` of ``SpinalConfig``.
