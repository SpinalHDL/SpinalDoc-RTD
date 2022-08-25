 
BinarySystem
============

Specification
-------------
  
| Here things have nothing to do with HDL, but they are very common in digital systems, In particular, the algorithm reference model is widely used. In addition, it is also used in build testbench. 



.. list-table::
   :header-rows: 1
   :widths: 4 8  1

   * - Syntax
     - Description
     - Return

   * - **String**.asHex
     - HexString to BigInt == BigInt(string, 16)
     - BigInt
   * - **String**.asDec
     - Decimal String to BigInt == BigInt(string, 10)
     - BigInt
   * - **String**.asOct
     - Octal String to BigInt == BigInt(string, 8)
     - BigInt
   * - **String**.asBin
     - Binary String to BigInt == BigInt(string, 2)
     - BigInt
   * - 
     - 
     -
   * - **Byte|Int|Long|BigInt**.hexString()
     - to HEX String
     - String
   * - **Byte|Int|Long|BigInt**.octString()
     - to Oct String
     - String
   * - **Byte|Int|Long|BigInt**.binString()
     - to Bin String
     - String
   * - **Byte|Int|Long|BigInt**.hexString(bitSize)
     - first align to bit Size, then to HEX String
     - String
   * - **Byte|Int|Long|BigInt**.octString(bitSize)
     - first align to bit Size, then to Oct String
     - String
   * - **Byte|Int|Long|BigInt**.binString(bitSize)
     - first align to bit Size, then to Bin String
     - String
   * - 
     - 
     -
   * - **Byte|Int|Long|BigInt**.toBinInts()
     - to BinaryList 
     - List[Int]
   * - **Byte|Int|Long|BigInt**.toDecInts()
     - to DecimalList 
     - List[Int]
   * - **Byte|Int|Long|BigInt**.toOctInts()
     - to OctalList 
     - List[Int]
   * - **Byte|Int|Long|BigInt**.toBinInts(num)
     - to BinaryList, align to num size and fill 0
     - List[Int]
   * - **Byte|Int|Long|BigInt**.toDecInts(num)
     - to DecimalList, align to num size and fill 0
     - List[Int]
   * - **Byte|Int|Long|BigInt**.toOctInts(num)
     - to OctalList, align to num size and fill 0
     - List[Int]
   * - **"3F2A"**.hexToBinInts
     - Hex String to BinaryList
     - List[Int]
   * - **"3F2A"**.hexToBinIntsAlign
     - Hex String to BinaryList Align to times of 4
     - List[Int]
   * - 
     - 
     -
   * - **List(1,0,1,0,...)**.binIntsToHex 
     - BinaryList to HexString 
     - String
   * - **List(1,0,1,0,...)**.binIntsToOct 
     - BinaryList to OctString
     - String  
   * - **List(1,0,1,0,...)**.binIntsToHexAlignHigh 
     - BinaryList size align to times of 4 (fill 0) then to HexString 
     - String
   * - **List(1,0,1,0,...)**.binIntsToOctAlignHigh
     - BinaryList size align to times of 3 (fill 0) then to HexString 
     - String
   * - **List(1,0,1,0,...)**.binIntsToInt
     - BinaryList (maxSize 32) to Int 
     - Int
   * - **List(1,0,1,0,...)**.binIntsToLong
     - BinaryList (maxSIZE 64) to Long 
     - Long
   * - **List(1,0,1,0,...)**.binIntsToBigInt
     - BinaryList (size no restrictions) to BigInt
     - BigInt
   * - 
     - 
     -
   * - **Int**.toBigInt
     - 32.toBigInt == BigInt(32)
     - BigInt
   * - **Long**.toBigInt
     - 3233113232L.toBigInt == BigInt(3233113232L)
     - BigInt
   * - **Byte**.toBigInt
     - 8.toByte.toBigInt == BigInt(8.toByte)
     - BigInt
    
String to Int/Long/BigInt
-------------------------

.. code-block:: scala 

   import spinal.core.lib._

   $: "32FF190".asHex

   $: "12384798999999".asDec

   $: "123456777777700".asOct

   $: "10100011100111111".asBin


Int/Long/BigInt to String
-------------------------

.. code-block:: scala 

   import spinal.core.lib._

   $: "32FF190".asHex.hexString()
   "32FF190"
   $: "123456777777700".asOct.octString() 
   "123456777777700"
   $: "10100011100111111".asBin.binString() 
   "10100011100111111"
   $: 32323239988L.hexString()
   7869d8034
   $: 3239988L.octString()
   14270064
   $: 34.binString()
   100010
 

Int/Long/BigInt to Binary-List
------------------------------

.. code-block:: scala

   import spinal.core.lib._

   $: 32.toBinInts
   List(0, 0, 0, 0, 0, 1)
   $: 1302309988L.toBinInts
   List(0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 1)
   $: BigInt("100101110", 2).toBinInts
   List(0, 1, 1, 1, 0, 1, 0, 0, 1)
   $: BigInt("123456789abcdef0", 16).toBinInts
   List(0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 1)
   $: BigInt("1234567", 8).toBinInts
   List(1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1)
   $: BigInt("123451118", 10).toBinInts
   List(0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1)
    
align to fix width

.. code-block:: scala

   import spinal.core.lib._

   $: 39.toBinInts()
   List(1, 1, 1, 0, 0, 1)
   $: 39.toBinInts(8)    // align to 8 bit fill with 0
   List(1, 1, 1, 0, 0, 1, 0, 0)


Binary-List to Int/Long/BigInt
------------------------------

.. code-block:: scala

   import spinal.core.lib._

   $: List(1, 1, 1, 0, 0, 1).binIntsToInt
   39
   $: List(1, 1, 1, 0:, 0, 1).binIntsToLong
   39
   $: List(0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 1).binIntsToBigInt
   1302309988


    
.. code-block:: scala

   $: List(1, 1, 1, 0, 0, 1).binIntsToHex
   27
   $: List(1, 1, 1, 0, 0, 1).binIntsToHexAlignHigh
   9c
   $: List(1, 1, 1, 0, 0, 1).binIntsToOct
   47
   $: List(1, 1, 1, 0, 0, 1).binIntsToHexAlignHigh
   47


BigInt enricher 
---------------

.. code-block:: scala

   $: 32.toBigInt
   32
   $: 3211323244L.toBigInt
   3211323244
   $: 8.toByte.toBigInt
   8
