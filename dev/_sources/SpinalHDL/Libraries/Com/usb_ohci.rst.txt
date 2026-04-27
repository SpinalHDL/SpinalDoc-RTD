
USB OHCI
========

Here exists a USB OHCi controller (host) in the SpinalHDL library.

A few bullet points to summarize support:

- It follow the `OpenHCI Open Host Controller Interface Specification for USB` specification (OHCI).
- It is compatible with the upstream linux / uboot OHCI drivers already. (there is also an OHCI driver on tinyUSB)
- This provides USB host full speed and low speed capabilities (12 Mbps and 1.5 Mbps)
- Tested on linux and uboot
- One controller can host multiple ports (up to 16)
- Bmb memory interface for DMA accesses
- Bmb memory interface for the configuration
- Requires a clock for the internal phy which is a multiple of 12 Mhz at least 48 Mhz
- The controller frequency is not restricted
- No external phy required

Devices tested and functional :

- Mass storage (~8 Mbps on ArtyA7 linux)
- Keyboard / Mouse
- Audio output
- Hub

Limitations :

- Some USB hub (had one so far) do not like having a full speed host with low speed devices attached.
- Some modern devices will not work on USB full speed (ex :  Gbps ethernet adapter)
- Require memory coherency with the CPU (or the cpu need to be able to flush its data cache in the driver)

Deployments :

- https://github.com/SpinalHDL/SaxonSoc/tree/dev-0.3/bsp/digilent/ArtyA7SmpLinux
- https://github.com/SpinalHDL/SaxonSoc/tree/dev-0.3/bsp/radiona/ulx3s/smp

Usage
--------------

.. code-block:: scala

    import spinal.core._
    import spinal.core.sim._
    import spinal.lib.bus.bmb._
    import spinal.lib.bus.bmb.sim._
    import spinal.lib.bus.misc.SizeMapping
    import spinal.lib.com.usb.ohci._
    import spinal.lib.com.usb.phy.UsbHubLsFs.CtrlCc
    import spinal.lib.com.usb.phy._

    class UsbOhciTop(val p : UsbOhciParameter) extends Component {
      val ohci = UsbOhci(p, BmbParameter(
        addressWidth = 12,
        dataWidth = 32,
        sourceWidth = 0,
        contextWidth = 0,
        lengthWidth = 2
      ))

      val phyCd = ClockDomain.external("phyCd", frequency = FixedFrequency(48 MHz))
      val phy = phyCd(UsbLsFsPhy(p.portCount, sim=true))

      val phyCc = CtrlCc(p.portCount, ClockDomain.current, phyCd)
      phyCc.input <> ohci.io.phy
      phyCc.output <> phy.io.ctrl

      // propagate io signals
      val irq = ohci.io.interrupt.toIo
      val ctrl = ohci.io.ctrl.toIo
      val dma = ohci.io.dma.toIo
      val usb = phy.io.usb.toIo
      val management = phy.io.management.toIo
    }

    object UsbHostGen extends App {
      val p = UsbOhciParameter(
        noPowerSwitching = true,
        powerSwitchingMode = true,
        noOverCurrentProtection = true,
        powerOnToPowerGoodTime = 10,
        dataWidth = 64, // DMA data width, up to 128
        portsConfig = List.fill(4)(OhciPortParameter()) // 4 Ports
      )

      SpinalVerilog(new UsbOhciTop(p))
    }
