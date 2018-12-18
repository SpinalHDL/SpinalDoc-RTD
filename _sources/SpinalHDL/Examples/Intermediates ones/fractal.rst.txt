
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

.. code-block:: scala

   case class PixelSolverGenerics(fixAmplitude : Int,
                                  fixResolution : Int,
                                  iterationLimit : Int){
     val iterationWidth = log2Up(iterationLimit+1)
     def iterationType = UInt(iterationWidth bits)
     def fixType = SFix(
       peak = fixAmplitude exp,
       resolution = fixResolution exp
     )
   }

.. note::
   iterationType and fixType are functions that you can call to instantiate new signals. It's like a typedef in C.

Bundle definition
-----------------

.. code-block:: scala

   case class PixelTask(g : PixelSolverGenerics) extends Bundle{
     val x,y = g.fixType
   }

   case class PixelResult(g : PixelSolverGenerics) extends Bundle{
     val iteration = g.iterationType
   }

Component implementation
------------------------

And now the implementation. The one below is a very simple one without pipelining / multi-threading.

.. code-block:: scala

   case class PixelSolver(g : PixelSolverGenerics) extends Component{
     val io = new Bundle{
       val cmd = slave  Stream(PixelTask(g))
       val rsp = master Stream(PixelResult(g))
     }

     import g._

     //Define states
     val x,y       = Reg(fixType) init(0)
     val iteration = Reg(iterationType) init(0)

     //Do some shared calculation
     val xx = x*x
     val yy = y*y
     val xy = x*y

     //Apply default assignement
     io.cmd.ready := False
     io.rsp.valid := False
     io.rsp.iteration := iteration

     when(io.cmd.valid) {
       //Is the mandelbrot iteration done ?
       when(xx + yy >= 4.0 || iteration === iterationLimit) {
         io.rsp.valid := True
         when(io.rsp.ready){
           io.cmd.ready := True
           x := 0
           y := 0
           iteration := 0
         }
       } otherwise {
         x := (xx - yy + io.cmd.x).truncated
         y := (((xy) << 1) + io.cmd.y).truncated
         iteration := iteration + 1
       }
     }
   }
