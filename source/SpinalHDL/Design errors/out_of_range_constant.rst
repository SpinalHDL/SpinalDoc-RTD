
Out of Range Constant
=====================

Introduction
------------

SpinalHDL checks that in comparisons with literals the literal is not wider than the value compared to.

Example
-------

For example the following code:

.. code-block:: scala

    val value = in UInt(2 bits)
    val result = out(value < 42)

Will result in the following error:

.. code-block:: text

	OUT OF RANGE CONSTANT. Operator UInt < UInt
	- Left  operand : (toplevel/value : in UInt[2 bits])
	- Right operand : (U"101010" 6 bits)
	 is checking a value against a out of range constant

Specifying exceptions
---------------------

In some cases, because of the design parametrization, it can make sense to compare a value to a larger constant and get a statically known ``True/False`` result.

You have the option to specifically whitelist one instance of a comparison with an out of range constant.

.. code-block:: scala

    val value = in UInt(2 bits)
    val result = out((value < 42).allowOutOfRangeLiterals)


Alternatively, you can allow comparisons to out of range constants for the whole design.

.. code-block:: scala

	SpinalConfig(allowOutOfRangeLiterals = true)
