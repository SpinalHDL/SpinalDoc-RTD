Logic Simplification Utilities and Decoder 
==========================================

A minimal Boolean simplification and decode-table utility for decoders using 
the `Quine–McCluskey algorithm <https://en.wikipedia.org/wiki/Quine%E2%80%93McCluskey_algorithm>`_.

Provides masked pattern matching, Quine–McCluskey style logic reduction, 
and a high-level decode-table builder. 

`Masked`
--------

Represents a bit pattern with care (significant) and don't-care bits. 
- `value` = bit values 
- `care` = which bits must match (1 = match, 0 = don't care) 

Example:

.. code-block:: scala

  Masked(0010), 
  Masked(11-1), 
  Masked(1--0) 

e.g RISC-V instructions:

.. code-block:: scala

  val ADD     = M"0000000----------000-----0110011"
  val ADDI    = M"-----------------000-----0010011"
  
Used to define instruction encodings for decode tables. 

`DecodingSpec`
--------------

High-level builder for decode tables using `Masked` patterns.

Methods:
  * `addNeeds(key : Masked, value : Masked)` 
  * `addNeeds(keys : Seq[Masked], value : Masked)` 
  * `build(sel, coverAll)`
  * `setDefault(value : Masked)`

This generate simplified decode logic.

Example:

.. code-block:: scala

  val spec = new DecodingSpec(UInt(4 bits)) 
  spec.setDefault(Masked(U"0011")) 
  spec.addNeeds(Masked(B"000"), Masked(U"1000")) 
  result := spec.build(sel, allPatterns) 

Generates minimized combinational decode logic.

The practical use is to define bit patterns as `Masked` and feed them into
`DecodingSpec` to build compact decode logic (e.g., RISC-V). The output hardware
is then minimized (fewer LUTs / simpler gates).
