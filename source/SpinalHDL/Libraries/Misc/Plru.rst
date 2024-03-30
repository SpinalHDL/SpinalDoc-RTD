.. role:: raw-html-m2r(raw)
   :format: html

Plru
==========================

Introduction
--------------------
/**
  * Pseudo least recently used combinatorial logic
  * io.context.state need to be handled externaly.
  * When you want to specify a access to a entry, you can use the io.update interface
  * to get the new state value.
  */


.. code-block:: scala

      class MyPlugin extends FiberPlugin {
        val logic = during setup new Area {
          // Here we are executing code in the setup phase
          awaitBuild()
          // Here we are executing code in the build phase
        }
      }

      class MyPlugin2 extends FiberPlugin {
        val logic = during build new Area {
          // Here we are executing code in the build phase
        }
      }


