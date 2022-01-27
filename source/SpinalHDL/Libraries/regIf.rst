 
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

.. code::  

   class cpInterruptExample extends Component {
      val io = new Bundle {
        val tx_done, rx_done, frame_end = in Bool()
        val interrupt = out Bool()
        val apb = slave(Apb3(Apb3Config(16, 32)))
      }
      val busif = Apb3BusInterface(io.apb, (0x000, 100 Byte))
      val M_CP_INT_EN    = busif.newReg(doc="cp int enable register")
      val tx_int_en      = M_CP_INT_EN.field(1 bits, RW, doc="tx interrupt enable register")
      val rx_int_en      = M_CP_INT_EN.field(1 bits, RW, doc="rx interrupt enable register")
      val frame_int_en   = M_CP_INT_EN.field(1 bits, RW, doc="frame interrupt enable register")
      val M_CP_INT_MASK  = busif.newReg(doc="cp int mask register")
      val tx_int_mask      = M_CP_INT_MASK.field(1 bits, RW, doc="tx interrupt mask register")
      val rx_int_mask      = M_CP_INT_MASK.field(1 bits, RW, doc="rx interrupt mask register")
      val frame_int_mask   = M_CP_INT_MASK.field(1 bits, RW, doc="frame interrupt mask register")
      val M_CP_INT_STATE   = busif.newReg(doc="cp int state register")
      val tx_int_state      = M_CP_INT_STATE.field(1 bits, RW, doc="tx interrupt state register")
      val rx_int_state      = M_CP_INT_STATE.field(1 bits, RW, doc="rx interrupt state register")
      val frame_int_state   = M_CP_INT_STATE.field(1 bits, RW, doc="frame interrupt state register")

      when(io.rx_done && rx_int_en(0)){tx_int_state(0).set()}
      when(io.tx_done && tx_int_en(0)){tx_int_state(0).set()}
      when(io.frame_end && frame_int_en(0)){tx_int_state(0).set()}

      io.interrupt := (tx_int_mask(0) && tx_int_state(0)  ||
        rx_int_mask(0) && rx_int_state(0) ||
        frame_int_mask(0) && frame_int_state(0))

   }

this is a very tedious and repetitive work, a better way is to use the "factory" paradigm to auto-generate the documentation for each signal.

now th InterruptFactory can do that.
    
Easy Way creat interruption:

.. code::  
    
    class EasyInterrupt extends Component {
      val io = new Bundle{
        val apb = slave(Apb3(Apb3Config(16,32)))
        val a, b, c, d, e = in Bool()
      }

      val busif = BusInterface(io.apb,(0x000,1 KiB), 0, regPre = "AP")

      InterruptFactory(busif, "T", io.a, io.b, io.c, io.d, io.e)

      busif.accept(HtmlGenerator("interrupt.html", "AP"))
    }

.. image:: /asset/image/regif/easy-intr.png

Developers Area
===============

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
       
 
