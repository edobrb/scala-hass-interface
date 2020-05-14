import hass.controller.Hass
import hass.model.entity.{Light, Sensor, Switch}
import hass.model.event._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Test extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val hass: Hass = new Hass("192.168.1.10:8123")

  hass auth "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIwZjQ1NWUwMWNkNzE0NGFkYjVkZmJhZDJkZDM2YzBlMiIsImlhdCI6MTU4NjEwODM5NCwiZXhwIjoxOTAxNDY4Mzk0fQ.1a2dodNofYhygdCybk-6MA7No8MnZozC94UTG_sbBQA"

  val lampada_edo = Light()
  val irr_davanti = Switch()
  val luce_pc_edo = Switch()
  val luce_letto_edo = Switch()
  val consumo_garage = Sensor()

  lampada_edo.turnOn(_.rgb(0,255,0))
  hass.onEvent {
    case SwitchStateChangedEvent(entityName, oldState, newState, timeFired, origin) => println(s"switch $entityName -> ${newState.state}")
    case LightStateChangedEvent(entityName, oldState, newState, timeFired, origin) => println(s"light $entityName -> ${newState.state}")
    case SensorStateChangedEvent(entityName, oldState, newState, timeFired, origin) => //println(s"sensor $entityName -> ${newState.state}")

    case LightTurnServiceCallEvent(service, fired, from) => println("Want " + service.turn + " lights: " + service.entityNames)
    case SwitchTurnServiceCallEvent(service, fired, from) => println("Want " + service.turn + " switches: " + service.entityNames)

    case a: StateChangedEvent[_] => println(a)
    case a: ServiceCallEvent => println(a)
    case a: UnknownEvent => println(a)
  }

  /*hass.onStateChange {
    case UnknownEntityState(entity_id, state, lastChanged, lastUpdated, attributes) => println("Unknown: " + entity_id + " " + state)
    case InputBooleanState(entity_name, state, lastChanged, lastUpdated, attributes) =>  println(s"${entity_name} = ${state} (${lastUpdated}) ($lastChanged)")
    case l: LightState =>  println(s"${l.entity_name} = ${l.state} (${l.brightness})")
    case l:InputDateTimeState => println(s"${l.entity_name} = ${l.state} (${l.lastChanged}) (${l.hasDate}, ${l.hasTime})")
    //case EntityState(entity_id, state, lastChanged, lastUpdated, attributes) => println("Generic state: " + entity_id + " " + state)
  }*/

}
