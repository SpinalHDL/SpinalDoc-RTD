
.. _section-analog_and_inout:

Analog and inout
================

Introduction
------------

You can define native tristate signals by using the ``Analog``/``inout`` features.
These features were added for the following reasons:

* Being able to add native tristate signals to the toplevel (it avoids having to manually wrap them with some hand-written VHDL/Verilog).
* Allowing the definition of blackboxes which contain ``inout`` pins.
* Being able to connect a blackbox's ``inout`` pin through the hierarchy to a toplevel ``inout`` pin.

As those features were only added for convenience, please do not try other fancy stuff with tristate logic just yet.

If you want to model a component like a memory-mapped GPIO peripheral, please use the :ref:`TriState/TriStateArray <section-tristate>` bundles from the Spinal standard library, which abstract over the true nature of tristate drivers.

Analog
------

``Analog`` is the keyword which allows a signal to be defined as something analog, which in the digital world could mean ``0``, ``1``, or ``Z`` (the disconnected, high-impedance state).

For instance:

.. code-block:: scala

   case class SdramInterface(g : SdramLayout) extends Bundle {
     val DQ    = Analog(Bits(g.dataWidth bits)) // Bidirectional data bus
     val DQM   = Bits(g.bytePerWord bits)
     val ADDR  = Bits(g.chipAddressWidth bits)
     val BA    = Bits(g.bankWidth bits)
     val CKE, CSn, CASn, RASn, WEn  = Bool()
   }

inout
-----

``inout`` is the keyword which allows you to set an ``Analog`` signal as a bidirectional (both "in" and "out") signal.

For instance:

.. code-block:: scala

   case class SdramInterface(g : SdramLayout) extends Bundle with IMasterSlave {
     val DQ    = Analog(Bits(g.dataWidth bits)) // Bidirectional data bus
     val DQM   = Bits(g.bytePerWord bits)
     val ADDR  = Bits(g.chipAddressWidth bits)
     val BA    = Bits(g.bankWidth bits)
     val CKE, CSn, CASn, RASn, WEn  = Bool()

     override def asMaster() : Unit = {
       out(ADDR, BA, CASn, CKE, CSn, DQM, RASn, WEn)
       inout(DQ) // Set the Analog DQ as an inout signal of the component
     }
   }

InOutWrapper
------------

``InOutWrapper`` is a tool which allows you to transform all ``master`` ``TriState``/``TriStateArray``/``ReadableOpenDrain`` bundles of a component into native ``inout(Analog(...))`` signals.
It allows you to keep your hardware description free of any ``Analog``/``inout`` things, and then transform the toplevel to make it synthesis ready.

For instance:

.. code-block:: scala

   case class Apb3Gpio(gpioWidth : Int) extends Component {
     val io = new Bundle {
       val gpio = master(TriStateArray(gpioWidth bits))
       val apb  = slave(Apb3(Apb3Gpio.getApb3Config()))
     }
     ...
   }

   SpinalVhdl(InOutWrapper(Apb3Gpio(32)))

Will generate:

.. code-block:: vhdl

   entity Apb3Gpio is
     port(
       io_gpio : inout std_logic_vector(31 downto 0); -- This io_gpio was originally a TriStateArray Bundle
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

Instead of:

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

Manually driving Analog bundles
-------------------------------

If an ``Analog`` bundle is not driven, it will default to being high-Z.
Therefore to manually implement a tristate driver (in case the ``InOutWrapper`` type can't be used for some reason) you have to conditionally drive the signal.

To manually connect a ``TriState`` signal to an ``Analog`` bundle:

.. code-block:: scala

    case class Example extends Component {
      val io = new Bundle {
        val tri = slave(TriState(Bits(16 bits)))
        val analog = inout(Analog(Bits(16 bits)))
      }
      io.tri.read := io.analog
      when(io.tri.writeEnable) { io.analog := io.tri.write }
    }
