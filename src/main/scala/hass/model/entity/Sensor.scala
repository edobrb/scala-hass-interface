package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.state.SensorState

object Sensor extends MetaDomain {
  val domain: DomainType = "sensor"

  def apply()(implicit name: sourcecode.Name, hass: Hass): Sensor = Sensor(name.value)(hass)
}

case class Sensor(entityName: String)(implicit hass: Hass)
  extends StatefulEntity[String, SensorState]() with Sensor.Domain
