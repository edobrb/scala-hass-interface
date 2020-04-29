package hass.model.service

import hass.model.Types.{Domain, ServiceType}
import hass.model.entity.{Light, Switch}
import hass.model.{MetaDomain, MetaService}
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

case class UnknownServiceRequest(override val domain: Domain, override val service: ServiceType, override val serviceData: JsObject) extends Service

object TurnOnService extends MetaService {
  override def service: ServiceType = "turn_on"
}

object TurnOffService extends MetaService {
  override def service: String = "turn_off"
}

object ToggleService extends MetaService {
  override def service: String = "toggle"
}

trait EntitiesService extends Service {
  override def serviceData: JsObject = attributes.foldLeft(JsObject(Seq()))({
    case (obj, att) => obj + att
  }) + ("entity_id" -> JsArray(entityIds.map(JsString.apply)))

  def attributes: Map[String, JsValue] = Map()

  def entityNames: Seq[String]

  def entityIds: Seq[String] = entityNames.map(n => s"$domain.$n")
}


case class SwitchTurnOnService(override val entityNames: Seq[String], override val attributes: Map[String, JsValue] = Map())
  extends EntitiesService with TurnOnService.ServiceMeta with Switch.DomainMeta

case class SwitchTurnOffService(override val entityNames: Seq[String], override val attributes: Map[String, JsValue] = Map())
  extends EntitiesService with TurnOffService.ServiceMeta with Switch.DomainMeta

case class SwitchToggleService(override val entityNames: Seq[String], override val attributes: Map[String, JsValue] = Map())
  extends EntitiesService with ToggleService.ServiceMeta with Switch.DomainMeta

case class LightToggleService(override val entityNames: Seq[String], override val attributes: Map[String, JsValue] = Map())
  extends EntitiesService with ToggleService.ServiceMeta with Light.DomainMeta

case class LightTurnOffService(override val entityNames: Seq[String], override val attributes: Map[String, JsValue] = Map())
  extends EntitiesService with TurnOffService.ServiceMeta with Light.DomainMeta {
  def transition(v: Int): LightTurnOffService = LightTurnOffService(entityNames, Map("transition" -> JsNumber(v)))
}

//TODO: turn on service support array of entities?
case class LightTurnOnService(override val entityNames: Seq[String], override val attributes: Map[String, JsValue] = Map())
  extends EntitiesService with TurnOnService.ServiceMeta with Light.DomainMeta {
  def withRawAttribute(attribute: (String, JsValue)): LightTurnOnService = LightTurnOnService(entityNames, attributes ++ Map(attribute))

  def withAttribute[T: Writes](attribute: (String, T)): LightTurnOnService = attribute match {
    case (name, value) => withRawAttribute(name -> Json.toJson(value))
  }

  def brightness(v: Int): LightTurnOnService = withAttribute("brightness" -> v)

  def colorTemp(v: Int): LightTurnOnService = withAttribute("color_temp" -> v)

  def kelvin(v: Float): LightTurnOnService = withAttribute("kelvin" -> v)

  def rgb(r: Int, g: Int, b: Int): LightTurnOnService = withAttribute("rgb_color" -> Seq(r, g, b))

  def xy(x: Float, y: Float): LightTurnOnService = withAttribute("xy_color" -> Seq(x, y))

  def effect(name: String): LightTurnOnService = withAttribute("effect" -> name)

  def transition(v: Int): LightTurnOnService = withAttribute("transition" -> v)

  //TODO: finish to add all Service data attribute
}