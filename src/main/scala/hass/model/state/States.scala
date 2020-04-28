package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import org.joda.time.LocalTime
import play.api.libs.json.{JsObject, Reads}

import scala.util.Try

sealed trait EntityState[T] {
  def entity_name: String

  def entity_domain: String

  def entity_id: String = s"$entity_domain.$entity_name"

  def lastChanged: DateTime

  def lastUpdated: DateTime

  def state: T

  def attributes: Option[JsObject]

  def attribute[A: Reads](name: String): Option[A] =
    for (a <- attributes;
         jsValue <- a.fields.collectFirst { case (`name`, value) => value };
         value = jsValue.as[A]) yield value
}

case class UnknownEntityState(override val entity_id: String, state: String, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject]) extends EntityState[String] {
  override def entity_name: String = entity_id.split('.')(1)

  override def entity_domain: String = entity_id.split('.')(0)
}

object SwitchState {
  def unknown(entity_name: String): SwitchState = SwitchState(entity_name, Unavailable, new DateTime(0), new DateTime(0), None)
}

case class SwitchState(override val entity_name: String, state: TurnState, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject]) extends EntityState[TurnState] {
  override def entity_domain: String = "switch"
}

object LightState {
  def unknown(entity_name: String): LightState = LightState(entity_name, Unavailable, new DateTime(0), new DateTime(0), None)
}

case class LightState(override val entity_name: String, state: TurnState, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject]) extends EntityState[TurnState] {
  override def entity_domain: String = "light"

  def brightness: Option[Int] = attribute[Int]("brightness")
}

case class SensorState(override val entity_name: String, state: String, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject]) extends EntityState[String] {
  override def entity_domain: String = "sensor"

  private def tryState[T](r: => T): Option[T] = Try(r).toOption

  def numericState: Option[Double] = tryState(state.toDouble)

  def datetimeState: Option[DateTime] = tryState(DateTime.parse(state))

  def booleanState: Option[Boolean] = tryState(state.toBoolean)
}

object InputBooleanState {
  def unknown(entity_name: String): InputBooleanState = InputBooleanState(entity_name, Unavailable, new DateTime(0), new DateTime(0), None)
}

case class InputBooleanState(override val entity_name: String, state: TurnState, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject]) extends EntityState[TurnState] {
  override def entity_domain: String = "input_boolean"

  def booleanState: Option[Boolean] = state match {
    case On => Some(true)
    case Off => Some(false)
    case Unavailable => None
  }
}

object InputDateTimeState {
  def domain: String = "input_datetime"
  def unknown(entity_name: String): InputDateTimeState = InputDateTimeState(entity_name, Left[DateTime, LocalTime](new DateTime(0)), new DateTime(0), new DateTime(0), None)
}

case class InputDateTimeState(override val entity_name: String, state: Either[DateTime, LocalTime], lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject]) extends EntityState[Either[DateTime, LocalTime]] {
  override def entity_domain: String = InputDateTimeState.domain

  def hasTime: Option[Boolean] = attribute[Boolean]("has_time")
  def hasDate: Option[Boolean] = attribute[Boolean]("has_date")
}

