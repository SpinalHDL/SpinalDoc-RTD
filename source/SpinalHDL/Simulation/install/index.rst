SBT setup for simulation
========================

To enable SpinalSim, the following lines have to be added in your build.sbt file :

.. code-block:: scala

   fork := true

Also the following imports have to be added in testbenches sources :

.. code-block:: scala

   import spinal.core._
   import spinal.core.sim._

.. _sim backend install:

Also, if you need to use gmake instead of make (ex OpenBSD) you can set the SPINAL_MAKE_CMD environnement variable to "gmake"

Backend-dependent installation instructions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. toctree::
   :glob:

   *
