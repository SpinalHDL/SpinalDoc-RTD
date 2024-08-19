=====
RegIf
=====

Register Interface Builder

- Automatic address, fields allocation and conflict detection
- 28 Register Access types (Covering the 25 types defined by the UVM standard)
- Automatic documentation generation

Automatic allocation
====================

Automatic address allocation

.. code:: scala

  class RegBankExample extends Component {
    val io = new Bundle {
      apb = slave(Apb3(Apb3Config(16,32)))
    }
    val busif = Apb3BusInterface(io.apb,(0x0000, 100 Byte))
    val M_REG0  = busif.newReg(doc="REG0")
    val M_REG1  = busif.newReg(doc="REG1")
    val M_REG2  = busif.newReg(doc="REG2")

    val M_REGn  = busif.newRegAt(address=0x40, doc="REGn")
    val M_REGn1 = busif.newReg(doc="REGn1")

    busif.accept(HtmlGenerator("regif", "AP"))
    // busif.accept(CHeaderGenerator("header", "AP"))
    // busif.accept(JsonGenerator("regif"))
    // busif.accept(RalfGenerator("regbank"))
    // busif.accept(SystemRdlGenerator("regif", "AP"))
  }

.. image:: /asset/image/regif/reg-auto-allocate.gif

Automatic filed allocation

.. code:: scala

  val M_REG0  = busif.newReg(doc="REG1")
  val fd0 = M_REG0.field(Bits(2 bit), RW, doc= "fields 0")
  M_REG0.reserved(5 bits)
  val fd1 = M_REG0.field(Bits(3 bit), RW, doc= "fields 0")
  val fd2 = M_REG0.field(Bits(3 bit), RW, doc= "fields 0")
  // auto reserved 2 bits
  val fd3 = M_REG0.fieldAt(pos=16, Bits(4 bit), doc= "fields 3")
  // auto reserved 12 bits

.. image:: /asset/image/regif/field-auto-allocate.gif

conflict detection

.. code:: scala

  val M_REG1  = busif.newReg(doc="REG1")
  val r1fd0 = M_REG1.field(Bits(16 bits), RW, doc="fields 1")
  val r1fd2 = M_REG1.field(Bits(18 bits), RW, doc="fields 1")
    ...
  cause Exception
  val M_REG1  = busif.newReg(doc="REG1")
  val r1fd0 = M_REG1.field(Bits(16 bits), RW, doc="fields 1")
  val r1fd2 = M_REG1.fieldAt(pos=10, Bits(2 bits), RW, doc="fields 1")
    ...
  cause Exception

28 Access Types
===============
  
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
HSRW        w: Hardware Set, SoftWare RW                                                    New
RWHS        w: SoftWare RW, Hardware Set                                                    New
ROV         w: ReadOnly Value, used for hardware version                                    New
CSTM        w: user custom Type, used for document                                          New
==========  =============================================================================   ====

Automatic documentation generation
==================================

Document Type

==========  =========================================================================================   ======
Document    Usage                                                                                       Status
==========  =========================================================================================   ======
HTML        ``busif.accept(HtmlGenerator("regif", title = "XXX register file"))``                         Y
CHeader     ``busif.accept(CHeaderGenerator("header", "AP"))``                                            Y
JSON        ``busif.accept(JsonGenerator("regif"))``                                                      Y
RALF(UVM)   ``busif.accept(RalfGenerator("header"))``                                                     Y
SystemRDL   ``busif.accept(SystemRdlGenerator("regif", "addrmap_name", Some("name"), Some("desc")))``     Y
Latex(pdf)                                                                                                N
docx                                                                                                      N
==========  =========================================================================================   ======

HTML auto-doc is now complete, Example source Code:

.. RegIfExample link: https://github.com/jijingg/SpinalHDL/tree/dev/tester/src/main/scala/spinal/tester/code/RegIfExample.scala
.. Axi4liteRegIfExample link: https://github.com/jijingg/SpinalHDL/tree/dev/tester/src/main/scala/spinal/tester/code/Axi4liteRegIfExample.scala
   
generated HTML document:

.. image:: /asset/image/regif/regif-html.png


Special Access Usage
====================

**CASE1:** ``RO`` usage

``RO`` is different from other types. It does not create registers and requires an external signal to drive it,
Attention, please don't forget to drive it.

.. code:: scala

   val io = new Bundle {
     val cnt = in UInt(8 bit)
   }

   val counter = M_REG0.field(UInt(8 bit), RO, 0, "counter")
   counter :=  io.cnt


.. code:: scala

   val xxstate = M_REG0.field(UInt(8 bit), RO, 0, "xx-ctrl state").asInput

