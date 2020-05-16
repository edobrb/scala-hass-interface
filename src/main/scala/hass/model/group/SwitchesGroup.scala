package hass.model.group

import hass.controller.Hass
import hass.model.entity.{Switch, Turnable}
import hass.model.service.SwitchTurnService
import hass.model.state.ground.TurnAction

case class SwitchesGroup(switches: Seq[Switch])(override implicit val hass: Hass)
  extends Turnable[SwitchTurnService] {
  override def service(turn: TurnAction): SwitchTurnService = SwitchTurnService(switches.map(_.entity_name), turn)
}