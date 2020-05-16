package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.Light
import hass.model.state.ground.TurnState
import play.api.libs.json.JsObject

case class LightState(override val entity_name: String,
                      override val state: TurnState,
                      override val lastChanged: DateTime,
                      override val lastUpdated: DateTime,
                      override val attributes: Option[JsObject]) extends EntityState[TurnState] with Light.Domain {


  def transition: Option[Int] = attribute[Int]("transition")

  def profile: Option[String] = attribute[String]("profile")

  def hs: Option[(Float, Float)] = attribute[List[Float]]("hs_color") match {
    case Some(h :: s :: Nil) => Some((h, s))
    case _ => None
  }

  def xy: Option[(Float, Float)] = attribute[List[Float]]("xy_color") match {
    case Some(x :: y :: Nil) => Some((x, y))
    case _ => None
  }

  def rgb: Option[(Int, Int, Int)] = attribute[List[Int]]("rgb_color") match {
    case Some(r :: g :: b :: Nil) => Some((r, g, b))
    case _ => None
  }

  def white: Option[Int] = attribute[Int]("white_value")

  def colorTemp: Option[Int] = attribute[Int]("color_temp")

  def kelvin: Option[Float] = attribute[Float]("kelvin")

  def color: Option[String] = attribute[String]("color_name")

  def brightness: Option[Int] = attribute[Int]("brightness")

  def brightnessPct: Option[Int] = attribute[Int]("brightness_pct")

  def brightnessStep: Option[Int] = attribute[Int]("brightness_step")

  def brightnessStepPct: Option[Int] = attribute[Int]("brightness_step_pct")

  def flash: Option[String] = attribute[String]("flash")

  def effect: Option[String] = attribute[String]("effect")
}
