package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.InputDateTime
import org.joda.time.LocalTime
import play.api.libs.json.JsObject

case class InputDateTimeState(override val entity_name: String, state: Either[DateTime, LocalTime], lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject]) extends EntityState[Either[DateTime, LocalTime]] {
  override def entity_domain: String = InputDateTime.domain

  def hasTime: Option[Boolean] = attribute[Boolean]("has_time")

  def hasDate: Option[Boolean] = attribute[Boolean]("has_date")
}
