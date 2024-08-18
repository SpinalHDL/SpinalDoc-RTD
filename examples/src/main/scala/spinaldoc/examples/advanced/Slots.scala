package spinaldoc.examples.advanced

import spinal.core._
import spinal.lib._
import scala.language.postfixOps

case class SlotsDemo(slotsCount : Int) extends Component {
  // ...
  
  
  // Create the hardware for each slot
  // Note each slot is an Area, not a Bundle
  val slots = for(i <- 0 until slotsCount) yield new Area {
    // Because the slot is an Area, we can define mix signal, registers, logic definitions
    // Here are the registers for each slots
    val valid = RegInit(False)
    val address = Reg(UInt(8 bits))
    val age = Reg(UInt(16 bits)) // Will count since how many cycles the slot is valid

    // Here is some hardware behavior for each slots
    // Implement the age logic
    when(valid) {
      age := age + 1
    }

    // removeIt will be used as a slot interface later on
    val removeIt = False
    when(removeIt) {
      valid := False
    }
  }

  // Logic to allocate a new slot
  val insert = new Area {
    val cmd = Stream(UInt(8 bits)) // interface to issue requests
    val free = slots.map(!_.valid)
    val freeOh = OHMasking.first(free) // Get the first free slot (on hot mask)
    cmd.ready := free.orR // Only allow cmd when there is a free slot
    when(cmd.fire) {
      // slots.onMask(freeOh)(code) will execute the code for each slot where the corresponding freeOh bit is set
      slots.onMask(freeOh){slot =>
        slot.valid := True
        slot.address := cmd.payload
        slot.age := 0
      }
    }
  }

  // Logic to remove the slots which match a given address (assuming there is not more than one match)
  val remove = new Area {
    val cmd = Flow(UInt(8 bits)) // interface to issue requests
    val oh = slots.map(s => s.valid && s.address === cmd.payload) // oh meaning "one hot"
    when(cmd.fire) {
      slots.onMask(oh){ slot =>
        slot.removeIt := True
      }
    }

    val reader = slots.reader(oh) // Create a facility to read the slots using "oh" as index
    val age = reader(_.age) // Age of the slot which is selected by "oh"
  }
  
  // ...
}

object SlotsDemo extends App {
  SpinalVerilog(SlotsDemo(4))
}
