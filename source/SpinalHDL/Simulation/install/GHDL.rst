
Setup and installation of GHDL
==============================

.. note::
   If you installed the recommended oss-cad-suite during SpinalHDL :ref:`setup <Install>` you
   can skip the instructions below - but you need to activate the oss-cad-suite environment.

Even though GHDL is generally available in linux distributions package system, SpinalHDL depends on bugfixes of GHDL codebase that were added after the release of GHDL v0.37. Therefore it is recommended to install GHDL from source.
The C++ library boost-interprocess, which is contained in the libboost-dev package in debian-like distributions, has to be installed too. boost-interprocess is required to generate the shared memory communication interface. 

Linux
^^^^^

.. code-block:: sh

   sudo apt-get install build-essential libboost-dev git
   sudo apt-get install gnat # Ada compiler used to buid GHDL
   git clone https://github.com/ghdl/ghdl.git
   cd ghdl
   mkdir build
   cd build
   ../configure
   make
   sudo make install



Also the openjdk package that corresponds to your Java version has to be installed.

For more configuration options and Windows installation see `<https://ghdl.github.io/ghdl/getting.html>`_
