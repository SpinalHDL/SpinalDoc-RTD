
Analog and inout
================

Introduction
------------

You can define native tristates signals by using the Analog/inout features. Those features were added for the following reasons :


* Being able to add native inout to the toplevel (it avoid having to manualy wrap them with some hand written VHDL/Verilog)
* Allowing the definition of blackbox which contain some inout pins
* Being able to connect a blackbox inout through the hierarchy to a toplevel inout pin.

As those feature were only added for convenance, do not do other fancy stuff with it and if you want to model a component like an memory mapped GPIO peripheral, please use the TriState/TriStateArray bundles from the spinal lib, which keep the true nature of the tristate driver.

Analog
------

``Analog`` is the keyword which allow to define a signal as something ... analog, which in the digital world could mean '0', '1', 'Z'.

For instance :

.. code-block:: scala

   case class SdramInterface(g : SdramLayout) extends Bundle{
     val DQ    = Analog(Bits(g.dataWidth bits)) //Bidirectional data bus
     val DQM   = Bits(g.bytePerWord bits)
     val ADDR  = Bits(g.chipAddressWidth bits)
     val BA    = Bits(g.bankWidth bits)
     val CKE, CSn, CASn, RASn, WEn  = Bool
   }

inout
-----

``inout`` is the keyword which allow to set an Analog signal as an component inout.

For instance :

.. code-block:: scala

   case class SdramInterface(g : SdramLayout) extends Bundle with IMasterSlave{
     val DQ    = Analog(Bits(g.dataWidth bits)) //Bidirectional data bus
     val DQM   = Bits(g.bytePerWord bits)
     val ADDR  = Bits(g.chipAddressWidth bits)
     val BA    = Bits(g.bankWidth bits)
     val CKE, CSn, CASn, RASn, WEn  = Bool

     override def asMaster() : Unit = {
       out(ADDR,BA,CASn,CKE,CSn,DQM,RASn,WEn)
       inout(DQ) //Set the Analog DQ as an inout of the component
     }
   }

InOutWrapper
------------

``InOutWrapper`` is a tool which allow you to tranform all master TriState/TriStateArray/ReadableOpenDrain bundles of a component into native inout(Analog(...)) signals. It allow you to keep all your hardware description without any Analog/inout things, and then transform the toplevel to make it synthesis ready.

For instance :

.. code-block:: scala

   case class Apb3Gpio(gpioWidth : Int) extends Component{
     val io = new Bundle{
       val gpio = master(TriStateArray(gpioWidth bits))
       val apb  = slave(Apb3(Apb3Gpio.getApb3Config()))
     }
     ...
   }

   SpinalVhdl(InOutWrapper(Apb3Gpio(32)))

Will generate :

.. code-block:: vhdl

   entity Apb3Gpio is
     port(
       io_gpio : inout std_logic_vector(31 downto 0); -- This io_gpio was originaly a TriStateArray Bundle
       io_apb_PADDR : in unsigned(3 downto 0);
       io_apb_PSEL : in std_logic_vector(0 downto 0);
       io_apb_PENABLE : in std_logic;
       io_apb_PREADY : out std_logic;
       io_apb_PWRITE : in std_logic;
       io_apb_PWDATA : in std_logic_vector(31 downto 0);
       io_apb_PRDATA : out std_logic_vector(31 downto 0);
       io_apb_PSLVERROR : out std_logic;
       clk : in std_logic;
       reset : in std_logic
     );
   end Apb3Gpio;

Instead of :

.. code-block:: vhdl

   entity Apb3Gpio is
     port(
       io_gpio_read : in std_logic_vector(31 downto 0);
       io_gpio_write : out std_logic_vector(31 downto 0);
       io_gpio_writeEnable : out std_logic_vector(31 downto 0);
       io_apb_PADDR : in unsigned(3 downto 0);
       io_apb_PSEL : in std_logic_vector(0 downto 0);
       io_apb_PENABLE : in std_logic;
       io_apb_PREADY : out std_logic;
       io_apb_PWRITE : in std_logic;
       io_apb_PWDATA : in std_logic_vector(31 downto 0);
       io_apb_PRDATA : out std_logic_vector(31 downto 0);
       io_apb_PSLVERROR : out std_logic;
       clk : in std_logic;
       reset : in std_logic
     );
   end Apb3Gpio;
