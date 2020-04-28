package hass.model.entity


import hass.controller.Hass
import hass.model.common.Observable
import hass.model.event.StateChangedEvent
import hass.model.service.Result
import hass.model.state.{EntityState, Off, On, TurnState}

import scala.concurrent.Future

trait MetaEntity {
  def domain: String
}

sealed trait Entity {
  def meta: MetaEntity

  def entity_name: String

  def entity_domain: String = meta.domain

  def entity_id: String = s"$entity_domain.$entity_name"
}

abstract class StatefulEntity[S, E <: EntityState[S]]()(implicit hass: Hass) extends Entity with Observable[E] {
  private var _state: Option[E] = hass.stateOf(entity_id)

  def state: Option[E] = _state

  def onStateValueChange(f: PartialFunction[S, Unit]): Unit = addObserver({
    case entityState: E if f.isDefinedAt(entityState.state) => f(entityState.state)
  })

  def onStateChange(f: PartialFunction[E, Unit]): Unit = addObserver(f)

  hass.onEvent {
    case StateChangedEvent(id, _, newState: E, _, _) if id == entity_id =>
      _state = Some(newState)
      notifyObservers(newState)
  }
}

trait TurnableEntity extends Entity {
  def turnOn: Future[Result]

  def turnOff: Future[Result]

  def turn(state: TurnState): Future[Result] = state match {
    case On => turnOn
    case Off => turnOff
  }

  def toggle: Future[Result]
}

case class UnknownEntity(entity_name: String, meta: MetaEntity) extends Entity