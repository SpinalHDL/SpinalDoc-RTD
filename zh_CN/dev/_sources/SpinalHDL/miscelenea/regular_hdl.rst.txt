:orphan:

.. _regular_hdl:

Why moving from traditional HDL to something else
=================================================

.. note::
   All the following statements will be about describing hardware. Verification is another tasty topic.
.. note::
   For simplicity, let's assume that SystemVerilog is a recent revision of Verilog.
.. note::
   When reading this, we should not underestimate how much our attachment for our favourite HDL will bias our judgement.
.. note::
   A language feature which isn't supported by EDA tools shouldn't be considerate as a language feature, as it is not usable. Look how VHDL 2008 is weakly supported, then look at the calendar.

VHDL/Verilog/SystemVerilog aren't Hardware Description Languages
----------------------------------------------------------------

Those languages are event driven languages created initially for simulation/documentation purposes. Only in a second time they were used as inputs languages for synthesis tools. Which explain the roots of a lot of following points of this page.

Event driven paradigm doesn't make any sense for RTL
----------------------------------------------------

When you think about it, describing digital hardware (RTL) by using process/always blocks doesn't make any practical senses. Why do we have to worry about a sensitivity list? Why do we have to split our design between processes/always blocks of different natures (combinatorial logic / register without reset / register with async reset) ?

Example of VHDL/SpinalHDL equivalences:

VHDL :

.. code-block:: vhdl

   signal mySignal : std_logic;
   signal myRegister : unsigned(3 downto 0);
   signal myRegisterWithReset : unsigned(3 downto 0);

   process(cond)
   begin
       mySignal <= '0';
       if cond = '1' then
           mySignal <= '1';
       end if;
   end process;

   process(clk)
   begin
       if rising_edge(clk) then
           if cond = '1' then
               myRegister <= myRegister + 1;
           end if;
       end if;
   end process;

   process(clk,reset)
   begin
       if reset = '1' then
           myRegisterWithReset <= 0;
       elsif rising_edge(clk) then
           if cond = '1' then
               myRegisterWithReset <= myRegisterWithReset + 1;
           end if;
       end if;
   end process;

SpinalHDL :

.. code-block:: scala

   val mySignal             = Bool()
   val myRegister           = Reg(UInt(4 bits))
   val myRegisterWithReset  = Reg(UInt(4 bits)) init(0)

   mySignal := False
   when(cond) {
       mySignal            := True
       myRegister          := myRegister + 1
       myRegisterWithReset := myRegisterWithReset + 1
   }

As for everything, you can get used to this event driven semantic, until you taste something better.

Recent revision of VHDL and Verilog aren't usable
-------------------------------------------------

The EDA industry is really slow to implement VHDL 2008 and SystemVerilog synthesis capabilities in their tools. Additionally, when it's done, it appear that only a constraining subset of the language is implemented (not talking about simulation features). It result that using any interesting feature of those language revision isn't safe as


* It will probably make your code incompatible with many EDA tools
* Others company will likely not accept your IP as their flow isn't ready for it.

Let's me tell you that I’m tired to wait on expensive and closed source tools.

Recent revisions of VHDL and Verilog aren't that good.
------------------------------------------------------

See VHDL 2008 parameterized packages and unconstrained records, sure it allow to write better VHDL sources, but from an OOP perspective they made me feel sick (See next topic for an SpinalHDL example).

And still those revisions doesn't change the heart of those HDL issues: They are based on a event driven paradigm which doesn't make sense to describe digital hardware.

VHDL records, Verilog struct are broken (SystemVerilog is good on this, if you can use it)
------------------------------------------------------------------------------------------

You can't use them to define an interface, because you can't define their internal signal directions. Even worst, you can't give them construction parameters! So, define your RGB record/struct once, and hope you never have to use it with bigger/smaller color channels ...

Also a fancy thing with VHDL is the fact that if you want to add an array of something into a component entity, you have to define the type of this array into a package ... which can't be parameterized...

A SpinalHDL APB3 bus definition:

.. code-block:: scala

   //Class which can be instantiated to represent a given APB3 configuration
   case class Apb3Config(
     addressWidth  : Int,
     dataWidth     : Int,
     selWidth      : Int     = 1,
     useSlaveError : Boolean = true
   )

   //Class which can be instantiated to represent a given hardware APB3 bus
   case class Apb3(config: Apb3Config) extends Bundle with IMasterSlave {
     val PADDR      = UInt(config.addressWidth bits)
     val PSEL       = Bits(config.selWidth bits)
     val PENABLE    = Bool()
     val PREADY     = Bool()
     val PWRITE     = Bool()
     val PWDATA     = Bits(config.dataWidth bits)
     val PRDATA     = Bits(config.dataWidth bits)
     val PSLVERROR  = if(config.useSlaveError) Bool() else null  //Optional signal

     //Can be used to setup a given APB3 bus into a master interface of the host component
     override def asMaster(): Unit = {
       out(PADDR,PSEL,PENABLE,PWRITE,PWDATA)
       in(PREADY,PRDATA)
       if(config.useSlaveError) in(PSLVERROR)
     }
   }

