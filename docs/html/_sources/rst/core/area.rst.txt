.. role:: raw-html-m2r(raw)
   :format: html

Area
====

Area
----

Sometime, creating a ``Component`` to define some logic is overkill :


* Need to define all construction parameters and IO (verbosity, duplication)
* Split your code (more than needed)

For this kind of cases you can use ``Area`` to define a group of signals/logic.

.. code-block:: scala

   class UartCtrl extends Component {
     ...
     val timer = new Area {
       val counter = Reg(UInt(8 bit))
       val tick = counter === 0
       counter := counter - 1
       when(tick) {
         counter := 100
       }
     }

     val tickCounter = new Area {
       val value = Reg(UInt(3 bit))
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

{% include tip.html content="Abuse of area !\ :raw-html-m2r:`<br>` No more toto_a, toto_b, toto_c as it so often done in common HDL, any ``Component``\ 's internal module could be an ``Area``\ " %}

{% include note.html content="\ `ClockingArea </SpinalDoc/spinal/core/clock_domain/>`_ are a special kind of ``Area`` which allow to define chunk of hardware which use a given ``ClockDomain``\ " %}
