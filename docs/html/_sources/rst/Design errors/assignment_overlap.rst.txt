
Assignement overlap
===================

Introduction
------------

SpinalHDL will check that no signal assignement completly erase a previous one.

Example
-------

The following code :

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     a := 42
     a := 66 //Erease the a := 42 :(
   }

will throw :

.. code-block:: text

   ASSIGNMENT OVERLAP completely the previous one of (toplevel/a :  UInt[8 bits])
     ***
     Source file location of the a := 66 assignement via the stack trace
     ***

A fix could be :

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     a := 42
     when(something){
       a := 66
     }
   }

But in the case you realy want to override the previous assignements (Yes, it could make sense in some cases), you can do as following :

.. code-block:: scala

   class TopLevel extends Component {
     val a = UInt(8 bits)
     a := 42
     a.allowOverride
     a := 66
   }
