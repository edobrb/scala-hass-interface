package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import play.api.libs.json.JsValue

trait EntityState[T] {
  def entity_id: String

  def lastChanged: DateTime

  def lastUpdated: DateTime

  def state: T

  def attributes: Option[JsValue]
}

case class UnknownEntityState(entity_id: String, state: String, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsValue]) extends EntityState[String]

object SwitchState {
  def unknown(entity_name: String): SwitchState = SwitchState(entity_name, Unavailable, new DateTime(0), new DateTime(0), None)
}

case class SwitchState(entity_name: String, state: TurnState, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsValue]) extends EntityState[TurnState] {
  override def entity_id: String = s"switch.$entity_name"
}

object LightState {
  def unknown(entity_name: String): LightState = LightState(entity_name, Unavailable, new DateTime(0), new DateTime(0), None)
}
case class LightState(entity_name: String, state: TurnState, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsValue]) extends EntityState[TurnState] {
  override def entity_id: String = s"light.$entity_name"
}

case class SensorState(entity_name: String, state: String, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsValue]) extends EntityState[String] {
  override def entity_id: String = s"sensor.$entity_name"

  def numericState: Double = state.toDouble

  def datetimeState: DateTime = DateTime.parse(state)

  def booleanState: Boolean = state.toBoolean
}

