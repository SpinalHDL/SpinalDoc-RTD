
USB device
============

Introduction
------------

There is a USB device controller in the SpinalHDL library. In a few bullet points it can be resumed to :

- Implemented to allow a CPU to configure and manage the endpoints
- A internal ram which store the endpoints states and transactions descriptors
- Up to 16 endpoints (for virtualy no price)
- Support USB host full speed (12Mbps)
- Test on linux using its own driver (https://github.com/SpinalHDL/linux/blob/dev/drivers/usb/gadget/udc/spinal_udc.c)
- Bmb memory interace for the configuration
- Require a clock for the internal phy which is a multiple of 12 Mhz at least 48 Mhz
- The controller frequency is not restricted
- No external phy required

Linux gadget tested and functional :

- Serial connection
- Ethernet connection
- Mass storage (~8 Mbps on ArtyA7 linux)

Deployments :

- https://github.com/SpinalHDL/SaxonSoc/tree/dev-0.3/bsp/digilent/ArtyA7SmpLinux
- https://github.com/SpinalHDL/SaxonSoc/tree/dev-0.3/bsp/radiona/ulx3s/smp

Usage
--------------

.. code-block:: scala

    import spinal.core._
    import spinal.core.sim._
    import spinal.lib.bus.bmb.BmbParameter
    import spinal.lib.com.usb.phy.UsbDevicePhyNative
    import spinal.lib.com.usb.sim.UsbLsFsPhyAbstractIoAgent
    import spinal.lib.com.usb.udc.{UsbDeviceCtrl, UsbDeviceCtrlParameter}


    case class UsbDeviceTop() extends Component {
      val ctrlCd = ClockDomain.external("ctrlCd", frequency = FixedFrequency(100 MHz))
      val phyCd = ClockDomain.external("phyCd", frequency = FixedFrequency(48 MHz))

      val ctrl = ctrlCd on new UsbDeviceCtrl(
        p = UsbDeviceCtrlParameter(
          addressWidth = 14
        ),
        bmbParameter = BmbParameter(
          addressWidth = UsbDeviceCtrl.ctrlAddressWidth,
          dataWidth = 32,
          sourceWidth = 0,
          contextWidth = 0,
          lengthWidth = 2
        )
      )

      val phy = phyCd on new UsbDevicePhyNative(sim = true)
      ctrl.io.phy.cc(ctrlCd, phyCd) <> phy.io.ctrl

      val bmb = ctrl.io.ctrl.toIo()
      val usb = phy.io.usb.toIo()
      val power = phy.io.power.toIo()
      val pullup = phy.io.pullup.toIo()
      val interrupts = ctrl.io.interrupt.toIo()
    }


    object UsbDeviceGen extends App{
      SpinalVerilog(new UsbDeviceTop())
    }


