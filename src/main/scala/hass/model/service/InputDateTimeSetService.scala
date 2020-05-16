package hass.model.service

import hass.model.Types.ServiceType
import hass.model.entity.InputDateTime
import hass.model.state.ground.TimeOrDate
import play.api.libs.json.{JsString, JsValue}

case class InputDateTimeSetService(override val entityNames: Seq[String], state: TimeOrDate)
  extends EntitiesService with InputDateTime.Domain  {
  override def attributes: Map[String, JsValue] = Map(state.kind -> JsString(state.formatted))

  override val service: ServiceType = "set_datetime"
}
