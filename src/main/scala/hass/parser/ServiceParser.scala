package hass.parser

import hass.model.entity.Light
import hass.model.service.{LightTurnOffService, LightTurnOnService, Service}
import hass.parser.CommonParser._
import play.api.libs.json.JsValue

object ServiceParser extends JsonParser[Service] {

  override def apply(data: JsValue): Option[Service] = first(parsers)(data)

  def parsers: Seq[JsonParser[Service]] = Seq[JsonParser[Service]](
    lightTurnOffServiceParser,
    lightTurnOnServiceParser)

  def lightTurnOnServiceParser: JsonParser[LightTurnOnService] =
    expectedServiceParser(Light.domain, "turn_on", LightTurnOnService.apply)

  def lightTurnOffServiceParser: JsonParser[LightTurnOffService] =
    expectedServiceParser(Light.domain, "turn_off", LightTurnOffService.apply)

  def expectedServiceParser[T](expectedDomain: String, expectedService: String, f: (String, Map[String, JsValue]) => T): JsonParser[T] =
    data => for (domain <- str("domain")(data)
                 if domain == expectedDomain;
                 service <- str("service")(data)
                 if service == expectedService;
                 serviceData <- jsonObj("service_data")(data);
                 entityId <- str("entity_id")(serviceData);
                 (_, entityName) <- entityIds(entityId);
                 attributes = serviceData.fields.filter(_._1 != "entity_id").toMap
                 ) yield f(entityName, attributes)
}
