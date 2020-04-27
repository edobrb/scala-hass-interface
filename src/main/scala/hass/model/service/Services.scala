package hass.model.service

import hass.model.state.{Off, On, TurnState}
import play.api.libs.json._

sealed trait Request {
  def tipe: String

  def domain: String

  def service: String

  def materialize(id: Long): JsObject = JsObject(
    Seq(
      "id" -> JsNumber(id),
      "type" -> JsString(tipe),
      "domain" -> JsString(domain),
      "service" -> JsString(service),
    )
  )
}

sealed trait Service extends Request {
  override def tipe: String = "call_service"
}

sealed trait TurnService extends Service {
  def turnState: TurnState

  override def service: String = turnState match {
    case On => "turn_on"
    case Off => "turn_off"
  }
}

sealed trait ToggleService extends Service {
  override def service: String = "toggle"
}

trait EntityService extends Service {
  def entity_id: String

  override def materialize(id: Long): JsObject = materialize(id, Map())

  def materialize(id: Long, attributes: Map[String, JsValue]): JsObject = {
    val serviceData = attributes.foldLeft(JsObject(Seq()))({
      case (obj, att) => obj + att
    }) + ("entity_id" -> JsString(s"$domain.$entity_id"))
    super.materialize(id) + ("service_data" -> serviceData)
  }
}

trait SwitchService extends EntityService {
  override def domain: String = "switch"
}
trait LightService extends EntityService {
  override def domain: String = "light"
}

case class SwitchTurnService(override val entity_id: String, override val turnState: TurnState) extends TurnService with SwitchService

case class SwitchToggleService(override val entity_id: String) extends ToggleService with SwitchService

case class LightToggleService(override val entity_id: String) extends ToggleService with LightService

case class LightTurnOffService(override val entity_id: String, attributes: Map[String, JsValue] = Map()) extends TurnService with LightService {
  override def turnState: TurnState = Off

  def transition(v: Int): LightTurnOffService = LightTurnOffService(entity_id, Map("transition" -> JsNumber(v)))
}

case class LightTurnOnService(override val entity_id: String, attributes: Map[String, JsValue] = Map()) extends TurnService with LightService {
  override def turnState: TurnState = On

  override def materialize(id: Long): JsObject = materialize(id, attributes)

  private def withAttribute2(attribute: (String, JsValue)): LightTurnOnService = LightTurnOnService(entity_id, attributes ++ Map(attribute))

  def withAttribute[T: Writes](attribute: (String, T)): LightTurnOnService = attribute match {
    case (name, value) => withAttribute2(name -> Json.toJson(value))
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