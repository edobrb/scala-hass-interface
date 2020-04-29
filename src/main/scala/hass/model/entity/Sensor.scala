package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.state.SensorState

object Sensor extends MetaDomain {
  def domain: Domain = "sensor"

  def apply()(implicit sensor_name: sourcecode.Name, hass: Hass): Sensor = Sensor(sensor_name.value)(hass)
}

case class Sensor(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[String, SensorState]() with Sensor.Domain
