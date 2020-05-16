package hass.model.service

import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

trait EntitiesService extends Service {
  override def serviceData: JsObject = attributes.foldLeft(JsObject(Seq()))({
    case (obj, att) => obj + att
  }) + ("entity_id" -> JsArray(entityIds.map(JsString.apply)))

  def attributes: Map[String, JsValue] = Map()

  def entityIds: Seq[String] = entityNames.map(n => s"$domain.$n")

  def entityNames: Seq[String]
}
