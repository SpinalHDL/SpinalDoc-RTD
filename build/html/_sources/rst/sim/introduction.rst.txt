.. role:: raw-html-m2r(raw)
   :format: html


Introduction
------------

As always you can use your standard simulation tools to simulate the VHDL/Verilog generated out from SpinalHDL, but since SpinalHDL 1.0.0 the language integrate an API to write testbenches and test your hardware directly in Scala. This API provide the capabilities to read and write the DUT signals, fork and join simulation processes, sleep and wait until a given condition is filled.

How SpinalHDL simulate the hardware
-----------------------------------

Behind the scene SpinalHDL is generating a C++ cycle accurate model of your hardware by generating the equivalent Verilog and asking Verilator to convert it into a C++ model. :raw-html-m2r:`<br>`
Then SpinalHDL ask GCC to compile the C++ model into an shared object (.so) and bind it back to Scala via JNR-FFI. :raw-html-m2r:`<br>`
Finaly, as the native Verilator API is rude, SpinalHDL abstract it by providing an simulation multi-threaded API to help the user in his testbench implementation.

This method has several advantage :


* The C++ simulation model is realy fast to process simulation steps
* It test the generated Verilog hardware instead of the SpinalHDL internal model
* It doesn't require SpinalHDL to be able itself to simulate the hardware (Less codebase, less bugs as Verilator is a reliable tool)

And some limitations :


* Verilator will only accept to translate Synthetisable Verilog code

Performance
-----------

As verilator is currently the simulation backend, the simulation speed is realy high. On a little SoC like `Murax <https://github.com/SpinalHDL/VexRiscv#murax-soc>`_ my laptop is capable to simulate 1.2 million clock cycles per realtime seconds.

Setup
-----

The SpinalSim with Verilator as backend is supported on both Linux and Windows platforms.

Scala
^^^^^

Don't forget to add the following in your build.sbt file

.. code-block:: scala

   scalaVersion := "2.11.6"
   addCompilerPlugin("org.scala-lang.plugins" % "scala-continuations-plugin_2.11.6" % "1.0.2")
   scalacOptions += "-P:continuations:enable"
   fork := true

And you will always need the following imports in your Scala testbench :

.. code-block:: scala

   import spinal.sim._
   import spinal.core._
   import spinal.core.sim._

Linux
^^^^^

You will also need a recent version of Verilator installed :

.. code-block:: sh

   sudo apt-get install git make autoconf g++ flex bison -y  # First time prerequisites
   git clone http://git.veripool.org/git/verilator   # Only first time
   unsetenv VERILATOR_ROOT  # For csh; ignore error if on bash
   unset VERILATOR_ROOT  # For bash
   cd verilator
   git pull        # Make sure we're up-to-date
   git checkout verilator_3_916
   autoconf        # Create ./configure script
   ./configure
   make -j$(nproc)
   sudo make install
   echo "DONE"

Windows
^^^^^^^

In order to get SpinalSim + Verilator working on windows, you have to do the following :


* Install MSYS2
* Via MSYS2 get gcc/g++/verilator (for verilator you can compile it from the sources)
* Add bin and usr\bin of MSYS2 into your windows PATH (ie : C:\msys64\usr\bin;C:\msys64\mingw64\bin)

Then you should be able to run SpinalSim + verilator from your Scala project without having to use MSYS2 anymore.

From a fresh install of MSYS2 MinGW 64-bits, you will have to run the following commands inside the MSYS2 MinGW 64-bits shell (enter commands one by one):

.. code-block:: sh

   pacman -Syuu
   #Close the MSYS2 shell once you're asked to
   pacman -Syuu
   pacman -S --needed base-devel mingw-w64-x86_64-toolchain \
                      git flex\
                      mingw-w64-x86_64-cmake

   git clone http://git.veripool.org/git/verilator  
   unset VERILATOR_ROOT
   cd verilator
   git pull        
   git checkout verilator_3_916
   autoconf      
   ./configure
   export CPLUS_INCLUDE_PATH=/usr/include:$CPLUS_INCLUDE_PATH
   export PATH=/usr/bin/core_perl:$PATH
   cp /usr/include/FlexLexer.h ./src

   make -j$(nproc)
   make install
   echo "DONE"
   #Add C:\msys64\usr\bin;C:\msys64\mingw64\bin to you windows PATH

{% include important.html content="Be sure that your PATH environnement variable is pointing to the JDK 1.8 and don't contain a JRE installation." %}

{% include important.html content="Adding the MSYS2 bin folders into your windows PATH could potentialy have some side effects. It's why it is safer to add them as last elements of the PATH to reduce their priority." %}
