package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.state.attributes.StatefulEntityAttributes
import play.api.libs.json.JsObject

case class UnknownEntityState(override val entityId: String,
                              override val value: String,
                              override val lastChanged: DateTime,
                              override val lastUpdated: DateTime,
                              override val attributes: Option[JsObject]) extends EntityState[String]
  with StatefulEntityAttributes[String] {
  override def entityName: String = entityId.split('.')(1)

  override def domain: String = entityId.split('.')(0)
}
