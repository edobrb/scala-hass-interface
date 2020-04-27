package hass.model.entity



import hass.model.service.result.Result
import hass.model.state.{Off, On, TurnState, Unavailable}

import scala.concurrent.Future

sealed trait Entity {
  def entity_name: String

  def entity_domain: String

  def entity_id: String = s"$entity_domain.$entity_name"
}

trait TurnableEntity extends Entity {
  def turnOn: Future[Result]

  def turnOff: Future[Result]

  def turn(state: TurnState): Future[Result] = state match {
    case On => turnOn
    case Off => turnOff
  }

  def toggle: Future[Result]

  def onTurnStateChange(f: PartialFunction[TurnState, Unit]): Unit
}

case class UnknownEntity(entity_name: String, entity_domain: String) extends Entity