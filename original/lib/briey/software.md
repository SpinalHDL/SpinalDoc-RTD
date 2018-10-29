---
layout: page
title: Pinsec software
description: ""
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/lib/pinsec/software/
---

## RISCV tool-chain

Binaries executed by the CPU can be defined in ASM/C/C++ and compiled by the GCC RISCV fork. Also, to load binaries and debug the CPU, an OpenOCD fork and RISCV GDB can be used.

RISCV official tools : [https://riscv.org/software-tools/](https://riscv.org/software-tools/)<br>
OpenOCD fork : [https://github.com/Dolu1990/openocd_riscv](https://github.com/Dolu1990/openocd_riscv)<br>
Software examples : [https://github.com/Dolu1990/pinsecSoftware](https://github.com/Dolu1990/pinsecSoftware)<br>

## OpenOCD/GDB/Eclipse  configuration

About the OpenOCD fork, there is the configuration file that could be used to connect the Pinsec SoC : [https://github.com/Dolu1990/openocd_riscv/blob/riscv_spinal/tcl/target/riscv_spinal.cfg](https://github.com/Dolu1990/openocd_riscv/blob/riscv_spinal/tcl/target/riscv_spinal.cfg)

There is an example of arguments used to run the OpenOCD tool :

```
openocd -f ../tcl/interface/ftdi/ft2232h_breakout.cfg -f ../tcl/target/riscv_spinal.cfg -d 3
```

To debug with eclipse, you will need the Zylin plugin and then create an "Zynlin embedded debug (native)".

Initialize commands :

```
target remote localhost:3333
monitor reset halt
load
```

Run commands :

```
continue
```
