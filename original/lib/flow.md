---
layout: page
title: Flow bus
description: "This pages describes the lib components of Spinal"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/lib/flow/
---

## Specification
The Flow interface is a simple valid/payload protocol which mean the slave can't halt the bus.<br>
It could be used, for example, to represent data coming from an UART controller, requests to write an on-chip memory, etc.

| Signal | Type | Driver| Description | Don't care when
| ------- | ---- | --- |  --- |  --- |
| valid | Bool | Master | When high => payload present on the interface  | - |
| payload | T | Master | Content of the transaction | valid is low |

## Functions

| Syntax | Description| Return | Latency |
| ------- | ---- | --- |  --- |
| Flow(type : Data) | Create a Flow of a given type | Flow[T] | |
| master/slave Flow(type : Data) | Create a Flow of a given type <br> Initialized with corresponding in/out setup | Flow[T] |
| x.m2sPipe() | Return a Flow drived by x <br>through a register stage that cut valid/payload paths | Flow[T] |  1 |
| x << y <br> y >> x | Connect y to x | | 0 |
| x <-< y <br> y >-> x | Connect y to x through a m2sPipe  |   | 1 |
| x.throwWhen(cond : Bool) | Return a Flow connected to x <br> When cond is high, transaction are dropped | Flow[T] | 0 |
| x.toReg() | Return a register which is loaded with `payload` when valid is high | T | |
