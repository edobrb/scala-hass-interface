package hass.model.service

import hass.model.entity.InputBoolean
import hass.model.state.ground.TurnAction

case class InputBooleanTurnService(override val entityNames: Seq[String], override val turn: TurnAction)
  extends TurnService with InputBoolean.Domain
