package hass.model.service

import hass.model.state.{Off, On, TurnState}
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}

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

  override def materialize(id: Long): JsObject = super.materialize(id) +
    ("service_data" -> Json.obj("entity_id" -> Json.toJson(s"$domain.$entity_id")))
}

trait SwitchService extends EntityService {
  override def domain: String = "switch"
}
trait LightService extends EntityService {
  override def domain: String = "light"
}

case class SwitchTurnService(override val entity_id: String, override val turnState: TurnState) extends TurnService with SwitchService

case class SwitchToggleService(override val entity_id: String) extends ToggleService with SwitchService

case class LightTurnService(override val entity_id: String, override val turnState: TurnState) extends TurnService with LightService

case class LightToggleService(override val entity_id: String) extends ToggleService with LightService