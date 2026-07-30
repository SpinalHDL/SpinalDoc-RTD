
HDElkDiagramGen
===============

``HDElkDiagramGen`` is a tool from the `SpinalSchemaGen <https://github.com/SpinalHDL/SpinalSchemaGen>`_ library that generates an interactive block diagram of a SpinalHDL design as an HTML file.
After running elaboration, pass the ``SpinalReport`` returned by ``SpinalVerilog`` or ``SpinalVHDL`` to generate the diagram.
Open the resulting HTML file in any modern web browser to explore the hierarchy.

.. code-block:: scala

   import spinal.schema.elk.HDElkDiagramGen

   HDElkDiagramGen(SpinalVerilog(new MyToplevel))

This will create a file named ``MyToplevel.html`` in the current working directory.

Setup
-----

``HDElkDiagramGen`` lives in the separate ``spinalhdl-schema-gen`` library.
Add the following dependency to your ``build.sbt``:

.. code-block:: scala

   libraryDependencies += "com.github.spinalhdl" %% "spinalhdl-schema-gen" % "0.0.4"

Usage
-----

The tool accepts the ``SpinalReport`` produced by ``SpinalVerilog`` or ``SpinalVHDL``:

.. code-block:: scala

   import spinal.schema.elk.HDElkDiagramGen

   // With Verilog output
   HDElkDiagramGen(SpinalVerilog(new MyToplevel))

   // With VHDL output
   HDElkDiagramGen(SpinalVHDL(new MyToplevel))

Output
------

The generated ``<TopLevelName>.html`` file contains an interactive block diagram for every module in the design hierarchy, rendered using the `HDElk <https://github.com/Readon/hdelk>`_ library.
Open it in any modern web browser to navigate between diagrams.

The diagram shows:

* Input and output ports of each module
* Internal signals and registers
* Connections between sub-modules
* Clock domain colouring (each clock domain is assigned a distinct highlight colour; mixed-clock modules are marked separately)
