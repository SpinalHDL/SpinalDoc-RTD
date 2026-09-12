RISC-V AIA Peripherals
======================

The AIA library provides building blocks for integrating the RISC-V Advanced
Interrupt Architecture into SpinalHDL systems. The implementation is split into
two groups:

* APLIC components, used for wired interrupt collection, direct interrupt delivery, MSI delivery, child-domain delegation, and TileLink/Fiber integration.
* IMSIC-related components, used for interrupt-file modeling and memory-write trigger decoding.

APLIC
-----

APLIC domain parameters
~~~~~~~~~~~~~~~~~~~~~~~

``APlicDomainParam`` describes where the APLIC domain sits in the APLIC
hierarchy. Only the following helper should be used for mode selection.

.. list-table::
   :header-rows: 1
   :widths: 3 7

   * - Constructor
     - Meaning
   * - ``APlicDomainParam.root(genParam)``
     - Root M-domain APLIC. MSI address configuration is owned locally and
       exported to child domains.
   * - ``APlicDomainParam.M(genParam)``
     - Non-root M-domain APLIC. MSI address configuration is provided by its
       parent.
   * - ``APlicDomainParam.S(genParam)``
     - Non-root S-domain APLIC. Supervisor MSI address configuration is
       provided by its parent.

APLIC generation parameters
~~~~~~~~~~~~~~~~~~~~~~~~~~~

``APlicGenParam`` selects which APLIC delivery paths are generated and controls
a few MSI-related implementation options.

Public parameters
*****************

.. list-table::
   :header-rows: 1
   :widths: 24 28 18 30

   * - Parameter
     - Type
     - Default
     - Description
   * - ``withDirect``
     - ``Boolean``
     - No default
     - Enables generation of the APLIC direct-delivery path.
   * - ``withMSI``
     - ``Boolean``
     - No default
     - Enables generation of the APLIC MSI-delivery path.
   * - ``genIEP``
     - ``Boolean``
     - ``true``
     - Enables generation of per-source pending and enable register logic.
   * - ``withIForce``
     - ``Boolean``
     - ``false``
     - Enables the optional ``iforce`` direct-delivery test path.

Configuration helpers
*********************
.. list-table::
   :header-rows: 1
   :widths: 3 7

   * - Function
     - Description
   * - ``lockMSI()``
     - Lock the MSI state (Set the L bit of ``mmsiaddrcfgh``), only valid for root domain.
   * - ``withMsiAddrcfg()``
     - Sets ``_withMsiAddrcfg`` to ``true`` and returns the same parameter
       object.
   * - ``withMachineMsiParams(param: APlicMsiParam)``
     - Replaces M mode MSI parameters with the supplied ``param``.
   * - ``withMachineMsiParams(address: BigInt = 0, hhxs: Int = 0, lhxs: Int = 0, hhxw: Int = 0, lhxw: Int = 0)``
     - Replaces M mode MSI parameters with the parameters.
   * - ``withSupervisorMsiParams(param: APlicMsiParam)``
     - Replaces S mode MSI parameters with the supplied ``param``.
   * - ``withSupervisorMsiParams(address: BigInt = 0, lhxs: Int = 0)``
     - Replaces S mode MSI parameters with the parameters.

Note: ``withMachineMsiParams``/``withSupervisorMsiParams`` is valid only if ``withMsiAddrcfg()`` is called.

The ``APlicMsiParam`` has the following parameters:

.. list-table::
   :header-rows: 1
   :widths: 3 3 3 7

   * - Name
     - Type
     - Default
     - Description
   * - ``base``
     - BigInt
     - 0x0
     - The base address of M mode IMSIC file
   * - ``hhxs``
     - Int
     - 0
     - ``hhxs`` value of ``msiaddrcfg`` register
   * - ``lhxs``
     - Int
     - 0
     - ``lhxs`` value of ``msiaddrcfg`` register
   * - ``hhxw``
     - Int
     - 0
     - ``hhxw`` value of ``msiaddrcfg`` register
   * - ``lhxw``
     - Int
     - 0
     - ``lhxw`` value of ``msiaddrcfg`` register

For detailed description of ``hhxs``/``lhxs``/``hhxw``/``lhxw``, see section ``Addresses and data for outgoing MSIs`` of standard AIA document.

Pre-defined configuration
**************************
.. list-table::
   :widths: 3 7

   * - ``APlicGenParam.direct``
     - Direct mode only
   * - ``APlicGenParam.msi``
     - MSI mode only
   * - ``APlicGenParam.full``
     - Both direct and MSI mode
   * - ``APlicGenParam.test``
     - Full configuration, with tested signal (``iforce``) enabled

Integration
~~~~~~~~~~~~~~

The ``TilelinkAPlicFiber`` and ``TilelinkAPlicMsiSender`` is provided for CPU integration. It can be used as the following:

.. code-block:: scala

   // Create APLIC device
   val aplic = TilelinkAPlicFiber(
     APlicDomainParam.root(APlicGenParam.full)
   )

   // Register IRQ
   val irq = aplic.createInternalInterruptSlave(
     id   = 1,
     mode = LEVEL_HIGH
   )

   // Register master
   val h0 = aplic.createInterruptMaster(id = 0)


For MSI delivery, the APLIC expose an MSI stream producer to connect MSI sender.

.. code-block:: scala

   val msiStream = aplic.createMsiStreamProducer()

   val msiSender = TilelinkAPlicMsiSenderFiber(
     pendingSize  = 4,
     addressWidth = 64
   )

   msiSender.createMsiStreamConsumer() << msiStream

Child domains are handled through the cascaded interrupt-controller fiber
interface. The parent APLIC collects child source IDs and creates
``APlicChildInfo`` entries during fiber elaboration.

Note: VexiiRiscv provides a system-level reference integration for APLIC. It can be used as a reference.

IMSIC
-----

IMSIC file
~~~~~~~~~~~~~~~~

There are two helper class for IMSIC file implementation. In almost all the cases, the ``ImsicFileRam`` should be used as it provided optimized resource usage and performance. The ``ImsicFile`` should only be used in cases where performance is critical and resource consumption is extremely abundant.

``ImsicFileRam``
****************

``ImsicFileRam`` provides a RAM-backed variant with one or more access ports. This file is constructed by ``ImsicFileParameters``, its parameters is:

.. list-table::
   :header-rows: 1
   :widths: 2 2 7

   * - Name
     - Type
     - Description
   * - ``hartId``
     - Int
     - The hartId of RISC-V hart that this IMSIC file binds to
   * - ``guestId``
     - Int
     - The guestId of RISC-V hart that this IMSIC file binds to
   * - ``sourceNum``
     - Int
     - The number of interrupt supports by tis file. It should be pow of 2
   * - ``xlen``
     - Int
     - The XLEN of RISC-V hart, used for provided aligned data
   * - ``portNum``
     - Int
     - The number of access ports, default is 2 (1 for CSR and 1 for bus)

``ImsicFile``
****************

.. list-table::
   :header-rows: 1
   :widths: 2 2 7

   * - Name
     - Type
     - Description
   * - ``hartId``
     - Int
     - The hartId of RISC-V hart that this IMSIC file binds to
   * - ``guestId``
     - Int
     - The guestId of RISC-V hart that this IMSIC file binds to
   * - ``sourceNum``
     - Int
     - The number of interrupt supports by tis file. It should be pow of 2

Integration
~~~~~~~~~~~

Check ImsicPlugin of VexiiRiscv for CPU integration for detailed IMSIC.
