package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.InputBooleanTurnService
import hass.model.state.InputBooleanState
import hass.model.state.ground.{TurnAction, TurnState}

object InputBoolean extends MetaDomain {
  val domain: DomainType = "input_boolean"

  def apply()(implicit name: sourcecode.Name, hass: Hass): InputBoolean = InputBoolean(name.value)(hass)
}

case class InputBoolean(entityName: String)(override implicit val hass: Hass)
  extends StatefulEntity[TurnState, InputBooleanState]() with Turnable[InputBooleanTurnService] with InputBoolean.Domain {
  override def service(turn: TurnAction): InputBooleanTurnService = InputBooleanTurnService(Seq(entityName), turn)
}
