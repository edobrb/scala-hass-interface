package hass.model.entity


import hass.controller.Hass
import hass.model.{Domain, MetaDomain}
import hass.model.Types.DomainType
import hass.model.service.LightTurnService
import hass.model.state._

object Light extends MetaDomain {
  def domain: DomainType = "light"

  def apply()(implicit light_name: sourcecode.Name, hass: Hass): Light = Light(light_name.value)(hass)

  implicit object LightDomain extends hass.model.Domain[Light] {
    override def value: DomainType = Light.domain
  }
}


case class Light(entity_name: String)(override implicit val hass: Hass)
  extends StatefulEntity[TurnState, LightState]() with Light.Domain
    with Turnable[LightTurnService] {
  override def service(turn: TurnAction): LightTurnService = LightTurnService(Seq(entity_name), turn)
}
