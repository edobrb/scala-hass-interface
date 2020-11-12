package hass.model.state

import hass.model.entity.Sun
import play.api.libs.json.JsObject
import com.github.nscala_time.time.Imports.DateTime
import hass.model.state.attributes.SunAttributes
import hass.model.state.ground.Horizon

case class SunState(override val entityName: String,
               override val value: Horizon,
               override val lastChanged: DateTime,
               override val lastUpdated: DateTime,
               override val attributes: Option[JsObject]) extends EntityState[Horizon] with Sun.Domain with SunAttributes
