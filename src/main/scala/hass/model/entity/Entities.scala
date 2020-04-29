package hass.model.entity


import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.common.Observable
import hass.model.event.StateChangedEvent
import hass.model.service.{Result, Service}
import hass.model.state._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag


sealed trait Entity extends MetaDomain {
  def entity_name: String

  def entity_id: String = s"$domain.$entity_name"
}

abstract class StatefulEntity[S, E <: EntityState[S] : ClassTag]()(implicit hass: Hass) extends Entity with Observable[E] {
  private var _state: Option[E] = hass.stateOf(entity_id)

  def state: Option[E] = _state

  def onStateValueChange(f: PartialFunction[S, Unit]): Unit = addObserver({
    case entityState if f.isDefinedAt(entityState.state) => f(entityState.state)
  })

  def onStateChange(f: PartialFunction[E, Unit]): Unit = addObserver(f)

  hass.onEvent {
    case StateChangedEvent(id, _, newState: E, _, _) if implicitly[ClassTag[E]].runtimeClass.isInstance(newState) && id == entity_id =>
      _state = Some(newState)
      notifyObservers(newState)
  }
}

trait Turnable[On <: Service, Off <: Service, Toggle <: Service] {
  def onService: On

  def offService: Off

  def toggleService: Toggle

  def hass: Hass


  def turnOn(f: On => On = identity): Future[Result] = hass call f(onService)

  /**
   * Turns off the light by calling LightTurnOffService.
   * @param f will be applied to the default LightTurnOffService. Ex: turnOff(_.transition(2))
   * @return the future result of the service call
   */
  def turnOff(f: Off => Off = identity): Future[Result] = hass call f(offService)

  def turn(state: TurnState): Future[Result] = state match {
    case On => turnOn()
    case Off => turnOff()
    case Unavailable => Future(Result(success = false, None))(ExecutionContext.global)
  }

  def toggle(f: Toggle => Toggle = identity): Future[Result] = hass call f(toggleService)
}

case class UnknownEntity(entity_name: String, override val domain: String) extends Entity