
PLL BlackBox and reset controller
=================================

Let's imagine you want to define a ``TopLevel`` component which instantiates a PLL ``BlackBox``\ , and create a new clock domain from it which will be used by your core logic. Let's also imagine that you want to adapt an external asynchronous reset into this core clock domain to a synchronous reset source.

The following imports will be used in code examples on this page:

.. code-block:: scala

   import spinal.core._
   import spinal.lib._

The PLL BlackBox definition
---------------------------

This is how to define the PLL ``BlackBox``\ :

.. code-block:: scala

   class PLL extends BlackBox{
     val io = new Bundle{
       val clkIn    = in Bool
       val clkOut   = out Bool
       val isLocked = out Bool
     }

     noIoPrefix()
   }

This will correspond to the following VHDL component:

.. code-block:: text

   component PLL is
     port(
       clkIn    : in std_logic;
       clkOut   : out std_logic;
       isLocked : out std_logic
     );
   end component;

TopLevel definition
-------------------

This is how to define your ``TopLevel`` which instantiates the PLL, creates the new ``ClockDomain``\ , and also adapts the asynchronous reset input to a synchronous reset:

.. code-block:: scala

   class TopLevel extends Component{
     val io = new Bundle {
       val aReset    = in Bool
       val clk100Mhz = in Bool
       val result    = out UInt(4 bits)
     }

     // Create an Area to manage all clocks and reset things
     val clkCtrl = new Area {
       //Instanciate and drive the PLL
       val pll = new PLL
       pll.io.clkIn := io.clk100Mhz

       //Create a new clock domain named 'core'
       val coreClockDomain = ClockDomain.internal(
         name = "core",
         frequency = FixedFrequency(200 MHz)  // This frequency specification can be used
       )                                      // by coreClockDomain users to do some calculations

       //Drive clock and reset signals of the coreClockDomain previously created
       coreClockDomain.clock := pll.io.clkOut
       coreClockDomain.reset := ResetCtrl.asyncAssertSyncDeassert(
         input = io.aReset || ! pll.io.isLocked,
         clockDomain = coreClockDomain
       )
     }

     //Create a ClockingArea which will be under the effect of the clkCtrl.coreClockDomain
     val core = new ClockingArea(clkCtrl.coreClockDomain){
       //Do your stuff which use coreClockDomain here
       val counter = Reg(UInt(4 bits)) init(0)
       counter := counter + 1
       io.result := counter
     }
   }
