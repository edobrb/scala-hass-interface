package hass.parser

import hass.model.service.{LightTurnOffService, LightTurnOnService, Service}
import hass.parser.CommonParser._
import play.api.libs.json.JsValue

object ServiceParser {

  def parsers: Seq[JsValue => Option[Service]] = Seq(lightTurnOffServiceParser, lightTurnOnServiceParser)

  def lightTurnOnServiceParser(data: JsValue): Option[Service] = {
    for ("light" <- str("domain")(data);
         "turn_on" <- str("service")(data);
         serviceData <- jsonObj("service_data")(data);
         entityId <- str("entity_id")(serviceData);
         attributes <- Some(serviceData.fields.filter(_._1 != "entity_id").toMap)
         ) yield LightTurnOnService(entityId.split('.')(1), attributes)
  }

  def lightTurnOffServiceParser(data: JsValue): Option[Service] = {
    for ("light" <- str("domain")(data);
         "turn_off" <- str("service")(data);
         serviceData <- jsonObj("service_data")(data);
         entityId <- str("entity_id")(serviceData);
         attributes <- Some(serviceData.fields.filter(_._1 != "entity_id").toMap)
         ) yield LightTurnOffService(entityId.split('.')(1), attributes)
  }
}
