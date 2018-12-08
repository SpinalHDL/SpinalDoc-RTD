---
layout: page
title: Stream bus
description: "This pages describes the lib components of Spinal"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/lib/stream/
---

## Specification
The Stream interface is a simple handshake protocol to carry payload.<br>
It could be used for example to push and pop elements into a FIFO, send requests to a UART controller, etc.

| Signal  | Type | Driver| Description | Don't care when
| ------- | ---- | --- |  --- |  --- |
| valid | Bool | Master | When high => payload present on the interface  | - |
| ready| Bool | Slave | When low => transaction are not consumed by the slave | valid is low |
| payload| T | Master | Content of the transaction | valid is low |

There is an example diagram :<br>
<img src="{{ "/images/stream.png" |  prepend: site.baseurl }}" alt="Company logo"/>

There is some examples of usage in SpinalHDL :

```scala
class StreamFifo[T <: Data](dataType: T, depth: Int) extends Component {
  val io = new Bundle {
    val push = slave Stream (dataType)
    val pop = master Stream (dataType)
  }
  ...
}

class StreamArbiter[T <: Data](dataType: T,portCount: Int) extends Component {
  val io = new Bundle {
    val inputs = Vec(slave Stream (dataType),portCount)
    val output = master Stream (dataType)
  }
  ...
}
```

