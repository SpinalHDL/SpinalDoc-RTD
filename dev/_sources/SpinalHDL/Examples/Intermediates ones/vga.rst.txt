VGA
===

Introduction
------------

VGA interfaces are becoming an endangered species, but implementing a VGA controller is still a good exercise.

An explanation about the VGA protocol can be found `here <https://web.mit.edu/6.111/www/s2004/NEWKIT/vga.shtml>`_.

This VGA controller tutorial is based on `this <https://github.com/SpinalHDL/SpinalHDL/blob/master/lib/src/main/scala/spinal/lib/graphic/vga/VgaCtrl.scala>`_ implementation.

Data structures
---------------

Before implementing the controller itself we need to define some data structures.

RGB color
^^^^^^^^^

First, we need a three channel color structure (Red, Green, Blue). This data structure will be used to feed the controller with pixels and also will be used by the VGA bus.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/VGA.scala
   :language: scala
   :start-at: case class RgbConfig(
   :end-before: // end case class Rgb

VGA bus
^^^^^^^

.. list-table::
   :header-rows: 1
   :widths: 1 1 10

   * - io name
     - Driver
     - Description
   * - vSync
     - master
     - Vertical synchronization, indicate the beginning of a new frame
   * - hSync
     - master
     - Horizontal synchronization, indicate the beginning of a new line
   * - colorEn
     - master
     - High when the interface is in the visible part
   * - color
     - master
     - Carry the color, don't care when colorEn is low


.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/VGA.scala
   :language: scala
   :start-at: case class Vga(
   :end-before: // end case class Vga

This Vga ``Bundle`` uses the ``IMasterSlave`` trait, which allows you to create master/slave VGA interfaces using the following:

.. code-block:: text

   master(Vga(...))
   slave(Vga(...))

VGA timings
^^^^^^^^^^^

The VGA interface is driven by using 8 different timings. Here is one simple example of a ``Bundle`` that is able to carry them.

.. code-block:: scala

   case class VgaTimings(timingsWidth: Int) extends Bundle {
     val hSyncStart  = UInt(timingsWidth bits)
     val hSyncEnd    = UInt(timingsWidth bits)
     val hColorStart = UInt(timingsWidth bits)
     val hColorEnd   = UInt(timingsWidth bits)
     val vSyncStart  = UInt(timingsWidth bits)
     val vSyncEnd    = UInt(timingsWidth bits)
     val vColorStart = UInt(timingsWidth bits)
     val vColorEnd   = UInt(timingsWidth bits)
   }

But this not a very good way to specify it because it is redundant for vertical and horizontal timings.

Let's write it in a clearer way:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/VGA.scala
   :language: scala
   :start-at: case class VgaTimingsHV(
   :end-at: val v
   :append: }

Then we can add some some functions to set these timings for specific resolutions and frame rates:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/VGA.scala
   :language: scala
   :start-at: case class VgaTimingsHV(
   :end-before: // end case class VgaTimings

VGA Controller
--------------

Specification
^^^^^^^^^^^^^

.. list-table::
   :header-rows: 1
   :widths: 1 1 10

   * - io name
     - Direction
     - Description
   * - softReset
     - in
     - Reset internal counters and keep the VGA interface inactive
   * - timings
     - in
     - Specify VGA horizontal and vertical timings
   * - pixels
     - slave
     - Stream of RGB colors that feeds the VGA controller
   * - error
     - out
     - High when the pixels stream is too slow
   * - frameStart
     - out
     - High when a new frame starts
   * - vga
     - master
     - VGA interface


The controller does not integrate any pixel buffering. It directly takes them from the ``pixels`` ``Stream`` and puts them on the ``vga.color`` out at the right time. If ``pixels`` is not valid then ``error`` becomes high for one cycle.

Component and io definition
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Let's define a new VgaCtrl ``Component``\ , which takes as ``RgbConfig`` and ``timingsWidth`` as parameters. Let's give the bit width a default value of 12.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/VGA.scala
   :language: scala
   :start-at: case class VgaCtrl(
   :end-before: // end VgaCtrl io
   :append: ...

Horizontal and vertical logic
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The logic that generates horizontal and vertical synchronization signals is quite the same. It kind of resembles ~PWM~. The horizontal one counts up each cycle, while the vertical one use the horizontal synchronization signal as to increment.

Let's define ``HVArea``\ , which represents one ~PWM~ and then instantiate it two times: one for both horizontal and vertical synchronization.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/VGA.scala
   :language: scala
   :start-after: // end VgaCtrl io
   :end-before: // end VgaCtrl HVArea
   :prepend: case class VgaCtrl(rgbConfig: RgbConfig, timingsWidth: Int = 12) extends Component {
             ...
   :append: ...

As you can see, it's done by using ``Area``. This is to avoid the creation of a new ``Component`` which would have been much more verbose.

Interconnections
^^^^^^^^^^^^^^^^

Now that we have timing generators for horizontal and vertical synchronization, we need to drive the outputs.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/VGA.scala
   :language: scala
   :start-after: // end VgaCtrl HVArea
   :end-before: // end VgaCtrl connections
   :prepend: case class VgaCtrl(rgbConfig: RgbConfig, timingsWidth: Int = 12) extends Component {
             ...
   :append: ...

Bonus
^^^^^

The VgaCtrl that was defined above is generic (not application specific).
We can imagine a case where the system provides a ``Stream`` of ``Fragment`` of RGB, which means the system transmits pixels between start/end of picture indications.

In this case we can automatically manage the ``softReset`` input by asserting it when an ``error`` occurs, then wait for the end of the current ``pixels`` picture to deassert ``error``.

Let's add a function to ``VgaCtrl`` that can be called from the parent component to feed ``VgaCtrl`` by using this ``Stream`` of ``Fragment`` of RGB.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/VGA.scala
   :language: scala
   :start-after: // end VgaCtrl connections
   :end-before: // end case class VgaCtrl
   :prepend: case class VgaCtrl(rgbConfig: RgbConfig, timingsWidth: Int = 12) extends Component {
             ...