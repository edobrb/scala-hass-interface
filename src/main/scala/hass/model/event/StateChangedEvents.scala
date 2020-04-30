package hass.model.event

import com.github.nscala_time.time.Imports.DateTime
import hass.model.state.{EntityState, LightState, SensorState, SwitchState}

import scala.reflect.ClassTag

abstract class MetaStateChangedEvent[S <: EntityState[_] : ClassTag]() {
  private val clazz = implicitly[ClassTag[S]].runtimeClass

  def unapply(event: Event): Option[(String, S, S, DateTime, String)] = event match {
    case StateChangedEvent(entity_id, oldState: S, newState: S, timeFired, origin)
      if clazz.isInstance(oldState) && clazz.isInstance(newState) =>
      Some(entity_id.split('.')(1), oldState, newState, timeFired, origin)
    case _ => None
  }
}

object SwitchStateChangedEvent extends MetaStateChangedEvent[SwitchState]()

object LightStateChangedEvent extends MetaStateChangedEvent[LightState]()

object SensorStateChangedEvent extends MetaStateChangedEvent[SensorState]()