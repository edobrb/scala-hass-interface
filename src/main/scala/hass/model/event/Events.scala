package hass.model.event

import com.github.nscala_time.time.Imports.DateTime
import hass.model.service.{LightTurnOnService, Service}
import hass.model.state.{EntityState, SensorState, SwitchState}
import play.api.libs.json.{JsObject, JsValue}

sealed trait Event {
  def timeFired: DateTime

  def origin: String
}


case class UnknownEvent(jsValue: JsValue, timeFired: DateTime, origin: String) extends Event

case class StateChangedEvent[S <: EntityState[_]](entity_id: String, oldState: S, newState: S, timeFired: DateTime, origin: String) extends Event

object SwitchStateChangedEvent {
  def unapply(event: Event): Option[(String, SwitchState, SwitchState, DateTime, String)] = event match {
    case StateChangedEvent(entity_id, oldState: SwitchState, newState: SwitchState, timeFired, origin) => Some(entity_id.split('.')(1), oldState, newState, timeFired, origin)
    case _ => None
  }
}

object SensorStateChangedEvent {
  def unapply(event: Event): Option[(String, SensorState, SensorState, DateTime, String)] = event match {
    case StateChangedEvent(entity_id, oldState: SensorState, newState: SensorState, timeFired, origin) => Some(entity_id.split('.')(1), oldState, newState, timeFired, origin)
    case _ => None
  }
}

case class ServiceCallEvent(service:Service, timeFired: DateTime, origin: String) extends Event

object LightTurnOnServiceCallEvent {
  def unapply(event: Event): Option[(LightTurnOnService, DateTime, String)] = event match {
    case ServiceCallEvent(service:LightTurnOnService, timeFired, origin) => Some(service, timeFired, origin)
    case _ => None
  }
}