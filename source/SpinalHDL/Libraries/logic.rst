Logic Simplification Utilities 
===============================
A minimal Boolean simplification and decode-table utility for decoders using quine-mcklusky. 
Provides masked pattern matching, Quine McCluskey style logic reduction, and a high-level decode-table builder. 
--- # Masked 
Represents a bit pattern with care (significant) and don't-care bits. 
`value` = bit values 
`care` = which bits must match (1 = match, 0 = don't care) 
**Main API:** 
* `covers(that)` check if one pattern includes another 
* `intersects(that)` check if patterns overlap 
* `mergeOneBitDifSmaller` merge patterns differing by one bit 
* `=== (bits)` hardware match under mask 
* `toString(bitCount)` shows bits with - for don't-care 
**Example:** 
.. code-block:: scala 
  Masked(BigInt("0010", 2), Masked("1111", 2)), Masked(1--0) 
  
Used to define instruction encodings for decode tables. 
--- # Symplify Performs Boolean logic minimization for decode circuits. 
**Main API:** 
* `apply(input, mapping, resultWidth)` simplified decode logic 
* `apply(input, trueTerms)` simplified Boolean output 
* `trueAndDontCare(...)` simplified Boolean with explicit don-t-care terms Based on Quine McCluskey; runs at elaboration time. 

--- # DecodingSpec High-level builder for decode tables using `Masked` patterns. 
  **API:** * `setDefault(value)` 
  * `addNeeds(key, value)` 
  * `addNeeds(keys, value)` 
  * `build(sel, coverAll)` 
  generate simplified decode logic 
**Example:** 
.. code-block:: scala ``` 
  val spec = new DecodingSpec(UInt(4 bits)) 
  spec.setDefault(Masked(U"0011")) 
  spec.addNeeds(Masked(B"000"), Masked(U"1000")) 
  result := spec.build(sel, allPatterns) 

Generates minimized combinational decode logic. 
  --- # Practical Use 
Define bit patterns as `Masked` and feed them into `DecodingSpec` or `Symplify` to build compact decode logic for CPU instruction sets (e.g., RISC-V). 

* Output hardware is minimized (fewer LUTs / simpler gates)
