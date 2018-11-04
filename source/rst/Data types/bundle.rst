
.. _Bundle:

Bundle
======

Description
^^^^^^^^^^^

The ``Bundle`` is a composite type that defines a group of named signals (of any SpinalHDL basic type)
 under a single name.

The Bundle can be used to model data structures, buses and interfaces.

Declaration
^^^^^^^^^^^

The syntax to declare a bundle is as follows:

.. code-block:: scala

   case class myBundle extends Bundle {
     val bundleItem0 = AnyType
     val bundleItem1 = AnyType
     val bundleItemN = AnyType
   }

For example, an Color Bundle could be :

.. code-block:: scala

   case class Color(channelWidth: Int) extends Bundle {
     val r,g,b = UInt(channelWidth bits)
   }

You can find an APB3 definition example :ref:`there <example_apb3>`

Operators
^^^^^^^^^

The following operators are available for the ``Bundle`` type

Comparison
~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return type
   * - x === y
     - Equality
     - Bool
   * - x =/= y
     - Inequality
     - Bool


.. code-block:: scala

   val color1 = Color(8)
   color1.r := 0 
   color1.g := 0 
   color1.b := 0

   val color2 = Color(8)
   color2.r := 0
   color2.g := 0 
   color2.b := 0


   myBool := color1 === color2

Type cast
~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Operator
     - Description
     - Return
   * - x.asBits
     - Binary cast in Bits
     - Bits(w(x) bits)


.. code-block:: scala

   val color1 = Color(8)
   val myBits := color1.asBits

Elements direction
^^^^^^^^^^^^^^^^^^

When you define an Bundle inside the IO definition of your component, you need to specify its direction.

in/out
~~~~~~

If all elements of your bundle go in the same direction you can use in(MyBundle()) or out(MyBundle()).

For example :

.. code-block:: scala

   val io = new Bundle{
     val input  = in (Color(8))
     val output = out(Color(8))
   }

master/slave
~~~~~~~~~~~~

If your interface obey to an master/slave topology, you can use the ``IMasterSlave`` trait. Then you have to implement the function ``def asMaster(): Unit`` to set the direction of each elements from an master perspective. Then you can use the ``master(MyBundle())`` and ``slave(MyBundle())`` syntax in the IO defintion.

For example :

.. code-block:: scala

   case class HandShake(payloadWidth: Int) extends Bundle with IMasterSlave {
     val valid   = Bool
     val ready   = Bool
     val payload = Bits(payloadWidth bits)

     //You have to implement this asMaster function.
     //This function should set the direction of each signals from an master point of view
     override def asMaster(): Unit = {
       out(valid,payload)
       in(ready)
     }
   }

   val io = new Bundle{
     val input  = slave(HandShake(8))
     val output = master(HandShake(8))
   }
