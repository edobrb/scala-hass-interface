package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.Switch
import hass.model.state.ground.TurnState
import play.api.libs.json.JsObject


case class SwitchState(override val entity_name: String,
                       override val state: TurnState,
                       override val lastChanged: DateTime,
                       override val lastUpdated: DateTime,
                       override val attributes: Option[JsObject])
  extends EntityState[TurnState] with Switch.Domain

