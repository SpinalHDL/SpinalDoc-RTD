Installation instructions
=========================

Scala
^^^^^

To enable SpinalSim, the following lines have to be added in your build.sbt file :

.. code-block:: scala

   fork := true

Also the following imports have to be added in testbenches sources :

.. code-block:: scala

   import spinal.core._
   import spinal.core.sim._

Backend-dependent installation instructions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. toctree::
   :glob:

   *
