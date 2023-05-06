.. _Simple example:

A simple example
================

Below is a simple hardware description from the `getting started
<https://github.com/SpinalHDL/SpinalTemplateSbt>`_ repository.

.. code-block:: scala

   case class MyTopLevel() extends Component {
     val io = new Bundle {
       val cond0 = in Bool()
       val cond1 = in Bool()
       val flag  = out Bool()
       val state = out UInt(8 bits)
     }

     val counter = Reg(UInt(8 bits)) init 0

     when(io.cond0) {
       counter := counter + 1
     }

     io.state := counter
     io.flag  := (counter === 0) | io.cond1
   }

It is split into chunks and explained in this section.


Component
---------

First, there is the structure of a SpinalHDL ``Component``.

A component is a piece of logic which can be instantiated (pasted) as many times
as needed, and where the only accessible signals are its inputs and outputs.

.. code-block:: scala

   case class MyTopLevel() extends Component {
     val io = new Bundle {
       // port definitions go here
     }

     // component logic goes here
   }

``MyTopLevel`` is the name of the component.

In SpinalHDL, components use ``UpperCamelCase``.

.. note::

   See :ref:`Component` for more information.


Ports
-----

Then, the ports are defined.

.. code-block:: scala

       val cond0 = in port Bool
       val cond1 = in port Bool
       val flag = out port Bool
       val state = out port UInt(8 bits)

Directions:

* ``cond0`` and ``cond1`` are inputs ports
* ``flag`` and ``state`` are outputs ports

Types:

* ``cond0``, ``cond1`` and ``flag`` are three bits (3 individual wires)
* ``state`` is an 8-bit unsigned integer (a set of 8 wires representing an
  unsigned integer)

.. note::

   This syntax is only available since SpinalHDL 1.8, see :ref:`io` for legacy
   syntax and more information.


Internal logic
--------------

Finally, there is the component logic:

.. code-block:: scala

     val counter = Reg(UInt(8 bits)) init 0

     when(io.cond0) {
       counter := counter + 1
     }

     io.state := counter
     io.flag := (counter === 0) | io.cond1

``counter`` is a register containing an 8-bits unsigned integer, with the
initial value 0. Assignments to a registers can be read only after the next
clock sampling.

.. note::

   REVIEWME We introduce the notion of the clock here but fail to explain
   how that is conveyed / exists in the example.  I think that is an important point
   to understand early on in the learning process.
   The below note does not really explain the concept of time, it is
   more important for a new user to translate the Scala code into HDL execution with
   understanding the clock and time relation to the code, than it is to know there
   are 2 hidden signals around somewhere.

.. note::

   Because of the presence of a register, two implicit signals are added to the
   component for the clock and the reset. See :ref:`Reg` and :ref:`clock_domain`
   for more information.

Then a conditional rule is described: when the input ``cond0`` (which is in the
``io`` bundle) is set, the ``counter`` is incremented by one, else ``counter``
keeps its value set in the last rule. But, there is no previous rule, you would
say. With a simple signal it would be a latch, and trigger an error. But here
``counter`` is a register, so it has a default case: it just keeps the same
value.

This creates a multiplexer: the input of the ``counter`` register can be its
output or its output plus one depending on ``io.cond0``.

Then unconditional rules (assignments) are described:

* The output ``state`` is connected to the output of the register ``counter``.
* The output ``flag`` is the output of an ``or`` gate between a signal which is
  true when the output of "``counter`` equals 0", and the input ``cond1``.

.. note::

   See :ref:`semantics` for more information.
