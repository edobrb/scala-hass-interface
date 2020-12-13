package hass.model.service

import hass.model.entity.Script
import hass.model.state.ground.TurnAction

case class ScriptTurnService(override val entityNames: Seq[String], override val turn: TurnAction)
  extends TurnService with Script.Domain