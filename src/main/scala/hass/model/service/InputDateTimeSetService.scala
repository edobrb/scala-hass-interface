package hass.model.service

import hass.model.MetaService
import hass.model.Types.ServiceType
import hass.model.entity.InputDateTime
import hass.model.state.ground.TimeOrDate
import play.api.libs.json.{JsString, JsValue}

object InputDateTimeSetService extends MetaService {
  override val service: ServiceType = "set_datetime"
}
case class InputDateTimeSetService(override val entityNames: Seq[String], state: TimeOrDate)
  extends EntitiesService with InputDateTime.Domain with InputDateTimeSetService.Service {
  override def attributes: Map[String, JsValue] = Map(state.kind -> JsString(state.formatted))
}
