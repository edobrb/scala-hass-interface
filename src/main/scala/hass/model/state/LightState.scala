package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.Light
import play.api.libs.json.JsObject

case class LightState(override val entity_name: String,
                      override val state: TurnState,
                      override val lastChanged: DateTime,
                      override val lastUpdated: DateTime,
                      override val attributes: Option[JsObject]) extends EntityState[TurnState] with Light.Domain {

  def brightness: Option[Int] = attribute[Int]("brightness")

  def colorTemp: Option[Int] = attribute[Int]("color_temp")

  def kelvin: Option[Float] = attribute[Float]("kelvin")

  def rgb: Option[(Int, Int, Int)] = attribute[List[Int]]("rgb_color") match {
    case Some(r::g::b::Nil) => Some((r, g, b))
    case _ => None
  }

  def xy: Option[(Float, Float)] = attribute[List[Float]]("xy_color") match {
    case Some(x :: y :: Nil) => Some((x, y))
    case _ => None
  }

  def effect: Option[String] = attribute[String]("effect")

  def transition: Option[Int] = attribute[Int]("transition")

  //TODO: finish to add all data attribute
}
