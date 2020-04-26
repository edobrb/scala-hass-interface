import hass.controller.Hass
import hass.model.entity.{Light, Switch}
import hass.model.event.{SensorStateChangedEvent, SwitchStateChangedEvent}
import hass.model.state.{Off, On}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Test extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val hass: Hass = new Hass("192.168.1.10:8123")

  hass auth "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIwZjQ1NWUwMWNkNzE0NGFkYjVkZmJhZDJkZDM2YzBlMiIsImlhdCI6MTU4NjEwODM5NCwiZXhwIjoxOTAxNDY4Mzk0fQ.1a2dodNofYhygdCybk-6MA7No8MnZozC94UTG_sbBQA"

  val lampada_edo = Light()
  val irr_davanti = Switch()

  irr_davanti.toggle.onComplete(println)

  irr_davanti.onChange {
    case On => println("irr_davanti accesa!");
      Thread.sleep(1000)
      irr_davanti.turn(Off)
    case Off => println("irr_davanti spenta!")
    case _ => println("irr_davanti in uno stato sconosciuto!")
  }
  //val res = lampada_edo.toggle

  hass.onEvent {
    //case UnknownEvent(jsValue, timeFired, origin) => println("Unknown: " + jsValue)
    case SwitchStateChangedEvent(entity_id, oldState, newState, timeFired, origin) => println(s"${newState.entity_name} -> ${newState.state}")
    case SensorStateChangedEvent("consumo_casa", oldState, newState, timeFired, origin) => println(s"${newState.entity_name} = ${newState.state} (${newState.lastUpdated})")
    case SensorStateChangedEvent("consumo_garage", oldState, newState, timeFired, origin) => println(s"${newState.entity_name} = ${newState.state} (${newState.lastUpdated})")
    case v => //println(v)
  }

}
