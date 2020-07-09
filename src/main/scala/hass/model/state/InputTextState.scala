package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.InputText
import hass.model.state.attributes.InputTextAttributes
import play.api.libs.json.JsObject


case class InputTextState(override val entityName: String,
                          override val value: String,
                          override val lastChanged: DateTime,
                          override val lastUpdated: DateTime,
                          override val attributes: Option[JsObject])
  extends EntityState[String] with InputText.Domain with InputTextAttributes