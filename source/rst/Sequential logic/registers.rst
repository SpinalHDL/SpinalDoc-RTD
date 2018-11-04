.. role:: raw-html-m2r(raw)
   :format: html

Registers
=========

Introduction
------------

Creating register is very different than VHDL/Verilog.\ :raw-html-m2r:`<br>`
In Spinal, you don't have process/always blocks. Registers are explicitly defined at the declaration. :raw-html-m2r:`<br>`
This difference against traditional event driven HDL has a big impact :


* You can assign registers and wires in the same scope, you don't have to split your code between process/always blocks
* It make things much more flexible (see :ref:`Functions <function>` )

The way how clock and reset wire are managed are explained in the :ref:`Clock domain <clock_domain>` chapter.

Instantiation
-------------

There is 4 ways to Instantiate a register :

.. list-table::
   :header-rows: 1

   * - Syntax
     - Description
   * - Reg(type : Data)
     - Register of the given type
   * - RegInit(resetValue : Data)
     - Register loaded with the given ``resetValue`` when a reset occur
   * - RegNext(nextValue : Data)
     - Register that sample the given ``nextValue`` each cycle
   * - RegNextWhen(nextValue : Data, cond : Bool)
     - Register that sample the given ``nextValue`` when a condition occurs


There is some usage examples :

.. code-block:: scala

   //UInt register of 4 bits    
   val reg1 = Reg(UInt(4 bit))  

   //Register that sample toto each cycle  
   val reg2 = RegNext(reg1 + 1)    

   //UInt register of 4 bits initialized with 0 when the reset occur
   val reg3 = RegInit(U"0000")
   reg3 := reg2
   when(reg2 === 5){
     reg3 := 0xF
   }

   //Register toto when cond is True
   val reg4 = RegNextWhen(reg3,cond)

The code above will infer the following logic :\ :raw-html-m2r:`<br>`
:raw-html-m2r:`<img src="https://cdn.rawgit.com/SpinalHDL/SpinalDoc/c7decc7420d1e790aba7c14d8bdc898aa6046869/asset/picture/register.svg"  align="middle" width="300">`

.. note::
   The reg3 example show how you can assign the value of a RegInit register. But this it's also right to use the same syntax for all others (Reg,RegNext,RegNextWhen).\ :raw-html-m2r:`<br>` As for combinatorial assignments, the rule is 'Last assignment win', but if no assignment is done, the register keep its value.

Also, RegNext is an abstraction which is build over the Reg syntax. The two followings sequances of code are strictly equivalents :

.. code-block:: scala

   //Standard way
   val something = Bool
   val value = Reg(Bool)
   value := something

   //Short way
   val something = Bool
   val value = RegNext(something)

Reset value
-----------

In addition of the ``RegInit(value : Data)`` syntax which directly create the register with a reset logic,
you can also set the reset value by calling the ``init(value : Data)`` function on the register.

.. code-block:: scala

   //UInt register of 4 bits initialized with 0 when the reset occur
   val reg1 = Reg(UInt(4 bit)) init(0)

If you have a Bundle register, you can use the ``init`` function on each elements of that Bundle.

.. code-block:: scala

   case class ValidRGB() extends Bundle{
     val valid = Bool
     val r,g,b = UInt(8 bits)
   }

   val reg = Reg(ValidRGB())
   reg.valid init(False)  //Only the valid of that register bundle will have an reset value.

Initialization value for simulation purposes
--------------------------------------------

For register that doesn't need a reset value in RTL, but need an initialization value for simulation (avoid x-propagation), you can ask for an initialization random value by calling the ``randBoot()`` function.

.. code-block:: scala

   // UInt register of 4 bits initialized with a random value
   val reg1 = Reg(UInt(4 bit)) randBoot()
