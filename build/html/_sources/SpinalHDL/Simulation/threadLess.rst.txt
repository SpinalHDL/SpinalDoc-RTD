Thread-less API
===============

There are some functions that you can use to avoid the need for threading, but which still allow you to control the flow of simulation time.

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Threadless functions
     - Description
   * - ``delayed(delay){ callback }``
     - Register the callback code to be called at a simulation time ``delay`` steps after the current timestep.

The advantages of the ``delayed`` function over using a regular simulation thread + sleep are:

 - Performance (no context switching)
 - Memory usage (no native JVM thread memory allocation)

Some other thread-less functions related to ``ClockDomain`` objects are documented as part of the :ref:`Callback API <sim_clock_threadless>`, and some others related with the delta-cycle execution process are documented as part of the :ref:`Sensitive API <sim_sensitive_api>`

 



