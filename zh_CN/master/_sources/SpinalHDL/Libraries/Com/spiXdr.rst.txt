
SPI XDR
=======

There is a SPI controller which support :

- half/full duplex
- single/dual/quad SPI
- SDR/DDR/.. data rate


You can find its APB3 implementation here : 

https://github.com/SpinalHDL/SpinalHDL/blob/68b6158700fc2440ea7980406f927262c004faca/lib/src/main/scala/spinal/lib/com/spi/xdr/Apb3SpiXdrMasterCtrl.scala#L43

Configuration
-------------

Here is an example.

.. code-block:: scala

      Apb3SpiXdrMasterCtrl(
        SpiXdrMasterCtrl.MemoryMappingParameters(
          SpiXdrMasterCtrl.Parameters(
            dataWidth = 8, // Each transfer will be 8 bits
            timerWidth = 12, // The timer is used to slow down the transmission
            spi = SpiXdrParameter( // Specify the physical SPI interface
              dataWidth = 4, // Number of physical SPI data pins
              ioRate = 1, // Specify the number of transfer that each spi pin can do per clock 1 => SDR, 2 => DDR
              ssWidth = 1 // Number of chip selects
            )
          )
          .addFullDuplex(id = 0) // Add support for regular SPI (MISO / MOSI) using the mode id 0
          .addHalfDuplex( // Add another mode
            id = 1,  // mapped on mode id 1
            rate = 1, // When rate is 1, the clock will do up to one toggle per cycle, divided by the (timer+1)
                      // When rate bigger (ex 2), the controller will ignore the timer, and use the SpiXdrParameter.ioRate
                      // capabilities to emit up to "rate" transition per clock cycle.
            ddr = false, // sdr => 1 bit per SPI clock, DDR => 2 bits per SPI clock
            spiWidth = 4 // Number of physical SPI data pin used for serialization
          ),
          cmdFifoDepth = 32,
          rspFifoDepth = 32,
          xip = null
        )
      )

Software Driver
---------------

See :

https://github.com/SpinalHDL/SaxonSoc/blob/dev-0.3/software/standalone/driver/spi.h
https://github.com/SpinalHDL/SaxonSoc/blob/dev-0.3/software/standalone/spiDemo/src/main.c

