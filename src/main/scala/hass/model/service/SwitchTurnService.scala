package hass.model.service

import hass.model.entity.Switch
import hass.model.state.ground.TurnAction
import play.api.libs.json.JsValue

case class SwitchTurnService(override val entityNames: Seq[String], override val turn: TurnAction, override val attributes: Map[String, JsValue] = Map())
  extends TurnService with Switch.Domain