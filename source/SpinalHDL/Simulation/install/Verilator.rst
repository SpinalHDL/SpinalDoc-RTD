
Setup and installation of Verilator
===================================


The SpinalSim API with Verilator as backend is supported on both Linux and Windows platforms.

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
   git checkout v3.916
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
* Add bin and usr\\bin of MSYS2 into your windows PATH (ie : C:\\msys64\\usr\\bin;C:\\msys64\\mingw64\\bin)

Then you should be able to run SpinalSim + verilator from your Scala project without having to use MSYS2 anymore.

From a fresh install of MSYS2 MinGW 64-bits, you will have to run the following commands inside the MSYS2 MinGW 64-bits shell (enter commands one by one):

from the minGW packet manager
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: sh

   pacman -Syuu
   #Close the MSYS2 shell once you're asked to
   pacman -Syuu
   pacman -S --needed base-devel mingw-w64-x86_64-toolchain \
                      git flex\
                      mingw-w64-x86_64-cmake

   pacman -S mingw-w64-x86_64-verilator
   
   #Add C:\msys64\usr\bin;C:\msys64\mingw64\bin to you windows PATH
   
from source
~~~~~~~~~~~

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

.. important::
   Be sure that your PATH environnement variable is pointing to the JDK 1.8 and don't contain a JRE installation.

.. important::
   Adding the MSYS2 bin folders into your windows PATH could potentialy have some side effects. It's why it is safer to add them as last elements of the PATH to reduce their priority.
