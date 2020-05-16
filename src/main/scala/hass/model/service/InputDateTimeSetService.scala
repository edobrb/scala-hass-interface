package hass.model.service

import hass.model.entity.InputDateTime
import hass.model.state.TimeOrDate
import play.api.libs.json.{JsString, JsValue}

case class InputDateTimeSetService(override val entityNames: Seq[String], state: TimeOrDate)
  extends EntitiesService with InputDateTime.Domain with InputDateTime.Service {
  override def attributes: Map[String, JsValue] = Map(state.kind -> JsString(state.formatted))
}
