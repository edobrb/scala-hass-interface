package hass.model.service

import hass.model.MetaService
import hass.model.Types.ServiceType
import hass.model.entity.InputText
import play.api.libs.json.{JsString, JsValue}

object InputTextSetService extends MetaService {
  override val service: ServiceType = "set_value"
}

case class InputTextSetService(override val entityNames: Seq[String], value: String)
  extends EntitiesService with InputText.Domain with InputTextSetService.Service {
  override def attributes: Map[String, JsValue] = Map("value" -> JsString(value))
}
