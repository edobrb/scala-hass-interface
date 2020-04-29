package hass.model.entity


import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.{LightToggleService, LightTurnOffService, LightTurnOnService, Result}
import hass.model.state.{LightState, TurnState}

import scala.concurrent.Future

object Light extends MetaDomain {
  def domain: Domain = "light"

  def apply()(implicit light_name: sourcecode.Name, hass: Hass): Light = Light(light_name.value)(hass)
}

case class Light(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[TurnState, LightState]() with Light.Domain
    with Turnable[LightTurnOnService, LightTurnOffService, LightToggleService] {

  override def onService: LightTurnOnService = LightTurnOnService(Seq(entity_name))

  override def offService: LightTurnOffService = LightTurnOffService(Seq(entity_name))

  override def toggleService: LightToggleService = LightToggleService(Seq(entity_name))
}
