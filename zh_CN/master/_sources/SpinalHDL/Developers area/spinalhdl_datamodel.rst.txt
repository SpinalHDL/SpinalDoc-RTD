
SpinalHDL internal datamodel
============================

.. role:: raw-html-m2r(raw)
   :format: html


Introduction
------------

This page provides documentation on the internal data structure utilized by SpinalHDL for storing and modifying the netlist described by users via the SpinalHDL API.

General structure
-----------------

The following diagrams follow the UML nomenclature : 

- A link with a white arrow mean "base extend target"
- A link with a black diamond mean "base contains target"
- A link with a white diamond mean "base has a reference to target"
- The * symbol mean "multiple"

The majority of the data structures are stored using double-linked lists, which facilitate the insertion and removal of elements.

There is a diagram of the global data structure : 

.. image:: /asset/picture/astGlobal.png
   :align: center
   
And here more details about the `Statement` class : 

.. image:: /asset/picture/astStatement.png
   :align: center

In general, when an element within the data model utilizes other expressions or statements, that element typically includes functions for iterating over these usages. For example, each Expression is equipped with a *foreachExpression* function.

When using these iteration functions, you have the option to remove the current element from the tree.

Additionally, as a side note, while the *foreachXXX* functions iterate only one level deep, there are often corresponding *walkXXX* functions that perform recursive iteration. For instance, using *myExpression.walkExpression* on *((a+b)+c)+d* will traverse the entire tree of addition operations.

There are also utilities like *myExpression.remapExpressions(Expression => Expression),* which iterate through all the expressions used within *myExpression* and replace them with the one you provide.

More generally, most of the graph checks and transformations done by SpinalHDL are located in <https://github.com/SpinalHDL/SpinalHDL/blob/dev/core/src/main/scala/spinal/core/internals/Phase.scala> 

Exploring the datamodel
-----------------------

Here is an example that identifies all adders within the netlist without utilizing shortcuts. : 

.. code-block:: scala

    object FindAllAddersManually {
      class Toplevel extends Component {
        val a,b,c = in UInt(8 bits)
        val result = out(a + b + c)
      }

      import spinal.core.internals._

      class PrintBaseTypes(message : String) extends Phase {
        override def impl(pc: PhaseContext) = {
          println(message)

          recComponent(pc.topLevel)

          def recComponent(c: Component): Unit = {
            c.children.foreach(recComponent)
            c.dslBody.foreachStatements(recStatement)
          }

          def recStatement(s: Statement): Unit = {
            s.foreachExpression(recExpression)
            s match {
              case ts: TreeStatement => ts.foreachStatements(recStatement)
              case _ =>
            }
          }

          def recExpression(e: Expression): Unit = {
            e match {
              case op: Operator.BitVector.Add => println(s"Found ${op.left} + ${op.right}")
              case _ =>
            }
            e.foreachExpression(recExpression)
          }

        }
        override def hasNetlistImpact = false

        override def toString = s"${super.toString} - $message"
      }

      def main(args: Array[String]): Unit = {
        val config = SpinalConfig()

        // Add a early phase
        config.addTransformationPhase(new PrintBaseTypes("Early"))

        // Add a late phase
        config.phasesInserters += {phases =>
          phases.insert(phases.indexWhere(_.isInstanceOf[PhaseVerilog]), new PrintBaseTypes("Late"))
        }
        config.generateVerilog(new Toplevel())
      }
    }
    
    
Which will produces :

