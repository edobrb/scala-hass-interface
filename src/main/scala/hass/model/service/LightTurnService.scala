package hass.model.service

import hass.model.entity.Light
import hass.model.state.ground.TurnAction
import play.api.libs.json.{JsValue, Json, Writes}

case class LightTurnService(override val entityNames: Seq[String], override val turn: TurnAction, override val attributes: Map[String, JsValue] = Map())
  extends TurnService with Light.Domain {

  def transition(v: Int): LightTurnService = withAttribute("transition" -> v)

  def profile(name: String): LightTurnService = withAttribute("profile" -> name)

  def hs(h: Float, s: Float): LightTurnService = withAttribute("hs_color" -> Seq(h, s))

  def xy(x: Float, y: Float): LightTurnService = withAttribute("xy_color" -> Seq(x, y))

  def withAttribute[T: Writes](attribute: (String, T)): LightTurnService = attribute match {
    case (name, value) => withRawAttribute(name -> Json.toJson(value))
  }

  def withRawAttribute(attribute: (String, JsValue)): LightTurnService = LightTurnService(entityNames, turn, attributes ++ Map(attribute))

  def rgb(r: Int, g: Int, b: Int): LightTurnService = withAttribute("rgb_color" -> Seq(r, g, b))

  def white(w: Int): LightTurnService = withAttribute("white_value" -> w)

  def colorTemp(v: Int): LightTurnService = withAttribute("color_temp" -> v)

  def kelvin(v: Float): LightTurnService = withAttribute("kelvin" -> v)

  def color(name: String): LightTurnService = withAttribute("color_name" -> name)

  def brightness(v: Int): LightTurnService = withAttribute("brightness" -> v)

  def brightnessPct(v: Int): LightTurnService = withAttribute("brightness_pct" -> v)

  def brightnessStep(v: Int): LightTurnService = withAttribute("brightness_step" -> v)

  def brightnessStepPct(v: Int): LightTurnService = withAttribute("brightness_step_pct" -> v)

  def flashShort: LightTurnService = withAttribute("flash" -> "short")

  def flashLong: LightTurnService = withAttribute("flash" -> "long")

  def effect(name: String): LightTurnService = withAttribute("effect" -> name)
}
