
Colors
======

RGB
---

You can use an Rgb bundle to model colors in hardware. This Rgb bundle take as parameter an RgbConfig classes which specify the number of bits for each channels :

.. code-block:: scala

   case class RgbConfig(rWidth : Int,gWidth : Int,bWidth : Int){
     def getWidth = rWidth + gWidth + bWidth
   }

   case class Rgb(c: RgbConfig) extends Bundle{
     val r = UInt(c.rWidth bits)
     val g = UInt(c.gWidth bits)
     val b = UInt(c.bWidth bits)
   }

Those classes could be used as following :

.. code-block:: scala

   val config = RgbConfig(5,6,5)
   val color = Rgb(config)
   color.r := 31
