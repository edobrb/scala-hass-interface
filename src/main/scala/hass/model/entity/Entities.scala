package hass.model.entity


import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.common.Observable
import hass.model.event.StateChangedEvent
import hass.model.service.{Result, Service, TurnService}
import hass.model.state._

import scala.concurrent.Future
import scala.reflect.ClassTag


sealed trait Entity extends MetaDomain {
  def entity_name: String

  def entity_id: String = s"$domain.$entity_name"
}

case class UnknownEntity(entity_name: String, override val domain: String) extends Entity

abstract class StatefulEntity[S, E <: EntityState[S] : ClassTag]()(implicit hass: Hass) extends Entity with Observable[E] {
  private var _state: Option[E] = hass.stateOf(entity_id)

  hass onEvent {
    case StateChangedEvent(id, _, newState: E, _, _) if implicitly[ClassTag[E]].runtimeClass.isInstance(newState) && id == entity_id =>
      _state = Some(newState)
      notifyObservers(newState)
  }

  def state: Option[E] = _state

  def onStateValueChange(f: PartialFunction[S, Unit]): Unit = addObserver({
    case entityState if f.isDefinedAt(entityState.state) => f(entityState.state)
  })

  def onStateChange(f: PartialFunction[E, Unit]): Unit = addObserver(f)
}

trait Turnable[S <: TurnService] {
  def service(turn:TurnAction): S

  def hass: Hass

  def turnOn(f: S => S = identity): Future[Result] = hass call f(service(On))

  /**
   * Turns off the entity by calling S service.
   *
   * @param f will be applied to the default S. Ex: turnOff(_.transition(2))
   * @return the future result of the service call
   */
  def turnOff(f: S => S = identity): Future[Result] = hass call f(service(Off))

  def toggle(f: S => S = identity): Future[Result] = hass call f(service(Toggle))

  def turn(state: TurnAction): Future[Result] = state match {
    case On => turnOn()
    case Off => turnOff()
    case Toggle => toggle()
  }
}
