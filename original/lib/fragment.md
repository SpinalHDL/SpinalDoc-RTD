---
layout: page
title: Stream bus
description: "This pages describes the lib components of Spinal"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/lib/fragment/
---

## Specification
The `Fragment` bundle is the concept of transmitting a "big" thing by using multiple "small" fragments. For examples :

- A picture transmitted with width*height transaction on a `Stream[Fragment[Pixel]]`
- An UART packet received from an controller without flow control could be transmitted on a `Flow[Fragment[Bits]]`
- An AXI read burst could be carried by an `Stream[Fragment[AxiReadResponse]]`

Signals defined by the `Fragment` bundle are :

| Signal  | Type  | Driver| Description |
| ------- | ---- | --- | --- |
| fragment | T | Master | The "payload" of the current transaction |
| last | Bool | Master | High when the fragment is the last of the current packet |

As you can see with this specification and precedent example, the `Fragment` concept doesn't specify how transaction are transmitted (You can use Stream,Flow or any other communication protocol). It only add enough information (`last`) to know if the current transaction is the first one, the last one or one in the middle of a given packet.

{% include note.html content="The protocol didn't carry a \'first\' bit because it can be generated at any place by doing \'RegNextWhen(bus.last, bus.fire) init(True)\'" %}

## Functions

For `Stream[Fragment[T]]` and `Flow[Fragment[T]]`, following function are presents :

| Syntax | Return | Description|
| ------- | ---- | --- |
| x.first | Bool | Return True when the next or the current transaction is/would be the first of a packet|
| x.tail | Bool | Return True when the next or the current transaction is/would be not the first of a packet |
| x.isFirst | Bool | Return True when an transaction is present and is the first of a packet |
| x.isTail | Bool |  Return True when an transaction is present and is the not the first/last of a packet |
| x.isLast | Bool |  Return True when an transaction is present and is the last of a packet |

For `Stream[Fragment[T]]`, following function are also accessible :

| Syntax | Return | Description|
| ------- | ---- | --- |
| x.insertHeader(header : T) |  Stream[Fragment[T]] | Add the `header` to each packet on `x` and return the resulting bus |
