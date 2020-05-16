package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.InputBoolean
import play.api.libs.json.JsObject


case class InputBooleanState(override val entity_name: String,
                             override val state: TurnState,
                             override val lastChanged: DateTime,
                             override val lastUpdated: DateTime,
                             override val attributes: Option[JsObject])
  extends EntityState[TurnState] with InputBoolean.Domain {

  def booleanState: Option[Boolean] = state match {
    case On => Some(true)
    case Off => Some(false)
    case Unavailable => None
  }
}
