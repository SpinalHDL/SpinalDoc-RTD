
Setup and installation of Verilator
===================================

.. note::
   If you installed the recommended oss-cad-suite during SpinalHDL :ref:`setup <Install>` you
   can skip the instructions below - but you need to activate the oss-cad-suite environment.

SpinalSim + Verilator is supported on both Linux and Windows platforms.

It is recommended that v4.218 is the oldest Verilator version to use.  While it maybe
possible to use older verilator versions, some optional and Scala source dependent
features that SpinalHDL can use (such as Verilog ``$urandom`` support) may not be supported
by older Verilator versions and will cause an error when trying to simulate.

Ideally the latest v4.xxx and v5.xxx is well supported and bug reports should be opened
with any issues you have.

Scala
^^^^^

Don't forget to add the following in your ``build.sbt`` file:

.. code-block:: scala

   fork := true

And you will always need the following imports in your Scala testbench:

.. code-block:: scala

   import spinal.core._
   import spinal.core.sim._

Linux
^^^^^

You will also need a recent version of Verilator installed :

.. code-block:: sh

   sudo apt-get install git make autoconf g++ flex bison  # First time prerequisites
   git clone http://git.veripool.org/git/verilator   # Only first time
   unsetenv VERILATOR_ROOT  # For csh; ignore error if on bash
   unset VERILATOR_ROOT  # For bash
   cd verilator
   git pull             # Make sure we're up-to-date
   git checkout v4.218  # Can use newer v4.228 and v5.xxx
   autoconf             # Create ./configure script
   ./configure
   make -j$(nproc)
   sudo make install
   echo "DONE"

Windows
^^^^^^^

In order to get SpinalSim + Verilator working on Windows, you have to do the following:

* Install `MSYS2 <https://www.msys2.org/>`_
* Via MSYS2 get gcc/g++/verilator (for Verilator you can compile it from the sources)
* Add ``bin`` and ``usr\bin`` of MSYS2 into your windows ``PATH`` (ie : ``C:\msys64\usr\bin;C:\msys64\mingw64\bin``)
* Check that the JAVA_HOME environment variable points to the JDK installation folder (i.e.: ``C:\Program Files\Java\jdk-13.0.2``)

Then you should be able to run SpinalSim + Verilator from your Scala project without having to use MSYS2 anymore.

From a fresh install of MSYS2 MinGW 64-bit, you will have to run the following commands inside the MSYS2 MinGW 64-bits shell (enter commands one by one):

From the MinGW package manager
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: sh

   pacman -Syuu
   # Close the MSYS2 shell once you're asked to
   pacman -Syuu
   pacman -S --needed base-devel mingw-w64-x86_64-toolchain \
                      git flex\
                      mingw-w64-x86_64-cmake

   pacman -U http://repo.msys2.org/mingw/x86_64/mingw-w64-x86_64-verilator-4.032-1-any.pkg.tar.xz
   
   # Add C:\msys64\usr\bin;C:\msys64\mingw64\bin to your Windows PATH
   
From source
~~~~~~~~~~~

.. code-block:: sh

   pacman -Syuu
   # Close the MSYS2 shell once you're asked to
   pacman -Syuu
   pacman -S --needed base-devel mingw-w64-x86_64-toolchain \
                      git flex\
                      mingw-w64-x86_64-cmake

   git clone http://git.veripool.org/git/verilator  
   unset VERILATOR_ROOT
   cd verilator
   git pull        
   git checkout v4.218   # Can use newer v4.228 and v5.xxx
   autoconf      
   ./configure
   export CPLUS_INCLUDE_PATH=/usr/include:$CPLUS_INCLUDE_PATH
   export PATH=/usr/bin/core_perl:$PATH
   cp /usr/include/FlexLexer.h ./src

   make -j$(nproc)
   make install
   echo "DONE"
   # Add C:\msys64\usr\bin;C:\msys64\mingw64\bin to your Windows PATH

.. important::
   Be sure that your ``PATH`` environnement variable is pointing to the JDK 1.8 and doesn't contain a JRE installation.

.. important::
   Adding the MSYS2 ``bin`` folders into your windows ``PATH`` could potentially have some side effects.
   This is why it is safer to add them as the last elements of the ``PATH`` to reduce their priority.
