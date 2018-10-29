---
type: homepage
title: "SpinalSim UART encoder example"
tags: [user guide]
keywords: spinal, user guide
last_updated: Apr 19, 2016
sidebar: spinal_sidebar
toc: true
permalink: /spinal/sim/example/uart_encoder/
---


```scala
  //Fork a simulation process which will get char typed into the simulation terminal and transmit them on the simulation uartPin
  fork{
    uartPin #= true
    while(true) {
      //System.in is the java equivalent of the C stdin
      if(System.in.available() != 0){
        val buffer = System.in.read()
        uartPin #= false
        sleep(baudPeriod)

        (0 to 7).suspendable.foreach{ bitId =>
          uartPin #= ((buffer >> bitId) & 1) != 0
          sleep(baudPeriod)
        }

        uartPin #= true
        sleep(baudPeriod)
      } else {
        sleep(baudPeriod * 10) //Sleep a little while to avoid pulling System.in to often
      }
    }
  }
```
