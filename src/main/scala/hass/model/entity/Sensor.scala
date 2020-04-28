package hass.model.entity

import hass.controller.Hass
import hass.model.common.Observable
import hass.model.event.StateChangedEvent
import hass.model.service.result.Result
import hass.model.service.{LightToggleService, LightTurnOffService, LightTurnOnService}
import hass.model.state.{LightState, SensorState, TurnState}

import scala.concurrent.Future


object Sensor extends MetaEntity {
  def domain: String = "sensor"
  def apply()(implicit sensor_name: sourcecode.Name, hass: Hass): Sensor = Sensor(sensor_name.value)(hass)
}

case class Sensor(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[String, SensorState]() {
  override def meta: MetaEntity = Sensor
}
