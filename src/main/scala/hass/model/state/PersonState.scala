package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.Person
import hass.model.state.attributes.PersonAttributes
import play.api.libs.json.JsObject


case class PersonState(override val entityName: String,
                       override val value: String,
                       override val lastChanged: DateTime,
                       override val lastUpdated: DateTime,
                       override val attributes: Option[JsObject])
  extends EntityState[String] with Person.Domain with PersonAttributes