.. code-block:: 

    [Runtime] SpinalHDL v1.6.1    git head : 3100c81b37a04715d05d9b9873c3df07a0786a9b
    [Runtime] JVM max memory : 8044.0MiB
    [Runtime] Current date : 2021.10.16 20:31:33
    [Progress] at 0.000 : Elaborate components
    [Progress] at 0.163 : Checks and transforms
    Early
    Found (toplevel/a : in UInt[8 bits]) + (toplevel/b : in UInt[8 bits])
    Found (toplevel/??? :  UInt[? bits]) + (toplevel/c : in UInt[8 bits])
    [Progress] at 0.191 : Generate Verilog
    Late
    Found (UInt + UInt)[8 bits] + (toplevel/c : in UInt[8 bits])
    Found (toplevel/a : in UInt[8 bits]) + (toplevel/b : in UInt[8 bits])
    [Done] at 0.218    
    
Please note that in many cases, shortcuts are available. All the recursive processes mentioned earlier could have been replaced by a single one. : 

.. code-block:: scala

    override def impl(pc: PhaseContext) = {
      println(message)
      pc.walkExpression {
        case op: Operator.BitVector.Add => println(s"Found ${op.left} + ${op.right}")
        case _ =>
      }
    }


Compilation Phases
------------------

Here is the complete list of default phases, arranged in order, that are employed to modify, check, and generate Verilog code from a top-level component. : 

<https://github.com/SpinalHDL/SpinalHDL/blob/ec8cd9f513566b43cbbdb08d0df4dee1f0fee655/core/src/main/scala/spinal/core/internals/Phase.scala#L2487>

If you, as a user, add a new compilation phase by using *SpinalConfig.addTransformationPhase(new MyPhase())*, this phase will be inserted immediately after the user component elaboration process, which is relatively early in the compilation sequence. During this phase, you can still make use of the complete SpinalHDL user API to introduce elements into the netlist.

If you choose to use the SpinalConfig.phasesInserters API, it's essential to exercise caution and ensure that any modifications made to the netlist align with the phases that have already been executed. For instance, if you insert your phase after the *PhaseInferWidth*, you must specify the width of each node you introduce.

Modifying a netlist as a user without plugins
---------------------------------------------

There are several user APIs that enable you to make modifications during the user elaboration phase. :

- mySignal.removeAssignments : Will remove all previous `:=` affecting the given signal
- mySignal.removeStatement : Will void the existence of the signal
- mySignal.setAsDirectionLess : Will turn a in / out signal into a internal signal
- mySignal.setName : Enforce a given name on a signal (there is many other variants)
- mySubComponent.mySignal.pull() : Will provide a readable copy of the given signal, even if that signal is somewhere else in the hierarchy
- myComponent.rework\{ myCode \} : Execute `myCode` in the context of `myComponent`, allowing modifying it with the user API

For example, the following code can be used to modify a top-level component by adding a three-stage shift register to each input and output of the component. This is particularly useful for synthesis testing.

.. code-block:: scala

  def ffIo[T <: Component](c : T): T = {
    def buf1[T <: Data](that : T) = KeepAttribute(RegNext(that)).addAttribute("DONT_TOUCH")
    def buf[T <: Data](that : T) = buf1(buf1(buf1(that)))
    c.rework {
      val ios = c.getAllIo.toList
      ios.foreach{io =>
        if(io.getName() == "clk") {
          // Do nothing
        } else if(io.isInput) {
          io.setAsDirectionLess().allowDirectionLessIo  // allowDirectionLessIo is to disable the io Bundle linting
          io := buf(in(cloneOf(io).setName(io.getName() + "_wrap")))
        } else if(io.isOutput) {
          io.setAsDirectionLess().allowDirectionLessIo
          out(cloneOf(io).setName(io.getName() + "_wrap")) := buf(io)
        } else ???
      }
    }
    c
  }
  
You can use the code in the following manner: : 
  
.. code-block:: scala

  SpinalVerilog(ffIo(new MyToplevel))
  
Here is a function that enables you to execute the body code as if the current component's context did not exist. This can be particularly useful for defining new signals without the influence of the current conditional scope (such as when or switch).

