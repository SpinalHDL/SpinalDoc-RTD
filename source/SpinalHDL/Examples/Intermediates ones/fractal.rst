
Fractal calculator
==================

Introduction
------------

This example will show a simple implementation (without optimization) of a Mandelbrot fractal calculator by using data streams and fixed point calculations.

Specification
-------------

The component will receive one ``Stream`` of pixel tasks (which contain the XY coordinates in the Mandelbrot space) and will produce one ``Stream`` of pixel results (which contain the number of iterations done for the corresponding task).

Let's specify the IO of our component:

.. list-table::
   :header-rows: 1
   :widths: 1 1 1 10

   * - IO Name
     - Direction
     - Type
     - Description
   * - cmd
     - slave
     - Stream[PixelTask]
     - Provide XY coordinates to process
   * - rsp
     - master
     - Stream[PixelResult]
     - Return iteration count needed for the corresponding cmd transaction


Let's specify the PixelTask ``Bundle``\ :

.. list-table::
   :header-rows: 1
   :widths: 1 1 5

   * - Element Name
     - Type
     - Description
   * - x
     - SFix
     - Coordinate in the Mandelbrot space
   * - y
     - SFix
     - Coordinate in the Mandelbrot space


Let's specify the PixelResult ``Bundle``\ :

.. list-table::
   :header-rows: 1
   :widths: 1 1 5

   * - Element Name
     - Type
     - Description
   * - iteration
     - UInt
     - Number of iterations required to solve the Mandelbrot coordinates


Elaboration parameters (Generics)
---------------------------------

Let's define the class that will provide construction parameters of our system:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Fractal.scala
   :language: scala
   :start-at: case class PixelSolverGenerics(
   :end-before: // end case class PixelSolverGenerics

.. note::
   iterationType and fixType are functions that you can call to instantiate new signals. It's like a typedef in C.

Bundle definition
-----------------

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Fractal.scala
   :language: scala
   :start-after: // begin bundles
   :end-before: // end bundles

Component implementation
------------------------

And now the implementation. The one below is a very simple one without pipelining / multi-threading.

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/intermediate/Fractal.scala
   :language: scala
   :start-at: case class PixelSolver(
   :end-before: // end case class PixelSolver