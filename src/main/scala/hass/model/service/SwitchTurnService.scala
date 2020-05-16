package hass.model.service

import hass.model.entity.Switch
import hass.model.state.ground.TurnAction

case class SwitchTurnService(override val entityNames: Seq[String], override val turn: TurnAction)
  extends TurnService with Switch.Domain