package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.Light
import play.api.libs.json.JsObject

case class LightState(override val entity_name: String,
                      state: TurnState,
                      lastChanged: DateTime,
                      lastUpdated: DateTime,
                      attributes: Option[JsObject]) extends EntityState[TurnState] with Light.DomainMeta {

  def brightness: Option[Int] = attribute[Int]("brightness")
}
