
In addition of scala run-time assertion, you can add hardware assertion via the following syntax:

``assert(assertion : Bool,message : String = null,severity: AssertNodeSeverity = Error)``

Severity levels are :

.. list-table::
   :header-rows: 1

   * - Name
     - Description
   * - NOTE
     - Used to report a informative message
   * - WARNING
     - Used to report a unusual case
   * - ERROR
     - Used to report an situation that should not happen
   * - FAILURE
     - Used to report a fatal situation and close the simulation


One practical example could be to check that the ``valid`` of a handshake protocol never drop when ``ready`` is low :

.. code-block:: scala

   class TopLevel extends Component {
     val valid = RegInit(False)
     val ready = in Bool

     when(ready){
       valid := False
     }
     // some logic

     assert(
       assertion = !(valid.fall && !ready),
       message   = "Valid drop when ready was low",
       severity  = ERROR
     )
   }
