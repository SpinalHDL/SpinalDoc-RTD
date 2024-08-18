.. _blackbox:

Instantiate VHDL and Verilog IP
===============================

Description
-----------

A blackbox allows the user to integrate an existing VHDL/Verilog component into the design by just specifying its
interfaces. It's up to the simulator or synthesizer to do the elaboration correctly.

Defining an blackbox
--------------------

An example of how to define a blackbox is shown below:

.. code-block:: scala

   // Define a Ram as a BlackBox
   class Ram_1w_1r(wordWidth: Int, wordCount: Int) extends BlackBox {
     // Add VHDL Generics / Verilog parameters to the blackbox
     // You can use String, Int, Double, Boolean, and all SpinalHDL base
     // types as generic values
     addGeneric("wordCount", wordCount)
     addGeneric("wordWidth", wordWidth)

     // Define IO of the VHDL entity / Verilog module
     val io = new Bundle {
       val clk = in Bool()
       val wr = new Bundle {
         val en   = in Bool()
         val addr = in UInt (log2Up(wordCount) bits)
         val data = in Bits (wordWidth bits)
       }
       val rd = new Bundle {
         val en   = in Bool()
         val addr = in UInt (log2Up(wordCount) bits)
         val data = out Bits (wordWidth bits)
       }
     }

     // Map the current clock domain to the io.clk pin
     mapClockDomain(clock=io.clk)
   }

| In VHDL, signals of type ``Bool`` will be translated into ``std_logic`` and ``Bits`` into ``std_logic_vector``. If you want to get ``std_ulogic``, you have to use a ``BlackBoxULogic`` instead of ``BlackBox``.
| In Verilog, ``BlackBoxUlogic`` does not change the generated verilog.

.. code-block:: scala

   class Ram_1w_1r(wordWidth: Int, wordCount: Int) extends BlackBoxULogic {
     ...
   }

Generics
--------

There are two different ways to declare generics:

.. code-block:: scala

   class Ram(wordWidth: Int, wordCount: Int) extends BlackBox {
       addGeneric("wordCount", wordCount)
       addGeneric("wordWidth", wordWidth)

       // OR 

       val generic = new Generic {
         val wordCount = Ram.this.wordCount
         val wordWidth = Ram.this.wordWidth
       }
   }

Instantiating a blackbox
------------------------

Instantiating a ``BlackBox`` is just like instantiating a ``Component``:

.. code-block:: scala

   // Create the top level and instantiate the Ram
   class TopLevel extends Component {
     val io = new Bundle {    
       val wr = new Bundle {
         val en   = in Bool()
         val addr = in UInt (log2Up(16) bits)
         val data = in Bits (8 bits)
       }
       val rd = new Bundle {
         val en   = in Bool()
         val addr = in UInt (log2Up(16) bits)
         val data = out Bits (8 bits)
       }
     }

     // Instantiate the blackbox
     val ram = new Ram_1w_1r(8,16)

     // Connect all the signals
     io.wr.en   <> ram.io.wr.en
     io.wr.addr <> ram.io.wr.addr
     io.wr.data <> ram.io.wr.data
     io.rd.en   <> ram.io.rd.en
     io.rd.addr <> ram.io.rd.addr
     io.rd.data <> ram.io.rd.data
   }

   object Main {
     def main(args: Array[String]): Unit = {
       SpinalVhdl(new TopLevel)
     }
   }

Clock and reset mapping
-----------------------

In your blackbox definition you have to explicitly define clock and reset wires. To map signals of a ``ClockDomain`` to corresponding inputs of the blackbox you can use the ``mapClockDomain`` or ``mapCurrentClockDomain`` function. ``mapClockDomain`` has the following parameters:

.. list-table::
   :header-rows: 1
   :widths: 1 1 1 5

   * - name
     - type
     - default
     - description
   * - clockDomain
     - ClockDomain
     - ClockDomain.current
     - Specify the clockDomain which provides the signals
   * - clock
     - Bool
     - Nothing
     - Blackbox input which should be connected to the clockDomain clock
   * - reset
     - Bool
     - Nothing
     - Blackbox input which should be connected to the clockDomain reset
   * - enable
     - Bool
     - Nothing
     - Blackbox input which should be connected to the clockDomain enable


``mapCurrentClockDomain`` has almost the same parameters as ``mapClockDomain`` but without the clockDomain.

For example:

.. code-block:: scala

   class MyRam(clkDomain: ClockDomain) extends BlackBox {

     val io = new Bundle {
       val clkA = in Bool()
       ...
       val clkB = in Bool()
       ...
     }

     // Clock A is map on a specific clock Domain 
     mapClockDomain(clkDomain, io.clkA)
     // Clock B is map on the current clock domain 
     mapCurrentClockDomain(io.clkB)
   }

By default the ports of the blackbox are considered clock-less, meaning no clock crossing checks will be made on their usage. You can specify ports clock domain by using the ClockDomainTag : 

.. code-block:: scala

    class DemoBlackbox extends BlackBox {
      val io = new Bundle {
        val clk, rst = in Bool()
        val a = in Bool()
        val b = out Bool()
      }
      mapCurrentClockDomain(io.clk, io.rst)
      ClockDomainTag(this.clockDomain)(
        io.a,
        io.b
      )  
    }

