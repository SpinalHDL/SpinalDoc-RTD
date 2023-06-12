
USB device
============

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


Architecture
--------------

The controller is composed of : 

- A few control registers
- A internal ram used to store the endpoint status, the transfer descriptors and the endpoint 0 SETUP data.

A linked list of descriptors for each endpoint in order to handle of the USB IN/OUT transactions and data.

The endpoint 0 manage the IN/OUT transactions like all the other endpoints but has some additional hardware to manage the SETUP transactions : 

- Its linked list is cleared on each setup transactions
- The data of the SETUP transaction are stored in a fixed location (SETUP_DATA)
- It has a specific interrupt flag for SETUP transactions  

Registers
--------------

Note that all registers and memories of the controller are only accessible in 32 bits word access, bytes access isn't supported.

FRAME (0xFF00)
**********************

+-------------------------+------+-----------+------------------------------------------------------------------+
| Name                    | Type | Bits      | Description                                                      |
+=========================+======+===========+==================================================================+
| usbFrameId              |  RO  | 31-0      | Current usb frame id                                             |
+-------------------------+------+-----------+------------------------------------------------------------------+


ADDRESS (0xFF04)
**********************

+-------------------------+------+-----------+------------------------------------------------------------------+
| Name                    | Type | Bits      | Description                                                      |
+=========================+======+===========+==================================================================+
| address                 |  WO  | 6-0       | The device will only listen at tokens with the specified address |
|                         |      |           | This field is automaticaly cleared on usb reset events           |
+-------------------------+------+-----------+------------------------------------------------------------------+
| enable                  |  WO  | 8         | Enable the USB address filtering if set                          |
+-------------------------+------+-----------+------------------------------------------------------------------+
| trigger                 |  WO  | 9         | Set the enable (see above) on the next EP0 IN tocken completion  |
|                         |      |           | Cleared by the hardware after any EP0 completion                 |
+-------------------------+------+-----------+------------------------------------------------------------------+

The idea here is to keep the whole register cleared until a USB SET_ADDRESS setup packet is received on EP0.
At that moment, you can set the address and the trigger field, then provide the IN zero length descriptor to EP0 to 
finalise the SET_ADDRESS sequance. The controller will then automaticaly turn on the address filtering at the completion of that descriptor.

INTERRUPT (0xFF08)
**********************

All bits of this register can be cleared by writing '1' in them.

+--------------+------+-----------+------------------------------------------------------------------+
| Name         | Type | Bits      | Description                                                      |
+==============+======+===========+==================================================================+
| endpoints    |  RC  | 15-0      | Raised when a enpoint generate a interrupt                       |
+--------------+------+-----------+------------------------------------------------------------------+
| reset        |  RC  | 16        | Raised when a USB reset appeared                                 |
+--------------+------+-----------+------------------------------------------------------------------+
| ep0Setup     |  RC  | 17        | Raised when endpoint 0 receive a setup transaction               |
+--------------+------+-----------+------------------------------------------------------------------+
| suspend      |  RC  | 18        | Raised when a USB suspend appeared                               |
+--------------+------+-----------+------------------------------------------------------------------+
| resume       |  RC  | 19        | Raised when a USB resume appeared                                |
+--------------+------+-----------+------------------------------------------------------------------+
| disconnect   |  RC  | 20        | Raised when a USB disconnect appeared                            |
+--------------+------+-----------+------------------------------------------------------------------+

HALT (0xFF0C)
**********************

This register allow to place a single enpoint in a dormant state in order to ensure atomicity of CPU operations, allowing to do things as read/modify/write on the endpoint registers and descriptors.
The peripheral will return NAK if the given endpoint is addressed by the usb host. 

+-------------------------+------+-----------+------------------------------------------------------------------+
| Name                    | Type | Bits      | Description                                                      |
+=========================+======+===========+==================================================================+
| endpointId              |  WO  | 3-0       | The endpoint you want to put in sleep                            |
+-------------------------+------+-----------+------------------------------------------------------------------+
| enable                  |  WO  | 4         |                                                                  |
+-------------------------+------+-----------+------------------------------------------------------------------+
| effective               |  RO  | 5         | After setting the enable, you need to wait for this bit to be    |
| enable                  |      |           | set by the hardware itself to ensure atomicity                   |
+-------------------------+------+-----------+------------------------------------------------------------------+

CONFIG (0xFF10)
**********************

+-------------------------+------+-----------+------------------------------------------------------------------+
| Name                    | Type | Bits      | Description                                                      |
+=========================+======+===========+==================================================================+
| pullupSet               |  SO  | 0         | Write '1' to enable the USB device pullup on the dp pin          |
+-------------------------+------+-----------+------------------------------------------------------------------+
| pullupClear             |  SO  | 1         |                                                                  |
+-------------------------+------+-----------+------------------------------------------------------------------+
| interruptEnableSet      |  SO  | 2         | Write '1' to let the present and future interrupt happening      |
+-------------------------+------+-----------+------------------------------------------------------------------+
| interruptEnableClear    |  SO  | 3         |                                                                  |
+-------------------------+------+-----------+------------------------------------------------------------------+