.. code:: scala

   val overflow = M_REG0.field(Bits(32 bit), RO, 0, "xx-ip parameter")
   val ovfreg = Reg(32 bit)
   overflow := ovfreg
   
   
.. code:: scala

   val inc    = in Bool()
   val counter = M_REG0.field(UInt(8 bit), RO, 0, "counter")
   val cnt = Counter(100,  inc)
   counter := cnt

      
**CASE2:** ``ROV`` usage

ASIC design often requires some solidified version information. Unlike RO, it is not expected to generate wire signals

old way:

.. code:: scala
   
   val version = M_REG0.field(Bits(32 bit), RO, 0, "xx-device version")
   version := BigInt("F000A801", 16)
   
new way: 

.. code:: scala
   
   M_REG0.field(Bits(32 bit), ROV, BigInt("F000A801", 16), "xx-device version")(Symbol("Version"))

   

**CASE3:** ``HSRW/RWHS`` hardware set type
In some cases, such registers are not only configured by software, but also set by hardware signals

.. code:: scala

   val io = new Bundle {
     val xxx_set = in Bool()
     val xxx_set_val = in Bits(32 bit)
   }

   val reg0 = M_REG0.fieldHSRW(io.xxx_set, io.xxx_set_val, 0, "xx-device version")  // 0x0000
   val reg1 = M_REG1.fieldRWHS(io.xxx_set, io.xxx_set_val, 0, "xx-device version")  // 0x0004

.. code:: verilog

   always @(posedge clk or negedge rstn)
     if(!rstn) begin
        reg0  <= '0;
        reg0  <= '0;
     end else begin
        if(hit_0x0000) begin
           reg0 <= wdata ;
        end
        if(io.xxx_set) begin      // HW have High priority than SW
           reg0 <= io.xxx_set_val ;
        end

        if(io.xxx_set) begin
           reg1 <= io.xxx_set_val ;
        end 
        if(hit_0x0004) begin      // SW have High priority than HW
           reg1 <= wdata ;
        end
     end

   

**CASE4:** ``CSTM`` Although SpinalHDL includes 25 register types and 6 extension types, 
there are still various demands for private register types in practical application.
Therefore, we reserve CSTM types for scalability. 
CSTM is only used to generate software interfaces, and does not generate actual circuits

.. code:: scala

   val reg = Reg(Bits(16 bit)) init 0
   REG.registerAtOnlyReadLogic(0, reg, CSTM("BMRW"), resetValue = 0, "custom field")

   when(busif.dowrite) {
      reg :=  reg & ~busif.writeData(31 downto 16) |  busif.writeData(15 downto 0) & busif.writeData(31 downto 16)
   }


**CASE5:** ``parasiteField``

This is used for software to share the same register on multiple address instead of generating multiple register entities

example1: clock gate software enable 

