package hass.model.entity


import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.common.Observable
import hass.model.event.StateChangedEvent
import hass.model.service.{Result, TurnService}
import hass.model.state._
import hass.model.state.ground.{Off, On, Toggle, TurnAction}
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.reflect.ClassTag


sealed trait Entity extends MetaDomain {
  def entityName: String

  def entityId: String = s"$domain.$entityName"
}

case class UnknownEntity(entityName: String, override val domain: String) extends Entity

abstract class StatefulEntity[S, E <: EntityState[S] : ClassTag]()(implicit hass: Hass) extends Entity with Observable[(S, DateTime, E)] {

  hass onEvent {
    case StateChangedEvent(id, _, newState: E, _, _) if implicitly[ClassTag[E]].runtimeClass.isInstance(newState) && id == entityId =>
      notifyObservers((newState.state, newState.lastChanged, newState))
  }

  def state: Option[E] = hass.stateOf[E](entityId)

  def onState(f: PartialFunction[(S, DateTime, E), Unit]): Unit = addObserver(f)
}

trait Turnable[S <: TurnService] {
  def service(turn: TurnAction): S

  def hass: Hass

  def turn(state: TurnAction): Future[Result] = state match {
    case On => turnOn()
    case Off => turnOff()
    case Toggle => toggle()
  }

  def turnOn(f: S => S = identity): Future[Result] = hass call f(service(On))

  /**
   * Turns off the entity by calling S service.
   *
   * @param f will be applied to the default S. Ex: turnOff(_.transition(2))
   * @return the future result of the service call
   */
  def turnOff(f: S => S = identity): Future[Result] = hass call f(service(Off))

  def toggle(f: S => S = identity): Future[Result] = hass call f(service(Toggle))
}
