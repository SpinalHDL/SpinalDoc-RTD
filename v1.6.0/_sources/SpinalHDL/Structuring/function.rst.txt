.. role:: raw-html-m2r(raw)
   :format: html

.. _function:

Function
========

Introduction
------------

The ways you can use Scala functions to generate hardware are radically different than VHDL/Verilog for many reasons:

* You can instantiate registers, combinational logic, and components inside them.
* You don't have to play with ``process``\ /\ ``@always`` blocks that limit the scope of assignment of signals.
* | Everything is passed by reference, which allows easy manipulation.
  | For example, you can give a bus to a function as an argument, then the function can internally read/write to it. You can also return a Component, a Bus, or anything else from Scala and the Scala world.

RGB to gray
-----------

For example, if you want to convert a Red/Green/Blue color into greyscale by using coefficients, you can use functions to apply them:

.. code-block:: scala

   // Input RGB color
   val r, g, b = UInt(8 bits)

   // Define a function to multiply a UInt by a Scala Float value.
   def coef(value: UInt, by: Float): UInt = (value * U((255 * by).toInt, 8 bits) >> 8)

   // Calculate the gray level
   val gray = coef(r, 0.3f) + coef(g, 0.4f) + coef(b, 0.3f)

Valid Ready Payload bus
-----------------------

For instance, if you define a simple bus with ``valid``, ``ready``, and ``payload`` signals, you can then define some useful functions inside of it.

.. code-block:: scala

   case class MyBus(payloadWidth: Int) extends Bundle with IMasterSlave {
     val valid   = Bool()
     val ready   = Bool()
     val payload = Bits(payloadWidth bits)

     // Define the direction of the data in a master mode
     override def asMaster(): Unit = {
       out(valid, payload)
       in(ready)
     }

     // Connect that to this
     def <<(that: MyBus): Unit = {
       this.valid   := that.valid
       that.ready   := this.ready
       this.payload := that.payload
     }

     // Connect this to the FIFO input, return the fifo output
     def queue(size: Int): MyBus = {
       val fifo = new MyBusFifo(payloadWidth, size)
       fifo.io.push << this
       return fifo.io.pop
     }
   }

   class MyBusFifo(payloadWidth: Int, depth: Int) extends Component {

     val io = new Bundle {
       val push = slave(MyBus(payloadWidth))
       val pop  = master(MyBus(payloadWidth))
     }

     val mem = Mem(Bits(payloadWidth bits), depth)

     // ...

   }
