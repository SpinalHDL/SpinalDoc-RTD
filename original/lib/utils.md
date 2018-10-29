---
layout: page
title: Utils
description: "This pages describes the lib components of Spinal"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/lib/utils/
---

Some utils are also present in [spinal.core](/SpinalDoc/spinal/core/utils/)

## State less utilities

| Syntax | Return | Description |
| ------------------------------- | ---- | --- |
| toGray(x : UInt) | Bits | Return the gray value converted from `x` (UInt) |
| fromGray(x : Bits) | UInt | Return the UInt value converted value from `x` (gray) |
| Reverse(x : T) | T | Flip all bits (lsb + n -> msb - n) |
| OHToUInt(x : Seq[Bool]) <br> OHToUInt(x : BitVector) | UInt | Return the index of the single bit set (one hot) in `x` |
| CountOne(x : Seq[Bool]) <br> CountOne(x : BitVector) | UInt | Return the number of bit set in `x` |
| MajorityVote(x : Seq[Bool]) <br> MajorityVote(x : BitVector) | Bool | Return True if the number of bit set is > x.size / 2 |
| EndiannessSwap(that: T[, base:BitCount]) | T | Big-Endian <-> Little-Endian |
| OHMasking.first(x : Bits) | Bits | Apply a mask on x to only keep the first bit set |
| OHMasking.last(x : Bits) | Bits | Apply a mask on x to only keep the last bit set |
| OHMasking.roundRobin(<br>&nbsp;&nbsp;requests : Bits,<br>&nbsp;&nbsp;ohPriority : Bits<br>) | Bits | Apply a mask on x to only keep the bit set from `requests`.<br> it start looking in `requests` from the `ohPriority` position <br>.<br>For example if `requests` is "1001" and `ohPriority` is "0010", the `roundRobin function will start looking in `requests` from its second bit and will return "1000". |

## State full utilities

| Syntax | Return | Description |
| ------------------------------- | ---- | --- |
| Delay(that: T, cycleCount: Int) | T | Return `that` delayed by `cycleCount` cycles |
| History(that: T, length: Int[,when : Bool]) | List[T] | Return a Vec of `length` elements <br> The first element is `that`, the last one is `that` delayed by `length`-1<br> The internal shift register sample when `when` is asserted |
| BufferCC(input : T) | T | Return the input signal synchronized with the current clock domain by using 2 flip flop |


### Counter

The Counter tool can be used to easly instanciate an hardware counter.

| Instanciation syntax | Notes |
| ------------------------------- | ---- |
| Counter(start: BigInt, end: BigInt[, inc : Bool]) | - |
| Counter(range : Ranget[, inc : Bool]) | Compatible with the  `x to y` `x until y` syntaxes|
| Counter(stateCount: BigInt[, inc : Bool]) | Start at zero and finish at `stateCount - 1`|
| Counter(bitCount: BitCount[, inc : Bool]) | Start at zero and finish at `(1 << bitCount) - 1`|


There is an example of different syntaxes which could be used with the Counter tool

```scala
val counter = Counter(2 to 9)  //Create a counter of 10 states (2 to 9)
counter.clear()            //When called it ask to reset the counter.
counter.increment()        //When called it ask to increment the counter.
counter.value              //current value
counter.valueNext          //Next value
counter.willOverflow       //Flag that indicate if the counter overflow this cycle
counter.willOverflowIfInc  //Flag that indicate if the counter overflow this cycle if an increment is done
when(counter === 5){ ... }
```

When a `Counter` overflow its end value, it restart to its start value.

{% include note.html content="Currently, only up counter are supported." %}


### Timeout
The Timeout tool can be used to easly instanciate an hardware timeout.

| Instanciation syntax | Notes |
| ------------------------------- | ---- |
| Timeout(cycles : BigInt) | Tick after `cycles` clocks |
| Timeout(time : TimeNumber) | Tick after a `time` duration |
| Timeout(frequency : HertzNumber) |  Tick at an `frequency` rate |

There is an example of different syntaxes which could be used with the Counter tool

```scala
val timeout = Timeout(10 ms)  //Timeout who tick after 10 ms
when(timeout){                //Check if the timeout has tick
    timeout.clear()           //Ask the timeout to clear its flag
}
```

{% include note.html content="If you instanciate an `Timeout` with an time or frequancy setup, the implicit `ClockDomain` should have an frequency setting." %}

### ResetCtrl
The ResetCtrl provide some utilities to manage resets.

#### asyncAssertSyncDeassert
You can filter an asynchronous reset by using an asynchronously asserted synchronously deaserted logic. To do it you can use the `ResetCtrl.asyncAssertSyncDeassert` function which will return you the filtred value.

| Argument name | Type | Description |
| ------------------------------- | ---- | ---- |
| input           | Bool  | Signal that should be filtered |
| clockDomain     | ClockDomain | ClockDomain which will use the filtred value |
| inputPolarity   | Polarity | HIGH/LOW (default=HIGH)|
| outputPolarity  | Polarity | HIGH/LOW (default=clockDomain.config.resetActiveLevel)|
| bufferDepth     | Int | Number of register stages used to avoid metastability (default=2) |

There is also an `ResetCtrl.asyncAssertSyncDeassertDrive` version of tool which directly assign the `clockDomain` reset with the filtred value.

## Special utilities

| Syntax | Return | Description |
| ------------------------------- | ---- | --- |
| LatencyAnalysis(paths : Node*) | Int | Return the shortest path,in therm of cycle, that travel through all nodes, <br> from the first one to the last one |
