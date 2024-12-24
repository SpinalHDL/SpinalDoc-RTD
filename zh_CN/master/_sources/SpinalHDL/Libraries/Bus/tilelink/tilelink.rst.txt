
Tilelink
========

Configuration and instantiation
-------------------------------

There is a short example to define two non coherent tilelink bus instance and connect them:

.. code-block:: scala

    import spinal.lib.bus.tilelink
    val param = tilelink.BusParameter.simple(
      addressWidth = 32,
      dataWidth    = 64,
      sizeBytes    = 64,
      sourceWidth  = 4
    )
    val busA, busB = tilelink.Bus(param)
    busA << busB

Here is the same as above, but with coherency channels

.. code-block:: scala

    import spinal.lib.bus.tilelink
    val param = tilelink.BusParameter(
      addressWidth = 32,
      dataWidth = 64,
      sizeBytes = 64,
      sourceWidth = 4,
      sinkWidth = 0,
      withBCE = false,
      withDataA = true,
      withDataB = false,
      withDataC = false,
      withDataD = true,
      node = null
    )
    val busA, busB = tilelink.Bus(param)
    busA << busB

Those above where for the hardware instantiation, the thing is that it is the simple / easy part. When things goes into SoC / memory coherency, you kind of need an additional layer to negotiate / propagate parameters all around. 
That's what tilelink.fabric.Node is about.