Then about the VHDL 2008 partial solution and the SystemVerilog interface/modport, lucky you are if your EDA tools / company flow / company policy allow you to use them.

VHDL and Verilog are so verbose
-------------------------------

Really, with VHDL and Verilog, when it start to be about component instanciation interconnection, the copypast good need to be invocated.

To understand it more deeply, there is an SpinalHDL example which do some peripherals instanciation and add the APB3 decoder required to access them.

.. code-block:: scala

   //Instanciate an AXI4 to APB3 bridge
   val apbBridge = Axi4ToApb3Bridge(
     addressWidth = 20,
     dataWidth    = 32,
     idWidth      = 4
   )

   //Instanciate some APB3 peripherals
   val gpioACtrl = Apb3Gpio(gpioWidth = 32)
   val gpioBCtrl = Apb3Gpio(gpioWidth = 32)
   val timerCtrl = PinsecTimerCtrl()
   val uartCtrl = Apb3UartCtrl(uartCtrlConfig)
   val vgaCtrl = Axi4VgaCtrl(vgaCtrlConfig)

   //Instanciate an APB3 decoder
   //- Drived by the apbBridge
   //- Map each peripherals in a memory region
   val apbDecoder = Apb3Decoder(
     master = apbBridge.io.apb,
     slaves = List(
       gpioACtrl.io.apb -> (0x00000, 4 KiB),
       gpioBCtrl.io.apb -> (0x01000, 4 KiB),
       uartCtrl.io.apb  -> (0x10000, 4 KiB),
       timerCtrl.io.apb -> (0x20000, 4 KiB),
       vgaCtrl.io.apb   -> (0x30000, 4 KiB)
     )
   )

And done, that's all, you don't have to bind each signal one by one when you instantiate a module/component because you can access their interfaces in a object oriented manner.

Also about VHDL/Verilog struct/records, i would just say that they are really dirty tricks, without true parameterization and reusability capabilities, some crutch which try to hide the fact that those languages were poorly designed.

Meta Hardware Description capabilities
--------------------------------------

Ok, this is a big chunk. Basically VHDL/Verilog/SystemVerilog will give you some elaboration tools which aren't directly mapped into hardware as loops / generate statements / macro / function / procedure / task. But that's all.

And even them are really limited. As instance why you can't define process/always/component/module blocks into a task/procedure? It is really a bottleneck for many fancy things. What if you can call a user defined task/procedure on a bus like that: myHandshakeBus.queue(depth=64) ? Wouldn't it be nice and safe to use?

.. code-block:: scala

   //Define the concept of handshake bus
   class Stream[T <: Data](dataType:  T) extends Bundle {
     val valid   = Bool()
     val ready   = Bool()
     val payload = cloneOf(dataType)

     //Define an operator to connect the left operand (this) to the right operand (that)
     def >>(that: Stream[T]): Unit = {
       this.valid := that.valid
       that.ready := this.ready
       this.payload := that.payload
     }

     //Return a Stream connected to this via a FIFO of depth elements
     def queue(depth: Int): Stream[T] = {
       val fifo = new StreamFifo(dataType, depth)
       this >> fifo.io.push
       return fifo.io.pop
     }
   }

Then let's see further, imagine you want define a state machine, you will have to write raw VHDL/Verilog with some switch statements to do it. You can't define kind of "StateMachine" abstraction which would give you a fancy syntax to define them, instead you will have to use a third party tool to draw your state machine and then generate your VHDL/Verilog equivalent code. Which is really messy anyway.

So by meta-hardware description capabilities, i mean the fact that by using raw SpinalHDL syntax, you can define tools which then allow you to define things in abstracts ways, as for state-machine.

There is an simple example of the usage of a state-machine abstraction defined on the top of SpinalHDL :

.. code-block:: scala

   //Define a new state machine
   val fsm = new StateMachine{
     //Define all states
     val stateA, stateB, stateC = new State

     //Set the state machine entry point
     setEntry(stateA)

     //Define a register used into the state machine
     val counter = Reg(UInt(8 bits)) init (0)

     //Define the state machine behavioural
     stateA.whenIsActive (goto(stateB))

     stateB.onEntry(counter := 0)
     stateB.onExit(io.result := True)
     stateB.whenIsActive {
       counter := counter + 1
       when(counter === 4){
         goto(stateC)
       }
     }

     stateC.whenIsActive (goto(stateA))
   }

Also imagine you want to generate the instruction decoding of your CPU, it could require some fancy elaboration time algorithms to generate the less logic possible. But in VHDL/Verilog/SystemVerilog, your only option to do this kind of things is to write a script which generates the .vhd .v that you want.

There is really much to say about meta-hardware-description, but the only true way to understand it and get its realy taste is to experiment it. The goal with it is stopping playing with wires and gates as monkeys, starting taking some distance with that low level stuff, thinking big and reusable.
