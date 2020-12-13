package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.Script
import hass.model.state.attributes.ScriptAttributes
import hass.model.state.ground.TurnState
import play.api.libs.json.JsObject


case class ScriptState(override val entityName: String,
                       override val value: TurnState,
                       override val lastChanged: DateTime,
                       override val lastUpdated: DateTime,
                       override val attributes: Option[JsObject])
  extends EntityState[TurnState] with Script.Domain with ScriptAttributes