package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.InputBoolean
import play.api.libs.json.JsObject


case class InputBooleanState(override val entity_name: String, state: TurnState, lastChanged: DateTime, lastUpdated: DateTime, attributes: Option[JsObject])
  extends EntityState[TurnState] with InputBoolean.DomainMeta {

  def booleanState: Option[Boolean] = state match {
    case On => Some(true)
    case Off => Some(false)
    case Unavailable => None
  }
}
