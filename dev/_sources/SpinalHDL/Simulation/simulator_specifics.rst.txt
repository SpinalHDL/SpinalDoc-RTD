==========================
Simulator specific details
==========================

How SpinalHDL simulates the hardware with Verilator backend
===========================================================

1. Behind the scenes, SpinalHDL generates a Verilog equivalent hardware model of the DUT and then uses Verilator to convert it to a C++ cycle-accurate model. 
2. The C++ model is compiled into a shared object (.so), which is bound to Scala via JNI-FFI.
3. The native Verilator API is abstracted by providing a simulation multi-threaded API.

**Advantages:**

* Since the Verilator backend uses a compiled C++ simulation model, the simulation speed is fast compared to most of the other commercial and free simulators.

**Limitations:**

* Verilator accepts only synthesizable Verilog/System Verilog code. Therefore special care has to be taken when simulating Verilog blackbox components that may have non-synthesizable statements.
* VHDL blackboxes cannot be simulated.
* The simulation boot process is slow due to the necessity to compile and link the generated C++ model.  Some support to incrementally compile and link exists which can provide speedups for subsequent simulations after building the first.

How SpinalHDL simulates the hardware with GHDL/Icarus Verilog backend
=====================================================================

1. Depending on the chosen simulator, SpinalHDL generates a Verilog or VHDL hardware model of the DUT. 
2. The HDL model is loaded in the simulator. 
3. The communication between the simulation and the JVM is established through shared memory. The commands are issued to the simulator using `VPI <https://en.wikipedia.org/wiki/Verilog_Procedural_Interface>`_.

**Advantages:**

* Both GHDL and Icarus Verilog can accept non-synthesizable HDL code.
* The simulation boot process is quite faster compared to Verilator.

**Limitations:**

* GHDL accepts VHDL code only. Therefore only VHDL blackboxes can be used with this simulator.
* Icarus Verilog accepts Verilog code only. Therefore only Verilog blackboxes can be used with this simulator.
* The simulation speed is around one order of magnitude slower compared to Verilator.

Finally, as the native Verilator API is rather crude, SpinalHDL abstracts over it by providing both single and multi-threaded simulation APIs to help the user construct testbench implementations.

How SpinalHDL simulates the hardware with Synopsys VCS backend
==============================================================

1. SpinalHDL generates a Verilog/VHDL (depended on your choice) hardware model of the DUT.
2. The HDL model is loaded in the simulator.
3. The communication between the simulation and the JVM is established through shared memory. The commands are issued to the simulator using `VPI <https://en.wikipedia.org/wiki/Verilog_Procedural_Interface>`_.

**Advantages:**

* Support all language features of SystemVerilog/Verilog/VHDL.
* Support encrypted IP.
* Support FSDB wave format dump.
* High Performance of both compilation and simulation.

**Limitations:**

* Synopsys VCS is a **commercial** simulation tool. It is close source and not free. You have to own the licenses to **legally** use it.

Before using VCS as the simulation backend, make sure that you have checked your system environment as :ref:`VCS environment<vcs_env>`.

How SpinalHDL simulates the hardware with Xilinx XSim backend
==============================================================

1. SpinalHDL generates a Verilog/VHDL (depended on your choice) hardware model of the DUT.
2. The HDL model is loaded in the simulator.
3. The communication between the simulation and the JVM is established through shared memory. The commands are issued to the simulator using XSI.

**Advantages:**

* Support Xilinx built-in primitives and cores.

**Limitations:**

* Xilinx XSim is a **commercial** tool installed with Vivado. It is closed source and subject to licensing terms to use. You have to own the licenses to **legally** use it.
* Vivado versions prior to 2019.1 do not work properly.

Before using XSim as the simulation backend, make sure that you have done following steps.
1. Define VIVADO_HOME environment variable to specify where your vivado located. ex `export VIVADO_HOME=/d/Xilinx/Vivado/2022.1` (under MSYS2).
2. Make sure two vivado path is inside the PATH. For Windows MSYS2 user, run shell command like `export PATH=$PATH:$VIVADO_HOME/bin:$VIVADO_HOME/lib/win64.o`. For Linux user just source the Vivado's settings64.sh file located at `VIVADO_HOME`.

Performance
===========

When a high-performance simulation is required, Verilator should be used as a backend. On a little SoC like `Murax <https://github.com/SpinalHDL/VexRiscv>`_, an Intel® Core™ i7-4720HQ is capable of simulating 1.2 million clock cycles per second. However, when the DUT is simple and a maximum of few thousands clock cycles have to be simulated, using GHDL or Icarus Verilog could yield a better result, due to their lower simulation loading overhead.

