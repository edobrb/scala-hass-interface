package hass.model.service

import hass.model.entity.InputBoolean
import hass.model.state.TurnAction
import play.api.libs.json.JsValue

case class InputBooleanTurnService(override val entityNames: Seq[String], override val turn: TurnAction, override val attributes: Map[String, JsValue] = Map())
  extends TurnService with InputBoolean.Domain //TODO: parser
