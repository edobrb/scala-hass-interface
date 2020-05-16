package hass.model.group

import hass.controller.Hass
import hass.model.entity.{Light, Turnable}
import hass.model.service.LightTurnService
import hass.model.state.ground.TurnAction

case class LightsGroup(lights: Seq[Light])(override implicit val hass: Hass)
  extends Turnable[LightTurnService] {
  override def service(turn: TurnAction): LightTurnService = LightTurnService(lights.map(_.entity_name), turn)
}