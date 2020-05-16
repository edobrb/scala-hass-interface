package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.Sensor
import play.api.libs.json.JsObject

import scala.util.Try


case class SensorState(override val entity_name: String,
                       override val state: String,
                       override val lastChanged: DateTime,
                       override val lastUpdated: DateTime,
                       override val attributes: Option[JsObject])
  extends EntityState[String] with Sensor.Domain {

  def numericState: Option[Double] = tryState(state.toDouble)

  def datetimeState: Option[DateTime] = tryState(DateTime.parse(state))

  def booleanState: Option[Boolean] = tryState(state.toBoolean)

  private def tryState[T](r: => T): Option[T] = Try(r).toOption
}

