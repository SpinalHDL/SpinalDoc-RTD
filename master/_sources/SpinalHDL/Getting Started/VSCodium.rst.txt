.. _Using VSCodium:

Using Spinal from VSCodium
==========================

.. note::
   VSCode works the same way as VSCodium.

As a one-time setup task, in the extensions options, install "Scala (Metals)".

Open the workspace: ``File`` > ``Open Folder...`` and open the folder you have
downloaded earlier in :ref:`template`.

Wait a little bit, a notification pop-up should appear on the bottom-right
corner: "Multiple build definitions found. Which would you like to use?". Click
``sbt``, then another pop-up appears, click ``Import build``.

Wait while running ``sbt bloopInstall``. Then a warning pop-up appears, you can
ignore it (don't show again).

In the explorer, find and open ``MyTopLevel.scala``. Wait a little bit, and see
the ``run | debug`` line that is displayed by Metals, before each ``App``. For
instance, click on ``run`` just above ``object MyTopLevelVerilog``. It performs
design checks and, as the checks pass, it generates the Verilog file
``MyTopLevel.v`` at the root of the workspace.

This is all you need to do to use Spinal from VSCodium!

Now you can use your environment, let's explore the code: :ref:`Simple example`.
