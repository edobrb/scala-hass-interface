package hass.parser

import hass.model.MetaDomain
import hass.model.Types.{DomainType, ServiceType}
import hass.model.entity.{InputBoolean, InputDateTime, Light, Switch}
import hass.model.service._
import hass.model.state.ground.{TimeOrDate, TurnAction}
import hass.parser.CommonParser._
import hass.parser.ImplicitReads._
import play.api.libs.json.{JsObject, JsValue}

object ServiceParser extends JsonParser[Service] {

  override def apply(data: JsValue): Option[Service] = first(all)(data)

  def all: Seq[JsonParser[Service]] = Seq[JsonParser[Service]](
    lightTurn,
    switchTurn,
    inputBooleanTurn,
    inputDatetimeSet,
    unknown)

  def lightTurn: JsonParser[LightTurnService] =
    turn(Light, LightTurnService.apply)

  def switchTurn: JsonParser[SwitchTurnService] =
    turn2(Switch, SwitchTurnService.apply)

  def inputBooleanTurn: JsonParser[InputBooleanTurnService] =
    turn2(InputBoolean, InputBooleanTurnService.apply)

  def inputDatetimeSet: JsonParser[InputDateTimeSetService] = data =>
    for ((InputDateTime.domain, InputDateTimeSetService.service, serviceData) <- defaultInfo(data);
         entityIds <- strOrStrSeq("entity_id")(serviceData);
         timeOrDate <- extract[TimeOrDate].apply(serviceData))
      yield InputDateTimeSetService(entityIds, timeOrDate)

  def unknown: JsonParser[Service] =
    defaultInfo.map { case (domain, service, serviceData) => UnknownService(domain, service, serviceData) }

  def turn[T](expectedDomain: MetaDomain, f: (Seq[String], TurnAction, Map[String, JsValue]) => T): JsonParser[T] = data => {
    for ((expectedDomain.domain, service, serviceData) <- defaultInfo(data);
         turn <- turnAction(service);
         entityIds <- strOrStrSeq("entity_id")(serviceData);
         entityIdsSplit <- entityIdsSeq(entityIds);
         attributes = serviceData.fields.filter { case (att, _) => att != "entity_id" }.toMap)
      yield f(entityIdsSplit.map { case (_, name) => name }, turn, attributes)
  }

  def turn2[T](expectedDomain: MetaDomain, f: (Seq[String], TurnAction) => T): JsonParser[T] =
    turn(expectedDomain, (e, s, _) => f(e, s))

  def defaultInfo: JsonParser[(DomainType, ServiceType, JsObject)] = data =>
    for (domain <- str("domain")(data);
         service <- str("service")(data);
         serviceData <- jsonObj("service_data")(data))
      yield (domain, service, serviceData)
}
