
SpinalHDL internal datamodel
===================================

.. role:: raw-html-m2r(raw)
   :format: html


Introduction
------------------------------------------------

This page document the internal data structure user by SpinalHDL to store and modify the netlist described by the user through the SpinalHDL API.

General structure
------------------------

The following diagrams follow the UML nomenclature : 

- A link with a white arrow mean "base extend target"
- A link with a black diamond mean "base contains target"
- A link with a white diamond mean "base has a reference to target"
- The * symbole mean "multiple"

Most of the data structure is stored via some double linked list to ease the insertion and the removal of elements.

There is a diagram of the global data structure : 

.. image:: /asset/picture/astGlobal.png
   :align: center
   
And here more details about the Statement class : 

.. image:: /asset/picture/astStatement.png
   :align: center

So in general, if a element of the datamodel use some other Expression or Statements, that element will have some functions to iterate over those usages. For instance, each Expression has a `foreachExpression` function.

When using those iterating functions, you are allowed to remove the current element of the tree.

Also asside the foreachXXX, which "only" iterate one level deep, there is often a walkXXX which will iterate recursively. So for instance myExpression.walkExpression on ((a+b)+c)+d will go through the whole tree of adders.

There is also utilities as myExpression.remapExpressions(Expression => Expression) which will iterate over all used expression of myExpression, and change it for your returned one.

More generaly, most of the graph checks and transformations done by SpinalHDL are located in <https://github.com/SpinalHDL/SpinalHDL/blob/dev/core/src/main/scala/spinal/core/internals/Phase.scala> 

Exploring the datamodel
-------------------------------

Here is an example which find all adders of the netlist without using "shortcuts" : 

.. code-block:: scala

    object FindAllAddersManualy {
      class Toplevel extends Component{
        val a,b,c = in UInt(8 bits)
        val result = out(a + b + c)
      }

      import spinal.core.internals._

      class PrintBaseTypes(message : String) extends Phase{
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

        //Add a early phase
        config.addTransformationPhase(new PrintBaseTypes("Early"))

        //Add a late phase
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
    
Note that in many case, there is shortcuts. All the recursive stuff above could have been remplaced by a single : 

.. code-block:: scala

    override def impl(pc: PhaseContext) = {
      println(message)
      pc.walkExpression{
        case op: Operator.BitVector.Add => println(s"Found ${op.left} + ${op.right}")
        case _ =>
      }
    }


Compilation Phases
-------------------------------

Here are all the default phases (in order) used to modify / check / generate verilog from a toplevel component : 

<https://github.com/SpinalHDL/SpinalHDL/blob/ec8cd9f513566b43cbbdb08d0df4dee1f0fee655/core/src/main/scala/spinal/core/internals/Phase.scala#L2487>

If as a use you add a new compilation phase using  SpinalConfig.addTransformationPhase(new MyPhase()), then the phase will be added directly after the user component elaboration (so quite early). At that time, you can still use the whole SpinalHDL user API to add elements into the netlist.

If you use the SpinalConfig.phasesInserters api, then you will have to be carefull to only modify the netlist in a way which is compatible with the phases which were already executed. For instance, if you insert you phase after the `PhaseInferWidth`, then you have to specify the width of each nodes you insert.

Modifying a netlist as a user without plugins
--------------------------------------------------------------

There is quite a few user API which allow to modify things durring the user elaboration time :

- mySignal.removeAssignments : Will remove all previous `:=` affecting the given signal
- mySignal.removeStatement : Will void the existance of the signal
- mySignal.setAsDirectionLess : Will turn a in / out signal into a internal signal
- mySignal.setName : Enforce a given name on a signal (there is many other variants)
- mySubComponent.mySignal.pull() : Will provide a readable copy of the given signal, even if that signal is somewhere else in the hierarchy
- myComponent.rework\{ myCode \} : Execute `myCode` in the context of `myComponent`, allowing modifying it with the user API

For instance, the following code will rework a toplevel component to insert a 3 stages shift register on each input / output of the component. (Usefull for synthesis tests)

.. code-block:: scala

  def ffIo[T <: Component](c : T): T ={
    def buf1[T <: Data](that : T) = KeepAttribute(RegNext(that)).addAttribute("DONT_TOUCH")
    def buf[T <: Data](that : T) = buf1(buf1(buf1(that)))
    c.rework{
      val ios = c.getAllIo.toList
      ios.foreach{io =>
        if(io.getName() == "clk"){
          //Do nothing
        } else if(io.isInput){
          io.setAsDirectionLess().allowDirectionLessIo  //allowDirectionLessIo is to disable the io Bundle linting
          io := buf(in(cloneOf(io).setName(io.getName() + "_wrap")))
        } else if(io.isOutput){
          io.setAsDirectionLess().allowDirectionLessIo
          out(cloneOf(io).setName(io.getName() + "_wrap")) := buf(io)
        } else ???
      }
    }
    c
  }
  
Which can be used the following way : 
  
.. code-block:: scala

  SpinalVerilog(ffIo(new MyToplevel))
  
User space netlist analysis
--------------------------------------------------------------

The SpinalHDL datamodel is also readable during usertime elaboration. Here is is an example which will find the shortest logical path (in therms of clock cycles) to travel through a list of signals. In the given case, it is to analyse the latency of the VexRiscv FPU design.

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
