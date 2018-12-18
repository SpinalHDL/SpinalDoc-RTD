
Unreachable is statement
========================

Introduction
------------

SpinalHDL will check all switch's is statements are reachable.

Example
-------

The following code :

.. code-block:: scala

   class TopLevel extends Component {
     val sel = UInt(2 bits)
     val result = UInt(4 bits)
     switch(sel){
       is(0){ result := 4 }
       is(1){ result := 6 }
       is(2){ result := 8 }
       is(3){ result := 9 }
       is(0){ result := 2 } //Duplicated statement is statement !
     }
   }

will throw :

.. code-block:: text

   UNREACHABLE IS STATEMENT in the switch statement at
     ***
     Source file location of the is statement definition via the stack trace
     ***

A fix could be :

.. code-block:: scala

   class TopLevel extends Component {
     val sel = UInt(2 bits)
     val result = UInt(4 bits)
     switch(sel){
       is(0){ result := 4 }
       is(1){ result := 6 }
       is(2){ result := 8 }
       is(3){ result := 9 }
     }
   }