.. code:: scala

   val M_CG_ENS_SET = busif.newReg(doc="Clock Gate Enables")  // x00000
   val M_CG_ENS_CLR = busif.newReg(doc="Clock Gate Enables")  // 0x0004
   val M_CG_ENS_RO  = busif.newReg(doc="Clock Gate Enables")  // 0x0008

   val xx_sys_cg_en = M_CG_ENS_SET.field(Bits(4 bit), W1S, 0, "clock gate enables, write 1 set" ) 
                      M_CG_ENS_CLR.parasiteField(xx_sys_cg_en, W1C, 0, "clock gate enables, write 1 clear" ) 
                      M_CG_ENS_RO.parasiteField(xx_sys_cg_en, RO, 0, "clock gate enables, read only"

example2: interrupt raw reg with force interface for software

.. code:: scala

   val RAW    = this.newRegAt(offset,"Interrupt Raw status Register\n set when event \n clear raw when write 1")
   val FORCE  = this.newReg("Interrupt Force  Register\n for SW debug use \n write 1 set raw")

   val raw    = RAW.field(Bool(), AccessType.W1C,    resetValue = 0, doc = s"raw, default 0" )
                FORCE.parasiteField(raw, AccessType.W1S,   resetValue = 0, doc = s"force, write 1 set, debug use" )

**CASE6:** ``SpinalEnum``

When the field type is SpinalEnum, the resetValue specifies the index of the enum elements.

.. code:: scala

   object UartCtrlTxState extends SpinalEnum(defaultEncoding = binaryOneHot) {
      val sIdle, sStart, sData, sParity, sStop = newElement()
   }

   val raw = M_REG2.field(UartCtrlTxState(), AccessType.RW, resetValue = 2, doc="state")
   // raw will be init to sData

Byte Mask
=========

withStrb


Typical Example 
===============

Batch create REG-Address and fields register

.. code:: scala   

  import spinal.lib.bus.regif._

  class RegBank extends Component {
    val io = new Bundle {
      val apb = slave(Apb3(Apb3Config(16, 32)))
      val stats = in Vec(Bits(16 bit), 10)
      val IQ  = out Vec(Bits(16 bit), 10)
    }
    val busif = Apb3BusInterface(io.apb, (0x000, 100 Byte), regPre = "AP")

    (0 to 9).map { i =>
      // here use setName give REG uniq name for Docs usage
      val REG = busif.newReg(doc = s"Register${i}").setName(s"REG${i}")
      val real = REG.field(SInt(8 bit), AccessType.RW, 0, "Complex real")
      val imag = REG.field(SInt(8 bit), AccessType.RW, 0, "Complex imag")
      val stat = REG.field(Bits(16 bit), AccessType.RO, 0, "Accelerator status")
      io.IQ(i)( 7 downto 0) := real.asBits
      io.IQ(i)(15 downto 8) := imag.asBits
      stat := io.stats(i)
    }

    def genDocs() = {
      busif.accept(CHeaderGenerator("regbank", "AP"))
      busif.accept(HtmlGenerator("regbank", "Interupt Example"))
      busif.accept(JsonGenerator("regbank"))
      busif.accept(RalfGenerator("regbank"))
      busif.accept(SystemRdlGenerator("regbank", "AP"))
    }

    this.genDocs()
  }

  SpinalVerilog(new RegBank())


Interrupt Factory 
=================

Manual writing interruption

.. code:: scala   

   class cpInterruptExample extends Component {
      val io = new Bundle {
        val tx_done, rx_done, frame_end = in Bool()
        val interrupt = out Bool()
        val apb = slave(Apb3(Apb3Config(16, 32)))
      }
      val busif = Apb3BusInterface(io.apb, (0x000, 100 Byte), regPre = "AP")
      val M_CP_INT_RAW   = busif.newReg(doc="cp int raw register")
      val tx_int_raw      = M_CP_INT_RAW.field(Bool(), W1C, doc="tx interrupt enable register")
      val rx_int_raw      = M_CP_INT_RAW.field(Bool(), W1C, doc="rx interrupt enable register")
      val frame_int_raw   = M_CP_INT_RAW.field(Bool(), W1C, doc="frame interrupt enable register")

      val M_CP_INT_FORCE = busif.newReg(doc="cp int force register\n for debug use")
      val tx_int_force     = M_CP_INT_FORCE.field(Bool(), RW, doc="tx interrupt enable register")
      val rx_int_force     = M_CP_INT_FORCE.field(Bool(), RW, doc="rx interrupt enable register")
      val frame_int_force  = M_CP_INT_FORCE.field(Bool(), RW, doc="frame interrupt enable register")

      val M_CP_INT_MASK    = busif.newReg(doc="cp int mask register")
      val tx_int_mask      = M_CP_INT_MASK.field(Bool(), RW, doc="tx interrupt mask register")
      val rx_int_mask      = M_CP_INT_MASK.field(Bool(), RW, doc="rx interrupt mask register")
      val frame_int_mask   = M_CP_INT_MASK.field(Bool(), RW, doc="frame interrupt mask register")

      val M_CP_INT_STATUS   = busif.newReg(doc="cp int state register")
      val tx_int_status      = M_CP_INT_STATUS.field(Bool(), RO, doc="tx interrupt state register")
      val rx_int_status      = M_CP_INT_STATUS.field(Bool(), RO, doc="rx interrupt state register")
      val frame_int_status   = M_CP_INT_STATUS.field(Bool(), RO, doc="frame interrupt state register")

      rx_int_raw.setWhen(io.rx_done)
      tx_int_raw.setWhen(io.tx_done)
      frame_int_raw.setWhen(io.frame_end)

      rx_int_status := (rx_int_raw || rx_int_force) && (!rx_int_mask)
      tx_int_status := (tx_int_raw || rx_int_force) && (!rx_int_mask)
      frame_int_status := (frame_int_raw || frame_int_force) && (!frame_int_mask)

      io.interrupt := rx_int_status || tx_int_status || frame_int_status

   }

this is a very tedious and repetitive work, a better way is to use the "factory" paradigm to auto-generate the documentation for each signal.

now the InterruptFactory can do that.
    
Easy Way create interruption:

.. code:: scala   
    
    class EasyInterrupt extends Component {
      val io = new Bundle {
        val apb = slave(Apb3(Apb3Config(16,32)))
        val a, b, c, d, e = in Bool()
      }

      val busif = BusInterface(io.apb,(0x000,1 KiB), 0, regPre = "AP")

      busif.interruptFactory("T", io.a, io.b, io.c, io.d, io.e)

      busif.accept(CHeaderGenerator("intrreg","AP"))
      busif.accept(HtmlGenerator("intrreg", "Interupt Example"))
      busif.accept(JsonGenerator("intrreg"))
      busif.accept(RalfGenerator("intrreg"))
      busif.accept(SystemRdlGenerator("intrreg", "AP"))
    }

.. image:: /asset/image/regif/easy-intr.png

IP level interrupt Factory
--------------------------

========== ==========  ======================================================================
Register   AccessType  Description                                                           
========== ==========  ======================================================================
RAW        W1C         int raw register, set by int event, clear when bus write 1  
FORCE      RW          int force register, for SW debug use 
MASK       RW          int mask register, 1: off; 0: open; default 1 int off 
STATUS     RO          int status, Read Only, ``status = raw && ! mask``                 
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
MASK       RW          int mask register, 1: off; 0: open; default 1 int off 
STATUS     RO          int status, RO, ``status = int_level && ! mask``                 
========== ==========  ======================================================================

.. image:: /asset/image/intc/MS.svg

SpinalUsage:

.. code:: scala 

    busif.interruptLevelFactory("T", sys_int0, sys_int1)
 
Spinal Factory
--------------
                                                                                                                                                 
============================================================================================= ===================================================================
BusInterface method                                                                           Description                                                        
============================================================================================= ===================================================================
``InterruptFactory(regNamePre: String, triggers: Bool*)``                                     create RAW/FORCE/MASK/STATUS for pulse event      
``InterruptFactoryNoForce(regNamePre: String, triggers: Bool*)``                              create RAW/MASK/STATUS for pulse event      
``InterruptLevelFactory(regNamePre: String, triggers: Bool*)``                                create MASK/STATUS for level_int merge       
``InterruptFactoryAt(addrOffset: Int, regNamePre: String, triggers: Bool*)``                  create RAW/FORCE/MASK/STATUS for pulse event at addrOffset 
``InterruptFactoryNoForceAt(addrOffset: Int, regNamePre: String, triggers: Bool*)``           create RAW/MASK/STATUS for pulse event at addrOffset     
``InterruptFactoryAt(addrOffset: Int, regNamePre: String, triggers: Bool*)``                  create MASK/STATUS for level_int merge at addrOffset      
``interrupt_W1SCmask_FactoryAt(addrOffset: BigInt, regNamePre: String, triggers: Bool*)``     create RAW/FORCE/MASK(SET/CLR)/STATUS for pulse event at addrOffset
``interruptLevel_W1SCmask_FactoryAt(addrOffset: BigInt, regNamePre: String, levels: Bool*)``  create RAW/FORCE/MASK(SET/CLR)/STATUS for level event at addrOffset
============================================================================================= ===================================================================
                               
Example
-------

.. code:: scala 

   class RegFileIntrExample extends Component {
      val io = new Bundle {
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
        busif.accept(CHeaderGenerator("intrreg","Intr"))
        busif.accept(HtmlGenerator("intrreg", "Interrupt Example"))
        busif.accept(JsonGenerator("intrreg"))
        busif.accept(RalfGenerator("intrreg"))
        busif.accept(SystemRdlGenerator("intrreg", "Intr"))
        this
      }

      this.genDoc()
    }

.. image:: /asset/image/intc/intc.jpeg

DefaultReadValue
================

When the software reads a reserved address, the current policy is to return normally, readerror=0.
In order to facilitate software debugging, the read back value can be configured, which is 0 by default

.. code:: scala 

   busif.setReservedAddressReadValue(0x0000EF00)


.. code:: verilog

   default: begin
      busif_rdata  <= 32'h0000EF00 ;
      busif_rderr  <= 1'b0         ;
   end

 

Developers Area
===============

You can add your document Type by extending the `BusIfVisitor` Trait 

``case class Latex(fileName : String) extends BusIfVisitor{ ... }``

BusIfVisitor give access BusIf.RegInsts to do what you want 

.. code:: scala

    // lib/src/main/scala/spinal/lib/bus/regif/BusIfBase.scala

    trait BusIfVisitor {
      def begin(busDataWidth : Int) : Unit
      def visit(descr : FifoDescr)  : Unit  
      def visit(descr : RegDescr)   : Unit
      def end()                     : Unit
    }
       
 

