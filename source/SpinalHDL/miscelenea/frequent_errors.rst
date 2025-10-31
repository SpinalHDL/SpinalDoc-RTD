:orphan:

.. role:: raw-html-m2r(raw)
   :format: html

Frequent Errors
---------------

This page will talk about errors which could happen when people are using SpinalHDL.

Exception in thread "main" java.lang.NullPointerException
---------------------------------------------------------

**Console symptoms :**

.. code-block:: text

   Exception in thread "main" java.lang.NullPointerException

**Code Example :**

.. code-block:: scala

   val a = b + 1         // b can't be read at that time, because b isn't instantiated yet
   val b = UInt(4 bits)

**Issue explanation :**

SpinalHDL is not a language, it is an Scala library, which mean, it obey to the same rules than the Scala general purpose programming language. When you run your SpinalHDL hardware description to generate the corresponding VHDL/Verilog RTL, your SpinalHDL hardware description will be executed as a Scala programm, and b will be a ``null`` reference until the programm execution come to that line, and it's why you can't use it before.

Hierarchy violation
-------------------

The SpinalHDL compiler check that all your assignments are legal from an hierarchy perspective. Multiple cases are elaborated in following chapters

Signal X can't be assigned by Y
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Console symptoms :**

.. code-block:: text

   Hierarchy violation : Signal X can't be assigned by Y

**Code Example :**

.. code-block:: scala

   class ComponentX extends Component {
     ...
     val X = Bool()
     ...
   }

   class ComponentY extends Component {
     ...
     val componentX = new ComponentX
     val Y = Bool()
     componentX.X := Y // This assignment is not legal
     ...
   }

.. code-block:: scala

   class ComponentX extends Component {
     val io = new Bundle {
       val X = Bool() // Forgot to specify an in/out direction
     }
     ...
   }

   class ComponentY extends Component {
     ...
     val componentX = new ComponentX
     val Y = Bool()
     componentX.io.X := Y // This assignment will be detected as not legal
     ...
   }

**Issue explanation :**

You can only assign input signals of subcomponents, else there is an hierarchy violation. If this issue happened, you probably forgot to specify the X signal's direction.

Input signal X can't be assigned by Y
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Console symptoms :**

.. code-block:: text

   Hierarchy violation : Input signal X can't be assigned by Y

**Code Example :**

.. code-block:: scala

   class ComponentXY extends Component {
     val io = new Bundle {
       val X = in Bool()
     }
     ...
     val Y = Bool()
     io.X := Y // This assignment is not legal
     ...
   }

**Issue explanation :**

You can only assign an input signals from the parent component, else there is an hierarchy violation. If this issue happened, you probably mixed signals direction declaration.

Output signal X can't be assigned by Y
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Console symptoms :**

.. code-block:: text

   Hierarchy violation : Output signal X can't be assigned by Y

**Code Example :**

.. code-block:: scala

   class ComponentX extends Component {
     val io = new Bundle {
       val X = out Bool()
     }
     ...
   }

   class ComponentY extends Component {
     ...
     val componentX = new ComponentX
     val Y = Bool()
     componentX.X := Y // This assignment is not legal
     ...
   }

**Issue explanation :**

You can only assign output signals of a component from the inside of it, else there is an hierarchy violation. If this issue happened, you probably mixed signals direction declaration.
