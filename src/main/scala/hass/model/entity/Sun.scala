package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.state.SunState
import hass.model.state.attributes.SunAttributes
import hass.model.state.ground.Horizon


object Sun extends MetaDomain {
  override val domain: DomainType = "sun"

  def apply()(implicit name: sourcecode.Name, hass: Hass): Sun = Sun(name.value)(hass)
}

case class Sun(entityName: String)(implicit val hass: Hass)
  extends StatefulEntity[Horizon, SunState]() with Sun.Domain
    with SunAttributes
