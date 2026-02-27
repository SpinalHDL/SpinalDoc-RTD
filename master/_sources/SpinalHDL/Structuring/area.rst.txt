Area
====

Sometimes, creating a ``Component`` to define some logic is overkill because you:

* Need to define all construction parameters and IO (verbosity, duplication)
* Split your code (more than needed)

For this kind of case you can use an ``Area`` to define a group of signals/logic:

.. code-block:: scala

   class UartCtrl extends Component {
     ...
     val timer = new Area {
       val counter = Reg(UInt(8 bits))
       val tick = counter === 0
       counter := counter - 1
       when(tick) {
         counter := 100
       }
     }

     val tickCounter = new Area {
       val value = Reg(UInt(3 bits))
       val reset = False
       when(timer.tick) {          // Refer to the tick from timer area
         value := value + 1
       }
       when(reset) {
         value := 0
       }
     }

     val stateMachine = new Area {
       ...
     }
   }

.. tip::
   | In VHDL and Verilog, sometimes prefixes are used to separate variables into logical sections. It is suggested that you use ``Area`` instead  of this in SpinalHDL.

.. note::
   \ :ref:`ClockingArea <clock_domain>` is a special kind of ``Area`` that allows you to define chunks of hardware which use a given ``ClockDomain``\


AreaObject
==========

This is a special kind of Area which is used to provide names to non-hardware thing. This is for instance used to provide names to pipelining keys : 

.. code-block:: scala

    object Fetch extends AreaObject {
      val PC = Payload(UInt(64 bits))// PC.getName() will give Fetch_PC
    }

