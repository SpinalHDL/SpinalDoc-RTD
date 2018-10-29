.. role:: raw-html-m2r(raw)
   :format: html


Introduction
------------

VGA interfaces are becoming an endangered species, but implementing a VGA controller is still a good exercise.

An explanation about the VGA protocol can be found `here <http://www.xess.com/blog/vga-the-rest-of-the-story/>`_.

This VGA controller tutorial is based on `this <https://github.com/SpinalHDL/SpinalHDL/blob/master/lib/src/main/scala/spinal/lib/graphic/vga/VgaCtrl.scala>`_ implementation.

Data structures
---------------

Before implementing the controller itself we need to define some data structures.

RGB color
^^^^^^^^^

First, we need a three channel color structure (Red, Green, Blue). This data structure will be used to feed the controller with pixels and also will be used by the VGA bus.

.. code-block:: scala

   case class RgbConfig(rWidth : Int,gWidth : Int,bWidth : Int){
     def getWidth = rWidth + gWidth + bWidth
   }

   case class Rgb(c: RgbConfig) extends Bundle{
     val r = UInt(c.rWidth bit)
     val g = UInt(c.gWidth bit)
     val b = UInt(c.bWidth bit)
   }

VGA bus
^^^^^^^

.. list-table::
   :header-rows: 1

   * - io name
     - Driver
     - Description
   * - vSync
     - master
     - Vertical syncronization, indicate the beginning of a new frame
   * - hSync
     - master
     - Horizontal syncronization, indicate the beginning of a new line
   * - colorEn
     - master
     - High when the interface is in the visible part
   * - color
     - master
     - Carry the color, don't care when colorEn is low


.. code-block:: scala

   case class Vga (rgbConfig: RgbConfig) extends Bundle with IMasterSlave{
     val vSync = Bool
     val hSync = Bool

     val colorEn = Bool
     val color   = Rgb(rgbConfig)

     override def asMaster() : Unit = this.asOutput()
   }

This Vga ``Bundle`` uses the ``IMasterSlave`` trait, which allows you to create master/slave VGA interfaces using the following: :raw-html-m2r:`<br>`

.. code-block::

   master(Vga(...))
   slave(Vga(...))

VGA timings
^^^^^^^^^^^

The VGA interface is driven by using 8 differents timings. Here is one simple example of a ``Bundle`` that is able to carry them.

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

.. code-block:: scala

   case class VgaTimingsHV(timingsWidth: Int) extends Bundle {
     val colorStart = UInt(timingsWidth bit)
     val colorEnd   = UInt(timingsWidth bit)
     val syncStart  = UInt(timingsWidth bit)
     val syncEnd    = UInt(timingsWidth bit)
   }

   case class VgaTimings(timingsWidth: Int) extends Bundle {
     val h = VgaTimingsHV(timingsWidth)
     val v = VgaTimingsHV(timingsWidth)
   }

Then we could add some some functions to set these timings for specific resolutions and frame rates:

.. code-block:: scala

   case class VgaTimingsHV(timingsWidth: Int) extends Bundle {
     val colorStart = UInt(timingsWidth bit)
     val colorEnd   = UInt(timingsWidth bit)
     val syncStart  = UInt(timingsWidth bit)
     val syncEnd    = UInt(timingsWidth bit)
   }

   case class VgaTimings(timingsWidth: Int) extends Bundle {
     val h = VgaTimingsHV(timingsWidth)
     val v = VgaTimingsHV(timingsWidth)

     def setAs_h640_v480_r60: Unit = {
       h.syncStart := 96 - 1
       h.syncEnd := 800 - 1
       h.colorStart := 96 + 16 - 1
       h.colorEnd := 800 - 48 - 1
       v.syncStart := 2 - 1
       v.syncEnd := 525 - 1
       v.colorStart := 2 + 10 - 1
       v.colorEnd := 525 - 33 - 1
     }

     def setAs_h64_v64_r60: Unit = {
       h.syncStart := 96 - 1
       h.syncEnd := 800 - 1
       h.colorStart := 96 + 16 - 1 + 288
       h.colorEnd := 800 - 48 - 1 - 288
       v.syncStart := 2 - 1
       v.syncEnd := 525 - 1
       v.colorStart := 2 + 10 - 1 + 208
       v.colorEnd := 525 - 33 - 1 - 208
     }
   }

