import hass.controller.Hass
import hass.model.entity.{Light, Sensor, Switch}
import hass.model.event._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.Try

object Test extends App {

  def read(fileName: String): Try[String] = {
    Try(scala.io.Source.fromFile(fileName)).map(source => {
      val result = Try(source.mkString)
      source.close()
      result
    }).flatten
  }

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  var conn = "192.168.1.10:8123"
  implicit val hass: Hass = new Hass(conn, read("C:\\Users\\Edo\\Desktop\\jwt.txt").getOrElse(""), true)

  val lampada_edo = Light()
  val irr_davanti = Switch()
  val luce_pc_edo = Switch()
  val luce_letto_edo = Switch()
  val consumo_garage = Sensor()

  hass.onConnection { () =>
    //lampada_edo.turnOn(_.rgb(0, 255, 0))
  }
  hass.onClose(() => {println("Closed"); conn = "192.168.1.10:8123"})

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

  System.in.read()
  hass.close()


  /*hass.onStateChange {
    case UnknownEntityState(entity_id, state, lastChanged, lastUpdated, attributes) => println("Unknown: " + entity_id + " " + state)
    case InputBooleanState(entity_name, state, lastChanged, lastUpdated, attributes) =>  println(s"${entity_name} = ${state} (${lastUpdated}) ($lastChanged)")
    case l: LightState =>  println(s"${l.entity_name} = ${l.state} (${l.brightness})")
    case l:InputDateTimeState => println(s"${l.entity_name} = ${l.state} (${l.lastChanged}) (${l.hasDate}, ${l.hasTime})")
    //case EntityState(entity_id, state, lastChanged, lastUpdated, attributes) => println("Generic state: " + entity_id + " " + state)
  }*/

}
