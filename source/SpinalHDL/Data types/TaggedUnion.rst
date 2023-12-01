TaggedUnion
===========

Description
^^^^^^^^^^^

The ``TaggedUnion`` is a composite type that allows for the creation of type-safe unions.
It is a data structure that can hold one of several typed fields called variants, where only one variant can be used at a time.
This is particularly useful in scenarios where different types of data might be passed around in the same wires.


Declaration
^^^^^^^^^^^

To declare a ``TaggedUnion``, you define a class that extends from ``TaggedUnion`` and specify the different types it can hold.

.. code-block:: scala

   case class MyUnion() extends TaggedUnion {
     val typeA = TypeA()
     val typeB = TypeB()
     // ... other types
   }

For example, a union holding either a signed or an unsigned integer could be defined as:

.. code-block:: scala

   case class SIntOrUInt() extends TaggedUnion {
     val s = SInt(8 bits)
     val u = UInt(10 bits)
   }

The union can then be used in your designs, and you can switch between the different types it holds based on your logic with dedicated methods.

Operators
^^^^^^^^^

Choose a variant
~~~~~~~~~~~~~~~~~~

There are two methods to choose (assign) a variant.
``choose`` is guided by the type only and is only applied if all variant types are different.
``chooseVariant`` allow to explicitely select the desired variant.

.. code-block:: scala

    case class TU1() extends TaggedUnion {
      val typeA = TypeA()
      val typeB = TypeB()
    }

    case class TU2() extends TaggedUnion {
      val a1 = TypeA()
      val a2 = TypeA()
      val b  = TypeB()
    }

    val tu1 = TU1()

    tu1.assignDontCare()
    tu1.choose {
        case a: TypeA => { // TypeA means that you select variant typeA without ambiguity
            // here you can assign to tu1, seen as of type TypeA
            a.valid := 1 // assigned the relevant wires in tu1
            // ...
        }
    }

    val tu2 = TU2()

    tu2.assignDontCare()
    tu2.chooseVariant(tu2.a2) { // we must specify the variant or we cannot decide between a1 and a2
        a: TypeA => { 
            // here you can assign to tu2, seen as of type TypeA
            // variant a2 has been chosen
            a.valid := 1
            // ...
        }
    }


Read a TaggedUnion
~~~~~~~~~~~~~~~~~~

You can react to the value of a TaggedUnion with methods ``among`` and ``amongVariants``.
``among`` is type-based only, and ``amongVariants`` allow to test the chosen variant explicitely.

.. code-block:: scala

    case class TU1() extends TaggedUnion {
      val typeA = TypeA()
      val typeB = TypeB()
    }

    case class TU2() extends TaggedUnion {
      val a1 = TypeA()
      val a2 = TypeA()
      val b  = TypeB()
    }

    val tu1 = TU1()

    tu1.among {
        case a: TypeA => { // here you can read the tu1 as a TypeA value when variant typeA is chosen
            do_something := a.valid
        }
        case b: TypeB => { // here you can read the tu1 as a TypeB value when variant typeB is chosen
            do_something := b.val
        }
    }

    val tu2 = TU2()

    tu2.amongVariants {
        case (tu2.a1, a: TypeA) => { // only when variant a1 is chosen
            do_something := a.valid
        }
        case (tu2.a2, a: TypeA) => { // only when variant a1 is chosen
            do_something := !a.valid
        }
        case (tu2.b, b: TypeB) => {
            do_something := b.val
        }
    }

