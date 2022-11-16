
PLL BlackBox and reset controller
=================================

Let's imagine you want to define a ``TopLevel`` component which instantiates a PLL ``BlackBox``\ , and create a new clock domain from it which will be used by your core logic. Let's also imagine that you want to adapt an external asynchronous reset into this core clock domain to a synchronous reset source.

The following imports will be used in code examples on this page:

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/PLL.scala
   :language: scala
   :start-at: import spinal.core._
   :end-at: import spinal.lib._

The PLL BlackBox definition
---------------------------

This is how to define the PLL ``BlackBox``\ :

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/PLL.scala
   :language: scala
   :start-at: case class PLL(
   :end-before: // end case class PLL

This will correspond to the following VHDL component:

.. code-block:: VHDL

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

.. literalinclude:: /../examples/src/main/scala/spinaldoc/examples/simple/PLL.scala
   :language: scala
   :start-at: case class TopLevel(
   :end-before: // end case class TopLevel
