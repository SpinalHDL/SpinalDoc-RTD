---
layout      : page
title       : Element and range
description : "Description of element and range"
tags        : [datatype]
categories  : [documentation, types, element]
permalink   : /spinal/core/types/elements
sidebar     : spinal_sidebar
---

# Element

Elements could be defined as follows:

| Element syntax                | Description                                                                          |
| ----------------------------- | ------------------------------------------------------------------------------------ |
| x : Int -> y : Boolean/Bool   |  Set bit x with y                                                                    |
| x : Range -> y : Boolean/Bool |  Set each bits in range x with y                                                     |
| x : Range -> y : T            |  Set bits in range x with y                                                          |
| x : Range -> y : String       |  Set bits in range x with y <br> The string format follow same rules than B"xyz" one |                                                  
| default -> y : Boolean/Bool   |  Set all unconnected bits with the y value.<br> This feature could only be use to do assignments without the B prefix or with the B prefix combined with the bits specification |


# Range

You can define a Range values

| Range syntax | Description    | Width  |
| ------------ | -----------    | -----  |
| (x downto y) |  [x:y], x >= y | x-y+1  |
| (x to y)     |  [x:y], x <= y | y-x+1  |
| (x until y)  |  [x:y[, x < y  | y-x    | 
