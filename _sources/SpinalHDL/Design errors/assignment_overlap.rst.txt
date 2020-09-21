
Assignment overlap
==================

Introduction
------------

SpinalHDL will check that no signal assignment completely erases a previous one.

Example
-------

The following code

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     a := 42
     a := 66 // Erase the a := 42 assignment
   }

will throw the following error:

.. code-block:: text

   ASSIGNMENT OVERLAP completely the previous one of (toplevel/a :  UInt[8 bits])
     ***
     Source file location of the a := 66 assignment via the stack trace
     ***

A fix could be:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     a := 42
     when(something) {
       a := 66
     }
   }

But in the case when you really want to override the previous assignment (as there are times when overriding makes sense), you can do the following:

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     a := 42
     a.allowOverride
     a := 66
   }
