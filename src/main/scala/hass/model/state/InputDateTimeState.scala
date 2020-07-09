package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.InputDateTime
import hass.model.state.attributes.InputDateTimeAttribute
import hass.model.state.ground.TimeOrDate
import play.api.libs.json.JsObject

case class InputDateTimeState(override val entityName: String,
                              override val value: TimeOrDate,
                              override val lastChanged: DateTime,
                              override val lastUpdated: DateTime,
                              override val attributes: Option[JsObject])
  extends EntityState[TimeOrDate] with InputDateTime.Domain with InputDateTimeAttribute