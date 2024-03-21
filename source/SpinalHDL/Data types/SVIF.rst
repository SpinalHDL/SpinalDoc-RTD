.. _SVIF:

SVIF
======

Description
^^^^^^^^^^^

The ``SVIF`` type specifically targets system Verilog designs.This type extends from ``Bundle``.When generating Verilog or VHDL, the behavior of this type is exactly the same as that of ``Bundle``.However, when generating System Verilog and enabling the ``svInterface`` option in SpinalConfig, this type will be generated as an Interface.

This type is still an experimental feature

Declaration
^^^^^^^^^^^

The syntax to declare a SVIF is as follows:

.. code-block:: scala

   case class myBundle extends SVIF {
     val bundleItem0 = AnyType
     val bundleItem1 = AnyType
     val bundleItemN = AnyType
   }

For example, a SVIF holding a color could be defined as:

.. code-block:: scala

   case class Color(channelWidth: Int) extends SVIF {
     val r, g, b = UInt(channelWidth bits)
   }

modport
~~~~~~~

``modport`` can be implemented through add annotations above functions, with the function name serving as the modport name.

.. code-block:: scala

   case class Color(channelWidth: Int) extends SVIF {
     val r, g, b = UInt(channelWidth bits)

     @modport
     def mst = {
        out(r, g, b)
     }

     @modport
     def slv = {
        in(r, g, b)
     }
   }

with ``IMasterSlave``：

.. code-block:: scala

   case class Color(channelWidth: Int) extends SVIF with IMasterSlave {
     val r, g, b = UInt(channelWidth bits)

     override def asMaster = {
        out(r, g, b)
     }

     @modport
     def mst = asMaster

     @modport
     def slv = asSlave
   }

Parameter
~~~~~~~~~

.. code-block:: scala

   case class Color(channelWidth: Int) extends SVIF {
     val width = addGeneric("WIDTH", channelWidth)// or addParameter
     val r, g, b = UInt(channelWidth bits)
     tieGeneric(r, width)// or tieParameter
     tieGeneric(g, width)
     tieGeneric(b, width)
   }

Definition Name
~~~~~~~~~~~~~~~

you can use ``setDefinitionName`` to set the definition name. But remember to use it before any clone of this interface
