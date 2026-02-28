.. _scopeproperty:

ScopeProperty
==================

A scope property is a thing which can store values locally to the current thread. Its API can be used to set/get that value, but also to apply modification to the value for a portion of the execution in a stack manner.

In other words it is a alternative to global variable, scala implicit, ThreadLocal.

* To compare with global variable, It allow to run multiple thread running the same code independently
* To compare with scala implicit, it is less intrusive in the code base
* To compare with ThreadLocal, it has some API to collect all ScopeProperty and restore them in the same state later on


.. code-block:: scala

  object Xlen extends ScopeProperty[Int]

  object ScopePropertyMiaou extends App {
    Xlen.set(1)
    println(Xlen.get) // 1
    Xlen(2) {
      println(Xlen.get) // 2
      Xlen(3) {
        println(Xlen.get) // 3
        Xlen.set(4)
        println(Xlen.get) // 4
      }
      println(Xlen.get) // 2
    }
  }



