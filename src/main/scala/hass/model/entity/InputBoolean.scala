package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.InputBooleanTurnService
import hass.model.state.InputBooleanState
import hass.model.state.ground.{TurnAction, TurnState}

object InputBoolean extends MetaDomain {
  val domain: DomainType = "input_boolean"

  def apply()(implicit input_boolean_name: sourcecode.Name, hass: Hass): InputBoolean = InputBoolean(input_boolean_name.value)(hass)
}

case class InputBoolean(entity_name: String)(override implicit val hass: Hass)
  extends StatefulEntity[TurnState, InputBooleanState]() with Turnable[InputBooleanTurnService] with InputBoolean.Domain {
  override def service(turn: TurnAction): InputBooleanTurnService = InputBooleanTurnService(Seq(entity_name), turn)
}
