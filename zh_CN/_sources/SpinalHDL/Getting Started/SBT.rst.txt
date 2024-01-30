.. _Using SBT:

Using Spinal from CLI with SBT
==============================

First, open a terminal in the root of the template you have downloaded earlier
in :ref:`template`.

Commands can be executed right from the terminal:

.. code-block:: sh

   sbt "firstCommand with arguments" "secondCommand with more arguments"

But ``sbt`` has a quite long boot time so the we recommend to use its
interactive mode:

.. code-block:: sh

   sbt

Now ``sbt`` shows a prompt. Let's start by doing Scala compilation. It will
fetch dependencies so it can take time the first time:

.. code-block::

   compile

Actually you never need to just ``compile`` as it is done automatically when
needed. The first build time will take a few moments longer compared to future builds
as the sbt tool builds the entire project from a cold start and then uses incremental
building where possible from that point on.  ``sbt`` supports autocompletion inside
the interactive shell to assist discovery and usage of the available commands. 
You can start the interactive shell with ``sbt shell`` or running ``sbt``
with no arguments from the command line.

To run a specific HDL code-generation or simulation, the command is ``runMain``. So
if you type ``runMain``, space, and tab, you should get this:

.. code-block::

   sbt:SpinalTemplateSbt> runMain 
      ;                               projectname.MyTopLevelVerilog
      projectname.MyTopLevelFormal    projectname.MyTopLevelVhdl
      projectname.MyTopLevelSim

The autocompletion suggests all things that can be run. Let's run the Verilog
generation for instance:

.. code-block::

   runMain projectname.MyTopLevelVerilog

Look at the directory ./hw/gen/: there is a new ``MyTopLevel.v`` file!

Now add a ``~`` at the beginning of the command:

.. code-block::

   ~ runMain projectname.MyTopLevelVerilog

It prints this:

.. code-block::

   sbt:SpinalTemplateSbt> ~ runMain mylib.MyTopLevelVerilog
   [info] running (fork) mylib.MyTopLevelVerilog
   [info] [Runtime] SpinalHDL v1.7.3    git head : aeaeece704fe43c766e0d36a93f2ecbb8a9f2003
   [info] [Runtime] JVM max memory : 3968,0MiB
   [info] [Runtime] Current date : 2022.11.17 21:35:10
   [info] running (fork) projectname.MyTopLevelVerilog 
   [info] [Runtime] SpinalHDL v1.9.3    git head : 029104c77a54c53f1edda327a3bea333f7d65fd9
   [info] [Runtime] JVM max memory : 4096.0MiB
   [info] [Runtime] Current date : 2023.10.05 19:30:19
   [info] [Progress] at 0.000 : Elaborate components
   [info] [Progress] at 0.508 : Checks and transforms
   [info] [Progress] at 0.560 : Generate Verilog
  [info] [Done] at 0.603
  [success] Total time: 1 s, completed Oct 5, 2023, 7:30:19 PM
  [info] 1. Monitoring source files for projectname/runMain projectname.MyTopLevelVerilog...
  [info]    Press <enter> to interrupt or '?' for more options.

So now, each time you save a source file, it will re-generate ``MyTopLevel.v``.
To do this, it automatically compiles the source files and it performs lint
checks. This way you can get errors printed on the terminal almost in real-time
while you are editing the source files.

You can press Enter to stop automatic generation, then Ctrl-D to exit ``sbt``.

It is also possible to start it right from the terminal, without using ``sbt``'s
interactive prompt:

.. code-block:: sh

   sbt "~ runMain mylib.MyTopLevelVerilog"

Now you can use your environment, let's explore the code: :ref:`Simple example`.
