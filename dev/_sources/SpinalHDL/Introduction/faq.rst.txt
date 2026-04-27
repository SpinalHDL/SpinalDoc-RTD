FAQ
===

What is the overhead of SpinalHDL generated RTL compared to human written VHDL/Verilog?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The overhead is nil.  SpinalHDL generates the same HDL constructs found in
human written VHDL/Verilog with no additional instantiated artifacts present
in the resulting implementation due to its use.  This makes the overhead
zero when comparing the generated HDL against handwritten HDL.

Due to the powerful expressive nature of the Scala/SpinalHDL languages, the
design is more concise for a given complex hardware design and has strong type safety,
strong HDL safety paradigms that result in fewer lines of code, able to achieve more
functionality with fewer bugs.

SpinalHDL does not take a HLS approach and is not itself a HLS solution. Its goal is not
to translate any arbitrary code into RTL, but to provide a powerful language to describe RTL
and raise the abstraction level and increase code reuse at the level the designer is working.

What if SpinalHDL becomes unsupported in the future?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This question has two sides:

1. SpinalHDL generates VHDL/Verilog files, which means that SpinalHDL will be supported by all EDA tools for many decades.
2. If there is a bug in SpinalHDL and there is no longer support to fix it, it's not a deadly situation,
   because the SpinalHDL compiler is fully open source.  For simple issues, you may be able to fix the issue
   yourself within a few hours.
3. Consider how much time it takes for a commercial EDA vendor to fix issues or to add new features in their closed tools.
   Consider also your cost and time savings achieved when using SpinalHDL and the potential for your own entity to give back
   to the community some of this as engineering time, open-source contribution time or donations to the project to improve
   its future.

Does SpinalHDL keep comments in generated VHDL/verilog?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

No, it doesn't. Generated files should be considered as a netlist. For example, when you compile C code, do you care about
your comments in the generated assembly code?

Could SpinalHDL scale up to big projects?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Yes, some experiments were done, and it appears that generating hundreds of 3KLUT CPUs with caches takes around 12 seconds,
which is a ridiculously short time compared to the time required to simulate or synthesize this kind of design.

How SpinalHDL came to be
~~~~~~~~~~~~~~~~~~~~~~~~

Between December 2014 and April 2016, it was as a personal hobby project.  But since April 2016 one person is working full
time on it. Some people are also regularly contributing to the project.

Why develop a new language when there is VHDL/Verilog/SystemVerilog?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The :ref:`Foreword` is dedicated to this topic.

How to use an unreleased version of SpinalHDL (but committed on git)?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

First, you need to get the repository, if you haven't cloned it yet:

.. code-block:: sh

   git clone --depth 1 -b dev https://github.com/SpinalHDL/SpinalHDL.git
   cd SpinalHDL

In the command above you can replace ``dev`` by the name of the branch you want
to checkout. ``--depth 1`` prevents from downloading the repository history.

Then publish the code as it is in the directory fetched:

.. code-block:: sh

   sbt clean '++ 2.12.13' publishLocal

Here ``2.12.13`` is the Scala version used. The first two numbers must match the
ones of the version used in your project. You can find it in your ``build.sbt``
and/or ``build.sc``:

.. code-block:: scala

   ThisBuild / scalaVersion := "2.12.16" // in build.sbt
   // or
   def scalaVersion = "2.12.16" // in build.sc

Then in your project, update the SpinalHDL version specified in your
``build.sbt`` or ``build.sc``: it should be set to ``dev`` instead of a version
number.

.. code-block:: scala

   val spinalVersion = "1.7.3"
   // becomes
   val spinalVersion = "dev"

.. note::

   Here it is always ``dev`` no matter the branch you have checked out earlier.
