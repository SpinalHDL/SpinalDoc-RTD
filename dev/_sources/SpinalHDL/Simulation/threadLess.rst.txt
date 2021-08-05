
Thread-less API
==========================================

There is some functions that you can use to avoid the usage of threading but which still allow you to move in time.

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Threadless functions
     - Description
   * - delayed(delay){callback}
     - Register the callback code to be called at in a given simulation time.

The advantage of the delayed function over using a regular simulation thread + sleep are : 

 - Performance (no context switching)
 - Memory usage (no native JVM thread memory allocation)

Also, some other thread-less function related with ClockDomain are documented as :ref:`Callback API <sim_clock_threadless>`, and some others related with the delta-cycle execution are documented :ref:`Sensitive API <sim_sensitive_api>`

 



