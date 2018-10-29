
The language provides 5 base types and 2 composite types that can be used.


* Base types : `\ ``Bool`` </SpinalDoc/spinal/core/types/Bool>`_\ , `\ ``Bits`` </SpinalDoc/spinal/core/types/Bits>`_\ , `\ ``UInt`` </SpinalDoc/spinal/core/types/Int>`_ for unsigned integers, `\ ``SInt`` </SpinalDoc/spinal/core/types/Int>`_ for signed integers and `\ ``Enum`` </SpinalDoc/spinal/core/types/Enum>`_.
* Composite types : `\ ``Bundle`` </SpinalDoc/spinal/core/types/Bundle>`_ and `\ ``Vec`` </SpinalDoc/spinal/core/types/Vector>`_.


.. raw:: html

   <center><img src="/SpinalDoc/images/types.svg" tyle="width: 400px;"></center>


In addition to the base types Spinal supports Fixed point that is documented `there </SpinalDoc/spinal/core/types/Fix>`_ and floating point that is actually under development `there </SpinalDoc/spinal/core/types/Floating>`_.

Finally, a special type is available for checking equality between a BitVector and a bits constant that contain hole (don't care values). Below, there is an example :

.. code-block:: scala

   val myBits  = Bits(8 bits)
   val itMatch = myBits === M"00--10--" // - don't care value
