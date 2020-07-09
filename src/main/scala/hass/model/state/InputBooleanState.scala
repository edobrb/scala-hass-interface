package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.InputBoolean
import hass.model.state.attributes.InputBooleanAttributes
import hass.model.state.ground.TurnState
import play.api.libs.json.JsObject


case class InputBooleanState(override val entityName: String,
                             override val value: TurnState,
                             override val lastChanged: DateTime,
                             override val lastUpdated: DateTime,
                             override val attributes: Option[JsObject])
  extends EntityState[TurnState] with InputBoolean.Domain with InputBooleanAttributes