.. code-block:: scala
  
  def atBeginingOfCurrentComponent[T](body : => T) : T = {
    val body = Component.current.dslBody  // Get the head of the current component symbols tree (AST in other words)
    val ctx = body.push()                 // Now all access to the SpinalHDL API will be append to it (instead of the current context)
    val swapContext = body.swap()         // Empty the symbol tree (but keep a reference to the old content)
    val ret = that                        // Execute the block of code (will be added to the recently empty body)
    ctx.restore()                         // Restore the original context in which this function was called
    swapContext.appendBack()              // append the original symbols tree to the modified body
    ret                                   // return the value returned by that
  }
  
  val database = mutable.HashMap[Any, Bool]()
  def get(key : Any) : Bool = {
    database.getOrElseUpdate(key, atBeginingOfCurrentComponent(False)
  }
  
  object key
  
  when(something) {
    if(somehow) {
      get(key) := True
    }
  }  
  when(database(key)) {
     ...
  }
  
This kind of functionality is, for instance, employed in the VexRiscv pipeline to dynamically create components or elements as needed.  
  
User space netlist analysis
--------------------------------------------------------------

The SpinalHDL data model is also accessible and can be read during user-time elaboration. Here's an example that can help find the shortest logical path (in terms of clock cycles) to traverse a list of signals. In this specific case, it is being used to analyze the latency of the VexRiscv FPU design.

.. code-block:: scala

    println("cpuDecode to fpuDispatch " + LatencyAnalysis(vex.decode.arbitration.isValid, logic.decode.input.valid))
    println("fpuDispatch to cpuRsp    " + LatencyAnalysis(logic.decode.input.valid, plugin.port.rsp.valid))

    println("cpuWriteback to fpuAdd   " + LatencyAnalysis(vex.writeBack.input(plugin.FPU_COMMIT), logic.commitLogic(0).add.counter))

    println("add                      " + LatencyAnalysis(logic.decode.add.rs1.mantissa, logic.get.merge.arbitrated.value.mantissa))
    println("mul                      " + LatencyAnalysis(logic.decode.mul.rs1.mantissa, logic.get.merge.arbitrated.value.mantissa))
    println("fma                      " + LatencyAnalysis(logic.decode.mul.rs1.mantissa, logic.get.decode.add.rs1.mantissa, logic.get.merge.arbitrated.value.mantissa))
    println("short                    " + LatencyAnalysis(logic.decode.shortPip.rs1.mantissa, logic.get.merge.arbitrated.value.mantissa))

Here you can find the implementation of that LatencyAnalysis tool : 
<https://github.com/SpinalHDL/SpinalHDL/blob/3b87c898cb94dc08456b4fe2b1e8b145e6c86f63/lib/src/main/scala/spinal/lib/Utils.scala#L620>


Enumerating every ClockDomain in use
----------------------------------------------------

In this case, this is accomplished after the elaboration process by utilizing the SpinalHDL report.

.. code-block:: scala


    object MyTopLevelVerilog extends App {
      class MyTopLevel extends Component {
        val cdA = ClockDomain.external("rawrr")
        val regA = cdA(RegNext(False))

        val sub = new Component {
          val cdB = ClockDomain.external("miaou")
          val regB = cdB(RegNext(False))

          val clkC = CombInit(regB)
          val cdC = ClockDomain(clkC)
          val regC = cdC(RegNext(False))
        }
      }

      val report = SpinalVerilog(new MyTopLevel)

      val clockDomains = mutable.LinkedHashSet[ClockDomain]()
      report.toplevel.walkComponents(c =>
        c.dslBody.walkStatements(s =>
          s.foreachClockDomain(cd =>
            clockDomains += cd
          )
        )
      )

      println("ClockDomains : " + clockDomains.mkString(", "))
      val externals = clockDomains.filter(_.clock.component == null)
      println("Externals : " + externals.mkString(", "))
    }
    
Will print out 

.. code-block:: 

    ClockDomains : rawrr_clk, miaou_clk, clkC
    Externals : rawrr_clk, miaou_clk

