.. _Using VSCodium:

Using Spinal from VSCodium
==========================

.. note::
    `VSCodium <https://vscodium.com/>`_ is the open source build of Visual Studio Code, but without the telemetry included in Microsoft's downloadable version.

As a one-time setup task, go to view->extensions search for "Scala" and install the "Scala (Metals)" `extension <https://marketplace.visualstudio.com/items?itemName=scalameta.metals>`_.

Open the workspace: ``File`` > ``Open Folder...`` and open the folder you have downloaded earlier in :ref:`template`.

The other way to start it, is to cd into the appropriate directory and type ``codium .``

Wait a little bit, a notification pop-up should appear on the bottom-right
corner: "Multiple build definitions found. Which would you like to use?". Click
``sbt``, then another pop-up appears, click ``Import build``.

Wait while running ``sbt bloopInstall``. Then a warning pop-up appears, you can
ignore it (don't show again).

Find and open ``hw/spinal/projectname/MyTopLevel.scala``.  Wait a little bit, and see the ``run | debug`` line that is displayed by Metals, before each ``App``. For instance, click on ``run`` just above ``object MyTopLevelVerilog``. Alternatively, you can select Menu Bar -> Run -> Run Without Debugging.  Either approach performs design checks and, as the checks pass, generates the Verilog file
``./hw/gen/MyTopLevel.v``

This is all you need to do to use SpinalHDL from VSCodium.  You now have the design-rule-checked Verilog and/or VHDL which you can use as input to your favorite synthesis tool.   

Now that you know how to use the VSCodium development environment, let's explore the code: :ref:`Simple example`.
