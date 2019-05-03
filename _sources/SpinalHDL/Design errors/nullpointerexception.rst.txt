.. role:: raw-html-m2r(raw)
   :format: html

NullPointerException
====================

Introduction
------------

NullPointerException is a Scala runtime reported error which can happen when a variable is accessed before it was initialised.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     a := 42
     val a = UInt(8 bits)
   }

will throw:

.. code-block:: text

   Exception in thread "main" java.lang.NullPointerException
     ***
     Source file location of the a := 42 assignement via the stack trace
     ***

A fix could be:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     a := 42
   }

| **Issue explanation** :
| SpinalHDL is not a language, it is a Scala library, which means it obeys the same rules as the Scala general purpose programming language. When you run your SpinalHDL hardware description to generate the corresponding VHDL/Verilog RTL, your SpinalHDL hardware description will be executed as a Scala programm, and ``a`` will be a null reference until the program executes `val a = UInt(8 bits)`, so trying to assign it before then will result in a NullPointerException.
