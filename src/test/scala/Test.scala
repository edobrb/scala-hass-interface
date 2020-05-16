import hass.controller.Hass
import hass.model.entity._
import hass.model.event._
import hass.model.service.LightTurnService
import hass.model.state.On
import org.joda.time.DateTime

import scala.util.Try

object Test extends App {

  def read(fileName: String): Try[String] = {
    Try(scala.io.Source.fromFile(fileName)).map(source => {
      val result = Try(source.mkString)
      source.close()
      result
    }).flatten
  }

  // implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  var conn = "192.168.1.10:8123"
  implicit val hass: Hass = Hass(conn, read("C:\\Users\\Edo\\Desktop\\jwt.txt").getOrElse(""), retryOnError = true)

  val lampada_edo = Light()
  val irr_davanti = Switch()
  val luce_pc_edo = Switch()
  val luce_letto_edo = Switch()
  val consumo_garage = Sensor()
  val irrigazione_davanti_martedi = InputBoolean()
  val irrigazione_davanti_inizio = InputDateTime()

  hass.onConnection { () =>
    lampada_edo.turnOn(_.kelvin(44))
    irrigazione_davanti_martedi.turnOn()
    irrigazione_davanti_inizio.setTime(DateTime.now().toLocalTime)
  }
  hass.onClose(() => println("Closed"))
  lampada_edo.onState {
    case (On, time, state) if state.brightness.exists(_ < 255) =>
      lampada_edo.turnOn(_.brightness(255))
  }
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

  val turnOnAllMyLight = LightTurnService(Seq("my_lamp", "my_other_lamp"), On).brightness(255)
  hass call turnOnAllMyLight
lampada_edo.toggle()
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