INFO (0xFF20)
**********************

+---------------+------+-----------+------------------------------------------------------------------+
| Name          | Type | Bits      | Description                                                      |
+===============+======+===========+==================================================================+
| ramSize       |  RO  | 3-0       | The internal ram will have (1 << this) bytes                     |
+---------------+------+-----------+------------------------------------------------------------------+

ENDPOINTS (0x0000 - 0x003F)
*********************************

The endpoints status are stored at the begining of the internal ram over one 32 bits word each.

+---------------+------+-----------+------------------------------------------------------------------+
| Name          | Type | Bits      | Description                                                      |
+===============+======+===========+==================================================================+
| enable        |  RW  | 0         | If not set, the endpoint will ignore all the trafic              |
+---------------+------+-----------+------------------------------------------------------------------+
| stall         |  RW  | 1         | If set, the endpoint will always return STALL status             |
+---------------+------+-----------+------------------------------------------------------------------+
| nack          |  RW  | 2         | If set, the endpoint will always return NACK status              |
+---------------+------+-----------+------------------------------------------------------------------+
| dataPhase     |  RW  | 3         | Specify the IN/OUT data PID used. '0' => DATA0.                  |
|               |      |           | This field is also updated by the controller.                    |
+---------------+------+-----------+------------------------------------------------------------------+
| head          |  RW  | 15-4      | Specify the current descriptor head (linked list).               |
|               |      |           | 0 => empty list, byte address = this << 4                        |
+---------------+------+-----------+------------------------------------------------------------------+
| isochronous   |  RW  | 16        |                                                                  |
+---------------+------+-----------+------------------------------------------------------------------+
| maxPacketSize |  RW  | 31-22     |                                                                  |
+---------------+------+-----------+------------------------------------------------------------------+

To get a endpoint responsive you need : 

- Set its enable flag to 1

Then the there is a few cases :
- Either you have the stall or nack flag set, and so, the controller will always responde with the corresponding responses 
- Either, for EP0 setup request, the controller will not use descriptors, but will instead write the data into the SETUP_DATA register, and ACK
- Either you have a empty linked list (head==0) in which case it will answer NACK
- Either you have at least one descriptor pointed by head, in which case it will execute it and ACK if all was going smooth

SETUP_DATA (0x0040 - 0x0047)
*********************************

When endpoint 0 receive a SETUP transaction, the data of the transaction will be stored at that place. 

Descriptors 
----------------------------

Descriptors allows to specify how a endpoint need to handle the data phase of IN/OUT transactions.
They are stored in the internal ram, can be linked together via their linked lists and need to be aligned on 16 bytes boundaries

+-------------------+------+-----------+------------------------------------------------------------------+
| Name              | Word | Bits      | Description                                                      |
+===================+======+===========+==================================================================+
| offset            | 0    | 15-0      | Specify the current progress in the transfer (in byte)           |
+-------------------+------+-----------+------------------------------------------------------------------+
| code              | 0    | 19-16     | 0xF => in progress, 0x0 => success                               |
+-------------------+------+-----------+------------------------------------------------------------------+
| next              | 1    | 15-4      | Point the the next descriptor                                    |
|                   |      |           | 0 => nothing, byte address = this << 4                           |
+-------------------+------+-----------+------------------------------------------------------------------+
| length            | 1    | 31-16     | Number of bytes allocated for the data field                     |
+-------------------+------+-----------+------------------------------------------------------------------+
| direction         | 2    | 16        | '0' => OUT, '1' => IN                                            |
+-------------------+------+-----------+------------------------------------------------------------------+
| interrupt         | 2    | 17        | If set, the completion of the descriptor will generate a         |
|                   |      |           | interrupt.                                                       |
+-------------------+------+-----------+------------------------------------------------------------------+
| completionOnFull  | 2    | 18        | Normaly, a descriptor completion only occure when a USB transfer |
|                   |      |           | is smaller than the maxPacketSize. But if this field is set,     |
|                   |      |           | then when the descriptor become full is also a considered        |
|                   |      |           | as a completion event. (offset == length)                        |
+-------------------+------+-----------+------------------------------------------------------------------+
| data1OnCompletion | 2    | 19        | force the endpoint dataPhase to DATA1 on the completion of the   |
|                   |      |           | descriptoo                                                       |
+-------------------+------+-----------+------------------------------------------------------------------+
| data              | ...  | ...       |                                                                  |
+-------------------+------+-----------+------------------------------------------------------------------+

Note, if the controller receive a frame where the IN/OUT does not match the descriptor IN/OUT, the frame will be ignored.

Also, to initialise a descriptor, the CPU should set the code field to 0xF                

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


