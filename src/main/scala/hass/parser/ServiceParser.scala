package hass.parser

import hass.model.entity.{Light, Switch}
import hass.model.service._
import hass.model.{MetaDomain, MetaService}
import hass.parser.CommonParser._
import play.api.libs.json.JsValue

object ServiceParser extends JsonParser[Service] {

  override def apply(data: JsValue): Option[Service] = first(parsers)(data)

  def parsers: Seq[JsonParser[Service]] = Seq[JsonParser[Service]](
    lightTurnOffServiceParser,
    lightTurnOnServiceParser,
    lightToggleServiceParser,
    switchTurnOffServiceParser,
    switchTurnOnServiceParser,
    switchToggleServiceParser,
    unknownServiceParser)

  def lightTurnOnServiceParser: JsonParser[LightTurnOnService] =
    expectedServiceParser(Light, TurnOnService, LightTurnOnService.apply)

  def lightTurnOffServiceParser: JsonParser[LightTurnOffService] =
    expectedServiceParser(Light, TurnOffService, LightTurnOffService.apply)

  def lightToggleServiceParser: JsonParser[LightToggleService] =
    expectedServiceParser(Light, ToggleService, LightToggleService.apply)

  def switchTurnOnServiceParser: JsonParser[SwitchTurnOnService] =
    expectedServiceParser(Switch, TurnOnService, SwitchTurnOnService.apply)

  def switchTurnOffServiceParser: JsonParser[SwitchTurnOffService] =
    expectedServiceParser(Switch, TurnOffService, SwitchTurnOffService.apply)

  def switchToggleServiceParser: JsonParser[SwitchToggleService] =
    expectedServiceParser(Switch, ToggleService, SwitchToggleService.apply)

  def unknownServiceParser: JsonParser[Service] = data =>
    for (domain <- str("domain")(data);
         service <- str("service")(data);
         serviceData <- jsonObj("service_data")(data))
      yield UnknownServiceRequest(domain, service, serviceData)

  def expectedServiceParser[T](expectedDomain: MetaDomain, expectedService: MetaService,
                               f: (Seq[String], Map[String, JsValue]) => T): JsonParser[T] = data => {
    for (UnknownServiceRequest(domain, service, serviceData) <- unknownServiceParser(data)
         if domain == expectedDomain.domain
         if service == expectedService.service;
         entityIds <- strOrStrSeq("entity_id")(serviceData);
         entityDomainsNames <- entityIdsSeq(entityIds);
         attributes = serviceData.fields.filter(_._1 != "entity_id").toMap)
      yield f(entityDomainsNames.map(_._2), attributes)
  }


}
