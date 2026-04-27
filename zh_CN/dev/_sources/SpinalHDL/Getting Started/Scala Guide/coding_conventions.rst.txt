
Coding conventions
==================

Introduction
------------

The coding conventions used in SpinalHDL are the same as the ones documented in the `Scala Style Guide <https://docs.scala-lang.org/style/>`_.

Some additional practical details and cases are explained in next pages.

class vs case class
-------------------

When you define a ``Bundle`` or a ``Component``, it is preferable to declare it as a case class.

The reasons are:

* It avoids the use of ``new`` keywords. Never having to use it is better than sometimes, under some conditions.
* A ``case class`` provides a ``clone`` function. This is useful in SpinalHDL when there is a need to clone a ``Bundle``, for example, when you define a new ``Reg`` or a new ``Stream`` of some kind.
* Construction parameters are directly visible from outside.

[case] class
^^^^^^^^^^^^

All classes names should start with a uppercase letter

.. code-block:: scala

   class Fifo extends Component {

   }

   class Counter extends Area {

   }

   case class Color extends Bundle {

   }

companion object
^^^^^^^^^^^^^^^^

A `companion object <https://docs.scala-lang.org/overviews/scala-book/companion-objects.html>`_ should start with an uppercase letter.

.. code-block:: scala

   object Fifo {
     def apply(that: Stream[Bits]): Stream[Bits] = {...}
   }

   object MajorityVote {
     def apply(that: Bits): UInt = {...}
   }

An exception to this rule is when the companion object is used as a function (only ``apply`` inside), and these ``apply`` functions don't generate hardware:

.. code-block:: scala

   object log2 {
     def apply(value: Int): Int = {...}
   }

function
^^^^^^^^

A function should always start with a lowercase letter:

.. code-block:: scala

   def sinTable = (0 until sampleCount).map(sampleIndex => {
     val sinValue = Math.sin(2 * Math.PI * sampleIndex / sampleCount)
     S((sinValue * ((1 << resolutionWidth) / 2 - 1)).toInt, resolutionWidth bits)
   })

   val rom =  Mem(SInt(resolutionWidth bits), initialContent = sinTable)

instances
^^^^^^^^^

Instances of classes should always start with a lowercase letter:

.. code-block:: scala

   val fifo   = new Fifo()
   val buffer = Reg(Bits(8 bits))

if / when
^^^^^^^^^

Scala ``if`` and SpinalHDL ``when`` should normally be written in the following way:

.. code-block:: scala

   if(cond) {
     ...
   } else if(cond) {
     ...
   } else {
     ...
   }

   when(cond) {
     ...
   } elsewhen(cond) {
     ...
   } otherwise {
     ...
   }

Exceptions could be:

* It's fine to include a dot before the keyword like methods ``.elsewhen`` and ``.otherwise``.
* It's fine to compress an ``if``\ /\ ``when`` statement onto a single line if it makes the code more readable.

switch
^^^^^^

SpinalHDL ``switch`` should normally be written in the following way:

.. code-block:: scala

   switch(value) {
     is(key) {

     }
     is(key) {

     }
     default {

     }
   }

It's fine to compress an ``is``\ /\ ``default`` statement onto a single line if it makes the code more readable.

Parameters
^^^^^^^^^^

Grouping parameters of a ``Component``/``Bundle`` inside a case class is generally welcome because:

* Easier to carry/manipulate to configure the design
* Better maintainability

.. code-block:: scala

   case class RgbConfig(rWidth: Int, gWidth: Int, bWidth: Int) {
     def getWidth = rWidth + gWidth + bWidth
   }

   case class Rgb(c: RgbConfig) extends Bundle {
     val r = UInt(c.rWidth bits)
     val g = UInt(c.gWidth bits)
     val b = UInt(c.bWidth bits)
   }

But this should not be applied in all cases. For example: in a FIFO, it doesn't make sense to group the ``dataType`` parameter with the ``depth`` parameter of the fifo because, in general, the ``dataType`` is something related to the design, while the ``depth`` is something related to the configuration of the design.

.. code-block:: scala

   class Fifo[T <: Data](dataType: T, depth: Int) extends Component {

   }
