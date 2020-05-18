package hass.model.entity


import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.LightTurnService
import hass.model.state._
import hass.model.state.ground.{TurnAction, TurnState}

object Light extends MetaDomain {
  val domain: DomainType = "light"

  def apply()(implicit name: sourcecode.Name, hass: Hass): Light = Light(name.value)(hass)
}


case class Light(entityName: String)(override implicit val hass: Hass)
  extends StatefulEntity[TurnState, LightState]() with Light.Domain
    with Turnable[LightTurnService] {
  override def service(turn: TurnAction): LightTurnService = LightTurnService(Seq(entityName), turn)
}
