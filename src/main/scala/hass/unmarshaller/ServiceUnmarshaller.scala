package hass.unmarshaller

import hass.model.MetaDomain
import hass.model.Types.{DomainType, ServiceType}
import hass.model.entity.{InputBoolean, InputDateTime, InputText, Light, Script, Switch}
import hass.model.service._
import hass.model.state.ground.{TimeOrDate, TurnAction}
import hass.unmarshaller.CommonUnmarshaller._
import hass.unmarshaller.ImplicitReads._
import play.api.libs.json.{JsObject, JsValue}

object ServiceUnmarshaller extends JsonUnmarshaller[Service] {

  override def apply(data: JsValue): Option[Service] = first(all)(data)

  def all: Seq[JsonUnmarshaller[Service]] = Seq[JsonUnmarshaller[Service]](
    lightTurn,
    switchTurn,
    scriptTurn,
    inputBooleanTurn,
    inputDatetimeSet,
    inputTextSet,
    unknown)

  def lightTurn: JsonUnmarshaller[LightTurnService] =
    turn(Light, LightTurnService.apply)

  def switchTurn: JsonUnmarshaller[SwitchTurnService] =
    turn2(Switch, SwitchTurnService.apply)

  def scriptTurn: JsonUnmarshaller[ScriptTurnService] =
    turn2(Script, ScriptTurnService.apply)

  def inputBooleanTurn: JsonUnmarshaller[InputBooleanTurnService] =
    turn2(InputBoolean, InputBooleanTurnService.apply)

  def inputDatetimeSet: JsonUnmarshaller[InputDateTimeSetService] = data =>
    for ((InputDateTime.domain, InputDateTimeSetService.service, serviceData) <- defaultInfo(data);
         entityIds <- strOrStrSeq("entity_id")(serviceData);
         timeOrDate <- extract[TimeOrDate].apply(serviceData))
      yield InputDateTimeSetService(entityIds, timeOrDate)

  def inputTextSet: JsonUnmarshaller[InputTextSetService] = data =>
    for ((InputText.domain, InputTextSetService.service, serviceData) <- defaultInfo(data);
         entityIds <- strOrStrSeq("entity_id")(serviceData);
         text <- str("value")(serviceData))
      yield InputTextSetService(entityIds, text)

  def unknown: JsonUnmarshaller[Service] =
    defaultInfo.map { case (domain, service, serviceData) => UnknownService(domain, service, serviceData) }

  def turn[T](expectedDomain: MetaDomain, f: (Seq[String], TurnAction, Map[String, JsValue]) => T): JsonUnmarshaller[T] = data => {
    for ((expectedDomain.domain, service, serviceData) <- defaultInfo(data);
         turn <- turnAction(service);
         entityIds <- strOrStrSeq("entity_id")(serviceData);
         entityIdsSplit <- entityIdsSeq(entityIds);
         attributes = serviceData.fields.filter { case (att, _) => att != "entity_id" }.toMap)
      yield f(entityIdsSplit.map { case (_, name) => name }, turn, attributes)
  }

  def turn2[T](expectedDomain: MetaDomain, f: (Seq[String], TurnAction) => T): JsonUnmarshaller[T] =
    turn(expectedDomain, (e, s, _) => f(e, s))

  def defaultInfo: JsonUnmarshaller[(DomainType, ServiceType, JsObject)] = data =>
    for (domain <- str("domain")(data);
         service <- str("service")(data);
         serviceData <- jsonObj("service_data")(data))
      yield (domain, service, serviceData)
}
