package hass.model.event

import com.github.nscala_time.time.Imports.DateTime
import hass.model.service.{LightTurnService, Service, SwitchTurnService}

import scala.reflect.ClassTag

abstract class MetaServiceCallEvent[S <: Service : ClassTag]() {
  private val clazz = implicitly[ClassTag[S]].runtimeClass

  def unapply(event: Event): Option[(S, DateTime, String)] = event match {
    case ServiceCallEvent(service: S, timeFired, origin)
      if clazz.isInstance(service) => Some(service, timeFired, origin)
    case _ => None
  }
}

object LightTurnServiceCallEvent extends MetaServiceCallEvent[LightTurnService]

object SwitchTurnServiceCallEvent extends MetaServiceCallEvent[SwitchTurnService]
