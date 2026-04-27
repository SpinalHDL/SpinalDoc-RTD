.. _sim_sensitive_api:

Sensitive API
=============

You can register callback functions to be called on each delta-cycle of the simulation:

.. list-table::
   :header-rows: 1
   :widths: 1 5

   * - Sensitive functions
     - Description
   * - ``forkSensitive { callback }``
     - Register the callback code to be called at each delta-cycle of the simulation
   * - ``forkSensitiveWhile { callback }``
     - Register the callback code to be called at each delta-cycle of the simulation, while the callback return value is true (meaning it should be rescheduled for the next delta-cycle)