{% include note.html content="Each slave can or can't allow the payload to change when valid is high and ready is low. For examples:<br>
- An priority arbiter without lock logic can switch from one input to the other (which will change the payload).<br>
- An UART controller could directly use the write port to drive UART pins and only consume the transaction at the end of the transmission. <br>
 Be careful with that." %}

## Functions

| Syntax | Description| Return | Latency |
| ------- | ---- | --- |  --- |
| Stream(type : Data) | Create a Stream of a given type | Stream[T] | |
| master/slave Stream(type : Data) | Create a Stream of a given type <br> Initialized with corresponding in/out setup | Stream[T] |
| x.fire | Return True when a transaction is consumed on the bus (valid && ready) | Bool | - |
| x.isStall | Return True when a transaction is stall on the bus (valid && ! ready) | Bool | - |
| x.queue(size:Int) | Return a Stream connected to x through a FIFO | Stream[T] | 2 |
| x.m2sPipe()<br> x.stage() | Return a Stream drived by x <br>through a register stage that cut valid/payload paths <br> Cost = (payload width + 1) flop flop | Stream[T] |  1 |
| x.s2mPipe() | Return a Stream drived by x <br> ready paths is cut by a register stage <br> Cost = payload width * (mux2 + 1 flip flop) | Stream[T] |  0 |
| x.halfPipe() | Return a Stream drived by x <br> valid/ready/payload paths are cut by some register <br> Cost = (payload width + 2) flip flop, bandwidth divided by two | Stream[T] |  1 |
| x << y <br> y >> x | Connect y to x | | 0 |
| x <-< y <br> y >-> x | Connect y to x through a m2sPipe  |   | 1 |
| x <&#47;< y <br> y >&#47;> x | Connect y to x through a s2mPipe|   | 0 |
| x <-/< y <br> y >&#47;-> x | Connect y to x through s2mPipe().m2sPipe() <br> Which imply no combinatorial path between x and y |  | 1 |
| x.haltWhen(cond : Bool) | Return a Stream connected to x <br> Halted when cond is true | Stream[T] | 0 |
| x.throwWhen(cond : Bool) | Return a Stream connected to x <br> When cond is true, transaction are dropped | Stream[T] | 0 |

The following code will create this logic :
<img src="https://cdn.jsdelivr.net/gh/SpinalHDL/SpinalDoc/asset/picture/stream_throw_m2spipe.svg"   align="middle" width="300">

```scala
case class RGB(channelWidth : Int) extends Bundle{
  val red   = UInt(channelWidth bit)
  val green = UInt(channelWidth bit)
  val blue  = UInt(channelWidth bit)

  def isBlack : Bool = red === 0 && green === 0 && blue === 0
}

val source = Stream(RGB(8))
val sink   = Stream(RGB(8))
sink <-< source.throwWhen(source.payload.isBlack)
```

## Utils

There is many utils that you can use in your design in conjunction with the Stream bus, This chapter will document them.

### StreamFifo

On each stream you can call the .queue(size) to get a buffered stream. But you can also instantiate the FIFO component itself :

```scala
val streamA,streamB = Stream(Bits(8 bits))
//...
val myFifo = StreamFifo(
  dataType = Bits(8 bits),
  depth    = 128
)
myFifo.io.push << streamA
myFifo.io.pop  >> streamB
```

| parameter name | Type | Description|
| ------- | ---- |  ---- |
| dataType | T | Payload data type |
| depth | Int | Size of the memory used to store elements |

| io name | Type | Description|
| ------- | ---- |  ---- |
| push | Stream[T] | Used to push elements |
| pop | Stream[T] | Used to pop elements |
| flush | Bool | Used to remove all elements inside the FIFO |
| occupancy | UInt of log2Up(depth + 1) bits | Indicate the internal memory occupancy |

### StreamFifoCC

You can instanciate the dual clock domain version of the fifo by the following way :

```scala
val clockA = ClockDomain(???)
val clockB = ClockDomain(???)
val streamA,streamB = Stream(Bits(8 bits))
//...
val myFifo = StreamFifoCC(
  dataType  = Bits(8 bits),
  depth     = 128,
  pushClock = clockA,
  popClock  = clockB
)
myFifo.io.push << streamA
myFifo.io.pop  >> streamB
```

| parameter name | Type | Description|
| ------- | ---- |  ---- |
| dataType | T | Payload data type |
| depth | Int | Size of the memory used to store elements |
| pushClock | ClockDomain | Clock domain used by the push side |
| popClock | ClockDomain |  Clock domain used by the pop side |

| io name | Type | Description|
| ------- | ---- |  ---- |
| push | Stream[T] | Used to push elements |
| pop | Stream[T] | Used to pop elements |
| pushOccupancy | UInt of log2Up(depth + 1) bits | Indicate the internal memory occupancy (from the push side perspective) |
| popOccupancy | UInt of log2Up(depth + 1) bits | Indicate the internal memory occupancy  (from the pop side perspective) |

### StreamCCByToggle

Component that provide a Stream cross clock domain bridge based on toggling signals.<br>
This way of doing cross clock domain bridge is characterized by a small area usage but also a low bandwidth.

```scala
val clockA = ClockDomain(???)
val clockB = ClockDomain(???)
val streamA,streamB = Stream(Bits(8 bits))
//...
val bridge = StreamCCByToggle(
  dataType    = Bits(8 bits),
  inputClock  = clockA,
  outputClock = clockB
)
bridge.io.input  << streamA
bridge.io.output >> streamB
```

| parameter name | Type | Description|
| ------- | ---- |  ---- |
| dataType | T | Payload data type |
| inputClock | ClockDomain | Clock domain used by the push side |
| outputClock | ClockDomain |  Clock domain used by the pop side |

| io name | Type | Description|
| ------- | ---- |  ---- |
| input | Stream[T] | Used to push elements |
| output | Stream[T] | Used to pop elements |

But you can also use a this shorter syntax which directly return you the cross clocked stream:

```scala
val clockA = ClockDomain(???)
val clockB = ClockDomain(???)
val streamA = Stream(Bits(8 bits))
val streamB = StreamCCByToggle(
  input       = streamA,
  inputClock  = clockA,
  outputClock = clockB
)
```

### StreamArbiter

When you have multiple Streams and you want to arbitrate them to drive a single one, you can use the StreamArbiterFactory.

```scala
val streamA, streamB, streamC = Stream(Bits(8 bits))
val arbitredABC = StreamArbiterFactory.roundRobin.onArgs(streamA, streamB, streamC)

val streamD, streamE, streamF = Stream(Bits(8 bits))
val arbitredDEF = StreamArbiterFactory.lowerFirst.noLock.onArgs(streamD, streamE, streamF)
```


| Arbitration functions | Description|
| ------- | ---- |
| lowerFirst | Lower port have priority over higher port |
| roundRobin | Fair round robin arbitration |
| sequentialOrder | Could be used to retrieve transaction in a sequancial order <br> First transaction should come from port zero, then from port one, ... |

| Lock functions  | Description|
| ------- | ---- |
| noLock | The port selection could change every cycle, even if the transaction on the selected port is not consumed. |
| transactionLock | The port selection is locked until the transaction on the selected port is consumed. |
| fragmentLock | Could be used to arbitrate Stream[Flow[T]].<br> In this mode, the port selection is locked until the selected port finish is burst (last=True). |

| Generation functions | Return |
| ------- | ---- |  ---- |
| on(inputs : Seq[Stream[T]]) | Stream[T] |
| onArgs(inputs : Stream[T]*) | Stream[T] |


### StreamFork

This utile take its input stream and duplicate it outputCount times.

```scala
val inputStream = Stream(Bits(8 bits))
val dispatchedStreams = StreamDispatcherSequencial(
  input = inputStream,
  outputCount = 3
)
```


### StreamDispatcherSequencial

This utile take its input stream and route it to `outputCount` stream in a sequential order.

```scala
val inputStream = Stream(Bits(8 bits))
val dispatchedStreams = StreamDispatcherSequencial(
  input = inputStream,
  outputCount = 3
)
```
