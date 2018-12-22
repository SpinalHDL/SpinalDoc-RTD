
JTAG TAP
========

.. role:: raw-html-m2r(raw)
   :format: html


Introduction
------------

.. important::
   The goal of this page is to show the implementation of a JTAG TAP (a slave) by a non-conventional way.

.. important::
   | This implementation is not a simple one, it mix object oriented programming, abstract interfaces decoupling, hardware generation and hardware description.
   | Of course a simple JTAG TAP implementation could be done only with a simple hardware description, but the goal here is really to going forward and creating an very reusable and extensible JTAG TAP generator

.. important::
   This page will not explains how JTAG work. A good tutorial could be find `there <http://www.fpga4fun.com/JTAG.html>`_.

One big difference between commonly used HDL and Spinal, is the fact that SpinalHDL allow you to define hardware generators/builders. It's very different than describing hardware.
Let's take a look into the example bellow because the difference between generate/build/describing could seem "playing with word" or could be interpreted differently.

The example bellow is a JTAG TAP which allow the JTAG master to read ``switchs``\ /\ ``keys`` inputs and write ``leds`` outputs. This TAP could also be recognized by a master by using the UID 0x87654321.

.. code-block:: scala

   class SimpleJtagTap extends Component {
     val io = new Bundle {
       val jtag    = slave(Jtag())
       val switchs = in  Bits(8 bit)
       val keys    = in  Bits(4 bit)
       val leds    = out Bits(8 bit)
     }

     val tap = new JtagTap(io.jtag, 8)
     val idcodeArea  = tap.idcode(B"x87654321") (instructionId=4)
     val switchsArea = tap.read(io.switchs)     (instructionId=5)
     val keysArea    = tap.read(io.keys)        (instructionId=6)
     val ledsArea    = tap.write(io.leds)       (instructionId=7)
   }

As you can see, a JtagTap is created but then some Generator/Builder functions (idcode,read,write) are called to create each JTAG instruction. This is what i call "Hardware generator/builder", then these Generator/Builder are used by the user to describing an hardware. And there is the point, in commonly HDL you can only describe your hardware, which imply many donkey job.

This JTAG TAP tutorial is based on `this <https://github.com/SpinalHDL/SpinalHDL/tree/master/lib/src/main/scala/spinal/lib/com/jtag>`_ implementation.

.. _jtag:

JTAG bus
--------

First we need to define a JTAG bus bundle.

.. code-block:: scala

   case class Jtag() extends Bundle with IMasterSlave {
     val tms = Bool
     val tdi = Bool
     val tdo = Bool

     override def asMaster() : Unit = {
       out(tdi, tms)
       in(tdo)
     }
   }

As you can see this bus don't contain the TCK pin because it will be provided by the clock domain.

JTAG state machine
------------------

Let's define the JTAG state machine as explained `here <http://www.fpga4fun.com/JTAG2.html>`_

.. code-block:: scala

   object JtagState extends SpinalEnum {
     val RESET, IDLE,
         IR_SELECT, IR_CAPTURE, IR_SHIFT, IR_EXIT1, IR_PAUSE, IR_EXIT2, IR_UPDATE,
         DR_SELECT, DR_CAPTURE, DR_SHIFT, DR_EXIT1, DR_PAUSE, DR_EXIT2, DR_UPDATE = newElement()
   }

   class JtagFsm(jtag: Jtag) extends Area {
     import JtagState._
     val stateNext = JtagState()
     val state = RegNext(stateNext) randBoot()

     stateNext := state.mux(
       default    -> (jtag.tms ? RESET     | IDLE),           //RESET
       IDLE       -> (jtag.tms ? DR_SELECT | IDLE),
       IR_SELECT  -> (jtag.tms ? RESET     | IR_CAPTURE),
       IR_CAPTURE -> (jtag.tms ? IR_EXIT1  | IR_SHIFT),
       IR_SHIFT   -> (jtag.tms ? IR_EXIT1  | IR_SHIFT),
       IR_EXIT1   -> (jtag.tms ? IR_UPDATE | IR_PAUSE),
       IR_PAUSE   -> (jtag.tms ? IR_EXIT2  | IR_PAUSE),
       IR_EXIT2   -> (jtag.tms ? IR_UPDATE | IR_SHIFT),
       IR_UPDATE  -> (jtag.tms ? DR_SELECT | IDLE),
       DR_SELECT  -> (jtag.tms ? IR_SELECT | DR_CAPTURE),
       DR_CAPTURE -> (jtag.tms ? DR_EXIT1  | DR_SHIFT),
       DR_SHIFT   -> (jtag.tms ? DR_EXIT1  | DR_SHIFT),
       DR_EXIT1   -> (jtag.tms ? DR_UPDATE | DR_PAUSE),
       DR_PAUSE   -> (jtag.tms ? DR_EXIT2  | DR_PAUSE),
       DR_EXIT2   -> (jtag.tms ? DR_UPDATE | DR_SHIFT),
       DR_UPDATE  -> (jtag.tms ? DR_SELECT | IDLE)
     )
   }

