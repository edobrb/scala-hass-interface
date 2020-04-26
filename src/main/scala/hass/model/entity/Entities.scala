package hass.model.entity

import java.util.concurrent.Future

import hass.model.service.result.Result
import hass.model.state.{Off, On, TurnState}

sealed trait Entity {
  def entity_name: String

  def entity_domain: String

  def entity_id: String = s"$entity_domain.$entity_name"
}

trait TurnableEntity extends Entity {
  def turnOn: Future[Result] = turn(On)

  def turnOff: Future[Result] = turn(Off)

  def turn(state: TurnState): Future[Result]

  def toggle: Future[Result]
}

case class UnknownEntity(entity_name: String, entity_domain: String) extends Entity