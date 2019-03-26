
Scope violation
===============

Introduction
------------

SpinalHDL will check that there no signals assigned outside the scope they are defined in. This error isn't easy to trigger as it requires some specific meta hardware description tricks.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val cond = Bool()

     var tmp : UInt = null
     when(cond){
       tmp = UInt(8 bits)
     }
     tmp := U"x42"
   }

will throw:

.. code-block:: text

   SCOPE VIOLATION : (toplevel/tmp :  UInt[8 bits]) is assigned outside its declaration scope at
     ***
     Source file location of the tmp := U"x42" via the stack trace
     ***

A fix could be:

.. code-block:: scala

   class TopLevel extends Component {
     val cond = Bool()

     var tmp : UInt = UInt(8 bits)
     when(cond){

     }
     tmp := U"x42"
   }
