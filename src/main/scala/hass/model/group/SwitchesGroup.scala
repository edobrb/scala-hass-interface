package hass.model.group

import hass.controller.Hass
import hass.model.entity.{Switch, Turnable}
import hass.model.service.{SwitchToggleService, SwitchTurnOffService, SwitchTurnOnService}

case class SwitchesGroup(switches: Seq[Switch])(override implicit val hass: Hass)
  extends Turnable[SwitchTurnOnService, SwitchTurnOffService, SwitchToggleService] {
  override def onService: SwitchTurnOnService = SwitchTurnOnService(switches.map(_.entity_name))

  override def offService: SwitchTurnOffService = SwitchTurnOffService(switches.map(_.entity_name))

  override def toggleService: SwitchToggleService = SwitchToggleService(switches.map(_.entity_name))
}