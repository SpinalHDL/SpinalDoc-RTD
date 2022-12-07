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

   Test/compile

Actually you never need to just ``compile`` as it is done automatically when
needed. This time was just to evacuate the long first build, and to get all
``sbt`` autocompletion features on the next commands.

To run a specific generation or simulation, the command is ``runMain``. So if
you type ``runMain``, space, and tab, you should get this:

.. code-block::

   sbt:SpinalTemplateSbt> runMain
   ;                                         mylib.MyTopLevelVerilog
   mylib.MyTopLevelFormal                    mylib.MyTopLevelVerilogWithCustomConfig
   mylib.MyTopLevelSim                       mylib.MyTopLevelVhdl

The autocompletion suggests all things that can be run. Let's run the Verilog
generation for instance:

.. code-block::

   runMain mylib.MyTopLevelVerilog

Look at the directory: there is a new ``MyTopLevel.v`` file!

Now add a ``~`` at the beginning of the command:

.. code-block::

   ~ runMain mylib.MyTopLevelVerilog

It prints this:

.. code-block::

   sbt:SpinalTemplateSbt> ~ runMain mylib.MyTopLevelVerilog
   [info] running (fork) mylib.MyTopLevelVerilog
   [info] [Runtime] SpinalHDL v1.7.3    git head : aeaeece704fe43c766e0d36a93f2ecbb8a9f2003
   [info] [Runtime] JVM max memory : 3968,0MiB
   [info] [Runtime] Current date : 2022.11.17 21:35:10
   [info] [Progress] at 0,000 : Elaborate components
   [info] [Progress] at 0,385 : Checks and transforms
   [info] [Progress] at 0,533 : Generate Verilog
   [info] [Done] at 0,634
   [success] Total time: 2 s, completed 17 nov. 2022, 21:35:11
   [info] 1. Monitoring source files for mylib/runMain mylib.MyTopLevelVerilog...
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
