---
layout: page
title: Wishbone
description: "This pages describes the wishbone specification"
tags: [components, intro]
categories: [intro]
sidebar: spinal_sidebar
permalink: /spinal/lib/bus/wishbone/specification/
---

{% include wavedrom.html %}

## Introduction
The wishbone bus is an open specification

## Wishbone transactions


### Classic
<div style="float:left">
<script type="WaveDrom">
{signal: [
  {name:'CLK',         wave: 'p...' },
  {name:'WE',          wave: 'x1.x' },
  {name:'CYC',         wave: '01.0' },
  {name:'STB',         wave: '01.0' },
  {name:'ACK',         wave: '0.10' },
  {name:'ADR',         wave: 'x2.x', data: 'addr'},
  {name:'DAT_MOSI',    wave: 'x2x.', data: 'data'},
  {name:'DAT_MISO',    wave: 'x...' },
],
 head:{
   text:'',
   tick:0,
 },
 foot:{
   text:'Classic Write',
   }
}
</script>
</div>

<script type="WaveDrom">
{signal: [
  {name:'CLK',         wave: 'p...' },
  {name:'WE',          wave: 'x0.x' },
  {name:'CYC',         wave: '01.0' },
  {name:'STB',         wave: '01.0' },
  {name:'ACK',         wave: '0.10' },
  {name:'ADR',         wave: 'x2.x', data: 'addr'},
  {name:'DAT_MOSI',    wave: 'x...' },
  {name:'DAT_MISO',    wave: 'x.2x', data: 'data'},
],
 head:{
   text:'',
   tick:0,
 },
 foot:{
   text:'Classic Read',
   }
}
</script>

### Pipelined

<div style="float:left">
<script type="WaveDrom">
{signal: [
  {name:'CLK',         wave: 'p...' },
  {name:'WE',          wave: 'x1.x' },
  {name:'CYC',         wave: '01.0' },
  {name:'STB',         wave: '010.' },
  {name:'ACK',         wave: '0.10' },
  {name:'ADR',         wave: 'x2.x', data: 'addr' },
  {name:'DAT_MOSI',    wave: 'x2x.', data: 'data' },
  {name:'DAT_MISO',    wave: 'x...'},
],
 head:{
   text:'',
   tick:0,
 },
 foot:{
   text:'Pipelined write',
   }
}
</script>
</div>

<script type="WaveDrom">
{signal: [
  {name:'CLK',         wave: 'p...' },
  {name:'WE',          wave: 'x0.x' },
  {name:'CYC',         wave: '01.0' },
  {name:'STB',         wave: '010.' },
  {name:'ACK',         wave: '0.10' },
  {name:'ADR',         wave: 'x2.x', data: 'addr'},
  {name:'DAT_MOSI',    wave: 'x...'  },
  {name:'DAT_MISO',    wave: 'x.2x', data: 'data'},
],
 head:{
   text:'',
   tick:0,
 },
 foot:{
   text:'Pipelined read',
   }
}
</script>
