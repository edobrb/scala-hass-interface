package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.Sensor
import play.api.libs.json.JsObject

import scala.util.Try


case class SensorState(override val entity_name: String, state: String, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject]) extends EntityState[String] {
  override def entity_domain: String = Sensor.domain

  private def tryState[T](r: => T): Option[T] = Try(r).toOption

  def numericState: Option[Double] = tryState(state.toDouble)

  def datetimeState: Option[DateTime] = tryState(DateTime.parse(state))

  def booleanState: Option[Boolean] = tryState(state.toBoolean)
}

