.. _packed_bundle:

PackedBundle
============

Introduction
------------

``PackedBundle`` is an extension of ``Bundle`` that allows fields to be placed at exact bit positions within a word.
This is useful when mapping hardware registers, protocol frames, or any other structure with a defined bit layout.

Unlike a plain ``Bundle``, which places fields consecutively and always starts from bit 0, ``PackedBundle`` lets you annotate each field with its target bit range using implicit helper methods.
Fields that are not annotated are placed immediately after the previous field, just like in a plain ``Bundle``.

Placement methods
-----------------

Inside a ``PackedBundle`` class body, each ``Data`` field can be annotated with one of the following helpers:

.. list-table::
   :header-rows: 1
   :widths: 3 5

   * - Method
     - Description
   * - ``.pack(range)``
     - Place the field at the given bit range. If the field is wider than the range, extra bits are lost.
   * - ``.pack(range, endianness)``
     - Place the field at the given range, aligning to ``LITTLE`` (ascending) or ``BIG`` (descending) endianness.
   * - ``.packFrom(pos)``
     - Place the field with its LSB at bit position ``pos``. Uses the full width of the field.
   * - ``.packTo(pos)``
     - Place the field with its MSB at bit position ``pos``. Uses the full width of the field.

Fields with no placement annotation are placed sequentially starting at the next free bit position after the last annotated or sequential field.

Example
-------

.. code-block:: scala

   import spinal.lib._

   val regWord = new PackedBundle {
     val init   = Bool().packFrom(0)   // Bit 0
     val stop   = Bool()               // Bit 1 (next available)
     val result = Bits(16 bits).packTo(31) // Bits 31 downto 16
   }

   // Obtain the packed Bits representation
   val packed: Bits = regWord.packed

   // Unpack from a Bits value
   regWord.unpack(someReadData)

Operations
----------

.. list-table::
   :header-rows: 1
   :widths: 2 2 5

   * - Method
     - Return
     - Description
   * - ``packed``
     - Bits
     - Returns a ``Bits`` signal that contains all fields assembled at their assigned positions
   * - ``unpack(bits)``
     - Unit
     - Assigns each field of the bundle from the corresponding bits of the provided ``Bits`` signal
   * - ``getPackedWidth``
     - Int
     - Returns the total bit-width needed to hold all packed fields (index of the highest bit + 1)
   * - ``mappings``
     - Seq[(Range, Data)]
     - Returns the ordered list of (bit range, data field) pairs for all fields

PackedWordBundle
----------------

``PackedWordBundle`` is an extension of ``PackedBundle`` that organises packing around fixed-size words.
Each field can be assigned to a specific word index using the ``.inWord(index)`` helper.
Fields that span more than one word automatically wrap into subsequent words.

The word width is supplied as a ``BitCount`` at construction time.

.. code-block:: scala

   import spinal.lib._

   val wordPacked = new PackedWordBundle(8 bits) {
     val aNumber = UInt(8 bits).inWord(0)              // Bits 7 downto 0
     val bNumber = UInt(8 bits).pack(0 to 7).inWord(1) // Bits 15 downto 8
     val large   = Bits(18 bits).inWord(2)             // Bits 33 downto 16
     val flag    = Bool()                              // Bit 34
   }

When ``.inWord(index)`` is combined with a prior ``.pack(range)`` annotation, the bit range is interpreted relative to the start of the specified word.

Using PackedBundle with streams
--------------------------------

``PackedBundle`` integrates with the ``StreamUnpacker`` and ``StreamPacker`` utilities, which allow packing and unpacking structured data across a stream of fixed-width words:

.. code-block:: scala

   import spinal.lib._

   case class MyFrame() extends PackedBundle {
     val addr = UInt(10 bits).packFrom(0)   // Bits 9 downto 0
     val data = Bits(16 bits).packFrom(10)  // Bits 25 downto 10
     val last = Bool().packFrom(26)         // Bit 26
   }

   val inputStream  = Stream(Bits(8 bits))
   val frame        = MyFrame()

   // Unpack a multi-word stream into the frame fields
   val unpacker = StreamUnpacker(inputStream, frame)
   when(unpacker.io.allDone) {
     // All fields have been filled
   }

   // Pack the frame fields back into a stream
   val outputStream = Stream(Bits(8 bits))
   val packer = StreamPacker(outputStream, frame)
   packer.io.start := startCondition
