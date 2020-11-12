package hass.model.entity


import com.github.nscala_time.time.Imports.DateTime
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

abstract class StatefulEntity[S, E <: EntityState[S] : ClassTag]()(implicit hass: Hass) extends Entity with Observable[(DateTime, E, E)] {

  hass onEvent {
    case StateChangedEvent(id, oldState: E, newState: E, _, _) if implicitly[ClassTag[E]].runtimeClass.isInstance(newState) && id == entityId =>
      notifyObservers((newState.lastChanged, oldState, newState))
  }

  def state: Option[E] = hass.stateOf[S, E](entityId)

  def value: Option[S] = state.map(_.value)

  def lastChanged: Option[DateTime] = state.map(_.lastChanged)

  def lastUpdated: Option[DateTime] = state.map(_.lastUpdated)

  def onValue(f: PartialFunction[(DateTime, S), Unit]): Unit = addObserver({
    case (time, _, newState) if f.isDefinedAt((time, newState.value)) => f((time, newState.value))
  })

  def onValueChange(f: PartialFunction[(DateTime, S, S), Unit]): Unit = addObserver({
    case (time, oldState, newState) if f.isDefinedAt((time, oldState.value, newState.value)) => f((time, oldState.value, newState.value))
  })

  def onState(f: PartialFunction[(DateTime, E), Unit]): Unit = addObserver({
    case (time, _, newState) if f.isDefinedAt((time, newState)) => f((time, newState))
  })

  def onStateChange(f: PartialFunction[(DateTime, E, E), Unit]): Unit = addObserver(f)
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
