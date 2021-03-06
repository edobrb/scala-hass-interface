package hass.model.entity


import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.SwitchTurnService
import hass.model.state._
import hass.model.state.ground.{TurnAction, TurnState}

object Switch extends MetaDomain {
  val domain: DomainType = "switch"

  def apply()(implicit name: sourcecode.Name, hass: Hass): Switch = Switch(name.value)(hass)
}

case class Switch(entityName: String)(override implicit val hass: Hass)
  extends StatefulEntity[TurnState, SwitchState]() with Switch.Domain
    with Turnable[SwitchTurnService] {
  override def service(turn: TurnAction): SwitchTurnService = SwitchTurnService(Seq(entityName), turn)
}


