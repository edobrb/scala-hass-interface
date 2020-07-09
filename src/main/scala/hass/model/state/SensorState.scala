package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.Sensor
import hass.model.state.attributes.SensorAttributes
import play.api.libs.json.JsObject


case class SensorState(override val entityName: String,
                       override val value: String,
                       override val lastChanged: DateTime,
                       override val lastUpdated: DateTime,
                       override val attributes: Option[JsObject])
  extends EntityState[String] with Sensor.Domain with SensorAttributes

