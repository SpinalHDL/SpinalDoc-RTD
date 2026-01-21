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

     @modport
     def mst = out(r, g, b)

     @modport
     def slv = in(r, g, b)
   }

.. code-block:: scala

   case class ColorHandShake(Width: Int) extends SVIF with IMasterSlave {
     val w = addGeneric("W", Width, default = "8")
     val valid = Bool()
     val payload = Color(Width)
     val ready = Bool()
     tieIFParameter(payload, "WIDTH", "W") // for generate "  .WIDTH (W)"

     override def asMaster = {
       out(valid, payload)
       in(ready)
     }

     @modport
     def mst = asMaster

     @modport
     def slv = asSlave
   }

this will generate system verilog code as below:

.. code-block:: scala

   interface ColorHandShake #(
      parameter W = 8
   ) () ;

      logic           valid ;
      Color #(
         .WIDTH (W)
      ) payload();
      logic           ready ;

      modport mst (
         output          valid,
         Color.slv       payload,
         input           ready
      );

      modport slv (
         input           valid,
         Color.mst       payload,
         output          ready
      );

   endinterface

   interface Color #(
      parameter WIDTH
   ) () ;

      logic  [WIDTH-1:0] r ;
      logic  [WIDTH-1:0] g ;
      logic  [WIDTH-1:0] b ;

      modport mst (
         input           r,
         input           g,
         input           b
      );

      modport slv (
         output          r,
         output          g,
         output          b
      );

   endinterface

Definition Name
~~~~~~~~~~~~~~~

you can use ``setDefinitionName`` to set the definition name. But remember to use it before any clone of this interface

Not Interface
~~~~~~~~~~~~~

If you have used a certain interface in multiple places, and at one of those locations ``sigA``, you wish to flatten it instead of generating an interface, you can achieve this by calling ``sigA.notSVIF()`` to fully flatten the signal. If the signal has nested interfaces and you only want to expand the outermost layer, you can use ``sigA.notSVIFthisLevel()``.
