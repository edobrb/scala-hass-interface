package hass.model.service

import hass.model.{MetaDomain, MetaService}
import play.api.libs.json._

trait Service extends MetaDomain with MetaService {
  def serviceData: JsObject

  def materialize(id: Long): JsObject = JsObject(Seq(
    "id" -> JsNumber(id),
    "type" -> JsString("call_service"),
    "domain" -> JsString(domain),
    "service" -> JsString(service),
    "service_data" -> serviceData
  ))
}