You can also apply the tag to the whole bundle with : 

.. code-block:: scala

      val io = new Bundle {
        val clk, rst = in Bool()
        val a = in Bool()
        val b = out Bool()
      }
      ClockDomainTag(this.clockDomain)(io)

You can also apply the current clock domain to all the ports using (SpinalHDL 1.10.2): 

.. code-block:: scala

      val io = new Bundle {
        val clk, rst = in Bool()
        val a = in Bool()
        val b = out Bool()
      }
      setIoCd()

io prefix
---------

In order to avoid the prefix "io\_" on each of the IOs of the blackbox, you can use the function ``noIoPrefix()`` as shown below :

.. code-block:: scala

   // Define the Ram as a BlackBox
   class Ram_1w_1r(wordWidth: Int, wordCount: Int) extends BlackBox {

     val generic = new Generic {
       val wordCount = Ram_1w_1r.this.wordCount
       val wordWidth = Ram_1w_1r.this.wordWidth
     }

     val io = new Bundle {
       val clk = in Bool()

       val wr = new Bundle {
         val en   = in Bool()
         val addr = in UInt (log2Up(_wordCount) bits)
         val data = in Bits (_wordWidth bits)
       }
       val rd = new Bundle {
         val en   = in Bool()
         val addr = in UInt (log2Up(_wordCount) bits)
         val data = out Bits (_wordWidth bits)
       }
     }

     noIoPrefix()

     mapCurrentClockDomain(clock=io.clk)
   }

Rename all io of a blackbox
---------------------------

IOs of a ``BlackBox`` or ``Component`` can be renamed at compile-time using the ``addPrePopTask`` function.
This function takes a no-argument function to be applied during compilation, and is useful for adding renaming passes, as shown in the following example:

.. code-block:: scala

   class MyRam() extends Blackbox {

     val io = new Bundle {
       val clk = in Bool()
       val portA = new Bundle {
         val cs   = in Bool()
         val rwn  = in Bool()
         val dIn  = in Bits(32 bits)
         val dOut = out Bits(32 bits)
       }
       val portB = new Bundle {
         val cs   = in Bool()
         val rwn  = in Bool()
         val dIn  = in Bits(32 bits)
         val dOut = out Bits(32 bits)
       }
     }

     // Map the clk 
     mapCurrentClockDomain(io.clk)

     // Remove io_ prefix 
     noIoPrefix() 

     // Function used to rename all signals of the blackbox 
     private def renameIO(): Unit = {
       io.flatten.foreach(bt => {
         if(bt.getName().contains("portA")) bt.setName(bt.getName().replace("portA_", "") + "_A") 
         if(bt.getName().contains("portB")) bt.setName(bt.getName().replace("portB_", "") + "_B") 
       })
     }

     // Execute the function renameIO after the creation of the component 
     addPrePopTask(() => renameIO())
   }

   // This code generate these names:
   //    clk 
   //    cs_A, rwn_A, dIn_A, dOut_A
   //    cs_B, rwn_B, dIn_B, dOut_B

Add RTL source
--------------

With the function ``addRTLPath()`` you can associate your RTL sources with the blackbox. After the generation of your SpinalHDL code you can call the function ``mergeRTLSource`` to merge all of the sources together.

.. code-block:: scala

   class MyBlackBox() extends Blackbox {

     val io = new Bundle {
       val clk   = in  Bool()
       val start = in Bool()
       val dIn   = in  Bits(32 bits)
       val dOut  = out Bits(32 bits)    
       val ready = out Bool()
     }

     // Map the clk 
     mapCurrentClockDomain(io.clk)

     // Remove io_ prefix 
     noIoPrefix() 

     // Add all rtl dependencies
     addRTLPath("./rtl/RegisterBank.v")                         // Add a verilog file 
     addRTLPath(s"./rtl/myDesign.vhd")                          // Add a vhdl file 
     addRTLPath(s"${sys.env("MY_PROJECT")}/myTopLevel.vhd")     // Use an environment variable MY_PROJECT (System.getenv("MY_PROJECT"))
   }

   ...

   class TopLevel() extends Component {
     // ...
     val bb = new MyBlackBox()
     // ...
   }

   val report = SpinalVhdl(new TopLevel)
   report.mergeRTLSource("mergeRTL") // Merge all rtl sources into mergeRTL.vhd and mergeRTL.v files

VHDL - No numeric type
----------------------

If you want to use only ``std_logic_vector`` in your blackbox component, you can add the tag ``noNumericType`` to the blackbox.

.. code-block:: scala

   class MyBlackBox() extends BlackBox {
     val io = new Bundle {
       val clk       = in  Bool()
       val increment = in  Bool()
       val initValue = in  UInt(8 bits)
       val counter   = out UInt(8 bits)
     }

     mapCurrentClockDomain(io.clk)

     noIoPrefix()

     addTag(noNumericType)  // Only std_logic_vector
   }

The code above will generate the following VHDL:

.. code-block:: vhdl

   component MyBlackBox is
     port( 
       clk       : in  std_logic;
       increment : in  std_logic;
       initValue : in  std_logic_vector(7 downto 0);
       counter   : out std_logic_vector(7 downto 0)    
     );
   end component;
