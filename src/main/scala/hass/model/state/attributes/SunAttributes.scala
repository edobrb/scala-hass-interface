package hass.model.state.attributes

import com.github.nscala_time.time.Imports.DateTime
import hass.model.state.ground.Horizon

trait SunAttributes extends StatefulEntityAttributes[Horizon] {

  def nextDawn: Option[DateTime] = tryMapAttribute[DateTime]("next_dawn", DateTime.parse)

  def nextDusk: Option[DateTime] = tryMapAttribute[DateTime]("next_dusk", DateTime.parse)

  def nextMidnight: Option[DateTime] = tryMapAttribute[DateTime]("next_midnight", DateTime.parse)

  def nextNoon: Option[DateTime] = tryMapAttribute[DateTime]("next_noon", DateTime.parse)

  def nextRising: Option[DateTime] = tryMapAttribute[DateTime]("next_rising", DateTime.parse)

  def nextSetting: Option[DateTime] = tryMapAttribute[DateTime]("next_setting", DateTime.parse)

  def elevation: Option[Double] = attribute[Double]("elevation")

  def azimuth: Option[Double] = attribute[Double]("azimuth")

  def rising: Option[Boolean] = attribute[Boolean]("rising")
}
