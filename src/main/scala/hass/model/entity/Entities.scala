package hass.model.entity


import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.common.Observable
import hass.model.event.StateChangedEvent
import hass.model.service.Result
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

trait TurnableEntity extends Entity {
  def turnOn: Future[Result]

  def turnOff: Future[Result]

  def turn(state: TurnState): Future[Result] = state match {
    case On => turnOn
    case Off => turnOff
    case Unavailable => Future(Result(success = false, None))(ExecutionContext.global)
  }

  def toggle: Future[Result]
}

case class UnknownEntity(entity_name: String, override val domain: String) extends Entity