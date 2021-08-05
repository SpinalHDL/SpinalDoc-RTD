
Unassigned register
===================

Introduction
------------

SpinalHDL will check that all registers which impact the design have been assigned somewhere.

Example
-------

The following code:

.. code-block:: scala

   class TopLevel extends Component {
     val result = out(UInt(8 bits))
     val a = Reg(UInt(8 bits))
     result := a
   }

will throw:

.. code-block:: text

   UNASSIGNED REGISTER (toplevel/a :  UInt[8 bits]), defined at
     ***
     Source file location of the toplevel/a definition via the stack trace
     ***

A fix could be:

.. code-block:: scala

   class TopLevel extends Component {
     val result = out(UInt(8 bits))
     val a = Reg(UInt(8 bits))
     a := 42
     result := a
   }

Register with only init
-----------------------

In some cases, because of the design parameterization, it could make sense to generate a register which has no assignment but only an ``init`` statement.

.. code-block:: scala

   class TopLevel extends Component {
     val result = out(UInt(8 bits))
     val a = Reg(UInt(8 bits)) init(42)

     if(something)
       a := somethingElse
     result := a
   }

will throw:

.. code-block:: text

   UNASSIGNED REGISTER (toplevel/a :  UInt[8 bits]), defined at
     ***
     Source file location of the toplevel/a definition via the stack trace
     ***

To fix it, you can ask SpinalHDL to transform the register into a combinational one if no assignment is present but it has an ``init`` statement:

.. code-block:: scala

   class TopLevel extends Component {
     val result = out(UInt(8 bits))
     val a = Reg(UInt(8 bits)).init(42).allowUnsetRegToAvoidLatch

     if(something)
       a := somethingElse
     result := a
   }
