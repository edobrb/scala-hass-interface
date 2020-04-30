package hass.model.service

import hass.model.Types.{DomainType, ServiceType}
import hass.model.entity.{Entity, Light, Switch}
import hass.model.state.TurnAction
import hass.model.{Domain, MetaDomain, MetaService}
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

case class UnknownServiceRequest(override val domain: DomainType, override val service: ServiceType, override val serviceData: JsObject) extends Service

trait EntitiesService extends Service {
  override def serviceData: JsObject = attributes.foldLeft(JsObject(Seq()))({
    case (obj, att) => obj + att
  }) + ("entity_id" -> JsArray(entityIds.map(JsString.apply)))

  def attributes: Map[String, JsValue] = Map()

  def entityNames: Seq[String]

  def entityIds: Seq[String] = entityNames.map(n => s"$domain.$n")
}

trait TurnService extends EntitiesService {
  def turn: TurnAction
  override def service: ServiceType = turn.service
}

case class TurnService2[T<:Entity:Domain](entities: Seq[T], turn: TurnAction, override val attributes: Map[String, JsValue] = Map())
  extends EntitiesService {
  override def service: ServiceType = turn.service

  override def entityNames: Seq[String] = entities.map(_.entity_name)

  override def domain: DomainType = implicitly[hass.model.Domain[T]].value
}

case class SwitchTurnService(override val entityNames: Seq[String], override val turn: TurnAction, override val attributes: Map[String, JsValue] = Map())
  extends TurnService with Switch.Domain

case class LightTurnService(override val entityNames: Seq[String], override val turn: TurnAction, override val attributes: Map[String, JsValue] = Map())
  extends TurnService with Light.Domain {

  def withRawAttribute(attribute: (String, JsValue)): LightTurnService = LightTurnService(entityNames, turn, attributes ++ Map(attribute))

  def withAttribute[T: Writes](attribute: (String, T)): LightTurnService = attribute match {
    case (name, value) => withRawAttribute(name -> Json.toJson(value))
  }

  def brightness(v: Int): LightTurnService = withAttribute("brightness" -> v)

  def colorTemp(v: Int): LightTurnService = withAttribute("color_temp" -> v)

  def kelvin(v: Float): LightTurnService = withAttribute("kelvin" -> v)

  def rgb(r: Int, g: Int, b: Int): LightTurnService = withAttribute("rgb_color" -> Seq(r, g, b))

  def xy(x: Float, y: Float): LightTurnService = withAttribute("xy_color" -> Seq(x, y))

  def effect(name: String): LightTurnService = withAttribute("effect" -> name)

  def transition(v: Int): LightTurnService = withAttribute("transition" -> v)

  //TODO: finish to add all Service data attribute

}