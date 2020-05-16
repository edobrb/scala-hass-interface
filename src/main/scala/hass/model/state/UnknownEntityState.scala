package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import play.api.libs.json.JsObject

case class UnknownEntityState(override val entity_id: String, state: String, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject]) extends EntityState[String] {
  override def entity_name: String = entity_id.split('.')(1)

  override def domain: String = entity_id.split('.')(0)
}
