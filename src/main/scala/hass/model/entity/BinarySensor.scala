package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.state._
import hass.model.state.ground.TurnState

object BinarySensor extends MetaDomain {
  val domain: DomainType = "binary_sensor"

  def apply()(implicit name: sourcecode.Name, hass: Hass): BinarySensor = BinarySensor(name.value)(hass)
}

case class BinarySensor(entityName: String)(implicit hass: Hass)
  extends StatefulEntity[TurnState, BinarySensorState]() with BinarySensor.Domain


