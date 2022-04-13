 
RegIf
=====
Register Interface Builder

- Automatic address, fields allocation and conflict detection
- 28 Register Access types(Covering the 25 types defined by the UVM standard)
- Automatic documentation generation

Automatic allocation
--------------------

Automatic address allocation

.. code:: scala

  class RegBankExample extends Component{
    val io = new Bundle{
      apb = Apb3(Apb3Config(16,32))
    }
    val busif = BusInterface(io.apb,(0x0000, 100 Byte)
    val M_REG0  = busif.newReg(doc="REG0")
    val M_REG1  = busif.newReg(doc="REG1")
    val M_REG2  = busif.newReg(doc="REG2")

    val M_REGn  = busif.newRegAt(address=0x40, doc="REGn")
    val M_REGn1 = busif.newReg(doc="REGn1")

    busif.accept(HtmlGenerator("regif.html", "AP"))
    // busif.accept(CHeaderGenerator("header.h", "AP"))
    // busif.accept(JsonGenerator("regif.json"))
  }

.. image:: /asset/image/regif/reg-auto-allocate.gif

Automatic fileds allocation

.. code:: scala

  val M_REG0  = busif.newReg(doc="REG1")
  val fd0 = M_REG0.field(2 bits, RW, doc= "fields 0")
  M_REG0.reserved(5 bits)
  val fd1 = M_REG0.field(3 bits, RW, doc= "fields 0")
  val fd2 = M_REG0.field(3 bits, RW, doc= "fields 0")
  //auto reserved 2 bits
  val fd3 = M_REG0.fieldAt(pos=16, 4 bits, doc= "fields 3")
  //auto reserved 12 bits

.. image:: /asset/image/regif/field-auto-allocate.gif

confilict detection

.. code:: scala

  val M_REG1  = busif.newReg(doc="REG1")
  val r1fd0 = M_REG1.field(16 bits, RW, doc="fields 1")
  val r1fd2 = M_REG1.field(18 bits, RW, doc="fields 1")
    ...
  cause Exception
  val M_REG1  = busif.newReg(doc="REG1")
  val r1fd0 = M_REG1.field(16 bits, RW, doc="fields 1")
  val r1fd2 = M_REG1.field(offset=10, 2 bits, RW, doc="fields 1")
    ...
  cause Exception

28 Access Types
---------------
  
Most of these come from UVM specification

==========  =============================================================================   ====
AccessType  Description                                                                     From
==========  =============================================================================   ====
RO          w: no effect, r: no effect                                                      UVM
RW          w: as-is, r: no effect                                                          UVM
RC          w: no effect, r: clears all bits                                                UVM
RS          w: no effect, r: sets all bits                                                  UVM
WRC         w: as-is, r: clears all bits                                                    UVM
WRS         w: as-is, r: sets all bits                                                      UVM
WC          w: clears all bits, r: no effect                                                UVM
WS          w: sets all bits, r: no effect                                                  UVM
WSRC        w: sets all bits, r: clears all bits                                            UVM
WCRS        w: clears all bits, r: sets all bits                                            UVM
W1C         w: 1/0 clears/no effect on matching bit, r: no effect                           UVM
W1S         w: 1/0 sets/no effect on matching bit, r: no effect                             UVM
W1T         w: 1/0 toggles/no effect on matching bit, r: no effect                          UVM
W0C         w: 1/0 no effect on/clears matching bit, r: no effect                           UVM
W0S         w: 1/0 no effect on/sets matching bit, r: no effect                             UVM
W0T         w: 1/0 no effect on/toggles matching bit, r: no effect                          UVM
W1SRC       w: 1/0 sets/no effect on matching bit, r: clears all bits                       UVM
W1CRS       w: 1/0 clears/no effect on matching bit, r: sets all bits                       UVM
W0SRC       w: 1/0 no effect on/sets matching bit, r: clears all bits                       UVM
W0CRS       w: 1/0 no effect on/clears matching bit, r: sets all bits                       UVM
WO          w: as-is, r: error                                                              UVM                                                        
WOC         w: clears all bits, r: error                                                    UVM
WOS         w: sets all bits, r: error                                                      UVM
W1          w: first one after hard reset is as-is, other w have no effects, r: no effect   UVM
WO1         w: first one after hard reset is as-is, other w have no effects, r: error       UVM
NA          w: reserved, r: reserved                                                        New
W1P         w: 1/0 pulse/no effect on matching bit, r: no effect                            New
W0P         w: 0/1 pulse/no effect on matching bit, r: no effect                            New
==========  =============================================================================   ====

Automatic documentation generation
----------------------------------

Document Type

==========  =============================================================================   ======
Document    Usage                                                                           Status
==========  =============================================================================   ======
JSON        ``busif.accept(JsonGenerator("regif.json"))``                                     Y
HTML        ``busif.accept(HtmlGenerator("regif.html", "AP"))``                               Y
CHeader     ``busif.accept(CHeaderGenerator("header.h", "AP"))``                              Y
RALF(UVM)                                                                                     N
Latex(pdf)                                                                                    N
docx                                                                                          N
==========  =============================================================================   ======

HTML auto-doc is now complete, Example source Code:

.. RegIfExample link: https://github.com/jijingg/SpinalHDL/tree/dev/tester/src/main/scala/spinal/tester/code/RegIfExample.scala
.. Axi4liteRegIfExample link: https://github.com/jijingg/SpinalHDL/tree/dev/tester/src/main/scala/spinal/tester/code/Axi4liteRegIfExample.scala
   
generated HTML document:

.. image:: /asset/image/regif/regif-html.png

Interrupt Factory 
-----------------

Manual writing interruption

.. code:: scala   

   class cpInterruptExample extends Component {
      val io = new Bundle {
        val tx_done, rx_done, frame_end = in Bool()
        val interrupt = out Bool()
        val apb = slave(Apb3(Apb3Config(16, 32)))
      }
      val busif = Apb3BusInterface(io.apb, (0x000, 100 Byte))
      val M_CP_INT_RAW   = busif.newReg(doc="cp int raw register")
      val tx_int_raw      = M_CP_INT_RAW.field(1 bits, W1C, doc="tx interrupt enable register").lsb
      val rx_int_raw      = M_CP_INT_RAW.field(1 bits, W1C, doc="rx interrupt enable register").lsb
      val frame_int_raw   = M_CP_INT_RAW.field(1 bits, W1C, doc="frame interrupt enable register").lsb

      val M_CP_INT_FORCE = busif.newReg(doc="cp int force register\n for debug use")
      val tx_int_force     = M_CP_INT_FORCE.field(1 bits, RW, doc="tx interrupt enable register").lsb
      val rx_int_force     = M_CP_INT_FORCE.field(1 bits, RW, doc="rx interrupt enable register").lsb
      val frame_int_force  = M_CP_INT_FORCE.field(1 bits, RW, doc="frame interrupt enable register").lsb

      val M_CP_INT_MASK    = busif.newReg(doc="cp int mask register")
      val tx_int_mask      = M_CP_INT_MASK.field(1 bits, RW, doc="tx interrupt mask register").lsb
      val rx_int_mask      = M_CP_INT_MASK.field(1 bits, RW, doc="rx interrupt mask register").lsb
      val frame_int_mask   = M_CP_INT_MASK.field(1 bits, RW, doc="frame interrupt mask register").lsb

      val M_CP_INT_STATUS   = busif.newReg(doc="cp int state register")
      val tx_int_status      = M_CP_INT_STATUS.field(1 bits, RO, doc="tx interrupt state register").lsb
      val rx_int_status      = M_CP_INT_STATUS.field(1 bits, RO, doc="rx interrupt state register").lsb
      val frame_int_status   = M_CP_INT_STATUS.field(1 bits, RO, doc="frame interrupt state register").lsb

      rx_int_raw.setwhen(rx_done)
      tx_int_raw.setwhen(tx_done)
      frame_int_raw.setwhen(frame_int_raw)

      io.interrupt := (rx_int_raw || rx_int_force) && (!rx_int_mask)  ||
        (tx_int_raw || rx_int_force) && (!rx_int_mask) ||
        (frame_int_raw || fram_int_force) && (!frame_int_mask)

   }

this is a very tedious and repetitive work, a better way is to use the "factory" paradigm to auto-generate the documentation for each signal.

now th InterruptFactory can do that.
    
Easy Way creat interruption:

.. code:: scala   
    
    class EasyInterrupt extends Component {
      val io = new Bundle{
        val apb = slave(Apb3(Apb3Config(16,32)))
        val a, b, c, d, e = in Bool()
      }

      val busif = BusInterface(io.apb,(0x000,1 KiB), 0, regPre = "AP")

      busif.interruptFactory("T", io.a, io.b, io.c, io.d, io.e)

      busif.accept(CHeaderGenerator("intrreg.h","AP"))
      busif.accept(HtmlGenerator("intrreg.html", "Interupt Example"))
      busif.accept(JsonGenerator("intrreg.json"))
    }

.. image:: /asset/image/regif/easy-intr.png

Interrupt Design Spec
=====================

IP level interrupt Factory
--------------------------

========== ==========  ======================================================================
Register   AccessType  Description                                                           
========== ==========  ======================================================================
RAW        W1C         int raw register, set by int event, clear when bus write 1  
FORCE      RW          int force register, for SW debug use 
MASK       RW          int mask register, 1: off; 0: open; defualt 1 int off 
STATUS     RO          int status, Read Only, ``status = (raw || force) && ! mask``                 
========== ==========  ======================================================================
 

.. image:: /asset/image/intc/RFMS.svg

SpinalUsage:

.. code:: scala 

    busif.interruptFactory("T", io.a, io.b, io.c, io.d, io.e)

SYS level interrupt merge
-------------------------

========== ==========  ======================================================================
Register   AccessType  Description                                                           
========== ==========  ======================================================================
MASK       RW          int mask register, 1: off; 0: open; defualt 1 int off 
STATUS     RO          int status, RO, ``status = int_level && ! mask``                 
========== ==========  ======================================================================

.. image:: /asset/image/intc/MS.svg

SpinalUsage:

.. code:: scala 

    busif.interruptLevelFactory("T", sys_int0, sys_int1)
 
Spinal Factory
--------------
                                                                                                                                                 
=================================================================================== ===========================================================
BusInterface method                                                                 Description                                                        
=================================================================================== ===========================================================
``InterruptFactory(regNamePre: String, triggers: Bool*)``                            creat RAW/FORCE/MASK/STATUS for pulse event      
``InterruptFactoryNoForce(regNamePre: String, triggers: Bool*)``                     creat RAW/MASK/STATUS for pulse event      
``InterruptFactory(regNamePre: String, triggers: Bool*)``                            creat MASK/STATUS for level_int merge       
``InterruptFactoryAt(addrOffset: Int, regNamePre: String, triggers: Bool*)``         creat RAW/FORCE/MASK/STATUS for pulse event at addrOffset 
``InterruptFactoryNoForceAt(addrOffset: Int, regNamePre: String, triggers: Bool*)``  creat RAW/MASK/STATUS for pulse event at addrOffset     
``InterruptFactoryAt(addrOffset: Int, regNamePre: String, triggers: Bool*)``         creat MASK/STATUS for level_int merge at addrOffset      
=================================================================================== ===========================================================
                               
Example
-------

.. code:: scala 

   class RegFileIntrExample extends Component{
      val io = new Bundle{
        val apb = slave(Apb3(Apb3Config(16,32)))
        val int_pulse0, int_pulse1, int_pulse2, int_pulse3 = in Bool()
        val int_level0, int_level1, int_level2 = in Bool()
        val sys_int = out Bool()
        val gpio_int = out Bool()
      }

      val busif = BusInterface(io.apb,  (0x000,1 KiB), 0, regPre = "AP")
      io.sys_int  := busif.interruptFactory("SYS",io.int_pulse0, io.int_pulse1, io.int_pulse2, io.int_pulse3)
      io.gpio_int := busif.interruptLevelFactory("GPIO",io.int_level0, io.int_level1, io.int_level2, io.sys_int)

      def genDoc() = {
        busif.accept(CHeaderGenerator("intrreg.h","Intr"))
        busif.accept(HtmlGenerator("intrreg.html", "Interupt Example"))
        busif.accept(JsonGenerator("intrreg.json"))
        this
      }

      this.genDoc()
    }

.. image:: /asset/image/intc/intc.jpeg

Developers Area
---------------

You can add your document Type by extending the `BusIfVistor` Trait 

``case class Latex(fileName : String) extends BusIfVisitor{ ... }``

BusIfVistor give access BusIf.RegInsts to do what you want 

.. code:: scala

    // lib/src/main/scala/lib/bus/regif/BusIfVistor.scala 

    trait  BusIfVisitor {
      def begin(busDataWidth : Int) : Unit
      def visit(descr : FifoDescr)  : Unit  
      def visit(descr : RegDescr)   : Unit
      def end()                     : Unit
    }
       
 
