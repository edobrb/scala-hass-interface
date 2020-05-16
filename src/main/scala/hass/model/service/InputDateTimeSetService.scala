package hass.model.service

import hass.model.entity.InputDateTime
import hass.model.state.{SetDateTimeMetaService, TimeOrDate}
import play.api.libs.json.{JsString, JsValue}

case class InputDateTimeSetService(override val entityNames: Seq[String], state: TimeOrDate)
  extends EntitiesService with InputDateTime.Domain with SetDateTimeMetaService { //TODO: parser
  override def attributes: Map[String, JsValue] = Map(state.kind -> JsString(state.formatted))
}
