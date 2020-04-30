package hass.parser

import hass.model.MetaDomain
import hass.model.entity.Light
import hass.model.service._
import hass.model.state.TurnAction
import hass.parser.CommonParser._
import play.api.libs.json.JsValue

object ServiceParser extends JsonParser[Service] {

  override def apply(data: JsValue): Option[Service] = first(parsers)(data)

  def parsers: Seq[JsonParser[Service]] = Seq[JsonParser[Service]](
    lightTurnServiceParser,
    switchTurnServiceParser,
    unknownServiceParser)

  def lightTurnServiceParser: JsonParser[LightTurnService] =
    expectedServiceParser(Light, LightTurnService.apply)

  def switchTurnServiceParser: JsonParser[SwitchTurnService] =
    expectedServiceParser(Light, SwitchTurnService.apply)

  def unknownServiceParser: JsonParser[Service] = data =>
    for (domain <- str("domain")(data);
         service <- str("service")(data);
         serviceData <- jsonObj("service_data")(data))
      yield UnknownServiceRequest(domain, service, serviceData)

  def expectedServiceParser[T](expectedDomain: MetaDomain,
                               f: (Seq[String], TurnAction, Map[String, JsValue]) => T): JsonParser[T] = data => {
    for (UnknownServiceRequest(domain, service, serviceData) <- unknownServiceParser(data)
         if domain == expectedDomain.domain;
         turn <- turnAction(service);
         entityIds <- strOrStrSeq("entity_id")(serviceData);
         entityDomainsNames <- entityIdsSeq(entityIds);
         attributes = serviceData.fields.filter(_._1 != "entity_id").toMap)
      yield f(entityDomainsNames.map(_._2), turn, attributes)
  }
}
