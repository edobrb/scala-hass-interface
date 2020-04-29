import hass.controller.Hass
import hass.model.entity.{InputDateTime, Light, Sensor, Switch}
import hass.model.event.{LightTurnOnServiceCallEvent, ServiceCallEvent, UnknownEvent}
import hass.model.group.{LightsGroup, SwitchesGroup}
import hass.model.service.{LightTurnOnService, SwitchTurnOffService, SwitchTurnOnService}
import hass.model.state._

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
  consumo_garage.onStateValueChange({
    case s =>println(s)
  })

  val prova_data_tempo = InputDateTime()
  prova_data_tempo.onStateValueChange {
    case Left(value) => println(value)
  }

  lampada_edo.turnOn(_.brightness(255).rgb(255,0,0).transition(1))
  SwitchesGroup(Seq(luce_pc_edo, luce_letto_edo)).toggle()

  lampada_edo.onStateChange {
    case v => println(v)
  }
  //irr_davanti.toggle.onComplete(println)

  hass.onEvent {
    /*case UnknownEvent(jsValue, timeFired, origin) => println("Unknown: " + jsValue)
    //case SwitchStateChangedEvent(entity_id, oldState, newState, timeFired, origin) => println(s"${newState.entity_name} -> ${newState.state}")
    //case SensorStateChangedEvent("consumo_casa", oldState, newState, timeFired, origin) => println(s"${newState.entity_name} = ${newState.state} (${newState.lastUpdated})")
    //case SensorStateChangedEvent("consumo_garage", oldState, newState, timeFired, origin) => println(s"${newState.entity_name} = ${newState.state} (${newState.lastUpdated})")

    case LightTurnOnServiceCallEvent(service, _, _) => println(service)
    */

    case a:ServiceCallEvent => println(a)
    //case e => println(e)
  }

  /*hass.onStateChange {
    case UnknownEntityState(entity_id, state, lastChanged, lastUpdated, attributes) => println("Unknown: " + entity_id + " " + state)
    case InputBooleanState(entity_name, state, lastChanged, lastUpdated, attributes) =>  println(s"${entity_name} = ${state} (${lastUpdated}) ($lastChanged)")
    case l: LightState =>  println(s"${l.entity_name} = ${l.state} (${l.brightness})")
    case l:InputDateTimeState => println(s"${l.entity_name} = ${l.state} (${l.lastChanged}) (${l.hasDate}, ${l.hasTime})")
    //case EntityState(entity_id, state, lastChanged, lastUpdated, attributes) => println("Generic state: " + entity_id + " " + state)
  }*/

}
