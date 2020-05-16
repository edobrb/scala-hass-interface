package hass.model.service

import hass.model.entity.Light
import hass.model.state.TurnAction
import play.api.libs.json.{JsValue, Json, Writes}

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
