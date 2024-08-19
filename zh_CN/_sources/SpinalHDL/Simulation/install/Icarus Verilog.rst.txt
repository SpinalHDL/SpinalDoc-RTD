
Setup and installation of Icarus Verilog
========================================

.. note::
   If you installed the recommended oss-cad-suite during SpinalHDL :ref:`setup <Install>` you
   can skip the instructions below - but you need to activate the oss-cad-suite environment.

In most recent linux distributions, a recent version of Icarus Verilog is generally available through the package system.
The C++ library boost-interprocess, which is contained in the libboost-dev package in debian-like distributions, has to be installed too. boost-interprocess is required to generate the shared memory communication interface. 

Linux
^^^^^

.. code-block:: sh

   sudo apt-get install build-essential libboost-dev iverilog


Also the openjdk package that corresponds to your Java version has to be installed.
Refer to `<https://iverilog.fandom.com/wiki/Installation_Guide>`_ for more information about Windows and installation from source.

