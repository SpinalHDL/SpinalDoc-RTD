
Assertions
==========

In addition to Scala run-time assertions, you can add hardware assertions using the following syntax:

``assert(assertion : Bool, message : String = null, severity: AssertNodeSeverity = Error)``

Severity levels are:

.. list-table::
   :header-rows: 1
   :widths: 1 3

   * - Name
     - Description
   * - NOTE
     - Used to report an informative message
   * - WARNING
     - Used to report an unusual case
   * - ERROR
     - Used to report an situation that should not happen
   * - FAILURE
     - Used to report a fatal situation and close the simulation


One practical example could be to check that the ``valid`` signal of a handshake protocol never drops when ``ready`` is low:

.. code-block:: scala

   class TopLevel extends Component {
     val valid = RegInit(False)
     val ready = in Bool

     when(ready) {
       valid := False
     }
     // some logic

     assert(
       assertion = !(valid.fall && !ready),
       message   = "Valid dropped when ready was low",
       severity  = ERROR
     )
   }
