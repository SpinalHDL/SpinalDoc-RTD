
JTAG TAP
========

Introduction
------------

.. important::
   The goal of this page is to show the implementation of a JTAG TAP (a slave) by a non-conventional way.

.. important::
   | This implementation is not a simple one, it mix object oriented programming, abstract interfaces decoupling, hardware generation and hardware description.
   | Of course a simple JTAG TAP implementation could be done only with a simple hardware description, but the goal here is really to going forward and creating an very reusable and extensible JTAG TAP generator

.. important::
   This page will not explain how JTAG works. A good tutorial can be found `there <https://www.fpga4fun.com/JTAG.html>`_.

One big difference between commonly used HDL and Spinal, is the fact that SpinalHDL allow you to define hardware generators/builders. It's very different than describing hardware.
Let's take a look into the example bellow because the difference between generate/build/describing could seem "playing with word" or could be interpreted differently.

The example bellow is a JTAG TAP which allow the JTAG master to read ``switchs``\ /\ ``keys`` inputs and write ``leds`` outputs. This TAP could also be recognized by a master by using the UID 0x87654321.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: class SimpleJtagTap
   :end-before: // end SimpleJtagTap

As you can see, a JtagTap is created but then some Generator/Builder functions (idcode,read,write) are called to create each JTAG instruction. This is what i call "Hardware generator/builder", then these Generator/Builder are used by the user to describing an hardware. And there is the point, in commonly HDL you can only describe your hardware, which imply many donkey job.

This JTAG TAP tutorial is based on `this <https://github.com/SpinalHDL/SpinalHDL/tree/master/lib/src/main/scala/spinal/lib/com/jtag>`_ implementation.

.. _jtag:

JTAG bus
--------

First we need to define a JTAG bus bundle.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: case class Jtag
   :end-before: // end case class Jtag

As you can see this bus don't contain the TCK pin because it will be provided by the clock domain.

JTAG state machine
------------------

Let's define the JTAG state machine as explained `here <https://www.fpga4fun.com/JTAG2.html>`_

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: object JtagState
   :end-before: // end class JtagFsm

.. note::
   The ``randBoot()`` on ``state`` make it initialized with a random state. It's only for simulation purpose.

JTAG TAP
--------

Let's implement the core of the JTAG TAP, without any instruction, just the base manage the instruction register (IR) and the bypass.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: class JtagTap
   :end-before: // JtagTap convenience functions
   :append: }

.. note::
   Ignore the reference to `with JTagTapAccess` for now, it will be explained further down.

Jtag instructions
-----------------

Now that the JTAG TAP core is done, we can think about how to implement JTAG instructions by an reusable way.

JTAG TAP class interface
^^^^^^^^^^^^^^^^^^^^^^^^

First we need to define how an instruction could interact with the JTAG TAP core. We could of course directly take the JtagTap area, but it's not very nice because is some situation the JTAG TAP core is provided by another IP (Altera virtual JTAG for example).

So let's define a simple and abstract interface between the JTAG TAP core and instructions :

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: trait JtagTapAccess
   :end-before: // JtagTapAccess convenience functions
   :append: }

Then let the ``JtagTap`` implement this abstract interface:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :caption: Additions to ``class JtagTap``
   :start-at: override def getTdi
   :end-before: // end class JtagTap
   :prepend: class JtagTap(val jtag: Jtag, ...) extends Area with JtagTapAccess {
             ...

Base class
^^^^^^^^^^

Let's define a useful base class for JTAG instruction that provide some callback (doCapture/doShift/doUpdate/doReset) depending the selected instruction and the state of the JTAG TAP :

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: class JtagInstruction
   :end-before: // end class JtagInstruction

.. note::
   | About the Component.current.addPrePopTask(...) : 
   | This allows you to call the given code at the end of the current component construction. Because of object oriented nature of JtagInstruction, doCapture, doShift, doUpdate and doReset should not be called before children classes construction (because children classes will use it as a callback to do some logic).

Read instruction
^^^^^^^^^^^^^^^^

Let's implement an instruction that allow the JTAG to read a signal.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: class JtagInstructionRead
   :end-before: // end class JtagInstructionRead

Write instruction
^^^^^^^^^^^^^^^^^

Let's implement an instruction that allow the JTAG to write a register (and also read its current value).

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: class JtagInstructionWrite
   :end-before: // end class JtagInstructionWrite

Idcode instruction
^^^^^^^^^^^^^^^^^^

Let's implement the instruction that return a idcode to the JTAG and also, when a reset occur, set the instruction register (IR) to it own instructionId.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: class JtagInstructionIdcode
   :end-before: // end class JtagInstructionIdcode

User friendly wrapper
---------------------

Let's add some user friendly function to the JtagTapAccess to make instructions instantiation easier .

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :caption: Additions to ``trait JtagTapAccess``
   :language: scala
   :start-at: def idcode(value: Bits)(instructionId: Bits) =
   :end-at: }
   :prepend: trait JtagTapAccess {
             ...

Usage demonstration
-------------------

And there we are, we can now very easily create an application specific JTAG TAP without having to write any logic or any interconnections.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/advanced/JTAG.scala
   :language: scala
   :start-at: class SimpleJtagTap extends Component {
   :end-at: // end SimpleJtagTap

This way of doing things (Generating hardware) could also be applied to, for example, generating an APB/AHB/AXI bus slave.
