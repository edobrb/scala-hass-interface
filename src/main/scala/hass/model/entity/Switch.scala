package hass.model.entity


import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.{SwitchToggleService, SwitchTurnOffService, SwitchTurnOnService}
import hass.model.state._

object Switch extends MetaDomain {
  def domain: Domain = "switch"

  def apply()(implicit switch_name: sourcecode.Name, hass: Hass): Switch = Switch(switch_name.value)(hass)
}

case class Switch(entity_name: String)(override implicit val hass: Hass)
  extends StatefulEntity[TurnState, SwitchState]() with Switch.Domain
    with Turnable[SwitchTurnOnService, SwitchTurnOffService, SwitchToggleService] {
  override def onService: SwitchTurnOnService = SwitchTurnOnService(Seq(entity_name))

  override def offService: SwitchTurnOffService = SwitchTurnOffService(Seq(entity_name))

  override def toggleService: SwitchToggleService = SwitchToggleService(Seq(entity_name))
}