.. note::
   The ``randBoot()`` on ``state`` make it initialized with a random state. It's only for simulation purpose.

JTAG TAP
--------

Let's implement the core of the JTAG TAP, without any instruction, just the base manage the instruction register (IR) and the bypass.

.. code-block:: scala

   class JtagTap(val jtag: Jtag, instructionWidth: Int) extends Area{
     val fsm = new JtagFsm(jtag)
     val instruction = Reg(Bits(instructionWidth bit))
     val instructionShift = Reg(Bits(instructionWidth bit))
     val bypass = Reg(Bool)

     jtag.tdo := bypass

     switch(fsm.state) {
       is(JtagState.IR_CAPTURE) {
         instructionShift := instruction
       }
       is(JtagState.IR_SHIFT) {
         instructionShift := (jtag.tdi ## instructionShift) >> 1
         jtag.tdo := instructionShift.lsb
       }
       is(JtagState.IR_UPDATE) {
         instruction := instructionShift
       }
       is(JtagState.DR_SHIFT) {
         bypass := jtag.tdi
       }
     }
   }

Jtag instructions
-----------------

Now that the JTAG TAP core is done, we can think about how to implement JTAG instructions by an reusable way.

JTAG TAP class interface
^^^^^^^^^^^^^^^^^^^^^^^^

First we need to define how an instruction could interact with the JTAG TAP core. We could of course directly take the JtagTap area, but it's not very nice because is some situation the JTAG TAP core is provided by another IP (Altera virtual JTAG for example).

So let's define a simple and abstract interface between the JTAG TAP core and instructions :

.. code-block:: scala

   trait JtagTapAccess {
     def getTdi : Bool
     def getTms : Bool
     def setTdo(value : Bool) : Unit

     def getState : JtagState.T
     def getInstruction() : Bits
     def setInstruction(value : Bits) : Unit
   }

Then let's the JtagTap implement this abstract interface :

.. code-block:: scala

   class JtagTap(val jtag: Jtag, ...) extends Area with JtagTapAccess{
     ...

     //JtagTapAccess impl
     override def getTdi: Bool = jtag.tdi
     override def setTdo(value: Bool): Unit = jtag.tdo := value
     override def getTms: Bool = jtag.tms

     override def getState: JtagState.T = fsm.state
     override def getInstruction(): Bits = instruction
     override def setInstruction(value: Bits): Unit = instruction := value
   }

Base class
^^^^^^^^^^

Let's define a useful base class for JTAG instruction that provide some callback (doCapture/doShift/doUpdate/doReset) depending the selected instruction and the state of the JTAG TAP :

.. code-block:: scala

   class JtagInstruction(tap: JtagTapAccess,val instructionId: Bits) extends Area {
     def doCapture(): Unit = {}
     def doShift(): Unit = {}
     def doUpdate(): Unit = {}
     def doReset(): Unit = {}

     val instructionHit = tap.getInstruction === instructionId

     Component.current.addPrePopTask(() => {
       when(instructionHit) {
         when(tap.getState === JtagState.DR_CAPTURE) {
           doCapture()
         }
         when(tap.getState === JtagState.DR_SHIFT) {
           doShift()
         }
         when(tap.getState === JtagState.DR_UPDATE) {
           doUpdate()
         }
       }
       when(tap.getState === JtagState.RESET) {
         doReset()
       }
     })
   }

.. note::
   | About the Component.current.addPrePopTask(...) : 
   | This  allow you to call the given code at the end of the current component construction. Because of object oriented nature of JtagInstruction, doCapture, doShift, doUpdate and doReset should not be called before children classes construction (because children classes will use it as a callback to do some logic)

Read instruction
^^^^^^^^^^^^^^^^

Let's implement an instruction that allow the JTAG to read a signal.

.. code-block:: scala

   class JtagInstructionRead[T <: Data](data: T) (tap: JtagTapAccess,instructionId: Bits)extends JtagInstruction(tap,instructionId) {
     val shifter = Reg(Bits(data.getBitsWidth bit))

     override def doCapture(): Unit = {
       shifter := data.asBits
     }

     override def doShift(): Unit = {
       shifter := (tap.getTdi ## shifter) >> 1
       tap.setTdo(shifter.lsb)
     }
   }

Write instruction
^^^^^^^^^^^^^^^^^

Let's implement an instruction that allow the JTAG to write a register (and also read its current value).

.. code-block:: scala

   class JtagInstructionWrite[T <: Data](data: T) (tap: JtagTapAccess,instructionId: Bits) extends JtagInstruction(tap,instructionId) {
     val shifter,store = Reg(Bits(data.getBitsWidth bit))

     override def doCapture(): Unit = {
       shifter := store
     }
     override def doShift(): Unit = {
       shifter := (tap.getTdi ## shifter) >> 1
       tap.setTdo(shifter.lsb)
     }
     override def doUpdate(): Unit = {
       store := shifter
     }

     data.assignFromBits(store)
   }

Idcode instruction
^^^^^^^^^^^^^^^^^^

Let's implement the instruction that return a idcode to the JTAG and also, when a reset occur, set the instruction register (IR) to it own instructionId.

.. code-block:: scala

   class JtagInstructionIdcode[T <: Data](value: Bits)(tap: JtagTapAccess, instructionId: Bits)extends JtagInstruction(tap,instructionId) {
     val shifter = Reg(Bits(32 bit))

     override def doShift(): Unit = {
       shifter := (tap.getTdi ## shifter) >> 1
       tap.setTdo(shifter.lsb)
     }

     override def doReset(): Unit = {
       shifter := value
       tap.setInstruction(instructionId)
     }
   }

User friendly wrapper
---------------------

Let's add some user friendly function to the JtagTapAccess to make instructions instantiation easier .

.. code-block:: scala

   trait JtagTapAccess {
     ...

     def idcode(value: Bits)(instructionId: Bits) =
       new JtagInstructionIdcode(value)(this,instructionId)

     def read[T <: Data](data: T)(instructionId: Bits)   =
       new JtagInstructionRead(data)(this,instructionId)

     def write[T <: Data](data: T,  cleanUpdate: Boolean = true, readable: Boolean = true)(instructionId: Bits) =
       new JtagInstructionWrite[T](data,cleanUpdate,readable)(this,instructionId)
   }

Usage demonstration
-------------------

And there we are, we can now very easly create an application specific JTAG TAP without having to write any logic or any interconnections.

.. code-block:: scala

   class SimpleJtagTap extends Component {
     val io = new Bundle {
       val jtag    = slave(Jtag())
       val switchs = in  Bits(8 bit)
       val keys    = in  Bits(4 bit)
       val leds    = out Bits(8 bit)
     }

     val tap = new JtagTap(io.jtag, 8)
     val idcodeArea  = tap.idcode(B"x87654321") (instructionId=4)
     val switchsArea = tap.read(io.switchs)     (instructionId=5)
     val keysArea    = tap.read(io.keys)        (instructionId=6)
     val ledsArea    = tap.write(io.leds)       (instructionId=7)
   }

This way of doing things (Generating hardware) could also be applied to, for example, generating an APB/AHB/AXI bus slave.