VGA Controller
--------------

Specification
^^^^^^^^^^^^^

.. list-table::
   :header-rows: 1

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

.. code-block:: scala

   class VgaCtrl(rgbConfig: RgbConfig, timingsWidth: Int = 12) extends Component {
     val io = new Bundle {
       val softReset = in Bool
       val timings = in(VgaTimings(timingsWidth))
       val pixels = slave Stream (Rgb(rgbConfig))

       val error = out Bool
       val frameStart = out Bool
       val vga = master(Vga(rgbConfig))
     }
     ...
   }

Horizontal and vertical logic
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The logic that generates horizontal and vertical syncronization signals is quite the same. It kind of resembles ~PWM~. The horizontal one counts up each cycle, while the vertical one use the horizontal syncronization signal as to increment.

Let's define ``HVArea``\ , which represents one ~PWM~ and then instantiate it two times: one for both horizontal and vertical syncronization.

.. code-block:: scala

   class VgaCtrl(rgbConfig: RgbConfig, timingsWidth: Int = 12) extends Component {
     val io = new Bundle {...}

     case class HVArea(timingsHV: VgaTimingsHV, enable: Bool) extends Area {
       val counter = Reg(UInt(timingsWidth bit)) init(0)

       val syncStart  = counter === timingsHV.syncStart
       val syncEnd    = counter === timingsHV.syncEnd
       val colorStart = counter === timingsHV.colorStart
       val colorEnd   = counter === timingsHV.colorEnd

       when(enable) {
         counter := counter + 1
         when(syncEnd) {
           counter := 0
         }
       }

       val sync    = RegInit(False) setWhen(syncStart) clearWhen(syncEnd)
       val colorEn = RegInit(False) setWhen(colorStart) clearWhen(colorEnd)

       when(io.softReset) {
         counter := 0
         sync    := False
         colorEn := False
       }
     }
     val h = HVArea(io.timings.h, True)
     val v = HVArea(io.timings.v, h.syncEnd)
   }

As you can see, it's done by using ``Area``. This is to avoid the creation of a new ``Component`` which would have been much more verbose.

Interconnections
^^^^^^^^^^^^^^^^

Now that we have timing generators for horizontal and vertical syncronization, we need to drive the outputs.

.. code-block:: scala

   class VgaCtrl(rgbConfig: RgbConfig, timingsWidth: Int = 12) extends Component {
     val io = new Bundle {...}

     case class HVArea(timingsHV: VgaTimingsHV, enable: Bool) extends Area {...}
     val h = HVArea(io.timings.h, True)
     val v = HVArea(io.timings.v, h.syncEnd)

     val colorEn = h.colorEn && v.colorEn
     io.pixels.ready := colorEn
     io.error := colorEn && ! io.pixels.valid

     io.frameStart := v.syncEnd

     io.vga.hSync := h.sync
     io.vga.vSync := v.sync
     io.vga.colorEn := colorEn
     io.vga.color := io.pixels.payload
   }

Bonus
^^^^^

The VgaCtrl that was defined above is generic (not application specific).
We can imagine a case where the system provides a ``Stream`` of ``Fragment`` of RGB, which means the system transmits pixels between start/end of picture indications.

In this case we can automaticly manage the ``softReset`` input by asserting it when an ``error`` occurs, then wait for the end of the current ``pixels`` picture to deassert ``error``.

Let's add a function to ``VgaCtrl`` that can be called from the parent component to feed ``VgaCtrl`` by using this ``Stream`` of ``Fragment`` of RGB.

.. code-block:: scala

   class VgaCtrl(rgbConfig: RgbConfig, timingsWidth: Int = 12) extends Component {
     ...
     def feedWith(that : Stream[Fragment[Rgb]]): Unit ={
       io.pixels << that.toStreamOfFragment

       val error = RegInit(False)
       when(io.error){
         error := True
       }
       when(that.isLast){
         error := False
       }

       io.softReset := error
       when(error){
         that.ready := True
       }
     }
   }
