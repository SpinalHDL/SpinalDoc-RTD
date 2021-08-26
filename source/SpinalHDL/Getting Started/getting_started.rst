.. role:: raw-html-m2r(raw)
   :format: html

.. _getting_started:

Getting Started
===============

*SpinalHDL* is a hardware description language written in `Scala <https://scala-lang.org/>`_\ , a statically-typed functional language using the Java virtual machine (JVM). In order to start programming with *SpinalHDL*\ , you must have a JVM as well as the Scala compiler. In the next section, we will explain how to download those tools if you don't have them already.

.. _requirements:

Requirements / Things to download to get started
-------------------------------------------------

Before you download the SpinalHDL tools, you need to install:


* A Java JDK, which can be downloaded from `here <https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html>`__ for instance.
* A Scala 2.11.X distribution, which can be downloaded `here <https://scala-lang.org/download/>`__ (not required if you use SBT).
* The SBT build tool, which can be downloaded `here <https://www.scala-sbt.org/download.html>`__.

Optionally:


* An IDE (which is not compulsory). We advise you to get `IntelliJ <https://www.jetbrains.com/idea/>`_ with its Scala plugin.
* `Git <https://git-scm.com/>`_, which is a tool for version control.

How to start programming with SpinalHDL
---------------------------------------

Once you have downloaded all the requirements, there are two ways to get started with SpinalHDL programming.


#. :ref:`The SBT way <getting_started_sbt_way>` : If you already are familiar with the SBT build system and/or if you don't need an IDE.
#. :ref:`The IDE way <getting_started_ide_way>` : Get a project already set up for you in an IDE and start programming right away.

.. _getting_started_sbt_way:

The SBT way 
^^^^^^^^^^^^

We have prepared a ready-to-go project for you on Github.


* Either clone or `download <https://codeload.github.com/SpinalHDL/SpinalTemplateSbt/zip/master>`_ the `"getting started" repository <https://github.com/SpinalHDL/SpinalTemplateSbt>`_.
* Open a terminal in the root of it and run ``sbt run``. When you execute it for the first time, the process could take some time as it will download all the dependencies required to run *SpinalHDL*.

Normally, this command must generate an output file ``MyTopLevel.vhd``\ , which corresponds to the top level *SpinalHDL* code defined in ``src\main\scala\MyCode.scala``\ , which corresponds to the :ref:`most simple SpinalHDL example <example>`

From a clean Debian distribution you can type the following commands into the shell. The commands will install Java, Scala, SBT, download the base project, and generate the corresponding VHDL file. Don't worry if it takes some time the first time that you run it.

.. code-block:: sh

   sudo apt-get install openjdk-8-jdk
   sudo apt-get install scala
   echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
   echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
   curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
   sudo apt-get update
   sudo apt-get install sbt
   git clone https://github.com/SpinalHDL/SpinalTemplateSbt.git SpinalTemplateSbt
   cd SpinalTemplateSbt
   sbt run   # select "mylib.MyTopLevelVhdl" in the menu
   ls MyTopLevel.vhd

SBT in a environnement isolated from internet
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Normally, SBT uses online repositories to download and cache your projects dependencies, this cache is located in your ``home/.ivy2`` folder. The way to set up an internet-free environnement is to copy this cache from an internet-full environnement where the cache was already filled once, and copy it over to your internet-less environnement.

| You can get a portable SBT setup here:
| https://www.scala-sbt.org/download.html

.. _getting_started_ide_way:

The IDE way, with IntelliJ IDEA and its Scala plugin
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In addition to the aforementioned :ref:`requirements <requirements>` , you also need to download the IntelliJ IDEA (the free *Community edition* is enough). When you have installed IntelliJ, also check that you have enabled its Scala plugin (\ `install information <https://www.jetbrains.com/help/idea/2016.1/enabling-and-disabling-plugins.html?origin=old_help>`_ can be found here).

And do the following :


* Either clone or `download <https://codeload.github.com/SpinalHDL/SpinalTemplateSbt/zip/master>`_ the `"getting started" repository <https://github.com/SpinalHDL/SpinalTemplateSbt>`_.
* In *Intellij IDEA*\ , "import project" with the root of this repository, the choose the *Import project from external model SBT* and be sure to check all boxes.
* In addition, you might need to specify some path like where you installed the JDK to *IntelliJ*.
* In the project (Intellij project GUI), right click on ``src/main/scala/mylib/MyTopLevel.scala`` and select "Run MyTopLevel".

This should generate the output file ``MyTopLevel.vhd`` in the project directory, which implements a simple 8-bit counter.

.. _example:

A very simple SpinalHDL example
-------------------------------

The following code generates an ``and`` gate between two one-bit inputs.

.. code-block:: scala

    import spinal.core._

    class AND_Gate extends Component {

      /**
        * This is the component definition that corresponds to
        * the VHDL entity of the component
        */
      val io = new Bundle {
        val a = in Bool()
        val b = in Bool()
        val c = out Bool()
      }

      // Here we define some asynchronous logic
      io.c := io.a & io.b
    }

    object AND_Gate {
      // Let's go
      def main(args: Array[String]) {
        SpinalVhdl(new AND_Gate)
      }
    }

As you can see, the first line you have to write in SpinalHDL is ``import spinal.core._`` which indicates that we are using the *Spinal* components in the file.

Generated code
^^^^^^^^^^^^^^

Once you have successfully compiled your code, the compiler should have emitted the following VHDL code:

.. code-block:: vhdl

   package pkg_enum is
     ...
   end pkg_enum;

   package pkg_scala2hdl is
     ...
   end  pkg_scala2hdl;

   library ieee;
   use ieee.std_logic_1164.all;
   use ieee.numeric_std.all;

   library work;
   use work.pkg_scala2hdl.all;
   use work.all;
   use work.pkg_enum.all;


   entity AND_Gate is
     port(
       io_a : in std_logic;
       io_b : in std_logic;
       io_c : out std_logic
     );
   end AND_Gate;

   architecture arch of AND_Gate is

   begin
     io_c <= (io_a and io_b);
   end arch;

What to do next?
----------------

It's up to you, but why not have a look at what the :ref:`types <type_introduction>` are in SpinalHDL or discover what primitives the language provides to describe hardware components? You could also have a look at our :ref:`examples <example_introduction>` to see some samples of what you could do next.


