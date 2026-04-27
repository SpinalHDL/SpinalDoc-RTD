.. _Using IntelliJ:

Using Spinal from IntelliJ IDEA
===============================

In addition to the aforementioned requirements, you also need to download the IntelliJ IDEA (the free *Community edition* is enough). When you have installed IntelliJ, also check that you have enabled its Scala plugin (\ `install information <https://www.jetbrains.com/help/idea/2016.1/enabling-and-disabling-plugins.html?origin=old_help>`_ can be found here).

And do the following:

* In *Intellij IDEA*\ , "import project" with the root of this repository, the choose the *Import project from external model SBT* and be sure to check all boxes.
* In addition, you might need to specify some path like where you installed the JDK to *IntelliJ*.
* In the project (Intellij project GUI), right click on ``src/main/scala/mylib/MyTopLevel.scala`` and select "Run MyTopLevel".

This should generate the output file ``MyTopLevel.vhd`` in the project directory, which implements a simple 8-bit counter.

Now you can use your environment, let's explore the code: :ref:`Simple example`.
