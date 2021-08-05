.. role:: raw-html-m2r(raw)
   :format: html

FAQ
===

What is the overhead of SpinalHDL generated RTL compared to human written VHDL/Verilog?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The overhead is null, SpinalHDL is not an HLS approach. Its goal is not to translate any arbitrary code into RTL, but to provide a powerful language to describe RTL and raise the abstraction level.

What if SpinalHDL becomes unsupported in the future?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This question has two sides:

1. SpinalHDL generates VHDL/Verilog files, which means that SpinalHDL will be supported by all EDA tools for many decades.
2. If there is a bug in SpinalHDL and there is no longer support to fix it, it's not a deadly situation, because the SpinalHDL compiler is fully open source. For simple issues, you may be able to fix the issue yourself in few hours. Remember how much time it takes to EDA companies to fix issues or to add new features in their closed tools.

Does SpinalHDL keep comments in generated VHDL/verilog?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

No, it doesn't. Generated files should be considered as a netlist. For example, when you compile C code, do you care about your comments in the generated assembly code?

Could SpinalHDL scale up to big projects?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Yes, some experiments were done, and it appears that generating hundreds of 3KLUT CPUs with caches takes around 12 seconds, which is a ridiculously short time compared to the time required to simulate or synthesize this kind of design.

How SpinalHDL came to be
~~~~~~~~~~~~~~~~~~~~~~~~

Between December 2014 and April 2016, it was as a personal hobby project. But since April 2016 one person is working full time on it. Some people are also regularly contributing to the project.

Why develop a new language when there is VHDL/Verilog/SystemVerilog?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

:ref:`This page <regular_hdl>`\  is dedicated to this topic.

How to use an unreleased version of SpinalHDL (but committed on git)?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

For instance, if you want to try the ``dev`` branch, do the following in a dummy folder :

.. code-block:: sh

   git clone https://github.com/SpinalHDL/SpinalHDL.git -b dev
   cd SpinalHDL
   sbt clean publishLocal

| Then in your project, don't forget to update the SpinalHDL version specified in the build.sbt file, see
| `https://github.com/SpinalHDL/SpinalTemplateSbt/blob/master/build.sbt#L10 <https://github.com/SpinalHDL/SpinalTemplateSbt/blob/master/build.sbt#L10>`_
| To know which version you have to set, look in
| `https://github.com/SpinalHDL/SpinalHDL/blob/dev/project/Version.scala#L7 <https://github.com/SpinalHDL/SpinalHDL/blob/dev/project/Version.scala#L7>`_
