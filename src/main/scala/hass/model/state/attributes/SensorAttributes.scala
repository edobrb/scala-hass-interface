package hass.model.state.attributes

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity.{Sensor, StatefulEntity}
import hass.model.state.ground.TurnState
import play.api.libs.json.JsObject

import scala.util.Try


trait SensorAttributes extends StatefulEntityAttributes[String] {

  def numericValue: Option[Double] = tryState(_.toDouble)

  def datetimeValue: Option[DateTime] = tryState(DateTime.parse)

  def booleanValue: Option[Boolean] = tryState(_.toBoolean)

  private def tryState[T](f: String => T): Option[T] = Try(state.map(_.value).map(f)).toOption.flatten
}

