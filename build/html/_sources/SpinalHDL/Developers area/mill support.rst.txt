Build through Mill
==================

SpinalHDL itself can be built with Mill.  This is an alternative to the Sbt
build tool that can be found at Introduction_to_Mill_.
It can compile/test/publishLocal the existing modules.
Build through mill can be much faster than Sbt, which is useful while debugging.

Compile the library
-------------------

.. code-block:: sh

   mill __.compile
   sbt compile # equivalent alternatives

Run all test suites
-------------------

.. code-block:: sh

   mill __.test
   sbt test # equivalent alternatives

Run a specified test suite
--------------------------

.. code-block:: sh

   mill tester.test.testOnly spinal.xxxxx.xxxxx
   sbt "tester/testOnly spinal.xxxxx.xxxxx" # equivalent alternatives

Run a specified App
-------------------

.. code-block:: sh

   mill tester.runMain spinal.xxxxx.xxxxx
   sbt "tester/runMain spinal.xxxxx.xxxxx" # equivalent alternatives

Publish locally
---------------

Mill can also publish the library to the local ivy2 repository as a ``dev`` version.

.. code-block:: sh

   mill __.publishLocal
   sbt publishLocal # equivalent alternatives

.. _Introduction_to_Mill: http://mill-build.com/mill/Intro_to_Mill.html
