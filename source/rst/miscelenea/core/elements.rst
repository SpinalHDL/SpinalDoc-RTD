.. role:: raw-html-m2r(raw)
   :format: html

.. _element:

Element
=======

Elements could be defined as follows:

.. list-table::
   :header-rows: 1

   * - Element syntax
     - Description
   * - x : Int -> y : Boolean/Bool
     - Set bit x with y
   * - x : Range -> y : Boolean/Bool
     - Set each bits in range x with y
   * - x : Range -> y : T
     - Set bits in range x with y
   * - x : Range -> y : String
     - Set bits in range x with y :raw-html-m2r:`<br>` The string format follow same rules than B"xyz" one
   * - default -> y : Boolean/Bool
     - Set all unconnected bits with the y value.\ :raw-html-m2r:`<br>` This feature could only be use to do assignments without the B prefix or with the B prefix combined with the bits specification

.. _range:

Range
=====

You can define a Range values

.. list-table::
   :header-rows: 1

   * - Range syntax
     - Description
     - Width
   * - (x downto y)
     - [x:y], x >= y
     - x-y+1
   * - (x to y)
     - [x:y], x <= y
     - y-x+1
   * - (x until y)
     - [x:y[, x < y
     - y-x

