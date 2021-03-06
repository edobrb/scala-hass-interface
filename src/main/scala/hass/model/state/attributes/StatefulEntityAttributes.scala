package hass.model.state.attributes

import hass.model.state.EntityState
import play.api.libs.json.JsObject


trait StatefulEntityAttributes[S] extends EntityAttributes {
  def attributes: Option[JsObject] = state.flatMap(_.attributes)

  protected def state: Option[EntityState[S]]
}
