package hass.parser

import hass.model.MetaDomain
import hass.model.Types.{DomainType, ServiceType}
import hass.model.entity.Light
import hass.model.service._
import hass.model.state.TurnAction
import hass.parser.CommonParser._
import play.api.libs.json.{JsObject, JsValue}

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

  def unknownServiceParser: JsonParser[Service] =
    elementParser.map { case (domain, service, serviceData) => UnknownService(domain, service, serviceData) }


  def expectedServiceParser[T](expectedDomain: MetaDomain,
                               f: (Seq[String], TurnAction, Map[String, JsValue]) => T): JsonParser[T] = data => {
    for ((domain, service, serviceData) <- elementParser(data)
         if domain == expectedDomain.domain;
         turn <- turnAction(service);
         entityIds <- strOrStrSeq("entity_id")(serviceData);
         entityIdsSplit <- entityIdsSeq(entityIds);
         attributes = serviceData.fields.filter { case (att, _) => att != "entity_id" }.toMap)
      yield f(entityIdsSplit.map { case (_, name) => name }, turn, attributes)
  }

  def elementParser: JsonParser[(DomainType, ServiceType, JsObject)] = data =>
    for (domain <- str("domain")(data);
         service <- str("service")(data);
         serviceData <- jsonObj("service_data")(data))
      yield (domain, service, serviceData)
}
